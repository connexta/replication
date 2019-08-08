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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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

  private final long period;

  private static final long STARTUP_DELAY = TimeUnit.MINUTES.toSeconds(1);

  private static final long DEFAULT_REPLICATION_PERIOD = TimeUnit.MINUTES.toSeconds(5);

  public ReplicatorRunner(
      Replicator replicator, ReplicatorConfigManager replicatorConfigManager, long period) {
    this(Executors.newSingleThreadScheduledExecutor(), replicator, replicatorConfigManager, period);
  }

  @VisibleForTesting
  ReplicatorRunner(
      ScheduledExecutorService scheduledExecutor,
      Replicator replicator,
      ReplicatorConfigManager replicatorConfigManager,
      long period) {
    this.scheduledExecutor = notNull(scheduledExecutor);
    this.replicator = notNull(replicator);
    this.replicatorConfigManager = notNull(replicatorConfigManager);
    this.period = period > 0 ? period : DEFAULT_REPLICATION_PERIOD;
  }

  public void init() {
    scheduledExecutor.scheduleAtFixedRate(
        this::scheduleReplication, STARTUP_DELAY, period, TimeUnit.SECONDS);
    LOGGER.info("Replication checks scheduled for every {} seconds.", period);
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
        replicator.submitSyncRequest(new SyncRequestImpl(config));
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
