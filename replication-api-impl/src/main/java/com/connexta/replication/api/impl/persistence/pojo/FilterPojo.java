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

/** A pojo used to load filters from persistence. */
@SolrDocument(collection = FilterPojo.COLLECTION)
public class FilterPojo extends Pojo<FilterPojo> {

  /**
   * List of possible versions:
   *
   * <ul>
   *   <li>1 - initial version.
   * </ul>
   */
  public static final int CURRENT_VERSION = 1;

  /** The oldest version supported by the current code (anything before that will fail). */
  public static final int MINIMUM_VERSION = 1;

  public static final String COLLECTION = "replication_filter";

  @Indexed(name = "site_id")
  @Nullable
  private String siteId;

  @Indexed(name = "filter", searchable = false)
  @Nullable
  private String filter;

  @Indexed(name = "name")
  @Nullable
  private String name;

  @Indexed(name = "description", searchable = false)
  @Nullable
  private String description;

  @Indexed(name = "suspended", searchable = false)
  private boolean suspended;

  @Indexed(name = "priority", searchable = false, type = "pint")
  private byte priority;

  /** Instantiates a default filter pojo set with the current version. */
  public FilterPojo() {
    super.setVersion(CURRENT_VERSION);
  }

  /**
   * Gets the ID of the site associated with this filter.
   *
   * @return the site id
   */
  @Nullable
  public String getSiteId() {
    return siteId;
  }

  /**
   * Sets the site id for this filter
   *
   * @param siteId the id of the site to associate with this filter.
   * @return this for chaining
   */
  public FilterPojo setSiteId(@Nullable String siteId) {
    this.siteId = siteId;
    return this;
  }

  /**
   * Gets the query text for this filter.
   *
   * @return the query text for this filter
   */
  @Nullable
  public String getFilter() {
    return filter;
  }

  /**
   * Sets the query text for this filter.
   *
   * @param filter the query text to give this filter
   * @return this for chaining
   */
  public FilterPojo setFilter(@Nullable String filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Gets the name for this filter.
   *
   * @return the name of this filter
   */
  @Nullable
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this filter.
   *
   * @param name the name to give this filter
   * @return this for chaining
   */
  public FilterPojo setName(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Gets the description of this filter.
   *
   * @return the description of this filter
   */
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of this filter.
   *
   * @param description the description to give this filter
   * @return this for chaining
   */
  public FilterPojo setDescription(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Gets the suspended status of this filter.
   *
   * @return the suspended status of this filter
   */
  public boolean isSuspended() {
    return suspended;
  }

  /**
   * Sets the suspended status of this filter.
   *
   * @param suspended the suspended status to give this filter
   * @return this for chaining
   */
  public FilterPojo setSuspended(boolean suspended) {
    this.suspended = suspended;
    return this;
  }

  /**
   * Gets the priority of this filter.
   *
   * @return the priority of this filter
   */
  public byte getPriority() {
    return priority;
  }

  /**
   * Sets the priority of this filter.
   *
   * @param priority the priority to give this filter
   * @return this for chaining
   */
  public FilterPojo setPriority(byte priority) {
    this.priority = priority;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), siteId, filter, name, description, suspended, priority);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof FilterPojo)) {
      final FilterPojo pojo = (FilterPojo) obj;

      return (suspended == pojo.suspended)
          && (priority == pojo.priority)
          && Objects.equals(siteId, pojo.siteId)
          && Objects.equals(filter, pojo.filter)
          && Objects.equals(name, pojo.name)
          && Objects.equals(description, pojo.description);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "FilterPojo[id=%s, version=%d, siteId=%s, filter=%s, name=%s, description=%s, suspended=%b, priority=%d]",
        getId(), getVersion(), siteId, filter, name, description, suspended, priority);
  }
}
