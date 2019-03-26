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

import com.google.common.annotations.VisibleForTesting;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.service.SecurityServiceException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ddf.persistence.PersistenceException;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.ReplicationItemManager;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicatorHistory;
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

  private final int pageSize;

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
        TimeUnit.MINUTES.toMillis(1),
        DEFAULT_PAGE_SIZE);
  }

  ScheduledReplicatorDeleter(
      ReplicatorConfigManager replicatorConfigManager,
      ScheduledExecutorService scheduledExecutorService,
      ReplicationItemManager replicationItemManager,
      ReplicatorHistory replicatorHistory,
      Metacards metacards,
      Security security,
      long pollPeriod,
      int pageSize) {
    this.replicatorConfigManager = replicatorConfigManager;
    this.scheduledExecutorService = scheduledExecutorService;
    this.replicationItemManager = replicationItemManager;
    this.replicatorHistory = replicatorHistory;
    this.metacards = metacards;
    this.security = security;
    this.pageSize = pageSize;

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
    List<ReplicationItem> replicationItems;

    do {
      try {

        replicationItems = replicationItemManager.getItemsForConfig("", startIndex, pageSize);

        Set<ReplicationItem> configlessItems =
            replicationItems
                .stream()
                .filter(item -> !replicatorConfigIds.contains(item.getConfigurationId()))
                .collect(Collectors.toSet());

        if (!configlessItems.isEmpty()) {
          Set<String> itemMetacardIds =
              configlessItems
                  .stream()
                  .map(ReplicationItem::getMetacardId)
                  .collect(Collectors.toSet());

          Set<String> idsInTheCatalog = metacards.getIdsOfMetacardsInCatalog(itemMetacardIds);

          Set<ReplicationItem> orphanedItems =
              configlessItems
                  .stream()
                  .filter(item -> itemNotInCatalog(item, idsInTheCatalog))
                  .collect(Collectors.toSet());

          orphanedItems
              .stream()
              .map(ReplicationItem::getId)
              .forEach(replicationItemManager::deleteItem);

          startIndex += replicationItems.size() - orphanedItems.size();
        } else {
          startIndex += replicationItems.size();
        }
      } catch (PersistenceException e) {
        LOGGER.debug(
            "Failed to delete orphaned replication items. Deletion will be retried next poll interval.",
            e);
        return;
      }
    } while (!replicationItems.isEmpty());
  }

  private boolean itemNotInCatalog(ReplicationItem item, Set<String> idsInCatalog) {
    return !idsInCatalog.contains(item.getId());
  }

  private void cleanupDeletedConfigs(List<ReplicatorConfig> replicatorConfigs) {
    List<ReplicatorConfig> deletedConfigs =
        replicatorConfigs.stream().filter(ReplicatorConfig::isDeleted).collect(Collectors.toList());

    for (ReplicatorConfig config : deletedConfigs) {
      final String configId = config.getId();
      final String configName = config.getName();

      if (config.deleteData()) {
        try {
          deleteMetacards(configId);
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
        replicationItemManager.deleteItemsForConfig(configId);
      } catch (PersistenceException e) {
        LOGGER.debug(
            "Failed to delete replication items for config: {}. Deletion will be retried next poll interval",
            configName,
            e);
        return;
      }

      try {
        deleteReplicatorHistory(config);
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

  private void deleteMetacards(String configId)
      throws PersistenceException, SourceUnavailableException {
    int startIndex = 0;
    List<ReplicationItem> replicationItems;

    do {
      replicationItems = replicationItemManager.getItemsForConfig(configId, startIndex, pageSize);

      Set<String> idsToDelete =
          replicationItems
              .stream()
              .filter(item -> item.getDestination().equals(getSiteName()))
              .map(ReplicationItem::getMetacardId)
              .collect(Collectors.toSet());

      if (!idsToDelete.isEmpty()) {
        Set<String> idsInTheCatalog = metacards.getIdsOfMetacardsInCatalog(idsToDelete);
        metacards.doDelete(idsInTheCatalog.toArray(new String[0]));
        startIndex += idsToDelete.size();
      } else {
        startIndex += pageSize;
      }

    } while (!replicationItems.isEmpty());
  }

  private void deleteReplicatorHistory(ReplicatorConfig config) {
    Set<String> eventIds =
        replicatorHistory
            .getReplicationEvents(config.getName())
            .stream()
            .map(ReplicationStatus::getId)
            .collect(Collectors.toSet());

    replicatorHistory.removeReplicationEvents(eventIds);
  }

  /** Solely for mocking out a static call. */
  @VisibleForTesting
  String getSiteName() {
    return SystemInfo.getSiteName();
  }
}
