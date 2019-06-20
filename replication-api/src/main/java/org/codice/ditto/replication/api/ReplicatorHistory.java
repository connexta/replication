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
package org.codice.ditto.replication.api;

import java.util.List;
import java.util.Set;

/** Persistence interface for storing, retrieving, and deleting history items */
public interface ReplicatorHistory {

  /**
   * Get all replication events
   *
   * @return List of all replication events
   */
  List<ReplicationStatus> getReplicationEvents();

  /**
   * Get all replication events for a given replication configuration
   *
   * @param replicationConfigId replication configuration id
   * @return List of associated replication events
   */
  List<ReplicationStatus> getReplicationEvents(String replicationConfigId);

  /**
   * Add a replication event to the history
   *
   * @param replicationStatus {@link ReplicationStatus} event to store
   * @throws ReplicationPersistenceException if there is an error adding the {@link
   *     ReplicationStatus}
   */
  void addReplicationEvent(ReplicationStatus replicationStatus);

  /**
   * Updates an existing replication event
   *
   * @param replicationStatus {@link ReplicationStatus} event to update
   * @throws ReplicationPersistenceException if there is an error adding the {@link
   *     ReplicationStatus}
   */
  void updateReplicationEvent(ReplicationStatus replicationStatus);

  /**
   * Remove a replication event from the history
   *
   * @param replicationStatus replication event to remove
   * @throws ReplicationPersistenceException if there is an error deleting the {@link
   *     ReplicationStatus}
   */
  void removeReplicationEvent(ReplicationStatus replicationStatus);

  /**
   * Remove set of replication events from the history
   *
   * @param ids replication event ids
   * @throws ReplicationPersistenceException if there is an error deleting 1 or more provided ids.
   */
  void removeReplicationEvents(Set<String> ids);
}
