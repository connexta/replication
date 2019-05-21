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

import java.util.UUID;
import org.codice.ditto.replication.api.data.Persistable;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;

/**
 * This class defines some data that any object to be saved in persistence should have and methods
 * to manipulate that data.
 */
public abstract class AbstractPersistable implements Persistable {

  @Id
  @Indexed(name = "id_txt")
  private String id;

  @Indexed(name = "version_int")
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
}
