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
package com.connexta.replication.api.impl;

import static org.apache.commons.lang3.Validate.notNull;

import com.connexta.replication.api.Replicator;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.impl.data.SyncRequestImpl;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.SiteManager;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ReplicatorRunner periodical queues up replication jobs for all the current replication
 * filters.
 */
public class ReplicatorRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorRunner.class);

  private final Replicator replicator;

  private final FilterManager filterManager;

  private final SiteManager siteManager;

  private final ScheduledExecutorService scheduledExecutor;

  private final Set<String> sites;

  private final long period;

  private static final long DEFAULT_REPLICATION_PERIOD = TimeUnit.MINUTES.toSeconds(5);

  /**
   * Instantiates a new replication runner.
   *
   * @param replicator the replicator where to send replication requests
   * @param filterManager a filter manager for retrieving {@link Filter}s from persistence
   * @param siteManager a site manager for retrieving {@link Site}s from persistence
   * @param sites the set of sites to be handled by this replication runner or empty to have all
   *     sites handled
   * @param period the replication polling period in seconds
   */
  public ReplicatorRunner(
      Replicator replicator,
      FilterManager filterManager,
      SiteManager siteManager,
      Stream<String> sites,
      long period) {
    this(
        Executors.newSingleThreadScheduledExecutor(),
        replicator,
        filterManager,
        siteManager,
        sites,
        period);
  }

  @VisibleForTesting
  ReplicatorRunner(
      ScheduledExecutorService scheduledExecutor,
      Replicator replicator,
      FilterManager filterManager,
      SiteManager siteManager,
      Stream<String> sites,
      long period) {
    this.scheduledExecutor = notNull(scheduledExecutor);
    this.replicator = notNull(replicator);
    this.filterManager = notNull(filterManager);
    this.siteManager = notNull(siteManager);
    this.sites = sites.collect(Collectors.toSet());
    this.period = period > 0 ? period : DEFAULT_REPLICATION_PERIOD;
  }

  public void init() {
    if (sites.isEmpty()) {
      LOGGER.info("Replication for all sites scheduled for every {} seconds.", period);
    } else {
      LOGGER.info("Replication for sites: {} scheduled for every {} seconds.", sites, period);
    }
    scheduledExecutor.scheduleAtFixedRate(this::scheduleReplication, 0, period, TimeUnit.SECONDS);
  }

  public void destroy() {
    LOGGER.info("shutting down replicatorRunner");
    scheduledExecutor.shutdownNow();
  }

  @VisibleForTesting
  void scheduleReplication() {
    Stream<Site> siteStream;

    if (sites.isEmpty()) {
      siteStream = siteManager.objects();
    } else {
      siteStream = sites.stream().map(this::getSiteOrNull);
    }

    siteStream
        .filter(this::isValidSite)
        .map(Site::getId)
        .flatMap(filterManager::filtersForSite)
        .filter(f -> !f.isSuspended())
        .forEach(this::submitSyncRequest);
  }

  private Site getSiteOrNull(String siteId) {
    try {
      return siteManager.get(siteId);
    } catch (NotFoundException e) {
      return null;
    }
  }

  private void submitSyncRequest(Filter filter) {
    try {
      replicator.submitSyncRequest(new SyncRequestImpl(filter));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private boolean isValidSite(@Nullable Site site) {
    if (site == null) {
      return false;
    } else if (site.getType().equals(SiteType.UNKNOWN)) {
      LOGGER.info("Site {} has an unknown type, ignoring filters for this site.", site.getName());
      return false;
    } else if (site.getKind().equals(SiteKind.UNKNOWN)) {
      LOGGER.info("Site {} has an unknown kind, ignoring filters for this site.", site.getName());
      return false;
    } else {
      return true;
    }
  }
}
