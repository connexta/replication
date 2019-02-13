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

/** A Persistable is an object that can be saved in persistence. */
public interface Persistable {

  /**
   * Returns the ID of the persistable object
   *
   * @return The ID of the object
   */
  String getId();

  /**
   * Sets the ID of the persistable object
   *
   * @param id the ID to set as the persistable object's ID
   */
  void setId(String id);

  /**
   * Returns the version of this persistable
   *
   * @return integer version of the persistable
   */
  int getVersion();
}
