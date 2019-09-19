/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.replication.api.impl.worker;

import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.AdapterInterruptedException;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.data.CreateRequest;
import com.connexta.replication.api.data.ErrorCode;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ReplicationPersistenceException;
import com.connexta.replication.api.data.Resource;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.ResourceRequest;
import com.connexta.replication.api.data.ResourceResponse;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.ddf.DdfMetadataInfo;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.impl.data.CreateRequestImpl;
import com.connexta.replication.api.impl.data.CreateStorageRequestImpl;
import com.connexta.replication.api.impl.data.ResourceRequestImpl;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.Queue;
import com.connexta.replication.data.MetadataImpl;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.URI;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a single transfer between a local and remote Site based on the tasks received from the
 * queue. This worker only supports harvesting operations. This worker will respond to thread
 * interruptions.
 */
public class Worker extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

  private volatile boolean processing = false;

  private volatile boolean running = true;

  private final NodeAdapter local;

  private final Queue queue;

  private final SiteManager siteManager;

  private final NodeAdapters nodeAdapters;

  /**
   * Creates a new worker which will monitor the provided queue and perform operations between the
   * provided local and destination sites.
   *
   * <p>Does not begin monitoring of the queue until {@link #run()} is called.
   *
   * @param queue the queue to monitor tasks for
   * @param local the local site's node adapter
   * @param siteManager site manager for accessing sites
   * @param nodeAdapters node adapter factory
   */
  Worker(Queue queue, NodeAdapter local, SiteManager siteManager, NodeAdapters nodeAdapters) {
    this.queue = queue;
    this.local = local;
    this.siteManager = siteManager;
    this.nodeAdapters = nodeAdapters;
  }

  /**
   * Begin monitoring a queue for tasks. The handler of this worker is responsible for halting this
   * thread by calling interrupt on this worker's thread, finishing the transfer of the current task
   * if necessary.
   */
  @Override
  public void run() {
    try {
      while (running) {
        doRun();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      try {
        local.close();
      } catch (IOException ie) {
        LOGGER.debug("Failed to close adapter", ie);
      }
    }
  }

  @VisibleForTesting
  void doRun() throws InterruptedException {
    Task task = null;
    try {
      task = queue.take();
      this.processing = true;
      if (supported(task)) {
        this.handle(task);
      } else {
        task.unlock();
      }
      this.processing = false;
    } catch (InterruptedException e) {
      if (task != null) {
        try {
          task.unlock();
        } catch (InterruptedException ignore) { // fallthrough
        }
      }
      running = false;
      processing = false;
      throw e;
    }
  }

  /**
   * Set this workers running flag to false if it is not currently transferring an item
   *
   * @return {@code true} if the worker was cancelled, otherwise {@code false}
   */
  boolean cancelIfNotProcessing() {
    if (!processing) {
      this.running = false;
      return true;
    }
    return false;
  }

  private boolean supported(Task task) {
    if (!OperationType.HARVEST.equals(task.getOperation())) {
      return false;
    }

    List<MetadataInfo> infos = task.metadatas().collect(Collectors.toList());
    if (infos.size() > 1) {
      LOGGER.warn("More than one metadata is currently not supported by this worker");
      return false;
    }

    return (infos.stream().filter(DdfMetadataInfo.class::isInstance).count() == 1L);
  }

  @SuppressWarnings(
      "squid:S3655" /* Optional.get() will return an info because we check it exists in the supported method */)
  private void handle(Task task) throws InterruptedException {
    NodeAdapter remote = getDestinationAdapter(task);
    if (remote == null) {
      task.unlock();
      return;
    }

    try {
      if (!adaptersAvailable(task, remote)) {
        return;
      }

      DdfMetadataInfo info =
          task.metadatas()
              .filter(DdfMetadataInfo.class::isInstance)
              .map(DdfMetadataInfo.class::cast)
              .findFirst()
              .get(); // should never end up being empty since it was filtered above

      final Metadata metadata =
          new MetadataImpl(
              info.getData(), info.getDataClass(), task.getId(), Date.from(info.getLastModified()));

      final OperationType type = task.getOperation();
      if (type == OperationType.HARVEST) {
        doCreate(task, metadata, remote, local);
      } else {
        // should never hit this since deserialization will return UNKNOWN
        task.unlock();
      }
    } finally {
      try {
        remote.close();
      } catch (IOException e) {
        LOGGER.debug("Failed to close destination adapter", e);
      }
    }
  }

  @Nullable
  private NodeAdapter getDestinationAdapter(Task task) throws InterruptedException {
    Site site;
    final String siteId = task.getQueue().getSite();
    try {
      site = siteManager.get(siteId);
    } catch (NotFoundException e) {
      LOGGER.debug("Unable to find site {} for queue", siteId, e);
      return null;
    } catch (ReplicationPersistenceException e) {
      LOGGER.debug("Failed to fetch site {} for queue", siteId, e);
      return null;
    }

    NodeAdapter nodeAdapter;
    try {
      nodeAdapter = nodeAdapters.factoryFor(site.getType()).create(site.getUrl());
    } catch (AdapterInterruptedException e) {
      throw new InterruptedException();
    } catch (IllegalArgumentException | AdapterException e) {
      LOGGER.debug("Error retrieving node adapter factory for site {}", site.getUrl(), e);
      return null;
    }

    return nodeAdapter;
  }

  private void doCreate(
      final Task task,
      final Metadata metadata,
      final NodeAdapter source,
      final NodeAdapter destination)
      throws InterruptedException {
    final String metadataId = task.getId();
    final String operation = task.getOperation().name();

    if (!metadataExists(task, metadata, source, destination)) {
      return;
    }

    URI uri = task.getResource().flatMap(ResourceInfo::getResourceUri).orElse(null);
    if (uri != null) {
      if (!createResource(task, metadata, source, destination, operation, uri)) {
        return;
      }
    } else if (!createMetadata(task, metadata, source, destination, metadataId, operation)) {
      return;
    }
    task.complete();
  }

  private boolean metadataExists(
      Task task, Metadata metadata, NodeAdapter source, NodeAdapter destination)
      throws InterruptedException {
    try {
      // TODO: remove existence check once better reporting is done by adapters
      // see https://github.com/connexta/replication/issues/235
      if (!source.exists(metadata)) {
        LOGGER.info(
            "Received task to create metadata on site {}, but it was deleted on {}",
            destination.getSystemName(),
            source.getSystemName());
        task.fail(
            ErrorCode.NO_LONGER_EXISTS,
            String.format(
                "Metadata deleted from site %s before able to create", source.getSystemName()));
        return false;
      }
    } catch (AdapterInterruptedException e) {
      throw new InterruptedException();
    } catch (AdapterException e) {
      LOGGER.debug("Failed to check if resource exists", e);
      task.fail(
          ErrorCode.OPERATION_FAILURE,
          String.format("Failed check resource existence on %s", destination.getSystemName()));
      return false;
    }
    return true;
  }

  private boolean createMetadata(
      Task task,
      Metadata metadata,
      NodeAdapter source,
      NodeAdapter destination,
      String metadataId,
      String operation)
      throws InterruptedException {
    try {
      LOGGER.info("No resource URI specified for task {}. Doing metadata create only", task);
      CreateRequest request = new CreateRequestImpl(List.of(metadata));
      boolean success = destination.createRequest(request);
      if (!success) {
        task.fail(
            ErrorCode.OPERATION_FAILURE,
            String.format(
                "Failed to %s metadata %s from %s to %s",
                operation, metadataId, source.getSystemName(), destination.getSystemName()));
        return false;
      }
    } catch (AdapterInterruptedException e) {
      throw new InterruptedException();
    } catch (AdapterException e) {
      LOGGER.debug("Failed to retrieve metadata", e);
      task.fail(
          ErrorCode.OPERATION_FAILURE,
          String.format("Failed to retrieve metadata from %s", destination.getSystemName()));
      return false;
    }
    return true;
  }

  private boolean createResource(
      Task task,
      Metadata metadata,
      NodeAdapter source,
      NodeAdapter destination,
      String operation,
      URI uri)
      throws InterruptedException {
    ResourceRequest request = new ResourceRequestImpl(metadata);
    ResourceResponse response;
    try {
      response = source.readResource(request);
    } catch (AdapterInterruptedException e) {
      throw new InterruptedException();
    } catch (AdapterException e) {
      LOGGER.debug("Failed to read resource", e);
      task.fail(
          ErrorCode.OPERATION_FAILURE,
          String.format("Failed to read resource from %s", source.getSystemName()));
      return false;
    }

    try {
      Resource resource = response.getResource();
      boolean success = destination.createResource(new CreateStorageRequestImpl(resource));
      if (!success) {
        task.fail(
            ErrorCode.OPERATION_FAILURE,
            String.format(
                "Failed to %s resource %s from %s to %s",
                operation, uri, source.getSystemName(), destination.getSystemName()));
        return false;
      }
    } catch (AdapterInterruptedException e) {
      throw new InterruptedException();
    } catch (AdapterException e) {
      LOGGER.debug("Failed to create resource", e);
      task.fail(
          ErrorCode.OPERATION_FAILURE,
          String.format(
              "Failed to create resource from %s to %s",
              source.getSystemName(), destination.getSystemName()));
      return false;
    }
    return true;
  }

  private boolean adaptersAvailable(Task task, NodeAdapter destination)
      throws InterruptedException {
    if (!local.isAvailable()) {
      task.fail(
          ErrorCode.SITE_UNAVAILABLE,
          String.format("Site %s is unavailable", local.getSystemName()));
      return false;
    }

    if (!destination.isAvailable()) {
      task.fail(
          ErrorCode.SITE_UNAVAILABLE,
          String.format("Site %s is unavailable", destination.getSystemName()));
      return false;
    }

    // if the task didn't complete with the call to fail, we still have the lock
    return true;
  }

  @VisibleForTesting
  void setProcessing(boolean processing) {
    this.processing = processing;
  }

  public boolean isRunning() {
    return this.running;
  }
}
