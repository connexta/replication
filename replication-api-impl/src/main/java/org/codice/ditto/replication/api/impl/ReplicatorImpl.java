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
package org.codice.ditto.replication.api.impl;

import static org.apache.commons.lang3.Validate.notNull;

import com.google.common.annotations.VisibleForTesting;
import ddf.security.Subject;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.collections4.queue.UnmodifiableQueue;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorImpl implements Replicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorImpl.class);

  private final NodeAdapters nodeAdapters;

  private final ReplicatorHistoryManager history;

  private final SiteManager siteManager;

  private final ExecutorService executor;

  private final Syncer syncer;

  /** Does not contain duplicates */
  private BlockingQueue<SyncRequest> pendingSyncRequests = new LinkedBlockingQueue<>();

  /** Does not contain duplicates */
  private final BlockingQueue<SyncRequest> activeSyncRequests = new LinkedBlockingQueue<>();

  private final Map<String, Syncer.Job> syncerJobMap = new ConcurrentHashMap<>();

  private final Security security;

  public ReplicatorImpl(
      NodeAdapters nodeAdapters,
      ReplicatorHistoryManager history,
      SiteManager siteManager,
      ExecutorService executor,
      Syncer syncer) {
    this(nodeAdapters, history, siteManager, executor, syncer, Security.getInstance());
  }

  public ReplicatorImpl(
      NodeAdapters nodeAdapters,
      ReplicatorHistoryManager history,
      SiteManager siteManager,
      ExecutorService executor,
      Syncer syncer,
      Security security) {
    this.nodeAdapters = notNull(nodeAdapters);
    this.history = notNull(history);
    this.executor = notNull(executor);
    this.siteManager = notNull(siteManager);
    this.syncer = syncer;
    this.security = security;
  }

  public void init() {
    LOGGER.trace("Configuring the single-thread scheduler to execute sync requests from the queue");

    executor.execute(
        () -> {
          while (true) {
            try {
              final SyncRequest syncRequest = pendingSyncRequests.take();
              LOGGER.trace(
                  "Just took sync request {} from the pendingSyncRequests queue. There are {} pending sync requests now in the queue.",
                  syncRequest,
                  pendingSyncRequests.size());

              if (activeSyncRequests.contains(syncRequest)) {
                LOGGER.debug(
                    "activeSyncRequests already contains sync request {}. Not executing again.",
                    syncRequest);
              } else {
                LOGGER.trace("Marking sync request {} as active", syncRequest);
                activeSyncRequests.put(syncRequest);
                executeSyncRequest(syncRequest);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              break;
            }
          }
        });

    LOGGER.debug(
        "Successfully configured the single-thread scheduler to execute replication requests.");
  }

  @VisibleForTesting
  void executeSyncRequest(final SyncRequest syncRequest) {
    final Subject systemSubject = security.runAsAdmin(security::getSystemSubject);
    systemSubject.execute(
        () -> {
          LOGGER.trace("Executing sync request {} with subject", syncRequest);

          ReplicationStatus status = syncRequest.getStatus();
          status.markStartTime();

          ReplicatorConfig config = syncRequest.getConfig();
          NodeAdapter node1 = null;
          NodeAdapter node2 = null;

          try {
            node1 = getStoreForId(config.getSource());
            node2 = getStoreForId(config.getDestination());
          } catch (Exception e) {
            final Status connectionUnavailable = Status.CONNECTION_UNAVAILABLE;
            LOGGER.debug(
                "Error getting node adapters for replicator config {}. Setting status to {}",
                config.getName(),
                connectionUnavailable,
                e);
            status.setStatus(connectionUnavailable);
            completeActiveSyncRequest(syncRequest, status);
            closeQuietly(node1);
            closeQuietly(node2);
            return;
          }

          try (NodeAdapter store1 = node1;
              NodeAdapter store2 = node2) {
            Status pullStatus = Status.SUCCESS;
            if (config.isBidirectional()) {
              status.setStatus(Status.PULL_IN_PROGRESS);
              pullStatus = sync(store2, store1, config, status);
            }

            if (pullStatus.equals(Status.SUCCESS)) {
              status.setStatus(Status.PUSH_IN_PROGRESS);
              sync(store1, store2, config, status);
            }
          } catch (Exception e) {
            final Status failureStatus = Status.FAILURE;
            LOGGER.warn(
                "Unexpected error when running config {}. Setting status to {}",
                config.getName(),
                failureStatus,
                e);
            status.setStatus(failureStatus);
          } finally {
            completeActiveSyncRequest(syncRequest, status);
            syncerJobMap.remove(syncRequest.getConfig().getId());
          }
        });
  }

  private Status sync(
      NodeAdapter source,
      NodeAdapter destination,
      ReplicatorConfig config,
      ReplicationStatus status) {
    Syncer.Job job = syncer.create(source, destination, config, status);
    syncerJobMap.put(config.getId(), job);
    SyncResponse response = job.sync();
    syncerJobMap.remove(config.getId());
    return response.getStatus();
  }

  private void completeActiveSyncRequest(SyncRequest syncRequest, ReplicationStatus status) {
    status.setDuration();
    LOGGER.trace("Removing sync request {} from the active queue", syncRequest);
    if (!activeSyncRequests.remove(syncRequest)) {
      LOGGER.debug("Failed to remove sync request {} from the active queue", syncRequest);
    }
    LOGGER.trace("Adding replication event to history: {}", status);
    history.save(status);
    LOGGER.trace("Successfully added replication event to history: {}", status);
  }

  public void cleanUp() {
    final RetryPolicy retryPolicy =
        new RetryPolicy()
            .retryWhen(false)
            .withDelay(1, TimeUnit.SECONDS)
            .withMaxDuration(30, TimeUnit.SECONDS);

    Failsafe.with(retryPolicy)
        .onSuccess(
            isEmpty ->
                LOGGER.trace(
                    "Successfully waited for all pending or active sync requests to be completed"))
        .onRetry(
            isEmpty ->
                LOGGER.debug(
                    "There are currently {} pending and {} active sync requests. Waiting another second for all sync requests to be completed.",
                    pendingSyncRequests.size(),
                    activeSyncRequests.size()))
        .onFailure(
            isEmpty ->
                LOGGER.debug(
                    "There are currently {} pending and {} active sync requests, but the timeout was reached for waiting for all sync requests to be completed.",
                    pendingSyncRequests.size(),
                    activeSyncRequests.size()))
        .get(() -> pendingSyncRequests.isEmpty() && activeSyncRequests.isEmpty());

    LOGGER.trace(
        "Shutting down now the single-thread scheduler that executes sync requests from the queue");
    executor.shutdownNow();
    LOGGER.trace("Successfully shut down replicator thread pool and scheduler");
  }

  @Override
  public void submitSyncRequest(final SyncRequest syncRequest) throws InterruptedException {
    LOGGER.trace("Submitting sync request for name = {}", syncRequest.getConfig().getName());
    if (pendingSyncRequests.contains(syncRequest) || activeSyncRequests.contains(syncRequest)) {
      LOGGER.debug(
          "The pendingSyncRequests already contains sync request {}, or it is already running. Not adding again to pending requests.",
          syncRequest);
    } else {
      syncRequest.getStatus().setStatus(Status.PENDING);
      pendingSyncRequests.put(syncRequest);
    }
  }

  @Override
  public void cancelSyncRequest(SyncRequest syncRequest) {
    if (pendingSyncRequests.remove(syncRequest)) {
      LOGGER.debug("Removed pending request with name: {}", syncRequest.getConfig().getName());
    }

    Syncer.Job job = syncerJobMap.remove(syncRequest.getConfig().getId());
    if (job != null) {
      job.cancel();
    }
  }

  @Override
  public void cancelSyncRequest(String configId) {
    Stream.of(getPendingSyncRequests(), getActiveSyncRequests())
        .flatMap(Collection::stream)
        .filter(req -> req.getConfig().getId().equals(configId))
        .forEach(this::cancelSyncRequest);
  }

  @Override
  public Queue<SyncRequest> getPendingSyncRequests() {
    return UnmodifiableQueue.unmodifiableQueue(pendingSyncRequests);
  }

  @Override
  public Set<SyncRequest> getActiveSyncRequests() {
    return Collections.unmodifiableSet(new HashSet<>(activeSyncRequests));
  }

  private NodeAdapter getStoreForId(String siteId) {
    NodeAdapter store;
    ReplicationSite site = siteManager.get(siteId);
    try {
      store = nodeAdapters.factoryFor(NodeAdapterType.DDF).create(new URL(site.getUrl()));
    } catch (Exception e) {
      throw new ReplicationException("Error connecting to node at " + site.getUrl(), e);
    }
    if (!store.isAvailable()) {
      throw new ReplicationException("System at " + site.getUrl() + " is currently unavailable");
    }
    return store;
  }

  @VisibleForTesting
  public void setPendingSyncRequestsQueue(BlockingQueue blockingQueue) {
    this.pendingSyncRequests = blockingQueue;
  }

  private void closeQuietly(Closeable c) {
    try {
      if (c != null) {
        c.close();
      }
    } catch (IOException e) {
      LOGGER.trace("Could not close closable. This is not an error.");
    }
  }
}
