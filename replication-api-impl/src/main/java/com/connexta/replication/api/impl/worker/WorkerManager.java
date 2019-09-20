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

import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.QueueBroker;
import com.connexta.replication.spring.ReplicationProperties;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for managing item workers. It will monitor sites currently available in storage
 * and create/destroy item workers accordingly for each site, respecting each site's parallelism
 * factor.
 */
public class WorkerManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkerManager.class);

  private final ScheduledExecutorService executorService =
      Executors.newSingleThreadScheduledExecutor();

  private final Set<String> siteIds;

  private final Site localSite;

  private final SiteManager siteManager;

  private final Map<String, WorkerThreadPool> threadPools = new ConcurrentHashMap<>();

  private final WorkerThreadPoolFactory threadPoolFactory;

  /**
   * Creates a new worker manager.
   *
   * @param broker the queue broker for retrieving queues
   * @param siteManager the site data access manager
   * @param nodeAdapters node adapters factory
   * @param properties replication system properties
   */
  public WorkerManager(
      QueueBroker broker,
      SiteManager siteManager,
      NodeAdapters nodeAdapters,
      ReplicationProperties properties) {
    this(
        siteManager,
        properties,
        new WorkerThreadPoolFactory(
            broker, siteManager.get(properties.getLocalSite()), siteManager, nodeAdapters));
  }

  /**
   * Creates a new worker manager.
   *
   * @param siteManager the site data access manager
   * @param properties replication system properties
   */
  public WorkerManager(
      SiteManager siteManager,
      ReplicationProperties properties,
      WorkerThreadPoolFactory threadPoolFactory) {
    this.siteManager = siteManager;
    this.siteIds = Set.of(properties.getSites().toArray(new String[0]));
    this.localSite = siteManager.get(properties.getLocalSite());
    this.threadPoolFactory = threadPoolFactory;
  }

  /** Initializes this manager and begins monitoring of sites. */
  public void init() {
    executorService.scheduleAtFixedRate(this::monitorSites, 0L, 30L, TimeUnit.SECONDS);
  }

  /** Shutdown the manager and any workers that it is currently managing */
  public void destroy() {
    executorService.shutdownNow();
    threadPools.forEach((site, pool) -> pool.shutdown());
    threadPools.clear();
  }

  @VisibleForTesting
  void monitorSites() {
    Stream<Site> supportedSites =
        siteManager
            .objects()
            .filter(site -> !SiteType.UNKNOWN.equals(site.getType()))
            .filter(site -> !SiteKind.UNKNOWN.equals(site.getKind()))
            .filter(site -> !site.getId().equals(localSite.getId()));

    if (!siteIds.isEmpty()) {
      supportedSites = supportedSites.filter(site -> siteIds.contains(site.getId()));
    }
    List<Site> sites = supportedSites.collect(Collectors.toList());
    sites.forEach(this::monitorSite);

    // if a site has been removed from storage, cleanup its thread pool
    Set<String> threadPoolSites = new HashSet<>(threadPools.keySet());
    sites.forEach(site -> threadPoolSites.remove(site.getId()));
    threadPoolSites.forEach(this::shutdownThreadPool);
  }

  private void monitorSite(Site site) {
    final String siteId = site.getId();

    final int poolSize =
        Math.min(localSite.getParallelismFactor().orElse(1), site.getParallelismFactor().orElse(1));
    WorkerThreadPool threadPool = threadPools.get(siteId);
    if (threadPool != null) {
      threadPool.setSize(poolSize);
    } else {
      threadPool = threadPoolFactory.create(siteId, poolSize);
      threadPools.put(siteId, threadPool);
    }
  }

  private void shutdownThreadPool(String siteId) {
    WorkerThreadPool threadPool = threadPools.remove(siteId);
    if (threadPool != null) {
      LOGGER.trace("Destroying worker thread pool for site {}", siteId);
      threadPool.shutdown();
    }
  }
}
