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
package com.connexta.replication.api.impl.query;

import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.ddf.DdfMetadataInfo;
import java.time.Instant;
import java.util.OptionalLong;

/**
 * Provides Information about metadata collected from a DDF, which is useful when processing a
 * {@link Task}.
 */
// todo Move back to query module when possible
public class DdfMetadataInfoImpl<T> implements DdfMetadataInfo<T> {

  private String type;

  private Instant lastModified;

  private long size;

  private Class<T> dataClass;

  private T data;

  /**
   * Creates a new DdfMetadataInfoImpl by copying the necessary information from the given metadata.
   *
   * @param type A String describing the type of the metadata (e.g. DDMS 2.0, DDMS 5.0, ...)
   * @param metadata The {@link Metadata} to create a DdfMetadataInfoImpl from
   * @throws IllegalArgumentException if the type of the metadata does not match the type of the raw
   *     metadata
   */
  public DdfMetadataInfoImpl(String type, Metadata metadata) {
    this(
        type,
        metadata.getMetadataModified().toInstant(),
        metadata.getMetadataSize(),
        metadata.getType(),
        (T) metadata.getRawMetadata());
  }

  /**
   * Creates a new DdfMetadataInfoImpl with the provided information.
   *
   * @param type A String describing the type of the metadata (e.g. DDMS 2.0, DDMS 5.0, ...)
   * @param lastModified The metadata modified time of the DDF metadata
   * @param size The size of the metadata in bytes
   * @param dataClass The class (i.e. type) of the raw metadata
   * @param data The raw metadata
   * @throws IllegalArgumentException if the type of the data does not match the given dataclass
   */
  public DdfMetadataInfoImpl(
      String type, Instant lastModified, long size, Class<T> dataClass, T data) {
    if (!dataClass.isInstance(data)) {
      throw new IllegalArgumentException(
          String.format(
              "Metadata type was %s but the actual metadata was %s",
              dataClass.getName(), data.getClass()));
    }
    this.type = type;
    this.lastModified = lastModified;
    this.size = size;
    this.dataClass = dataClass;
    this.data = data;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public Instant getLastModified() {
    return lastModified;
  }

  @Override
  public OptionalLong getSize() {
    return OptionalLong.of(size);
  }

  @Override
  public Class<T> getDataClass() {
    return dataClass;
  }

  @Override
  public T getData() {
    return data;
  }
}
