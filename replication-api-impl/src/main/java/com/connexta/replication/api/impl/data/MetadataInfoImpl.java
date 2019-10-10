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
import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.UnsupportedVersionException;
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownPojo;
import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.Objects;
import java.util.OptionalLong;
import javax.annotation.Nullable;

/**
 * Provides information about metadata collected, which is useful when processing a {@link Task}.
 */
public class MetadataInfoImpl extends AbstractPersistable<MetadataInfoPojo<?>>
    implements MetadataInfo {
  private static final String PERSISTABLE_TYPE = "metadata info";

  protected String type;

  protected Instant lastModified;

  protected long size = -1L;

  protected boolean hasUnknowns = false;

  /**
   * Creates a new metadata.
   *
   * @param metadata the metadata to be cloned
   */
  public MetadataInfoImpl(MetadataInfo metadata) {
    this(MetadataInfoImpl.PERSISTABLE_TYPE, metadata);
  }

  /**
   * Creates a new metadata by copying the necessary information from the given metadata.
   *
   * @param type a string describing the type of the metadata (e.g. DDMS 2.0, DDMS 5.0, ...)
   * @param metadata the {@link Metadata} to create a metadata from
   */
  public MetadataInfoImpl(String type, Metadata metadata) {
    this(MetadataInfoImpl.PERSISTABLE_TYPE, type, metadata);
  }

  /**
   * Creates a new metadata with the provided information.
   *
   * @param type a string describing the type of the metadata (e.g. DDMS 2.0, DDMS 5.0, ...)
   * @param lastModified the metadata modified time of the DDF metadata
   * @param size the size of the metadata in bytes or <code>-1L</code> if not known
   */
  public MetadataInfoImpl(String type, Instant lastModified, long size) {
    this(MetadataInfoImpl.PERSISTABLE_TYPE, type, lastModified, size);
  }

  /**
   * Instantiates a metadata info based on the information provided by the specified pojo.
   *
   * @param pojo the pojo to initializes the metadata info with
   */
  public MetadataInfoImpl(MetadataInfoPojo<?> pojo) {
    super(MetadataInfoImpl.PERSISTABLE_TYPE, null);
    readFrom(pojo);
  }

  /**
   * Creates a new metadata by copying the necessary information from the given metadata.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param type a string describing the type of the metadata (e.g. DDMS 2.0, DDMS 5.0, ...)
   * @param metadata the {@link Metadata} to create a metadata from
   */
  protected MetadataInfoImpl(String persistableType, String type, Metadata metadata) {
    this(
        persistableType,
        type,
        metadata.getMetadataModified().toInstant(),
        metadata.getMetadataSize());
  }

  /**
   * Creates a new metadata.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param metadata the metadata to be cloned
   */
  protected MetadataInfoImpl(String persistableType, MetadataInfo metadata) {
    this(
        persistableType,
        metadata.getType(),
        metadata.getLastModified(),
        metadata.getSize().orElse(-1L));
  }

  /**
   * Creates a new metadata with the provided information.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param type a string describing the type of the metadata (e.g. DDMS 2.0, DDMS 5.0, ...)
   * @param lastModified the metadata modified time of the DDF metadata
   * @param size the size of the metadata in bytes or <code>-1L</code> if not known
   */
  protected MetadataInfoImpl(String persistableType, String type, Instant lastModified, long size) {
    super(persistableType);
    this.type = type;
    this.lastModified = lastModified;
    this.size = Math.max(-1L, size);
  }

  /**
   * Creates a new metadata.
   *
   * @param persistableType a string representing the type of this object (used when generating
   *     exception or logs)
   * @param id the previously generated identifier for this object or <code>null</code> if it is
   *     expected to be restored later when {@link #readFrom} is called by the subclass
   */
  protected MetadataInfoImpl(String persistableType, @Nullable String id) {
    super(persistableType, id);
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
    return (size != -1L) ? OptionalLong.of(size) : OptionalLong.empty();
  }

  /**
   * Checks if this metadata information contains unknown information.
   *
   * @return <code>true</code> if it contains unknown info; <code>false</code> otherwise
   */
  public boolean hasUnknowns() {
    return hasUnknowns;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type, lastModified, size);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof MetadataInfoImpl)) {
      final MetadataInfoImpl info = (MetadataInfoImpl) obj;

      return (size == info.size)
          && Objects.equals(type, info.type)
          && Objects.equals(lastModified, info.lastModified);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "MetadataInfoImpl[id=%s, type=%s, lastModified=%s, size=%d]",
        getId(), type, lastModified, size);
  }

  @Override
  protected MetadataInfoPojo<?> writeTo(MetadataInfoPojo<?> pojo) {
    if (hasUnknowns()) { // cannot serialize if it contains unknowns
      throw new InvalidFieldException("unknown metadata info");
    }
    super.writeTo(pojo);
    setOrFailIfNullOrEmpty("type", this::getType, pojo::setType);
    setOrFailIfNull("last modified", this::getLastModified, pojo::setLastModified);
    return pojo.setVersion(MetadataInfoPojo.CURRENT_VERSION).setSize(Math.max(-1L, size));
  }

  @Override
  protected final void readFrom(MetadataInfoPojo<?> pojo) {
    super.readFrom(pojo);
    if (pojo.getVersion() < MetadataInfoPojo.MINIMUM_VERSION) {
      throw new UnsupportedVersionException(
          "unsupported "
              + MetadataInfoImpl.PERSISTABLE_TYPE
              + " version: "
              + pojo.getVersion()
              + " for object: "
              + getId());
    } // do support pojo.getVersion() > CURRENT_VERSION for forward compatibility
    this.hasUnknowns = pojo instanceof UnknownPojo; // reset the unknown flag
    readFromCurrentOrFutureVersion(pojo);
  }

  @VisibleForTesting
  void setType(String type) {
    this.type = type;
  }

  @VisibleForTesting
  void setLastModified(Instant lastModified) {
    this.lastModified = lastModified;
  }

  @VisibleForTesting
  void setSize(long size) {
    this.size = size;
  }

  private void readFromCurrentOrFutureVersion(MetadataInfoPojo<?> pojo) {
    setOrFailIfNullOrEmpty("type", pojo::getType, this::setType);
    setOrFailIfNull("last modified", pojo::getLastModified, this::setLastModified);
    this.size = Math.max(-1L, pojo.getSize());
  }
}
