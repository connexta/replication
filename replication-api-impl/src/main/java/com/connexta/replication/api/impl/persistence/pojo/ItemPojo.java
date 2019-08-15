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

import com.connexta.ion.replication.api.Action;
import com.connexta.ion.replication.api.Status;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * This class provides a pojo implementation for a replication item capable of reloading all
 * supported fields for all supported versions from the database. It also provides the capability of
 * persisting back the fields based on the latest version format.
 */
@SolrDocument(collection = ItemPojo.COLLECTION)
public class ItemPojo extends Pojo<ItemPojo> {
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

  public static final String COLLECTION = "replication_item";

  @Indexed(name = "metadata_id", type = "string")
  private String metadataId;

  @Indexed(name = "resource_modified", searchable = false)
  private Date resourceModified;

  @Indexed(name = "metadata_modified", searchable = false)
  private Date metadataModified;

  @Indexed(name = "done_time", searchable = false)
  private Date doneTime;

  @Indexed(name = "source", searchable = false)
  private String source;

  @Indexed(name = "destination", searchable = false)
  private String destination;

  @Indexed(name = "config_id")
  private String configId;

  @Indexed(name = "metadata_size", searchable = false)
  private long metadataSize;

  @Indexed(name = "resource_size", searchable = false)
  private long resourceSize;

  @Indexed(name = "start_time", searchable = false)
  private Date startTime;

  @Indexed(name = "status", type = "string")
  private String status;

  @Indexed(name = "action", type = "string", searchable = false)
  private String action;

  /** This default ctor is needed for spring-solr to instantiate an item when querying solr */
  public ItemPojo() {
    super.setVersion(ItemPojo.CURRENT_VERSION);
  }

  /**
   * Gets the identifier of the metadata retrieved from a remote system.
   *
   * @return the metadata id
   */
  @Nullable
  public String getMetadataId() {
    return metadataId;
  }

  /**
   * Sets the identifier of the metadata retrieved from a remote system.
   *
   * @param metadataId the metadata id
   * @return this for chaining
   */
  public ItemPojo setMetadataId(@Nullable String metadataId) {
    this.metadataId = metadataId;
    return this;
  }

  /**
   * Gets the last time the resource associated with this item was modified.
   *
   * @return the last time the resource associated with this item was modified, or {@code null} if
   *     there is no resource associated with this metadata
   */
  @Nullable
  public Date getResourceModified() {
    return resourceModified;
  }

  /**
   * Sets the last time the resource associated with this item was modified.
   *
   * @param resourceModified the last time the resource associated with this item was modified, or
   *     {@code null} if there is no resource associated with this metadata
   * @return this for chaining
   */
  public ItemPojo setResourceModified(@Nullable Date resourceModified) {
    this.resourceModified = resourceModified;
    return this;
  }

  /**
   * Gets the last time the metadata associated with this item was modified.
   *
   * @return the last time the metadata associated with this item was modified
   */
  @Nullable
  public Date getMetadataModified() {
    return metadataModified;
  }

  /**
   * Sets the last time the metadata associated with this item was modified.
   *
   * @param metadataModified the last time the metadata associated with this item was modified
   * @return this for chaining
   */
  public ItemPojo setMetadataModified(@Nullable Date metadataModified) {
    this.metadataModified = metadataModified;
    return this;
  }

  /**
   * Gets the size in bytes of this item's resource, or <code>0</code> if no resource is associated
   * with the metadata.
   *
   * @return the size in bytes of this item's resource, or <code>0</code> if there is no resource
   *     associated with the metadata
   */
  public long getResourceSize() {
    return resourceSize;
  }

  /**
   * Sets the size in bytes of this item's resource, or <code>0</code> if no resource is associated
   * with the metadata.
   *
   * @param resourceSize the size in bytes of this item's resource, or <code>0</code> if there is no
   *     resource associated with the metadata
   * @return this for chaining
   */
  public ItemPojo setResourceSize(long resourceSize) {
    this.resourceSize = resourceSize;
    return this;
  }

  /**
   * Gets the size in bytes of this item's metadata.
   *
   * @return the size in bytes of this item's metadata, cannot be negative
   */
  public long getMetadataSize() {
    return metadataSize;
  }

  /**
   * Sets the size in bytes of this item's metadata.
   *
   * @param metadataSize the size in bytes of this item's metadata, cannot be negative
   * @return this for chaining
   */
  public ItemPojo setMetadataSize(long metadataSize) {
    this.metadataSize = metadataSize;
    return this;
  }

  /**
   * Gets the time at which this item began processing.
   *
   * @return the time at which this item began processing
   */
  @Nullable
  public Date getStartTime() {
    return startTime;
  }

  /**
   * Sets the time at which this item began processing.
   *
   * @param startTime the time at which this item began processing
   * @return this for chaining
   */
  public ItemPojo setStartTime(@Nullable Date startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * Gets the time at which this item finished processing.
   *
   * @return the time at which this item finished processing
   */
  @Nullable
  public Date getDoneTime() {
    return doneTime;
  }

  /**
   * Gets the time at which this item finished processing.
   *
   * @param doneTime the time at which this item finished processing
   * @return this for chaining
   */
  public ItemPojo setDoneTime(@Nullable Date doneTime) {
    this.doneTime = doneTime;
    return this;
  }

  /**
   * Gets the status of this replication item (see {@link Status}).
   *
   * @return the status of this replication item
   */
  @Nullable
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status of this replication item (see {@link Status}).
   *
   * @param status the status of this replication item
   * @return this for chaining
   */
  public ItemPojo setStatus(@Nullable String status) {
    this.status = status;
    return this;
  }

  /**
   * Gets the action performed on the metadata/resource (see {@link Action}).
   *
   * @return the action performed on the metadata/resource
   */
  @Nullable
  public String getAction() {
    return action;
  }

  /**
   * Sets the action performed on the metadata/resource (see {@link Action}).
   *
   * @param action the action performed on the metadata/resource
   * @return this for chaining
   */
  public ItemPojo setAction(@Nullable String action) {
    this.action = action;
    return this;
  }

  /**
   * Gets the name for the source {@link com.connexta.ion.replication.api.NodeAdapter} the
   * metadata/resource was being replicated from.
   *
   * @return the name for the source {@link {@link com.connexta.ion.replication.api.NodeAdapter}}
   *     the metadata/resource was being replicated from
   */
  @Nullable
  public String getSource() {
    return source;
  }

  /**
   * Sets the name for the source {@link com.connexta.ion.replication.api.NodeAdapter} the
   * metadata/resource was being replicated from.
   *
   * @param source the name for the source {@link {@link
   *     com.connexta.ion.replication.api.NodeAdapter}} the metadata/resource was being replicated
   *     from
   * @return this for chaining
   */
  public ItemPojo setSource(@Nullable String source) {
    this.source = source;
    return this;
  }

  /**
   * Gets the name for the destination {@link com.connexta.ion.replication.api.NodeAdapter} the
   * metadata/resource was being replicated to.
   *
   * @return the name for the destination {@link NodeAdapter} the metadata/resource was being
   *     replicated to
   */
  @Nullable
  public String getDestination() {
    return destination;
  }

  /**
   * Sets the name for the destination {@link com.connexta.ion.replication.api.NodeAdapter} the
   * metadata/resource was being replicated to.
   *
   * @param destination the name for the destination {@link NodeAdapter} the metadata/resource was
   *     being replicated to
   * @return this for chaining
   */
  public ItemPojo setDestination(@Nullable String destination) {
    this.destination = destination;
    return this;
  }

  /**
   * Gets the identifier of the {@link ConfigPojo} this item belongs to.
   *
   * @return the id of the {@link ConfigPojo} this item belongs to
   */
  @Nullable
  public String getConfigId() {
    return configId;
  }

  /**
   * Sets the identifier of the {@link ConfigPojo} this item belongs to.
   *
   * @param configId the id of the {@link ConfigPojo} this item belongs to
   * @return this for chaining
   */
  public ItemPojo setConfigId(@Nullable String configId) {
    this.configId = configId;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        metadataId,
        resourceModified,
        metadataModified,
        doneTime,
        source,
        destination,
        configId,
        metadataSize,
        resourceSize,
        startTime,
        status,
        action);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof ItemPojo)) {
      final ItemPojo pojo = (ItemPojo) obj;

      return (metadataSize == pojo.metadataSize)
          && (resourceSize == pojo.resourceSize)
          && Objects.equals(metadataId, pojo.metadataId)
          && Objects.equals(resourceModified, pojo.resourceModified)
          && Objects.equals(metadataModified, pojo.metadataModified)
          && Objects.equals(doneTime, pojo.doneTime)
          && Objects.equals(source, pojo.source)
          && Objects.equals(destination, pojo.destination)
          && Objects.equals(configId, pojo.configId)
          && Objects.equals(startTime, pojo.startTime)
          && Objects.equals(status, pojo.status)
          && Objects.equals(action, pojo.action);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "ItemPojo[id=%s, version=%d, metadataId=%s, resourceModified=%s, metadataModified=%s, doneTime=%s, source=%s, destination=%s, configId=%s, metadataSize=%d, resourceSize=%d, startTime=%s, status=%s, action=%s]",
        getId(),
        getVersion(),
        metadataId,
        resourceModified,
        metadataModified,
        doneTime,
        source,
        destination,
        configId,
        metadataSize,
        resourceSize,
        startTime,
        status,
        action);
  }
}
