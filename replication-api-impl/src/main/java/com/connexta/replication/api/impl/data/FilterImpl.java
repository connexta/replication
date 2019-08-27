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

import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.UnsupportedVersionException;
import com.connexta.replication.api.impl.persistence.pojo.FilterPojo;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * A filter describing what data to replicate to a site. A site can have multiple filters associated
 * with it to describe what data it would like to receive.
 */
public class FilterImpl extends AbstractPersistable<FilterPojo> implements Filter {
  private static final String PERSISTABLE_TYPE = "replication filter";

  private String siteId;

  private String filter;

  private String name;

  @Nullable private String description;

  private boolean isSuspended;

  private byte priority = 0;

  /** Creates a default filter. */
  public FilterImpl() {
    super(FilterImpl.PERSISTABLE_TYPE);
  }

  /**
   * Creates a filter from the given {@link FilterPojo}.
   *
   * @param pojo a pojo containing the values this filter should be instantiated with.
   */
  protected FilterImpl(FilterPojo pojo) {
    super(FilterImpl.PERSISTABLE_TYPE);
    readFrom(pojo);
  }

  @Override
  public String getSiteId() {
    return siteId;
  }

  @Override
  public String getFilter() {
    return filter;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  @Override
  public boolean isSuspended() {
    return isSuspended;
  }

  @Override
  public byte getPriority() {
    return priority;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), siteId, filter, name, description, isSuspended, priority);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof FilterImpl)) {
      final FilterImpl pojo = (FilterImpl) obj;

      return (isSuspended == pojo.isSuspended)
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
        "FilterImpl[id=%s, siteId=%s, filter=%s, name=%s, description=%s, suspended=%b, priority=%d]",
        getId(), siteId, filter, name, description, isSuspended, priority);
  }

  @VisibleForTesting
  void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  @VisibleForTesting
  void setFilter(String filter) {
    this.filter = filter;
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setDescription(String description) {
    this.description = description;
  }

  @VisibleForTesting
  void setSuspended(boolean suspended) {
    this.isSuspended = suspended;
  }

  @VisibleForTesting
  void setPriority(byte priority) {
    this.priority = priority;
  }

  @Override
  protected FilterPojo writeTo(FilterPojo pojo) {
    super.writeTo(pojo);
    setOrFailIfNullOrEmpty("siteId", this::getSiteId, pojo::setSiteId);
    setOrFailIfNullOrEmpty("filter", this::getFilter, pojo::setFilter);
    setOrFailIfNullOrEmpty("name", this::getName, pojo::setName);
    return pojo.setVersion(FilterPojo.CURRENT_VERSION)
        .setDescription(description)
        .setSuspended(isSuspended)
        .setPriority((byte) Math.min(Math.max(priority, 0), 9));
  }

  @Override
  protected void readFrom(FilterPojo pojo) {
    super.readFrom(pojo);
    if (pojo.getVersion() < FilterPojo.MINIMUM_VERSION) {
      throw new UnsupportedVersionException(
          "unsupported "
              + FilterImpl.PERSISTABLE_TYPE
              + " version: "
              + pojo.getVersion()
              + " for object: "
              + getId());
    } // do support pojo.getVersion() > CURRENT_VERSION for forward compatibility
    readFromCurrentOrFutureVersion(pojo);
  }

  private void readFromCurrentOrFutureVersion(FilterPojo pojo) {
    setOrFailIfNullOrEmpty("siteId", pojo::getSiteId, this::setSiteId);
    setOrFailIfNullOrEmpty("filter", pojo::getFilter, this::setFilter);
    setOrFailIfNullOrEmpty("name", pojo::getName, this::setName);
    this.description = pojo.getDescription();
    this.isSuspended = pojo.isSuspended();
    this.priority = (byte) Math.min(Math.max(pojo.getPriority(), 0), 9);
  }
}
