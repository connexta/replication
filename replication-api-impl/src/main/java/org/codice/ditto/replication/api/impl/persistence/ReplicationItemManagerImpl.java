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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.impl.data.ReplicationItemImpl;
import org.codice.ditto.replication.api.impl.spring.ItemRepository;
import org.codice.ditto.replication.api.persistence.ReplicationItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

public class ReplicationItemManagerImpl implements ReplicationItemManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationItemManagerImpl.class);

  private static final int DEFAULT_START_INDEX = 0;

  private final ItemRepository itemRepository;

  public ReplicationItemManagerImpl(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Override
  public Optional<ReplicationItem> getItem(String metadataId, String source, String destination) {
    Optional<ReplicationItemImpl> result =
        itemRepository.findByIdAndSourceAndDestination(metadataId, source, destination);

    if (!result.isPresent()) {
      LOGGER.debug(
          "couldn't find persisted item with id: {}, source: {}, and destination: {}. This is expected during initial replication.",
          metadataId,
          source,
          destination);
    }
    return result.map(ReplicationItem.class::cast);
  }

  @Override
  public void deleteAllItems() {
    itemRepository.deleteAll();
  }

  @Override
  public List<ReplicationItem> getItemsForConfig(String configId, int startIndex, int pageSize) {
    return itemRepository
        .findByConfigId(configId, PageRequest.of(startIndex, pageSize))
        .map(ReplicationItem.class::cast)
        .getContent();
  }

  @Override
  public void saveItem(ReplicationItem replicationItem) {
    if (replicationItem instanceof ReplicationItemImpl) {
      itemRepository.save((ReplicationItemImpl) replicationItem);
    } else {
      throw new IllegalArgumentException(
          "Expected a ReplicationItemImpl but got a " + replicationItem.getClass().getSimpleName());
    }
  }

  @Override
  public void deleteItem(String metadataId, String source, String destination) {
    itemRepository.deleteByIdAndSourceAndDestination(metadataId, source, destination);
  }

  @Override
  public List<String> getFailureList(int maximumFailureCount, String source, String destination) {
    return itemRepository
        .findByFailureCountBetweenAndSourceAndDestination(
            1,
            maximumFailureCount - 1,
            source,
            destination,
            PageRequest.of(DEFAULT_START_INDEX, 50))
        .stream()
        .map(ReplicationItemImpl::getId)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteItemsForConfig(String configId) {
    itemRepository.deleteByConfigId(configId);
  }
}
