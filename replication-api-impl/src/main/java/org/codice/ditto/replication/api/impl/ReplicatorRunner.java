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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.codice.ddf.security.Security;
import org.codice.ditto.replication.api.Heartbeater;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.data.Persistable;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.impl.data.SyncRequestImpl;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
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

  private final Heartbeater heartbeater;

  private final ReplicatorConfigManager replicatorConfigManager;

  private final ScheduledExecutorService scheduledExecutor;

  private final SiteManager siteManager;

  private static final long STARTUP_DELAY = TimeUnit.MINUTES.toSeconds(1);

  private static final String DEFAULT_REPLICATION_PERIOD_STR =
      String.valueOf(TimeUnit.MINUTES.toSeconds(5));
  private static final String DEFAULT_HEARTBEAT_PERIOD_STR =
      String.valueOf(TimeUnit.MINUTES.toSeconds(1));

  public ReplicatorRunner(
      ScheduledExecutorService scheduledExecutor,
      Replicator replicator,
      Heartbeater heartbeater,
      ReplicatorConfigManager replicatorConfigManager,
      SiteManager siteManager,
      Security security) {
    this.scheduledExecutor = notNull(scheduledExecutor);
    this.replicator = notNull(replicator);
    this.heartbeater = notNull(heartbeater);
    this.replicatorConfigManager = notNull(replicatorConfigManager);
    this.siteManager = notNull(siteManager);
    this.security = security;
  }

  public void init() {
    final long replicationPeriod =
        Long.parseLong(
            System.getProperty("org.codice.replication.period", DEFAULT_REPLICATION_PERIOD_STR));
    final long heartbeatPeriod =
        Long.parseLong(
            System.getProperty(
                "org.codice.replication.heartbeat.period", DEFAULT_HEARTBEAT_PERIOD_STR));

    scheduledExecutor.scheduleAtFixedRate(
        this::replicateAsSystemUser, STARTUP_DELAY, replicationPeriod, TimeUnit.SECONDS);
    LOGGER.info("Replication checks scheduled for every {} seconds.", replicationPeriod);
    scheduledExecutor.scheduleAtFixedRate(
        this::heartbeatAsSystemUser, STARTUP_DELAY, heartbeatPeriod, TimeUnit.SECONDS);
    LOGGER.info("Heartbeats scheduled for every {} seconds.", heartbeatPeriod);
  }

  public void destroy() {
    scheduledExecutor.shutdownNow();
  }

  @VisibleForTesting
  void replicateAsSystemUser() {
    security.runAsAdmin(
        () -> {
          try {
            security.runWithSubjectOrElevate(this::scheduleReplication);
          } catch (SecurityServiceException | InvocationTargetException e) {
            LOGGER.debug("Error scheduling replication.", e);
          }
          return null;
        });
  }

  @VisibleForTesting
  void heartbeatAsSystemUser() {
    security.runAsAdmin(
        () -> {
          try {
            security.runWithSubjectOrElevate(this::scheduleHeartbeat);
          } catch (SecurityServiceException | InvocationTargetException e) {
            LOGGER.debug("Error scheduling heartbeat.", e);
          }
          return null;
        });
  }

  @VisibleForTesting
  Void scheduleReplication() {
    final Map<String, ReplicationSite> sites =
        siteManager.objects().collect(Collectors.toMap(Persistable::getId, Function.identity()));
    final List<ReplicatorConfig> configsToSchedule =
        replicatorConfigManager
            .objects()
            .filter(((Predicate<ReplicatorConfig>) ReplicatorConfig::isSuspended).negate())
            .collect(Collectors.toList());

    try {
      for (ReplicatorConfig config : configsToSchedule) {
        final String sourceId = config.getSource();
        final ReplicationSite source = sites.get(sourceId);
        final String destinationId = config.getDestination();
        final ReplicationSite destination = sites.get(destinationId);

        if (source == null) {
          LOGGER.debug(
              "Unable to determine the source '{}' for replication '{}'. This replication will not be run.",
              sourceId,
              config.getName());
        } else if (destination == null) {
          LOGGER.debug(
              "Unable to determine the destination '{}' for replication '{}'. This replication will not be run.",
              destinationId,
              config.getName());
        } else if (source.isRemoteManaged()) {
          LOGGER.trace(
              "Config {}'s source site is remotely managed, not running the config",
              config.getName());
        } else if (destination.isRemoteManaged()) {
          LOGGER.trace(
              "Config {}'s destination site is remotely managed, not running the config",
              config.getName());
        } else {
          replicator.submitSyncRequest(
              new SyncRequestImpl(
                  config, source, destination, new ReplicationStatusImpl(config.getName())));
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return null;
  }

  @VisibleForTesting
  Void scheduleHeartbeat() {
    siteManager
        .objects()
        .filter(ReplicatorRunner::isRemoteOrNotVerified)
        .forEach(this::scheduleHeartbeat);
    return null;
  }

  private void scheduleHeartbeat(ReplicationSite site) {
    try {
      heartbeater.heartbeat(site);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static boolean isRemoteOrNotVerified(ReplicationSite site) {
    // even if not verified yet, try the heartbeater as it will first verify it and skip it if
    // it turns out it ain't remotely managed
    return site.isRemoteManaged() || (site.getVerifiedUrl() == null);
  }
}
