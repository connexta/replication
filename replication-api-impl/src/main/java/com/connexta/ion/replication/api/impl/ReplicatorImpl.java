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
package com.connexta.ion.replication.api.impl;

import static org.apache.commons.lang3.Validate.notNull;

import com.connexta.ion.replication.api.NodeAdapter;
import com.connexta.ion.replication.api.Replicator;
import com.connexta.ion.replication.api.SyncRequest;
import com.connexta.replication.api.ReplicationException;
import com.connexta.replication.api.data.ReplicationItem;
import com.connexta.replication.api.data.ReplicatorConfig;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.persistence.SiteManager;
import com.google.common.annotations.VisibleForTesting;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.collections4.queue.UnmodifiableQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorImpl implements Replicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorImpl.class);

  private final NodeAdapters nodeAdapters;

  private final SiteManager siteManager;

  private final ExecutorService executor;

  private final Syncer syncer;

  /** Does not contain duplicates */
  private BlockingQueue<SyncRequest> pendingSyncRequests = new LinkedBlockingQueue<>();

  /** Does not contain duplicates */
  private final BlockingQueue<SyncRequest> activeSyncRequests = new LinkedBlockingQueue<>();

  private final Set<Consumer<ReplicationItem>> callbacks =
      Collections.synchronizedSet(new HashSet<>());

  /**
   * Creates a new {@code ReplicatorImpl}.
   *
   * @param nodeAdapters bean for creating {@link NodeAdapter}s
   * @param siteManager manager for accessing {@link Site}s
   * @param syncer bean for transferring metadata and/or resources
   */
  public ReplicatorImpl(NodeAdapters nodeAdapters, SiteManager siteManager, Syncer syncer) {
    this(nodeAdapters, siteManager, Executors.newSingleThreadScheduledExecutor(), syncer);
  }

  @VisibleForTesting
  ReplicatorImpl(
      NodeAdapters nodeAdapters, SiteManager siteManager, ExecutorService executor, Syncer syncer) {
    this.nodeAdapters = notNull(nodeAdapters);
    this.executor = notNull(executor);
    this.siteManager = notNull(siteManager);
    this.syncer = notNull(syncer);
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
            } catch (Exception e) {
              LOGGER.error("Unexpected exception in replication management thread.", e);
            }
          }
        });

    LOGGER.debug(
        "Successfully configured the single-thread scheduler to execute replication requests.");
  }

  @VisibleForTesting
  void executeSyncRequest(final SyncRequest syncRequest) {
    LOGGER.trace("Executing sync request {} with subject", syncRequest);
    ReplicatorConfig config = syncRequest.getConfig();
    NodeAdapter node1 = null;
    NodeAdapter node2 = null;

    try {
      node1 = getStoreForId(config.getSource());
      node2 = getStoreForId(config.getDestination());
    } catch (ReplicationException e) {
      LOGGER.debug("Error getting node adapters for replicator config {}.", config.getName(), e);
      completeActiveSyncRequest(syncRequest);
      closeQuietly(node1);
      closeQuietly(node2);
      return;
    }

    try (NodeAdapter store1 = node1;
        NodeAdapter store2 = node2) {
      if (config.isBidirectional()) {
        sync(store2, store1, config);
      }
      sync(store1, store2, config);
    } catch (Exception e) {
      LOGGER.warn("Unexpected error when running config {}", config.getName(), e);
    } finally {
      completeActiveSyncRequest(syncRequest);
    }
  }

  private void sync(NodeAdapter source, NodeAdapter destination, ReplicatorConfig config) {
    Syncer.Job job = syncer.create(source, destination, config, Set.copyOf(callbacks));
    job.sync();
  }

  private void completeActiveSyncRequest(SyncRequest syncRequest) {
    LOGGER.trace("Removing sync request {} from the active queue", syncRequest);
    if (!activeSyncRequests.remove(syncRequest)) {
      LOGGER.debug("Failed to remove sync request {} from the active queue", syncRequest);
    }
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
                LOGGER.info(
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
      pendingSyncRequests.put(syncRequest);
    }
  }

  @Override
  public Queue<SyncRequest> getPendingSyncRequests() {
    return UnmodifiableQueue.unmodifiableQueue(pendingSyncRequests);
  }

  @Override
  public Set<SyncRequest> getActiveSyncRequests() {
    return Set.copyOf(activeSyncRequests);
  }

  @Override
  public void registerCompletionCallback(Consumer<ReplicationItem> callback) {
    callbacks.add(callback);
  }

  @VisibleForTesting
  NodeAdapter getStoreForId(String siteId) {
    NodeAdapter store;
    Site site = siteManager.get(siteId);
    try {
      store = nodeAdapters.factoryFor(site.getType()).create(site.getUrl());
    } catch (Exception e) {
      throw new ReplicationException("Error connecting to node at " + site.getUrl(), e);
    }
    if (!store.isAvailable()) {
      throw new ReplicationException("System at " + site.getUrl() + " is currently unavailable");
    }
    return store;
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
