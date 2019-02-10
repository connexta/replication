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
import ddf.security.service.SecurityServiceException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.impl.data.SyncRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ReplicatorRunner periodical queues up replication jobs for all the current replication
 * configurations.
 */
public class ReplicatorRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorRunner.class);

  private final Security security;

  private final Replicator replicator;

  private final ReplicatorConfigLoader replicatorConfigLoader;

  private final ScheduledExecutorService scheduledExecutor;

  private static final long STARTUP_DELAY = TimeUnit.MINUTES.toSeconds(1);

  private static final String DEFAULT_REPLICATION_PERIOD_STR =
      String.valueOf(TimeUnit.MINUTES.toSeconds(5));

  public ReplicatorRunner(
      ScheduledExecutorService scheduledExecutor,
      Replicator replicator,
      ReplicatorConfigLoader replicatorConfigLoader) {
    this(scheduledExecutor, replicator, replicatorConfigLoader, Security.getInstance());
  }

  /*Visible for testing only*/
  ReplicatorRunner(
      ScheduledExecutorService scheduledExecutor,
      Replicator replicator,
      ReplicatorConfigLoader replicatorConfigLoader,
      Security security) {
    this.scheduledExecutor = notNull(scheduledExecutor);
    this.replicator = notNull(replicator);
    this.replicatorConfigLoader = notNull(replicatorConfigLoader);
    this.security = security;
  }

  public void init() {
    long period =
        Long.parseLong(
            System.getProperty("org.codice.replication.period", DEFAULT_REPLICATION_PERIOD_STR));
    scheduledExecutor.scheduleAtFixedRate(
        this::replicateAsSystemUser, STARTUP_DELAY, period, TimeUnit.SECONDS);
    LOGGER.info("Replication checks scheduled for every {} seconds.", period);
  }

  public void destroy() {
    scheduledExecutor.shutdownNow();
  }

  @VisibleForTesting
  void replicateAsSystemUser() {
    security.runAsAdmin(
        () -> {
          try {
            security.runWithSubjectOrElevate(
                () -> {
                  scheduleReplication();
                  return null;
                });
          } catch (SecurityServiceException | InvocationTargetException e) {
            LOGGER.debug("Error scheduling replication.", e);
          }
          return null;
        });
  }

  @VisibleForTesting
  void scheduleReplication() {
    try {
      for (ReplicatorConfig config : replicatorConfigLoader.getAllConfigs()) {
        replicator.submitSyncRequest(
            new SyncRequestImpl(config, new ReplicationStatus(config.getName())));
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
