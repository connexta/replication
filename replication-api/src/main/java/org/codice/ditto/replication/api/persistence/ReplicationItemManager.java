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
package org.codice.ditto.replication.api.persistence;

import java.util.List;
import java.util.Optional;
import org.codice.ditto.replication.api.ReplicationItem;

public interface ReplicationItemManager {

  /**
   * If present, returns a {@link ReplicationItem} for the given {@link
   * org.codice.ditto.replication.api.data.Metadata} identified by its.
   *
   * @param metadataId a unique metadata id
   * @param source the source {@link org.codice.ditto.replication.api.NodeAdapter}'s name
   * @param destination the destination {@link org.codice.ditto.replication.api.NodeAdapter}'s name
   * @return an optional containing the item, or an empty optional if there was an error fetching
   *     the item or it was not found.
   */
  Optional<ReplicationItem> getItem(String metadataId, String source, String destination);

  /**
   * Returns a list of {@code ReplicationItem}s associated with a {@link
   * org.codice.ditto.replication.api.data.ReplicatorConfig}.
   *
   * @param configId unique id for the {@link
   *     org.codice.ditto.replication.api.data.ReplicatorConfig}
   * @param startIndex index to start query at
   * @param pageSize max number of results to return in a single query
   * @return list of items for the given {@link
   *     org.codice.ditto.replication.api.data.ReplicatorConfig} id
   * @throws org.codice.ditto.replication.api.ReplicationPersistenceException if there is an error
   *     fetching the items
   */
  List<ReplicationItem> getItemsForConfig(String configId, int startIndex, int pageSize);

  /**
   * Saves a new {@code ReplicationItem}.
   *
   * @param replicationItem the item to save.
   */
  void saveItem(ReplicationItem replicationItem);

  /**
   * Deletes all {@code ReplicationItem}s.
   *
   * @throws org.codice.ditto.replication.api.ReplicationPersistenceException
   */
  void deleteAllItems();

  /**
   * Delete an item associated with the given {@link org.codice.ditto.replication.api.data.Metadata}
   * id.
   *
   * @param metadataId the metadata's id
   * @param source the source {@link org.codice.ditto.replication.api.NodeAdapter} name
   * @param destination the destination {@link org.codice.ditto.replication.api.NodeAdapter} name
   */
  void deleteItem(String metadataId, String source, String destination);

  /**
   * Get the list of IDs for {@link ReplicationItem}s that failed to be transferred between the
   * source and destination {@link org.codice.ditto.replication.api.NodeAdapter}s.
   *
   * @param maximumFailureCount the failure count that {@link ReplicationItem#getFailureCount()}
   *     should not exceed.
   * @param source the source {@link org.codice.ditto.replication.api.NodeAdapter} name
   * @param destination the destination {@link org.codice.ditto.replication.api.NodeAdapter} name
   * @return
   */
  List<String> getFailureList(int maximumFailureCount, String source, String destination);

  /**
   * Deletes all the items for a {@link org.codice.ditto.replication.api.data.ReplicatorConfig}.
   *
   * @param configId id of the {@link org.codice.ditto.replication.api.data.ReplicatorConfig}
   * @throws org.codice.ditto.replication.api.ReplicationPersistenceException if there was an error
   *     deleting the items
   */
  void deleteItemsForConfig(String configId);
}
