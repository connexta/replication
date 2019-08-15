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
@SolrDocument(collection = "replication_site")
public class SitePojo extends Pojo<SitePojo> {
  /**
   * List of possible versions:
   *
   * <ul>
   *   <li>1 - initial version.
   * </ul>
   */
  public static final int CURRENT_VERSION = 2;

  @Indexed(name = "remote_managed")
  private boolean isRemoteManaged = false;

  @Indexed(name = "name")
  @Nullable
  private String name;

  @Indexed(name = "url")
  @Nullable
  private String url;

  @Indexed(name = "type", searchable = false)
  @Nullable
  private String type;

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
   * When {@code false}, the local process is responsible for running replicator configs associated
   * with this site.
   *
   * <p>When {@code true}, the local process is no longer responsible for performing replication for
   * any replicator configs associated with this site. This effectively disables running replication
   * locally.
   *
   * @return {@code true} if replication should not run locally, otherwise {@code false}.
   */
  public boolean isRemoteManaged() {
    return isRemoteManaged;
  }

  /**
   * See {@link #isRemoteManaged()}.
   *
   * @param remoteManaged whether or not the local process is responsible for running replications
   *     this site is associated with.
   * @return this for chaining
   */
  public SitePojo setRemoteManaged(boolean remoteManaged) {
    this.isRemoteManaged = remoteManaged;
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

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isRemoteManaged, name, url, type);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof SitePojo)) {
      final SitePojo pojo = (SitePojo) obj;

      return (isRemoteManaged == pojo.isRemoteManaged)
          && Objects.equals(name, pojo.name)
          && Objects.equals(url, pojo.url)
          && Objects.equals(type, pojo.type);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "SitePojo[id=%s, version=%d, name=%s, url=%s, type=%s, remoteManaged=%s]",
        getId(), getVersion(), name, url, type, isRemoteManaged);
  }
}
