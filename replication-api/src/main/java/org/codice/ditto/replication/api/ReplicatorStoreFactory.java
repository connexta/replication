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

import java.net.URL;

public interface ReplicatorStoreFactory {

  /**
   * Creates a replicator store for the given url. Multiple requests to this method with the same
   * url should return store objects that are independent of each other.
   *
   * @param url The url the store should connect to
   * @return The created replicator store.
   */
  NodeAdapter createReplicatorStore(URL url);
}
