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
package com.connexta.replication.api.persistence;

import com.connexta.ion.replication.api.NodeAdapter;
import com.connexta.ion.replication.api.data.Metadata;
import com.connexta.replication.api.data.ReplicationItem;
import com.connexta.replication.api.data.ReplicationPersistenceException;
import com.connexta.replication.api.data.ReplicatorConfig;
import java.util.List;
import java.util.Optional;

public interface ReplicationItemManager extends DataManager<ReplicationItem> {

  /**
   * If present, returns the latest {@link ReplicationItem} (based on {@link
   * ReplicationItem#getDoneTime()}) for the given {@link Metadata}.
   *
   * @param configId id for the {@link ReplicatorConfig} which the item belongs to
   * @param metadataId a unique metadata id
   * @return an optional containing the item, or an empty optional if there was an error fetching
   *     the item or it was not found.
   */
  Optional<ReplicationItem> getLatest(String configId, String metadataId);

  /**
   * Returns a list of {@code ReplicationItem}s associated with a {@link ReplicatorConfig}.
   *
   * @param configId unique id for the {@link ReplicatorConfig}
   * @param startIndex index to start query at
   * @param pageSize max number of results to return in a single query
   * @return list of items for the given {@link ReplicatorConfig} id
   * @throws ReplicationPersistenceException if there is an error fetching the items
   */
  List<ReplicationItem> getAllForConfig(String configId, int startIndex, int pageSize);

  /**
   * Get the list of IDs for {@link ReplicationItem}s that failed to be transferred between the
   * source and destination {@link NodeAdapter}s.
   *
   * @param configId the {@link ReplicatorConfig} id to get failures for
   * @return list of ids for items that failed to be transferred
   */
  List<String> getFailureList(String configId);

  /**
   * Deletes all the items for a {@link ReplicatorConfig}.
   *
   * @param configId id of the {@link ReplicatorConfig}
   * @throws ReplicationPersistenceException if there was an error deleting the items
   */
  void removeAllForConfig(String configId);
}
