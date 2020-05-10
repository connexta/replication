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

import java.util.stream.Stream;
import org.codice.ditto.replication.api.data.Query;

/** A service which fetches saved user {@link Query}s. */
public interface QueryService {

  /**
   * Provides a stream for retrieving all saved user queries as {@link Query}s.
   *
   * @return all the user queries saved on the system
   * @throws QueryException if an exception occurs while querying the local catalog
   */
  Stream<Query> queries();
}
