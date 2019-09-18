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
package com.connexta.replication.query;

import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.AdapterInterruptedException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.queue.QueueException;
import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Managers for instantiating {@link QueryService} class based on configuration. */
public class QueryManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryManager.class);

  /** A Facade for the various objects and services used by the query service. */
  private final QueryServiceTools queryServiceTools;

  /** Set of identifiers for the sites to be managed or empty to manage all sites. */
  private final Set<String> sites;

  /** Executor service that controls the thread for regularly checking the configured sites. */
  private final ScheduledExecutorService executor;

  /** Map of active query services keyed by site IDs. */
  private final Map<String, QueryService> services = new HashMap<>();

  /**
   * The number of seconds to wait before reloading the site configs and updating the {@link
   * com.connexta.replication.query.QueryService}s accordingly.
   */
  private long serviceRefreshPeriod;

  private String localSiteId;

  /**
   * Instantiate a new query manager.
   *
   * @param sites identifiers for the sites to be managed or empty to manage all sites
   * @param queryServiceTools facade for various objects used by the queryService
   */
  public QueryManager(
      Stream<String> sites,
      QueryServiceTools queryServiceTools,
      long serviceRefreshPeriod,
      String localSiteId) {
    this(
        sites,
        queryServiceTools,
        serviceRefreshPeriod,
        localSiteId,
        Executors.newSingleThreadScheduledExecutor());
  }

  QueryManager(
      Stream<String> sites,
      QueryServiceTools queryServiceTools,
      long serviceRefreshPeriod,
      String localSiteId,
      ScheduledExecutorService executor) {
    this.sites = sites.collect(Collectors.toSet());
    this.queryServiceTools = queryServiceTools;
    this.serviceRefreshPeriod = serviceRefreshPeriod > 0L ? serviceRefreshPeriod : 30L;
    this.localSiteId = localSiteId;
    this.executor = executor;
  }

  /**
   * Initializes the query manager and kick start all query services to start polling sites that
   * must be polled.
   */
  public void init() {
    if (sites.isEmpty()) {
      LOGGER.info("Managing queries for all configured sites.");
    } else {
      LOGGER.info("Managing queries for sites: {}.", sites);
    }
    executor.scheduleAtFixedRate(
        this::reloadSiteConfigs, 0, serviceRefreshPeriod, TimeUnit.SECONDS);
    LOGGER.info(
        "Query Manager started with a service refresh period of {} seconds. The local site is {}.",
        serviceRefreshPeriod,
        localSiteId);
  }

  /** Destroys the query manager and stop all currently running query services. */
  public void destroy() {
    LOGGER.info("Shutting down query managers");
    executor.shutdownNow();
    synchronized (this) {
      for (QueryService service : services.values()) {
        service.stop();
      }
      services.clear();
    }
  }

  @VisibleForTesting
  void reloadSiteConfigs() {
    LOGGER.trace("reloading site configs.");
    Stream<Site> siteStream =
        queryServiceTools.sites().filter(site -> !site.getId().equals(localSiteId));
    synchronized (this) {
      if (Thread.currentThread().isInterrupted()) {
        return;
      }

      if (!sites.isEmpty()) {
        siteStream = siteStream.filter(site -> sites.contains(site.getId()));
      }
      final Map<String, Site> newSites =
          siteStream.collect(Collectors.toMap(Site::getId, Function.identity()));
      LOGGER.debug("Creating/updating services for sites: {}.", newSites);

      // stop query services for sites that are no longer configured or have changed to a non-polled
      // type
      // update them if they are already running
      // TODO: remove filters for regional DDF when we support other kinds of sites
      try {
        updateQueryServices(newSites);

        // start query services for new sites that must be polled
        newSites.values().stream()
            .filter(QueryManager::mustBePolled)
            .filter(site -> site.getKind() == SiteKind.REGIONAL)
            .map(this::newService)
            .filter(Objects::nonNull)
            .forEach(QueryService::start);
      } catch (AdapterInterruptedException e) {
        LOGGER.debug(
            "Thread interrupted while creating/updating QueryServices. This is normal when shutting down.",
            e);
        Thread.currentThread().interrupt();
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Running QueryServices: {}", services.values());
      }
    }
  }

  @VisibleForTesting
  void updateQueryServices(Map<String, Site> newSites) {
    final Iterator<Entry<String, QueryService>> serviceIterator = services.entrySet().iterator();
    while (serviceIterator.hasNext()) {
      final Entry<String, QueryService> entry = serviceIterator.next();
      final Site updatedSite = newSites.remove(entry.getKey());

      if (updatedSite != null
          && updatedSite.getType().mustBePolled()
          && updatedSite.getKind() == SiteKind.REGIONAL) {
        try {
          entry.getValue().update(updatedSite);
        } catch (AdapterInterruptedException e) {
          throw e;
        } catch (AdapterException | IllegalArgumentException e) {
          LOGGER.warn(
              "Failed to create an adapter for {} stopping the query service for now.",
              updatedSite);
          LOGGER.debug(
              "Failed to create an adapter for {} stopping the query service for now.",
              updatedSite,
              e);
          entry.getValue().stop();
          serviceIterator.remove();
        }
      } else {
        entry.getValue().stop();
        serviceIterator.remove();
      }
    }
  }

  @VisibleForTesting
  Map<String, QueryService> getServices() {
    return new HashMap<>(services);
  }

  @VisibleForTesting
  void putService(String id, QueryService service) {
    services.put(id, service);
  }

  @Nullable
  private QueryService newService(Site site) {
    try {
      return services.computeIfAbsent(site.getId(), i -> new QueryService(site, queryServiceTools));
    } catch (AdapterInterruptedException e) {
      throw e;
    } catch (AdapterException | IllegalArgumentException | QueueException e) {
      LOGGER.warn(
          "Failed to create an adapter for {} not creating a query service for this site.", site);
      LOGGER.debug(
          "Failed to create an adapter for {} not creating a query service for this site.",
          site,
          e);
      return null;
    }
  }

  private static boolean mustBePolled(Site site) {
    return site.getType().mustBePolled();
  }
}
