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
package org.codice.ditto.replication.api.impl.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.codice.ditto.replication.api.data.Persistable;

/**
 * This class defines some data that any object to be saved in persistence should have and methods
 * to manipulate that data.
 */
public abstract class AbstractPersistable implements Persistable {

  private static final String ID_KEY = "id";

  private static final String VERSION_KEY = "version";

  private String id;

  private int version;

  protected AbstractPersistable() {
    this.id = UUID.randomUUID().toString();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * Writes the variables of the persistable to a map. Any implementation of this method in a
   * subclass should first make a call to the super version before performing its own functionality.
   *
   * @return a map containing the variable of the persistable
   */
  public Map<String, Object> toMap() {
    Map<String, Object> result = new HashMap<>();
    result.put(ID_KEY, getId());
    result.put(VERSION_KEY, getVersion());
    return result;
  }

  /**
   * Attempts to populate the variables of the persistable by reading them from the given map. Any
   * implementation of this method in a subclass should first make a call to the super version
   * before performing its own functionality.
   *
   * @param properties the properties to read into the persistable's variables
   */
  public void fromMap(Map<String, Object> properties) {
    setId((String) properties.get(ID_KEY));
    setVersion((int) properties.get(VERSION_KEY));
  }
}
