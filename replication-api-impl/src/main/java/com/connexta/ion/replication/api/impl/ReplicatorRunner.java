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

import com.connexta.ion.replication.api.Replicator;
import com.connexta.ion.replication.api.data.ReplicatorConfig;
import com.connexta.ion.replication.api.impl.data.SyncRequestImpl;
import com.connexta.ion.replication.api.persistence.ReplicatorConfigManager;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ReplicatorRunner periodical queues up replication jobs for all the current replication
 * configurations.
 */
public class ReplicatorRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorRunner.class);

  private final Replicator replicator;

  private final ReplicatorConfigManager replicatorConfigManager;

  private final ScheduledExecutorService scheduledExecutor;

  private final Set<String> sites;

  private final long period;

  private static final long STARTUP_DELAY = TimeUnit.MINUTES.toSeconds(1);

  private static final long DEFAULT_REPLICATION_PERIOD = TimeUnit.MINUTES.toSeconds(5);

  /**
   * Instantiates a new replication runner.
   *
   * @param replicator the replicator where to send replication requests
   * @param replicatorConfigManager the config manager
   * @param sites the set of sites to be handled by this replication runner or empty to have all
   *     sites handled
   * @param period the replication polling period in seconds
   */
  public ReplicatorRunner(
      Replicator replicator,
      ReplicatorConfigManager replicatorConfigManager,
      Stream<String> sites,
      long period) {
    this(
        Executors.newSingleThreadScheduledExecutor(),
        replicator,
        replicatorConfigManager,
        sites,
        period);
  }

  @VisibleForTesting
  ReplicatorRunner(
      ScheduledExecutorService scheduledExecutor,
      Replicator replicator,
      ReplicatorConfigManager replicatorConfigManager,
      Stream<String> sites,
      long period) {
    this.scheduledExecutor = notNull(scheduledExecutor);
    this.replicator = notNull(replicator);
    this.replicatorConfigManager = notNull(replicatorConfigManager);
    this.sites = sites.collect(Collectors.toSet());
    this.period = period > 0 ? period : DEFAULT_REPLICATION_PERIOD;
  }

  public void init() {
    if (sites.isEmpty()) {
      LOGGER.info("Replication for all sites scheduled for every {} seconds.", period);
    } else {
      LOGGER.info("Replication for sites: {} scheduled for every {} seconds.", sites, period);
    }
    scheduledExecutor.scheduleAtFixedRate(
        this::scheduleReplication, STARTUP_DELAY, period, TimeUnit.SECONDS);
  }

  public void destroy() {
    LOGGER.info("shutting down replicatorRunner");
    scheduledExecutor.shutdownNow();
  }

  @VisibleForTesting
  void scheduleReplication() {
    List<ReplicatorConfig> configsToSchedule =
        replicatorConfigManager
            .objects()
            .filter(c -> !c.isSuspended())
            .collect(Collectors.toList());
    try {
      for (ReplicatorConfig config : configsToSchedule) {
        if (sites.isEmpty() || sites.contains(config.getDestination())) {
          replicator.submitSyncRequest(new SyncRequestImpl(config));
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
