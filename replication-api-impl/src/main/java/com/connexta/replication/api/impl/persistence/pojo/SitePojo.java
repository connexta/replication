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

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * This class provides a pojo implementation for a replication site capable of reloading all
 * supported fields for all supported versions from the database. It also provides the capability of
 * persisting back the fields based on the latest version format.
 */
@SolrDocument(collection = SitePojo.COLLECTION)
public class SitePojo extends Pojo<SitePojo> {
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

  public static final String COLLECTION = "replication_site";

  @Indexed(name = "name")
  @Nullable
  private String name;

  @Indexed(name = "description", searchable = false)
  @Nullable
  private String description;

  @Indexed(name = "url")
  @Nullable
  private String url;

  @Indexed(name = "type", searchable = false)
  @Nullable
  private String type;

  @Indexed(name = "kind", searchable = false)
  @Nullable
  private String kind;

  @Indexed(name = "polling_timeout", searchable = false)
  private long pollingTimeout;

  @Indexed(name = "parallelism_factor", searchable = false)
  private int parallelismFactor;

  /** Instantiates a default site pojo set with the current version. */
  public SitePojo() {
    super.setVersion(SitePojo.CURRENT_VERSION);
  }

  /**
   * Gets the human readable name of this site.
   *
   * @return the site name
   */
  @Nullable
  public String getName() {
    return name;
  }

  /**
   * Set the name of this site.
   *
   * @param name the name to give this site
   * @return this for chaining
   */
  public SitePojo setName(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Gets an optional description for this site.
   *
   * @return the site description or <code>null</code> if none defined
   */
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * Gets an optional description for this site.
   *
   * @param description the site description or <code>null</code> if none defined
   * @return this for chaining
   */
  @Nullable
  public SitePojo setDescription(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get the URL of this site.
   *
   * @return the site URL
   */
  @Nullable
  public String getUrl() {
    return url;
  }

  /**
   * Set the URL of this site
   *
   * @param url the URL to give this site
   * @return this for chaining
   */
  public SitePojo setUrl(@Nullable String url) {
    this.url = url;
    return this;
  }

  /**
   * Gets the type of this site.
   *
   * @return the type for this site
   */
  @Nullable
  public String getType() {
    return type;
  }

  /**
   * Sets the type of this site.
   *
   * @param type the type for the site
   * @return this for chaining
   */
  public SitePojo setType(@Nullable String type) {
    this.type = type;
    return this;
  }

  /**
   * Gets the kind of this site.
   *
   * @return the kind for this site
   */
  @Nullable
  public String getKind() {
    return kind;
  }

  /**
   * Sets the kind of this site.
   *
   * @param kind the kind for the site
   * @return this for chaining
   */
  public SitePojo setKind(@Nullable String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * Gets the optional amount of time in milliseconds to wait in between polling attempts whenever
   * polling for intel from this site.
   *
   * @return the maximum amount of time in milliseconds to wait in between polling attempts or
   *     <code>0L</code> if polling is not required or if the local configured default value should
   *     be used
   */
  @Nullable
  public long getPollingTimeout() {
    return pollingTimeout;
  }

  /**
   * Sets the optional amount of time in milliseconds to wait in between polling attempts whenever
   * polling for intel from this site.
   *
   * @param pollingTimeout the maximum amount of time in milliseconds to wait in between polling
   *     attempts or <code>0L</code> if polling is not required or if the local configured default
   *     value should be used
   * @return this for chaining
   */
  public SitePojo setPollingTimeout(long pollingTimeout) {
    this.pollingTimeout = pollingTimeout;
    return this;
  }

  /**
   * Gets the parallelism factor for this site. This corresponds to the maximum number of pieces of
   * intel information that can be transferred from/to this site. For example, a tactical site might
   * want to ensure its bandwidth is not over utilized by forcing the local Ion site to
   * sequentialize all its communications with it by setting this factor to <code>1</code>.
   *
   * <p><i>Note:</i> The local Ion site cannot exceed this value if set. However it can decide to
   * ignore it and use a smaller factor based on its own requirements.
   *
   * @return the parallelism factor for this site or <code>0</code> if it is up to Ion to decide
   */
  public int getParallelismFactor() {
    return parallelismFactor;
  }

  /**
   * Sets the parallelism factor for this site. This corresponds to the maximum number of pieces of
   * intel information that can be transferred from/to this site. For example, a tactical site might
   * want to ensure its bandwidth is not over utilized by forcing the local Ion site to
   * sequentialize all its communications with it by setting this factor to <code>1</code>.
   *
   * <p><i>Note:</i> The local Ion site cannot exceed this value if set. However it can decide to
   * ignore it and use a smaller factor based on its own requirements.
   *
   * @param parallelismFactor the parallelism factor for this site or <code>0</code> if it is up to
   *     Ion to decide
   * @return this for chaining
   */
  public SitePojo setParallelismFactor(int parallelismFactor) {
    this.parallelismFactor = parallelismFactor;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(), name, description, url, type, kind, pollingTimeout, parallelismFactor);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof SitePojo)) {
      final SitePojo pojo = (SitePojo) obj;

      return (pollingTimeout == pojo.pollingTimeout)
          && (parallelismFactor == pojo.parallelismFactor)
          && Objects.equals(name, pojo.name)
          && Objects.equals(description, pojo.description)
          && Objects.equals(url, pojo.url)
          && Objects.equals(type, pojo.type)
          && Objects.equals(kind, pojo.kind);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "SitePojo[id=%s, version=%d, name=%s, url=%s, type=%s, kind=%s, pollingTimeout=%d, parallelismFactor=%d, description=%s]",
        getId(),
        getVersion(),
        name,
        url,
        type,
        kind,
        pollingTimeout,
        parallelismFactor,
        description);
  }
}
