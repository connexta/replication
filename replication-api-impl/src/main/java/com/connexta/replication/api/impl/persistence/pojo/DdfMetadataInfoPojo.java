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

import com.connexta.replication.api.impl.jackson.JsonUtils;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownDdfMetadataInfoPojo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * This class provides a pojo implementation for DDF metadata information objects capable of
 * reloading all supported fields for all supported versions from Json using Jackson. It also
 * provides the capability of persisting back the fields based on the latest version format.
 * Versioning is expected to be controlled by the container class.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "clazz",
    defaultImpl = UnknownDdfMetadataInfoPojo.class)
@JsonSubTypes(@Type(DdfMetadataInfoPojo.class))
@JsonTypeName("ddf_metadata")
public class DdfMetadataInfoPojo extends MetadataInfoPojo<DdfMetadataInfoPojo> {
  /**
   * Current version format.
   *
   * <p>Version history:
   *
   * <ul>
   *   <li>1 - Initial Ion version.
   * </ul>
   */
  public static final int CURRENT_VERSION = 1;

  /** The oldest version supported by the current code (anything before that will fail). */
  public static final int MINIMUM_VERSION = 1;

  @JsonProperty("ddf_version")
  private int ddfVersion;

  @JsonProperty("data_class")
  @Nullable
  private String dataClass;

  @JsonProperty("data")
  @Nullable
  private String data;

  /**
   * Gets the ddf serialized version for this pojo.
   *
   * @return the ddf version for this pojo
   */
  public int getDdfVersion() {
    return ddfVersion;
  }

  /**
   * Sets the ddf serialized version for this pojo.
   *
   * @param version the pojo ddf version
   * @return this for chaining
   */
  public DdfMetadataInfoPojo setDdfVersion(int version) {
    this.ddfVersion = version;
    return this;
  }

  /**
   * Gets the classname for the raw data defining the metadata.
   *
   * @return the classname for the raw data
   */
  @Nullable
  public String getDataClass() {
    return dataClass;
  }

  /**
   * Gets the Java class for the raw data defining the metadata.
   *
   * @return the Java class for the raw data
   * @throws ClassNotFoundException if unable to find a corresponding Java class
   */
  @Nullable
  @JsonIgnore
  public Class<?> getJavaDataClass() throws ClassNotFoundException {
    return Class.forName(dataClass);
  }

  /**
   * Sets the classname for the raw data defining the metadata.
   *
   * @param dataClass the classname for the raw data
   * @return this for chaining
   */
  public DdfMetadataInfoPojo setDataClass(@Nullable String dataClass) {
    this.dataClass = dataClass;
    return this;
  }

  /**
   * Sets the class for the raw data defining the metadata.
   *
   * @param dataClass the class for the raw data
   * @return this for chaining
   */
  public DdfMetadataInfoPojo setDataClass(@Nullable Class<?> dataClass) {
    this.dataClass = (dataClass != null) ? dataClass.getName() : null;
    return this;
  }

  /**
   * Gets the Json serialized data defining the metadata.
   *
   * @return the Json serialized data
   */
  @Nullable
  public String getData() {
    return data;
  }

  /**
   * Gets the raw data as a deserialized Java object.
   *
   * @return the corresponding deserialized object
   * @throws ClassNotFoundException if unable to find a corresponding Java class
   * @throws JsonParseException if underlying data contains invalid content of type {@link
   *     JsonParser} supports (JSON for default case)
   * @throws com.fasterxml.jackson.databind.JsonMappingException if the data JSON structure does not
   *     match structure expected for result type (or has other mismatch issues)
   */
  @Nullable
  @JsonIgnore
  public Object getJavaData() throws ClassNotFoundException, JsonProcessingException {
    return ((dataClass != null) && (data != null))
        ? JsonUtils.read(getJavaDataClass(), data)
        : null;
  }

  /**
   * Sets the Json serialized data defining the metadata.
   *
   * @param data the Json serialized data
   * @return this for chaining
   */
  public DdfMetadataInfoPojo setData(@Nullable String data) {
    this.data = data;
    return this;
  }

  /**
   * Sets the Json serialized data defining the metadata.
   *
   * @param data the Json serialized data
   * @return this for chaining
   * @throws JsonProcessingException if a failure occur while serializing the value
   */
  @JsonIgnore
  public DdfMetadataInfoPojo setData(@Nullable Object data) throws JsonProcessingException {
    this.data = (data != null) ? JsonUtils.write(data) : null;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), ddfVersion, dataClass, data);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof DdfMetadataInfoPojo)) {
      final DdfMetadataInfoPojo pojo = (DdfMetadataInfoPojo) obj;

      return (ddfVersion == pojo.ddfVersion)
          && Objects.equals(dataClass, pojo.dataClass)
          && Objects.equals(data, pojo.data);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "DdfMetadataInfoPojo[id=%s, version=%d, ddfVersion=%d, type=%s, lastModified=%s, size=%d, dataClass=%s, data=%s]",
        getId(),
        getVersion(),
        ddfVersion,
        getType(),
        getLastModified(),
        getSize(),
        dataClass,
        data);
  }
}
