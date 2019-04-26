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

public interface QueryResponse {

  /**
   * An iterable of {@link Metadata} returned by a {@link QueryRequest} sent to a {@link
   * org.codice.ditto.replication.api.NodeAdapter}, which translates a node's specific metadata
   * format into {@link Metadata}.
   *
   * <p>The returned {@link Metadata} must be sorted in ascending order according to the {@link
   * Metadata#getMetadataModified()} field.
   *
   * <p>Considerations of paging when iterating should be taken into account in order to avoid
   * memory issues.
   *
   * @return the iterable of {@link Metadata}
   */
  Iterable<Metadata> getMetadata();
}
