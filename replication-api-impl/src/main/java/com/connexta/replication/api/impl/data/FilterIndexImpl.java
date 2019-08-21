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

import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.impl.persistence.pojo.FilterIndexPojo;
import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;

/** Simple implementation of FilterIndex. */
public class FilterIndexImpl extends AbstractPersistable<FilterIndexPojo> implements FilterIndex {

  private static final String TYPE = "filter_index";

  private Instant modifiedSince;

  private String filterId;

  /** Creates a new FilterIndex with the filter index type. */
  public FilterIndexImpl() {
    super(FilterIndexImpl.TYPE);
  }

  /**
   * Creates a new FilterIndex.
   *
   * @param pojo to create the filter index from
   */
  protected FilterIndexImpl(FilterIndexPojo pojo) {
    super(FilterIndexImpl.TYPE, null);
    readFrom(pojo);
  }

  @VisibleForTesting
  FilterIndexImpl(Instant modifiedSince, String filterId) {
    super(FilterIndexImpl.TYPE);
    this.modifiedSince = modifiedSince;
    this.filterId = filterId;
  }

  @Override
  public Instant getModifiedSince() {
    return modifiedSince;
  }

  @Override
  public String getFilterId() {
    return filterId;
  }

  @Override
  public String toString() {
    return String.format(
        "FilterIndexImpl[id=%s, modifiedSince=%s, filterId=%s]", getId(), modifiedSince, filterId);
  }

  @Override
  public FilterIndexPojo writeTo(FilterIndexPojo pojo) {
    super.writeTo(pojo);
    setOrFailIfNullOrEmpty("filterId", this::getFilterId, pojo::setFilterId);
    return pojo.setVersion(FilterIndexPojo.CURRENT_VERSION).setModifiedSince(modifiedSince);
  }

  @Override
  public void readFrom(FilterIndexPojo pojo) {
    super.readFrom(pojo);
    if (pojo.getVersion() < FilterIndexPojo.CURRENT_VERSION) {
      throw new ReplicationPersistenceException(
          "unsupported "
              + FilterIndexImpl.TYPE
              + " version: "
              + pojo.getVersion()
              + " for object: "
              + getId());
    } // do support pojo.getVersion() > CURRENT_VERSION for forward compatibility
    setOrFailIfNullOrEmpty("filterId", pojo::getFilterId, this::setFilterId);
    setModifiedSince(pojo.getModifiedSince());
  }

  private void setModifiedSince(Instant modifiedSince) {
    this.modifiedSince = modifiedSince;
  }

  private void setFilterId(String filterId) {
    this.filterId = filterId;
  }
}
