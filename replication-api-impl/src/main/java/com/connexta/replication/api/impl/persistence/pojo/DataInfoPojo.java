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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * This class provides a pojo implementation for data information objects capable of reloading all
 * supported fields for all supported versions from Json using Jackson. It also provides the
 * capability of persisting back the fields based on the latest version format.
 *
 * @param <T> the type of data info pojo this is
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class DataInfoPojo<T extends DataInfoPojo<?>> extends Pojo<T> {
  @JsonProperty("last_modified")
  @Nullable
  private Instant lastModified;

  @JsonProperty("size")
  private long size = -1L;

  /**
   * Gets the last modified timestamp from the source site for the piece of data that should be
   * transferred by the task.
   *
   * @return last modified timestamp for the piece of data
   */
  @Nullable
  public Instant getLastModified() {
    return lastModified;
  }

  /**
   * Sets the last modified timestamp from the source site for the piece of data that should be
   * transferred by the task.
   *
   * @param lastModified the last modified timestamp for the piece of data
   * @return this for chaining
   */
  public T setLastModified(@Nullable Instant lastModified) {
    this.lastModified = lastModified;
    return (T) this;
  }

  /**
   * Gets the size of the piece of data to be transferred if known.
   *
   * @return the size of the piece of data or a negative value if unknown
   */
  public long getSize() {
    return size;
  }

  /**
   * Sets the size of the piece of data to be transferred if known.
   *
   * @param size the size of the piece of data or a negative value if unknown
   * @return this for chaining
   */
  public T setSize(long size) {
    this.size = size;
    return (T) this;
  }

  @Override
  public int hashCode() {
    return hashCode0();
  }

  @Override
  public boolean equals(Object obj) {
    return equals0(obj);
  }

  @Override
  public String toString() {
    return String.format("DataInfoPojo[lastModified=%s, size=%d]", lastModified, size);
  }

  @VisibleForTesting
  int hashCode0() {
    return Objects.hash(super.hashCode(), lastModified, size);
  }

  @VisibleForTesting
  boolean equals0(Object obj) {
    if (super.equals(obj) && (obj instanceof DataInfoPojo)) {
      final DataInfoPojo<?> pojo = (DataInfoPojo<?>) obj;

      return (size == pojo.size) && Objects.equals(lastModified, pojo.lastModified);
    }
    return false;
  }
}
