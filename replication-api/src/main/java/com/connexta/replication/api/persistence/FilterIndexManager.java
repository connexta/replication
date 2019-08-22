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

import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.data.ReplicationPersistenceException;

/** Manages Filter Indices */
public interface FilterIndexManager extends DataManager<FilterIndex> {

  /**
   * Gets an existing index for the given filter, or creates a new index whose id will be the given
   * filter's id.
   *
   * @param filter this index belongs to
   * @return the existing or newly created index
   * @throws ReplicationPersistenceException if there is a deserialization error fetching the
   *     existing index
   */
  FilterIndex getOrCreate(Filter filter);
}
