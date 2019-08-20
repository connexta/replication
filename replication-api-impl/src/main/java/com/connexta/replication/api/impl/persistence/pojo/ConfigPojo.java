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

import com.connexta.ion.replication.api.data.QueryRequest;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * This class provides a pojo implementation for a replicator configuration capable of reloading all
 * supported fields for all supported versions from the database. It also provides the capability of
 * persisting back the fields based on the latest version format.
 */
@SolrDocument(collection = ConfigPojo.COLLECTION)
public class ConfigPojo extends Pojo<ConfigPojo> {
  /**
   * List of possible versions:
   *
   * <ul>
   *   <li>1 - initial version.
   * </ul>
   */
  public static final int CURRENT_VERSION = 1;

  public static final String COLLECTION = "replication_config";

  @Indexed(name = "name", searchable = false)
  @Nullable
  private String name;

  @Indexed(name = "bidirectional", searchable = false)
  private boolean bidirectional;

  @Indexed(name = "source", searchable = false)
  @Nullable
  private String source;

  @Indexed(name = "destination", searchable = false)
  @Nullable
  private String destination;

  @Indexed(name = "filter", searchable = false)
  @Nullable
  private String filter;

  @Indexed(name = "description", searchable = false)
  @Nullable
  private String description;

  @Indexed(name = "suspended", searchable = false)
  private boolean suspended;

  @Indexed(name = "last_metadata_modified", searchable = false)
  @Nullable
  private Instant lastMetadataModified;

  /** Instantiates a default replicator config pojo set with the current version. */
  public ConfigPojo() {
    super.setVersion(ConfigPojo.CURRENT_VERSION);
  }

  /**
   * Gets the human readable name for this configuration.
   *
   * @return the configuration name
   */
  @Nullable
  public String getName() {
    return name;
  }

  /**
   * Sets the human readable name for this configuration.
   *
   * @param name the new configuration name
   * @return this for chaining
   */
  public ConfigPojo setName(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Gets the source site id associated with this configuration.
   *
   * @return the source id
   */
  @Nullable
  public String getSource() {
    return source;
  }

  /**
   * Sets the source site id associated with this configuration.
   *
   * @param source the id for the source site
   * @return this for chaining
   */
  public ConfigPojo setSource(@Nullable String source) {
    this.source = source;
    return this;
  }

  /**
   * Gets the destination site id associated with this configuration.
   *
   * @return the destination id
   */
  @Nullable
  public String getDestination() {
    return destination;
  }

  /**
   * Sets the destination site id associated with this configuration.
   *
   * @param destination the id for the destination site
   * @return this for chaining
   */
  public ConfigPojo setDestination(@Nullable String destination) {
    this.destination = destination;
    return this;
  }

  /**
   * Gets the filter used for determining the data set to replicate.
   *
   * @return the replication cql filter
   */
  @Nullable
  public String getFilter() {
    return filter;
  }

  /**
   * Sets the filter used for determining the data set to replicate.
   *
   * @param filter the new filter to use
   * @return this for chaining
   */
  public ConfigPojo setFilter(@Nullable String filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Tells whether this replication is configured to be bidirectional or not.
   *
   * @return <code>true</code> if this replication is bidirectional, otherwise, <code>false</code>.
   */
  public boolean isBidirectional() {
    return bidirectional;
  }

  /**
   * Sets whether or not this configuration is bidirectional.
   *
   * @param bidirectional flag indicating whether this configuration is bidirectional or not
   * @return this for chaining
   */
  public ConfigPojo setBidirectional(boolean bidirectional) {
    this.bidirectional = bidirectional;
    return this;
  }

  /**
   * Gets a short description for this configuration.
   *
   * @return the configuration description
   */
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description for this configuration.
   *
   * @param description the new description
   * @return this for chaining
   */
  public ConfigPojo setDescription(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Gets the suspended state of this config. Suspended configs will not be run.
   *
   * @return flag indicating if this config is suspended
   */
  public boolean isSuspended() {
    return suspended;
  }

  /**
   * Sets the suspended state of this config. Suspended configs will not be run.
   *
   * @param suspended the suspended state to give this config
   * @return this for chaining
   */
  public ConfigPojo setSuspended(boolean suspended) {
    this.suspended = suspended;
    return this;
  }

  /**
   * Gets a {@link Instant} which represents the modified instant of the last metadata that was
   * attempted to be replicated. If available, this should be used by the {@link
   * QueryRequest#getModifiedAfter()}.
   *
   * @return the {@link Instant}, or {@code null} if no metadata has attempted to be replicated
   */
  @Nullable
  public Instant getLastMetadataModified() {
    return lastMetadataModified;
  }

  /**
   * See {@link #getLastMetadataModified()}.
   *
   * @param lastMetadataModified the metadata's modified date
   * @return this for chaining
   */
  public ConfigPojo setLastMetadataModified(@Nullable Instant lastMetadataModified) {
    this.lastMetadataModified = lastMetadataModified;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        name,
        source,
        destination,
        filter,
        bidirectional,
        description,
        suspended,
        lastMetadataModified);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof ConfigPojo)) {
      final ConfigPojo pojo = (ConfigPojo) obj;

      return (bidirectional == pojo.bidirectional)
          && (suspended == pojo.suspended)
          && Objects.equals(name, pojo.name)
          && Objects.equals(source, pojo.source)
          && Objects.equals(destination, pojo.destination)
          && Objects.equals(filter, pojo.filter)
          && Objects.equals(description, pojo.description)
          && Objects.equals(lastMetadataModified, pojo.lastMetadataModified);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "ConfigPojo[id=%s, version=%d, name=%s, source=%s, destination=%s, filter=%s, bidirectional=%s, description=%s, suspended=%s, lastMetadataModified=%s]",
        getId(),
        getVersion(),
        name,
        source,
        destination,
        filter,
        bidirectional,
        description,
        suspended,
        lastMetadataModified);
  }
}
