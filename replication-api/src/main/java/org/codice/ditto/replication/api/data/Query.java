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
package org.codice.ditto.replication.api.data;

import javax.annotation.Nullable;

/** A POJO for representing user queries that are retrieved from the catalog. */
public interface Query {

  /**
   * Get the title of this query.
   *
   * @return the title of the query
   */
  @Nullable
  String getTitle();

  /**
   * Get the cql of the query.
   *
   * @return the cql of the query
   */
  String getCql();
}
