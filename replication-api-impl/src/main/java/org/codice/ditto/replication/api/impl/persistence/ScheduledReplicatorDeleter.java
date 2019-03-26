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
package org.codice.ditto.replication.api.impl.persistence;

import com.google.common.collect.Sets;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.service.SecurityServiceException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ddf.persistence.PersistenceException;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.*;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically polls for available {@link ReplicatorConfig}s and deletes them based on the {@link
 * ReplicatorConfig#isDeleted()} and {@link ReplicatorConfig#deleteData()} properties. A {@link
 * ReplicatorConfig} marked as deleted always has its history deleted.
 */
public class ScheduledReplicatorDeleter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledReplicatorDeleter.class);

  private static final int DEFAULT_PAGE_SIZE = 1000;

  private final ReplicatorConfigManager replicatorConfigManager;

  private final ReplicationItemManager replicationItemManager;

  private final ReplicatorHistory replicatorHistory;

  private final Metacards metacards;

  private final ScheduledExecutorService scheduledExecutorService;

  private final Security security;

  public ScheduledReplicatorDeleter(
      ReplicatorConfigManager replicatorConfigManager,
      ScheduledExecutorService scheduledExecutorService,
      ReplicationItemManager replicationItemManager,
      ReplicatorHistory replicatorHistory,
      Metacards metacards) {
    this(
        replicatorConfigManager,
        scheduledExecutorService,
        replicationItemManager,
        replicatorHistory,
        metacards,
        Security.getInstance(),
        TimeUnit.MINUTES.toMillis(1));
  }

  public ScheduledReplicatorDeleter(
      ReplicatorConfigManager replicatorConfigManager,
      ScheduledExecutorService scheduledExecutorService,
      ReplicationItemManager replicationItemManager,
      ReplicatorHistory replicatorHistory,
      Metacards metacards,
      Security security,
      long pollPeriod) {
    this.replicatorConfigManager = replicatorConfigManager;
    this.scheduledExecutorService = scheduledExecutorService;
    this.replicationItemManager = replicationItemManager;
    this.replicatorHistory = replicatorHistory;
    this.metacards = metacards;
    this.security = security;

    LOGGER.info(
        "Scheduling replicator config cleanup every {} seconds",
        TimeUnit.MILLISECONDS.toSeconds(pollPeriod));
    scheduledExecutorService.scheduleAtFixedRate(
        this::cleanup, 0, pollPeriod, TimeUnit.MILLISECONDS);
  }

  public void destroy() {
    scheduledExecutorService.shutdownNow();
  }

  public void cleanup() {
    security.runAsAdmin(
        () -> {
          try {
            security.runWithSubjectOrElevate(
                () -> {
                  List<ReplicatorConfig> replicatorConfigs =
                      replicatorConfigManager.objects().collect(Collectors.toList());

                  cleanupOrphanedReplicationItems(replicatorConfigs);
                  cleanupDeletedConfigs(replicatorConfigs);

                  return null;
                });
          } catch (SecurityServiceException | InvocationTargetException e) {
            LOGGER.debug("Failed scheduled cleanup of deleted replicator configs", e);
          }

          return null;
        });
  }

  /**
   * Deletes {@link ReplicationItem}s which have no corresponding data store entry or {@link
   * ReplicatorConfig}. This occurs when a {@link ReplicatorConfig} was deleted without cleaning up
   * data, but the data was then deleted manually afterwards.
   */
  private void cleanupOrphanedReplicationItems(List<ReplicatorConfig> replicatorConfigs) {
    Set<String> replicatorConfigIds =
        replicatorConfigs.stream().map(ReplicatorConfig::getId).collect(Collectors.toSet());

    int startIndex = 0;
    Set<String> configlessItemIds = Collections.emptySet();

    do {
      try {

        configlessItemIds =
            replicationItemManager
                .getItemsForConfig("", startIndex, DEFAULT_PAGE_SIZE)
                .stream()
                .filter(item -> !replicatorConfigIds.contains(item.getConfigurationId()))
                .map(ReplicationItem::getMetacardId)
                .collect(Collectors.toSet());

        if (!configlessItemIds.isEmpty()) {
          Set<String> idsInTheCatalog = metacards.getIdsOfMetacardsInCatalog(configlessItemIds);
          Set<String> orphanedItemIds = Sets.difference(configlessItemIds, idsInTheCatalog);
          orphanedItemIds.forEach(replicationItemManager::deleteItem);

          // todo: do we really increment the index this way?
          startIndex += configlessItemIds.size();
        }
      } catch (PersistenceException e) {
        LOGGER.debug("Failed to retrieve replication items. Continuing", e);
        startIndex += DEFAULT_PAGE_SIZE;
      }
    } while (!configlessItemIds.isEmpty());
  }

  private void cleanupDeletedConfigs(List<ReplicatorConfig> replicatorConfigs) {
    List<ReplicatorConfig> deletedConfigs =
        replicatorConfigs.stream().filter(ReplicatorConfig::isDeleted).collect(Collectors.toList());

    for (ReplicatorConfig config : deletedConfigs) {
      final String configId = config.getId();
      final String configName = config.getName();

      if (config.deleteData()) {
        try {
          deleteReplicationItems(configId);
        } catch (PersistenceException e) {
          LOGGER.debug(
              "Failed to retrieve replication items for config: {}. Deletion will be retried next poll interval.",
              configName,
              e);
          return;
        } catch (SourceUnavailableException e) {
          LOGGER.debug(
              "Failed to delete metacards replicated by config: {}. Deletion will be retried next poll interval.",
              configName,
              e);
          return;
        }
      }

      try {
        deleteReplicatorHistory(configId);
      } catch (ReplicationPersistenceException e) {
        LOGGER.debug(
            "History for replicator configuration {} could not be deleted. Deletion will be retried next polling interval.",
            configName,
            e);
        return;
      }

      replicatorConfigManager.remove(configId);
    }
  }

  private void deleteReplicationItems(String configId)
      throws PersistenceException, SourceUnavailableException {
    int startIndex = 0;
    Set<String> idsToDelete;

    do {
      idsToDelete =
          replicationItemManager
              .getItemsForConfig(configId, startIndex, DEFAULT_PAGE_SIZE)
              .stream()
              .filter(item -> item.getDestination().equals(SystemInfo.getSiteName()))
              .map(ReplicationItem::getMetacardId)
              .collect(Collectors.toSet());

      if (!idsToDelete.isEmpty()) {
        Set<String> idsInTheCatalog = metacards.getIdsOfMetacardsInCatalog(idsToDelete);
        metacards.doDelete(idsInTheCatalog.toArray(new String[0]));
        startIndex += idsToDelete.size();
      }

    } while (!idsToDelete.isEmpty());

    replicationItemManager.deleteItemsForConfig(configId);
  }

  private void deleteReplicatorHistory(String replicatorId) {
    ReplicatorConfig config = replicatorConfigManager.get(replicatorId);
    Set<String> eventIds =
        replicatorHistory
            .getReplicationEvents(config.getName())
            .stream()
            .map(ReplicationStatus::getId)
            .collect(Collectors.toSet());

    replicatorHistory.removeReplicationEvents(eventIds);
  }
}
