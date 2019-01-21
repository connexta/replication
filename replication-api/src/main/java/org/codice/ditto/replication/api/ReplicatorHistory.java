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
   * @param replicatorid replication configuration id
   * @return List of associated replication events
   */
  List<ReplicationStatus> getReplicationEvents(String replicatorid);

  /**
   * Add a replication event to the history
   *
   * @param replicationStatus ReplicationConfig event to store
   */
  void addReplicationEvent(ReplicationStatus replicationStatus);
}
