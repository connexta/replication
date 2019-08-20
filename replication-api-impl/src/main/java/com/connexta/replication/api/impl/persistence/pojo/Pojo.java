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
package com.connexta.replication.api.impl.persistence.pojo;

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;

/**
 * This class provides a base pojo implementation capable of reloading common supported fields for
 * all supported versions from the database. It also provides the capability of persisting back
 * those common fields based on the latest version format.
 *
 * @param <T> the type of pojo this is
 */
public abstract class Pojo<T extends Pojo> {
  @Id
  @Indexed(name = "id", required = true)
  @Nullable
  private String id;

  @Indexed(name = "version", required = true)
  private int version;

  /** Instantiates a blank pojo. */
  protected Pojo() {}

  /**
   * Gets the identifier for this pojo.
   *
   * @return the pojo id
   */
  @Nullable
  public String getId() {
    return id;
  }

  /**
   * Sets the identifier for this pojo.
   *
   * @param id the id for this pojo
   * @return this for chaining
   */
  public T setId(String id) {
    this.id = id;
    return (T) this;
  }

  /**
   * Gets the serialized version for this pojo.
   *
   * @return the version for this pojo
   */
  public int getVersion() {
    return version;
  }

  /**
   * Sets the serialized version for this pojo.
   *
   * @param version the pojo version
   * @return this for chaining
   */
  public T setVersion(int version) {
    this.version = version;
    return (T) this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Pojo) {
      final Pojo pojo = (Pojo) obj;

      return (version == pojo.version) && Objects.equals(id, pojo.id);
    }
    return false;
  }
}
