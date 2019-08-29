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
package com.connexta.replication.query;

import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.ResourceInfo;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import javax.annotation.Nullable;

/** Defines a resource to be transferred through replication */
public class ResourceInfoImpl implements ResourceInfo {

  @Nullable private URI resourceUri;

  private final Instant lastModified;

  private final long size;

  /**
   * Create a new ResourceInfoImpl.
   *
   * @param metadata A piece of metadata from which to create the ResourceInfoImpl
   */
  public ResourceInfoImpl(Metadata metadata) {
    this(
        metadata.getResourceUri(),
        metadata.getResourceModified().toInstant(),
        metadata.getResourceSize());
  }

  /**
   * Create a new ResourceInfoImpl with a null resourceURI.
   *
   * @param lastModified the modified date of the resource
   * @param size the size of the resource in bytes
   */
  public ResourceInfoImpl(Instant lastModified, long size) {
    this(null, lastModified, size);
  }

  /**
   * Create a new ResourceInfoImpl.
   *
   * @param resourceUri the resourceUri of the resource
   * @param lastModified the modified date of the resource
   * @param size the size of the resource in bytes
   */
  public ResourceInfoImpl(@Nullable URI resourceUri, Instant lastModified, long size) {
    this.resourceUri = resourceUri;
    this.lastModified = lastModified;
    this.size = size;
  }

  @Override
  public Optional<URI> getResourceUri() {
    return Optional.ofNullable(resourceUri);
  }

  @Override
  public Instant getLastModified() {
    return lastModified;
  }

  @Override
  public OptionalLong getSize() {
    return OptionalLong.of(size);
  }
}
