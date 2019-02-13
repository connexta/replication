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
package org.codice.ditto.replication.commands;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.types.Core;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.impl.DeleteRequestImpl;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.util.impl.ResultIterable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.codice.ddf.commands.catalog.SubjectCommands;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ddf.persistence.PersistenceException;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.ReplicationPersistentStore;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.mcard.Replication;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.codice.ditto.replication.api.mcard.ReplicationHistory;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Command(
  scope = "replication",
  name = "delete",
  description = "Delete replication configurations, metacards and history"
)
public class ConfigDeleteCommand extends SubjectCommands {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDeleteCommand.class);

  @Option(
    name = "--delete-data",
    aliases = {"-m"},
    description = "Delete replicated metacards/products"
  )
  boolean deleteMetacards;

  @Option(
    name = "--delete-history",
    aliases = {"-h"},
    description = "Delete replication history"
  )
  boolean deleteHistory;

  @Option(
    name = "--delete-config",
    aliases = {"-c"},
    description = "Delete replication configuration"
  )
  boolean deleteConfig;

  @Option(
    name = "--force",
    aliases = {"-f"},
    description =
        "Delete replication configuration even if the metacard or history deletions fail. Only applies when configNames are supplied"
  )
  boolean force;

  @Argument(
    name = "configNames",
    description =
        "Names of the replication configurations to perform removes on. If none are supplied then all requested items types will be removed",
    multiValued = true
  )
  @Nullable
  List<String> configNames;

  @Reference ReplicatorConfigManager replicatorConfigManager;

  @Reference CatalogFramework framework;

  @Reference ReplicationPersistentStore store;

  @Reference FilterBuilder builder;

  private static final int BATCH_SIZE = 250;

  private static final int DEFAULT_PAGE_SIZE = 1000;

  private static final int DEFAULT_START_INDEX = 0;

  @Override
  protected final Object executeWithSubject() throws Exception {
    if (CollectionUtils.isEmpty(configNames)) {
      if (deleteMetacards) {
        removeAllReplicatedMetacards();
        removeReplicationItems(null);
      }

      if (deleteHistory) {
        removeHistoryItems(null);
      }

      if (deleteConfig) {
        replicatorConfigManager
            .objects()
            .map(ReplicatorConfig::getId)
            .forEach(replicatorConfigManager::remove);
      }
    } else {
      List<ReplicatorConfig> configs =
          replicatorConfigManager.objects().collect(Collectors.toList());
      configNames
          .stream()
          .distinct()
          .forEach(
              configName -> {
                final ReplicatorConfig config =
                    configs
                        .stream()
                        .filter(c -> c.getName().equals(configName))
                        .findFirst()
                        .orElse(null);

                if (config != null) {
                  executeWithExistingConfig(configName, config);
                } else {
                  printErrorMessage(
                      "There are no replication configurations with the name \""
                          + configName
                          + "\". Nothing was executed for that name.");
                }
              });
    }

    return null;
  }

  void executeWithExistingConfig(final String configName, final ReplicatorConfig config) {

    boolean success = true;
    if (deleteMetacards) {
      success = removeMetacards(config.getId()) && removeReplicationItems(config.getId());
    }

    if (success && deleteHistory) {
      success = removeHistoryItems(config.getName());
    }

    if (deleteConfig && (force || success)) {
      try {
        replicatorConfigManager.remove(config.getId());
        printSuccessMessage(
            "The replication configuration with the name \"" + configName + "\" was deleted.");
      } catch (ReplicationException e) {
        printErrorMessage("Problem removing replication configuration ");
      }
    }
  }

  private boolean removeReplicationItems(@Nullable String configId) {
    try {
      if (configId == null) {
        store.deleteAllItems();
      } else {
        store.deleteItemsForConfig(configId);
      }
    } catch (PersistenceException e) {
      printErrorMessage("Problem removing replication items: " + e.getMessage());
      return false;
    }
    return true;
  }

  private boolean removeMetacards(String id) {
    printSuccessMessage("Removing replicated metacards/products ...");
    try {
      int startIndex = DEFAULT_START_INDEX;
      int deletedMetacardCount = 0;

      String[] idsToDelete;
      do {
        idsToDelete =
            store
                .getItemsForConfig(id, startIndex, DEFAULT_PAGE_SIZE)
                .stream()
                .filter(item -> item.getDestination().equals(SystemInfo.getSiteName()))
                .map(ReplicationItem::getMetacardId)
                .toArray(String[]::new);

        if (idsToDelete.length != 0) {
          String[] idsInTheCatalog = getIdsOfMetacardsInCatalog(idsToDelete);
          doDelete(idsInTheCatalog);
          deletedMetacardCount += idsInTheCatalog.length;
          startIndex += idsToDelete.length;
        }
      } while (idsToDelete.length != 0);

      printSuccessMessage("Removed " + deletedMetacardCount + " replicated metacards/products");

    } catch (SourceUnavailableException | PersistenceException e) {
      printErrorMessage("Problem removing replicated metacards: " + e.getMessage());
      return false;
    }
    return true;
  }

  private String[] getIdsOfMetacardsInCatalog(String[] idsToQueryFor) {
    List<Filter> filters = new ArrayList<>();

    for (String idString : idsToQueryFor) {
      filters.add(builder.attribute(Core.ID).is().equalTo().text(idString));
    }
    Filter filter = builder.anyOf(filters);

    QueryRequest request =
        new QueryRequestImpl(
            new QueryImpl(
                filter,
                1,
                idsToQueryFor.length,
                new SortByImpl(Core.ID, SortOrder.ASCENDING),
                false,
                0L));

    ResultIterable results = ResultIterable.resultIterable(framework::query, request);
    return results.stream().map(Result::getMetacard).map(Metacard::getId).toArray(String[]::new);
  }

  private void doDelete(String[] idsToDelete) throws SourceUnavailableException {
    if (idsToDelete.length > 0) {
      int start = 0;
      int end;

      while (start < idsToDelete.length) {
        end = start + BATCH_SIZE;
        if (end > idsToDelete.length) {
          end = idsToDelete.length;
        }
        deleteBatch(Arrays.copyOfRange(idsToDelete, start, end));
        start += BATCH_SIZE;
      }
    }
  }

  private void deleteBatch(String[] idsToDelete) throws SourceUnavailableException {
    try {
      framework.delete(new DeleteRequestImpl(idsToDelete));
    } catch (IngestException ie) {

      // One metacard failing to delete will cause the entire batch to not be deleted. So,
      // if the batch fails, perform the deletes individually and just skip over the ones that fail.
      for (String id : idsToDelete) {
        try {
          framework.delete(new DeleteRequestImpl(id));
        } catch (IngestException e) {
          LOGGER.debug("Failed to delete metacard with id:%s because of exception {}", id, e);
        }
      }
    }
  }

  private boolean removeHistoryItems(String configName) {
    int deleteCount = 0;
    try {
      Filter filter =
          builder
              .attribute(Core.METACARD_TAGS)
              .is()
              .equalTo()
              .text(ReplicationHistory.METACARD_TAG);
      if (configName != null) {
        filter =
            builder.allOf(
                filter, builder.attribute(ReplicationConfig.NAME).is().equalTo().text(configName));
      }
      QueryRequest request = new QueryRequestImpl(new QueryImpl(filter));
      ResultIterable iterable = ResultIterable.resultIterable(framework, request);
      List<Serializable> idsToDelete = new ArrayList<>();
      for (Result result : iterable) {
        idsToDelete.add(result.getMetacard().getId());
        deleteCount++;
        if (idsToDelete.size() >= BATCH_SIZE) {
          framework.delete(new DeleteRequestImpl(idsToDelete, Core.ID, new HashMap<>()));
          idsToDelete.clear();
        }
      }
      if (!idsToDelete.isEmpty()) {
        framework.delete(new DeleteRequestImpl(idsToDelete, Core.ID, new HashMap<>()));
      }
    } catch (IngestException | SourceUnavailableException e) {
      printErrorMessage("Problem removing replication history: " + e.getMessage());
      return false;
    }
    printSuccessMessage("Removed " + deleteCount + " history items");
    return true;
  }

  private boolean removeAllReplicatedMetacards() {
    int deleteCount = 0;
    try {
      Filter filter =
          builder.attribute(Core.METACARD_TAGS).is().equalTo().text(Replication.REPLICATED_TAG);

      QueryRequest request = new QueryRequestImpl(new QueryImpl(filter));
      ResultIterable iterable = ResultIterable.resultIterable(framework, request);
      List<Serializable> idsToDelete = new ArrayList<>();
      for (Result result : iterable) {
        idsToDelete.add(result.getMetacard().getId());
        deleteCount++;
        if (idsToDelete.size() >= BATCH_SIZE) {
          framework.delete(new DeleteRequestImpl(idsToDelete, Core.ID, new HashMap<>()));
          idsToDelete.clear();
        }
      }
      if (!idsToDelete.isEmpty()) {
        framework.delete(new DeleteRequestImpl(idsToDelete, Core.ID, new HashMap<>()));
      }
    } catch (IngestException | SourceUnavailableException e) {
      printErrorMessage("Problem removing replication history: " + e.getMessage());
      return false;
    }
    printSuccessMessage("Removed " + deleteCount + " history items");
    return true;
  }
}
