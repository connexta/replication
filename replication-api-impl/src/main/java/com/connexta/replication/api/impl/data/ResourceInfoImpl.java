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
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.UnsupportedVersionException;
import com.connexta.replication.api.impl.persistence.pojo.ResourceInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownPojo;
import com.google.common.annotations.VisibleForTesting;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import javax.annotation.Nullable;

/** Defines a resource to be transferred through replication */
public class ResourceInfoImpl extends AbstractPersistable<ResourceInfoPojo>
    implements ResourceInfo {
  private static final String PERSISTABLE_TYPE = "resource info";

  @Nullable private URI uri;

  private Instant lastModified;

  private long size = -1L;

  private boolean hasUnknowns = false;

  /**
   * Create a resource info.
   *
   * @param resource the resource to be cloned
   */
  public ResourceInfoImpl(ResourceInfo resource) {
    this(
        resource.getUri().orElse(null), resource.getLastModified(), resource.getSize().orElse(-1L));
  }

  /**
   * Create a resource info.
   *
   * @param metadata a piece of metadata from which to create the resource
   */
  public ResourceInfoImpl(Metadata metadata) {
    this(
        metadata.getResourceUri(),
        metadata.getResourceModified().toInstant(),
        metadata.getResourceSize());
  }

  /**
   * Creates a resource info with no uri.
   *
   * @param lastModified the modified date of the resource
   * @param size the size of the resource in bytes
   */
  public ResourceInfoImpl(Instant lastModified, long size) {
    this(null, lastModified, size);
  }

  /**
   * Creates a resource info.
   *
   * @param uri the uri of the resource or <code>null</code> if unknown
   * @param lastModified the modified date of the resource
   * @param size the size of the resource in bytes or <code>-1</code> if unknown
   */
  public ResourceInfoImpl(@Nullable URI uri, Instant lastModified, long size) {
    super(ResourceInfoImpl.PERSISTABLE_TYPE);
    this.uri = uri;
    this.lastModified = lastModified;
    this.size = Math.max(-1L, size);
  }

  /**
   * Instantiates a resource info based on the information provided by the specified pojo.
   *
   * @param pojo the pojo to initializes the resource info with
   */
  public ResourceInfoImpl(ResourceInfoPojo pojo) {
    super(ResourceInfoImpl.PERSISTABLE_TYPE, null);
    readFrom(pojo);
  }

  @Override
  public Optional<URI> getUri() {
    return Optional.ofNullable(uri);
  }

  @Override
  public Instant getLastModified() {
    return lastModified;
  }

  @Override
  public OptionalLong getSize() {
    return (size != -1L) ? OptionalLong.of(size) : OptionalLong.empty();
  }

  /**
   * Checks if this resource information contains unknown information.
   *
   * @return <code>true</code> if it contains unknown info; <code>false</code> otherwise
   */
  public boolean hasUnknowns() {
    return hasUnknowns;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), uri, lastModified, size);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof ResourceInfoImpl)) {
      final ResourceInfoImpl info = (ResourceInfoImpl) obj;

      return (size == info.size)
          && Objects.equals(uri, info.uri)
          && Objects.equals(lastModified, info.lastModified);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "ResourceInfoImpl[id=%s, uri=%s, lastModified=%s, size=%d]",
        getId(), uri, lastModified, size);
  }

  @Override
  protected ResourceInfoPojo writeTo(ResourceInfoPojo pojo) {
    if (hasUnknowns()) { // cannot serialize if it contains unknowns
      throw new InvalidFieldException("unknown resource info");
    }
    super.writeTo(pojo);
    setOrFailIfNull("last modified", this::getLastModified, pojo::setLastModified);
    return pojo.setVersion(ResourceInfoPojo.CURRENT_VERSION)
        .setUri(uri)
        .setSize(Math.max(-1L, size));
  }

  @Override
  protected final void readFrom(ResourceInfoPojo pojo) {
    super.readFrom(pojo);
    if (pojo.getVersion() < ResourceInfoPojo.MINIMUM_VERSION) {
      throw new UnsupportedVersionException(
          "unsupported "
              + ResourceInfoImpl.PERSISTABLE_TYPE
              + " version: "
              + pojo.getVersion()
              + " for object: "
              + getId());
    } // do support pojo.getVersion() > CURRENT_VERSION for forward compatibility
    this.hasUnknowns = pojo instanceof UnknownPojo; // reset the unknown flag
    readFromCurrentOrFutureVersion(pojo);
  }

  @VisibleForTesting
  void setUri(URI uri) {
    this.uri = uri;
  }

  @VisibleForTesting
  void setLastModified(Instant lastModified) {
    this.lastModified = lastModified;
  }

  private void readFromCurrentOrFutureVersion(ResourceInfoPojo pojo) {
    setOrFailIfNull("last modified", pojo::getLastModified, this::setLastModified);
    convertAndSet(pojo::getUri, ResourceInfoImpl::toUri, this::setUri);
    this.size = Math.max(-1L, pojo.getSize());
  }

  private static URI toUri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new InvalidFieldException("invalid uri: " + uri, e);
    }
  }
}
