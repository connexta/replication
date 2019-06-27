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
package com.connexta.ion.replication.api.persistence;

import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.ion.replication.api.data.ReplicationStatus;

/** Persistence interface for storing, retrieving, and deleting history items */
public interface ReplicatorHistoryManager extends DataManager<ReplicationStatus> {

  /**
   * Gets the replication history for a given replication configuration
   *
   * @param replicatorId the replication configuration id
   * @return the {@link ReplicationStatus} associated with the given
   * @throws ReplicationPersistenceException if an error occurs while trying to retrieve the object
   * @throws NotFoundException if a {@link ReplicationStatus} with the given replicator id cannot be
   *     found
   */
  ReplicationStatus getByReplicatorId(String replicatorId);
}
