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

import com.connexta.replication.api.data.FilterIndex;
import java.util.Optional;

/** Manages Filter Indices */
public interface FilterIndexManager extends DataManager<FilterIndex> {

  /**
   * Gets an existing index for the given filter, or creates a new index whose id will be the given
   * id.
   *
   * @param filterId for the index
   * @return the existing or newly created index
   */
  FilterIndex getOrCreate(String filterId);

  /**
   * Retrieves a filter index by the given filter id.
   *
   * @param filterId the filter id to fetch the index for
   * @return the index for the filter, or an empty optional if there is no matching index
   * @throws com.connexta.ion.replication.api.ReplicationPersistenceException if there is an error
   *     deserializing the data
   */
  Optional<FilterIndex> getByFilter(String filterId);
}
