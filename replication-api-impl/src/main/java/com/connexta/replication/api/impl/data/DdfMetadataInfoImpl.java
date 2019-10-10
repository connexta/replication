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
package com.connexta.replication.api.impl.data;

import com.connexta.replication.api.data.InvalidFieldException;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.UnsupportedVersionException;
import com.connexta.replication.api.data.ddf.DdfMetadataInfo;
import com.connexta.replication.api.impl.jackson.JsonUtils;
import com.connexta.replication.api.impl.persistence.pojo.DdfMetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Provides Information about metadata collected from DDF, which is useful when processing a {@link
 * Task}.
 *
 * @param <D> the type of raw metadata held by this class
 */
public class DdfMetadataInfoImpl<D> extends MetadataInfoImpl implements DdfMetadataInfo<D> {
  private static final String PERSISTABLE_TYPE = "ddf metadata info";

  private Class<D> dataClass;

  private D data;

  /**
   * Creates a new ddf metadata.
   *
   * @param metadata the ddf metadata to be cloned
   */
  public DdfMetadataInfoImpl(DdfMetadataInfo metadata) {
    super(DdfMetadataInfoImpl.PERSISTABLE_TYPE, metadata);
    this.dataClass = metadata.getDataClass();
    this.data = (D) metadata.getData();
  }

  /**
   * Creates a new ddf metadata by copying the necessary information from the given metadata.
   *
   * @param type a string describing the type of the metadata (e.g. DDMS 2.0, DDMS 5.0, ...)
   * @param metadata the {@link Metadata} to create a DdfMetadataInfoImpl from
   * @throws IllegalArgumentException if the type of the metadata does not match the type of the raw
   *     metadata
   */
  public DdfMetadataInfoImpl(String type, Metadata metadata) {
    super(DdfMetadataInfoImpl.PERSISTABLE_TYPE, type, metadata);
    this.dataClass = metadata.getType();
    this.data = (D) metadata.getRawMetadata();
    if (!dataClass.isInstance(data)) {
      throw new IllegalArgumentException(
          String.format(
              "Metadata type was %s but the actual metadata was %s",
              dataClass.getName(), data.getClass().getName()));
    }
  }

  /**
   * Creates a new ddf metadata with the provided information.
   *
   * @param type a string describing the type of the metadata (e.g. DDMS 2.0, DDMS 5.0, ...)
   * @param lastModified rhe metadata modified time of the DDF metadata
   * @param size rhe size of the metadata in bytes
   * @param dataClass the class (i.e. type) of the raw metadata
   * @param data rhe raw metadata
   * @throws IllegalArgumentException if the type of the data does not match the given dataclass
   */
  public DdfMetadataInfoImpl(
      String type, Instant lastModified, long size, Class<D> dataClass, D data) {
    super(DdfMetadataInfoImpl.PERSISTABLE_TYPE, type, lastModified, size);
    if (!dataClass.isInstance(data)) {
      throw new IllegalArgumentException(
          String.format(
              "Metadata type was %s but the actual metadata was %s",
              dataClass.getName(), data.getClass().getName()));
    }
    this.dataClass = dataClass;
    this.data = data;
  }

  /**
   * Instantiates a ddf metadata info based on the information provided by the specified pojo.
   *
   * @param pojo the pojo to initializes the ddf metadata info with
   */
  public DdfMetadataInfoImpl(DdfMetadataInfoPojo pojo) {
    super(DdfMetadataInfoImpl.PERSISTABLE_TYPE, (String) null);
    readFrom(pojo);
  }

  @Override
  public Class<D> getDataClass() {
    return dataClass;
  }

  @Override
  public D getData() {
    return data;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), dataClass, data);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof DdfMetadataInfoImpl)) {
      final DdfMetadataInfoImpl info = (DdfMetadataInfoImpl) obj;

      return Objects.equals(dataClass, info.dataClass) && Objects.equals(data, info.data);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "DdfMetadataInfoImpl[id=%s, type=%s, lastModified=%s, size=%d, dataClass=%s, data=%s]",
        getId(), type, lastModified, size, dataClass, data);
  }

  @Override
  protected MetadataInfoPojo writeTo(MetadataInfoPojo pojo) {
    if (pojo instanceof DdfMetadataInfoPojo) {
      return writeTo((DdfMetadataInfoPojo) pojo);
    }
    return super.writeTo(pojo);
  }

  protected DdfMetadataInfoPojo writeTo(DdfMetadataInfoPojo pojo) {
    super.writeTo(pojo);
    setOrFailIfNull("data class", this::getDataClass, pojo::setDataClass);
    convertAndSetOrFailIfNull("data", this::getData, DdfMetadataInfoImpl::toJson, pojo::setData);
    return pojo.setDdfVersion(DdfMetadataInfoPojo.CURRENT_VERSION);
  }

  protected final void readFrom(DdfMetadataInfoPojo pojo) {
    super.readFrom(pojo);
    if (pojo.getDdfVersion() < DdfMetadataInfoPojo.MINIMUM_VERSION) {
      throw new UnsupportedVersionException(
          "unsupported "
              + DdfMetadataInfoImpl.PERSISTABLE_TYPE
              + " version: "
              + pojo.getDdfVersion()
              + " for object: "
              + getId());
    } // do support pojo.getDdfVersion() > CURRENT_VERSION for forward compatibility
    readFromCurrentOrFutureVersion(pojo);
  }

  @VisibleForTesting
  void setDataClass(Class<D> dataClass) {
    this.dataClass = dataClass;
  }

  @VisibleForTesting
  void setData(D data) {
    this.data = data;
  }

  private void readFromCurrentOrFutureVersion(DdfMetadataInfoPojo pojo) {
    convertAndSetOrFailIfNull("data class", pojo::getDataClass, this::toClass, this::setDataClass);
    convertAndSetOrFailIfNull("data", pojo::getData, this::fromJson, this::setData);
  }

  @Nullable
  private Class<D> toClass(String dataClass) {
    try {
      return (Class<D>) Class.forName(dataClass);
    } catch (ClassNotFoundException e) {
      super.hasUnknowns = true;
      return null;
    }
  }

  @Nullable
  private D fromJson(String data) {
    if (dataClass == null) {
      return null;
    }
    try {
      return JsonUtils.read(dataClass, data);
    } catch (JsonProcessingException e) {
      super.hasUnknowns = true;
      return null;
    }
  }

  private static String toJson(Object data) {
    try {
      return JsonUtils.write(data);
    } catch (JsonProcessingException e) {
      throw new InvalidFieldException("invalid data: " + data, e);
    }
  }
}
