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

import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownMetadataInfoPojo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * This class provides a pojo implementation for metadata information objects capable of reloading
 * all supported fields for all supported versions from Json using Jackson. It also provides the
 * capability of persisting back the fields based on the latest version format. Versioning is
 * expected to be controlled by the container class.
 *
 * @param <T> the type of metadata info this is
 */
@JsonPropertyOrder({"version", "clazz", "type"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "clazz",
    defaultImpl = UnknownMetadataInfoPojo.class)
@JsonSubTypes({@Type(MetadataInfoPojo.class), @Type(DdfMetadataInfoPojo.class)})
@JsonTypeName("metadata")
public class MetadataInfoPojo<T extends MetadataInfoPojo<?>> extends DataInfoPojo<T> {
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

  @JsonProperty("type")
  @Nullable
  private String type;

  /**
   * Gets the type of metadata to be transferred (e.g. metacard, DDMS 2.0, DDMS 5.0, ...).
   *
   * @return the type of metadata to be transferred
   */
  @Nullable
  public String getType() {
    return type;
  }

  /**
   * Sets the type of metadata to be transferred (e.g. metacard, DDMS 2.0, DDMS 5.0, ...).
   *
   * @param type the type of metadata to be transferred
   * @return this for chaining
   */
  public T setType(@Nullable String type) {
    this.type = type;
    return (T) this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof MetadataInfoPojo)) {
      final MetadataInfoPojo<?> pojo = (MetadataInfoPojo<?>) obj;

      return Objects.equals(type, pojo.type);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "MetadataInfoPojo[id=%s, version=%d, type=%s, lastModified=%s, size=%d]",
        getId(), getVersion(), type, getLastModified(), getSize());
  }
}
