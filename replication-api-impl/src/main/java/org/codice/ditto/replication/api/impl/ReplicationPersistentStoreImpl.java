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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codice.ddf.persistence.PersistenceException;
import org.codice.ddf.persistence.PersistentItem;
import org.codice.ddf.persistence.PersistentStore;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.ReplicationPersistentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationPersistentStoreImpl implements ReplicationPersistentStore {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ReplicationPersistentStoreImpl.class);

  private static final String ID_KEY = "id";

  private static final String RESOURCE_MODIFIED_KEY = "resource-modified";

  private static final String METACARD_MODIFIED_KEY = "metacard-modified";

  private static final String SOURCE_NAME_KEY = "source";

  private static final String DESTINATION_NAME_KEY = "destination";

  private static final String CONFIGURATION_ID_KEY = "config-id";

  private static final String FAILURE_COUNT_KEY = "failure-count";

  private static final String PERSISTENCE_TYPE = "replication_item";

  private static final int DEFAULT_PAGE_SIZE = 1000;

  private static final int DEFAULT_START_INDEX = 0;

  private final PersistentStore persistentStore;

  public ReplicationPersistentStoreImpl(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  @Override
  public Optional<ReplicationItem> getItem(String id, String source, String destination) {
    String cqlFilter =
        String.format(
            "'id' = '%s' AND 'source' = '%s' AND 'destination' = '%s'", id, source, destination);
    List<Map<String, Object>> matchingPersistentItems;

    try {
      matchingPersistentItems = persistentStore.get(PERSISTENCE_TYPE, cqlFilter);
    } catch (PersistenceException e) {
      LOGGER.debug(
          "failed to retrieve item with id: {}, source: {}, and destination: {}",
          id,
          source,
          destination);
      return Optional.empty();
    }

    if (matchingPersistentItems == null || matchingPersistentItems.isEmpty()) {
      LOGGER.debug(
          "couldn't find persisted item with id: {}, source: {}, and destination: {}. This is expected during initial replication.",
          id,
          source,
          destination);
      return Optional.empty();
    } else if (matchingPersistentItems.size() > 1) {
      throw new IllegalStateException(
          "Found multiple persistent items with id: "
              + id
              + ", source: "
              + source
              + ", and destination: "
              + destination);
    }

    return Optional.of(mapToReplicationItem(matchingPersistentItems.get(0)));
  }

  @Override
  public void deleteAllItems() throws PersistenceException {
    // passing in empty cql defaults to *:* solr query. proper cql, `id` like `*`, to solr query
    // translation currently does not work.
    String cql = "";
    int index = DEFAULT_START_INDEX;
    long itemsDeleted = 0;
    do {
      itemsDeleted = persistentStore.delete(PERSISTENCE_TYPE, cql, index, DEFAULT_PAGE_SIZE);
      index += DEFAULT_PAGE_SIZE;
    } while (itemsDeleted != 0);
  }

  @Override
  public List<ReplicationItem> getItemsForConfig(String configId, int startIndex, int pageSize)
      throws PersistenceException {
    String cql = String.format("'config-id' = '%s'", configId);
    List<Map<String, Object>> matchingPersistentItems;

    matchingPersistentItems = persistentStore.get(PERSISTENCE_TYPE, cql, startIndex, pageSize);

    return matchingPersistentItems
        .stream()
        .map(this::mapToReplicationItem)
        .collect(Collectors.toList());
  }

  @Override
  public void saveItem(ReplicationItem replicationItem) {
    try {
      persistentStore.add(PERSISTENCE_TYPE, replicationToPersistentItem(replicationItem));
    } catch (PersistenceException e) {
      LOGGER.error("error persisting item");
    }
  }

  @Override
  public void deleteItem(String id, String source, String destination) {
    String cqlFilter =
        String.format(
            "'id' = '%s' AND 'source' = '%s' AND 'destination' = '%s'", id, source, destination);
    try {
      persistentStore.delete(PERSISTENCE_TYPE, cqlFilter);
    } catch (PersistenceException e) {
      LOGGER.error(
          "error deleting persisted item with id: {}, source: {}, and destination: {}",
          id,
          source,
          destination);
    }
  }

  @Override
  public List<String> getFailureList(int maximumFailureCount, String source, String destination) {
    List<String> failureList = new ArrayList<>();
    String cqlFilter =
        String.format(
            "'%s' > 0 AND '%s' < '%d' AND 'source' = '%s' AND 'destination' = '%s'",
            FAILURE_COUNT_KEY, FAILURE_COUNT_KEY, maximumFailureCount, source, destination);
    try {
      // 50 (maxResults) is the batch size for failure retries. if left unspecified, the size will
      // default to 10
      List<Map<String, Object>> failList =
          persistentStore.get(PERSISTENCE_TYPE, cqlFilter, DEFAULT_START_INDEX, 50);
      for (Map<String, Object> failItem : failList) {
        ReplicationItem item = mapToReplicationItem(failItem);
        failureList.add(item.getMetacardId());
      }
    } catch (PersistenceException e) {
      LOGGER.error(
          "error fetching failures from previous run with source: {}, and destination: {}",
          source,
          destination);
    }
    return failureList;
  }

  @Override
  public void deleteItemsForConfig(String configId) throws PersistenceException {
    String cql = String.format("'config-id' = '%s'", configId);
    int itemsDeleted;

    do {
      itemsDeleted =
          persistentStore.delete(PERSISTENCE_TYPE, cql, DEFAULT_START_INDEX, DEFAULT_PAGE_SIZE);
    } while (itemsDeleted == DEFAULT_PAGE_SIZE);
  }

  private PersistentItem replicationToPersistentItem(ReplicationItem replicationItem) {
    PersistentItem persistentItem = new PersistentItem();
    persistentItem.addIdProperty(replicationItem.getMetacardId());
    persistentItem.addProperty(RESOURCE_MODIFIED_KEY, replicationItem.getResourceModified());
    persistentItem.addProperty(METACARD_MODIFIED_KEY, replicationItem.getMetacardModified());
    persistentItem.addProperty(FAILURE_COUNT_KEY, replicationItem.getFailureCount());
    persistentItem.addProperty(SOURCE_NAME_KEY, replicationItem.getSource());
    persistentItem.addProperty(DESTINATION_NAME_KEY, replicationItem.getDestination());
    persistentItem.addProperty(CONFIGURATION_ID_KEY, replicationItem.getConfigurationId());

    return persistentItem;
  }

  private ReplicationItem mapToReplicationItem(Map<String, Object> persistedMap) {
    Map<String, Object> attributes = PersistentItem.stripSuffixes(persistedMap);

    final String metacardId = (String) attributes.get(ID_KEY);
    final Date resourceModified = (Date) attributes.get(RESOURCE_MODIFIED_KEY);
    final Date metacardModified = (Date) attributes.get(METACARD_MODIFIED_KEY);
    final String source = (String) attributes.get(SOURCE_NAME_KEY);
    final String destination = (String) attributes.get(DESTINATION_NAME_KEY);
    final String configId = (String) attributes.get(CONFIGURATION_ID_KEY);
    final int failureCount = (int) attributes.get(FAILURE_COUNT_KEY);

    return new ReplicationItemImpl(
        metacardId,
        resourceModified,
        metacardModified,
        source,
        destination,
        configId,
        failureCount);
  }
}
