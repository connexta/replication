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
package com.connexta.replication.api;

import com.connexta.replication.api.data.SiteType;
import java.net.URL;

/**
 * Factories which enable the {@link Replicator} to create {@link NodeAdapter}s to perform
 * replication.
 */
public interface NodeAdapterFactory {

  /**
   * Creates a new {@link NodeAdapter}.
   *
   * @param url the base url of the system
   * @return the adapter
   * @throws AdapterException if the adapter couldn't be created from the url
   */
  NodeAdapter create(URL url);

  /** @return the type of the node that will be created */
  SiteType getType();
}
