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

/** Manages Filter Indices */
public interface FilterIndexManager extends DataManager<FilterIndex> {

  /**
   * Retrieves a filter index by the given filter id.
   *
   * @param filterId the filter id to fetch the index for
   * @return the index for the filter
   */
  FilterIndex getByFilter(String filterId);
}
