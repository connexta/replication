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

import java.util.Date;
import java.util.List;

/**
 * A query request object to be sent to {@link org.codice.ditto.replication.api.NodeAdapter}s to
 * fetch {@link Metadata}.
 */
public interface QueryRequest {

  /**
   * A CQL string representing a filter to search on.
   *
   * <p>If other criteria of the query request is available, it must be respected in the final
   * query.
   *
   * @return the cql
   */
  String getCql();

  /**
   * A list of system names that should be compared against the lineage of a {@link Metadata}. If
   * the {@link Metadata}s lineage contains the {@link
   * org.codice.ditto.replication.api.NodeAdapter}s name, it should not be returned in the query.
   *
   * @return a list of system names to exclude, or empty list if there are none.
   */
  List<String> getExcludedNodes();

  /**
   * A list of {@link Metadata} IDs that have failed to be replicated between {@link
   * org.codice.ditto.replication.api.NodeAdapter}s by the {@link
   * org.codice.ditto.replication.api.Replicator}.
   *
   * @return a list of IDs, or empty list if there are none
   */
  List<String> getFailedItemIds();

  /**
   * A {@link Date} indicating that queried {@link Metadata} should have a modified date after.
   *
   * @return the modified after {@link Date}
   */
  Date getModifiedAfter();

  /**
   * The result index at which the query results should start from
   *
   * @return
   */
  int getStartIndex();

  /**
   * The maximum number of results to return for any single query run
   *
   * @return
   */
  int getPageSize();
}
