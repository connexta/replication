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

import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownResourceInfoPojo;
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
import java.net.URI;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * This class provides a pojo implementation for resource information objects capable of reloading
 * all supported fields for all supported versions from Json using Jackson. It also provides the
 * capability of persisting back the fields based on the latest version format. Versioning is
 * expected to be controlled by the container class.
 */
@JsonPropertyOrder({"version", "clazz", "uri"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "clazz",
    defaultImpl = UnknownResourceInfoPojo.class)
@JsonSubTypes(@Type(ResourceInfoPojo.class))
@JsonTypeName("resource")
public class ResourceInfoPojo extends DataInfoPojo<ResourceInfoPojo> {
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

  @JsonProperty("uri")
  @Nullable
  private String uri;

  /**
   * Gets the URI for the resource.
   *
   * @return the URI for the resource or <code>null</code> if there is none or if it is not known
   */
  @Nullable
  public String getUri() {
    return uri;
  }

  /**
   * Sets the URI for the resource.
   *
   * @param uri the URI for the resource or <code>null</code> if there is none or if it is not known
   * @return this for chaining
   */
  public ResourceInfoPojo setUri(@Nullable String uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Sets the URI for the resource.
   *
   * @param uri the URI for the resource or <code>null</code> if there is none or if it is not known
   * @return this for chaining
   */
  public ResourceInfoPojo setUri(@Nullable URI uri) {
    this.uri = (uri != null) ? uri.toString() : null;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), uri);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof ResourceInfoPojo)) {
      final ResourceInfoPojo pojo = (ResourceInfoPojo) obj;

      return Objects.equals(uri, pojo.uri);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "ResourceInfoPojo[id=%s, version=%d, uri=%s, lastModified=%s, size=%d]",
        getId(), getVersion(), uri, getLastModified(), getSize());
  }
}
