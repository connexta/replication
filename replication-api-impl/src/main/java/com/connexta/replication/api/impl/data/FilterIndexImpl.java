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

import com.connexta.ion.replication.api.UnsupportedVersionException;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.impl.persistence.pojo.FilterIndexPojo;
import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

/** Simple implementation of FilterIndex. */
public class FilterIndexImpl extends AbstractPersistable<FilterIndexPojo> implements FilterIndex {

  private static final String TYPE = "filter_index";

  @Nullable private Instant modifiedSince;

  @VisibleForTesting
  FilterIndexImpl() {
    super(FilterIndexImpl.TYPE, null);
  }

  /**
   * Creates a new index for a Filter.
   *
   * @param filter to create an index for
   */
  public FilterIndexImpl(Filter filter) {
    super(FilterIndexImpl.TYPE, filter.getId());
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

  @Override
  public Optional<Instant> getModifiedSince() {
    return Optional.ofNullable(modifiedSince);
  }

  @Override
  public void setModifiedSince(Instant modifiedSince) {
    this.modifiedSince = modifiedSince;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), modifiedSince);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof FilterIndexImpl)) {
      final FilterIndexImpl persistable = (FilterIndexImpl) obj;

      return Objects.equals(modifiedSince, persistable.modifiedSince);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("FilterIndexImpl[id=%s, modifiedSince=%s]", getId(), modifiedSince);
  }

  @Override
  public FilterIndexPojo writeTo(FilterIndexPojo pojo) {
    super.writeTo(pojo);
    return pojo.setVersion(FilterIndexPojo.CURRENT_VERSION).setModifiedSince(modifiedSince);
  }

  @Override
  public void readFrom(FilterIndexPojo pojo) {
    super.readFrom(pojo);
    if (pojo.getVersion() < FilterIndexPojo.MINIMUM_VERSION) {
      throw new UnsupportedVersionException(
          "unsupported "
              + FilterIndexImpl.TYPE
              + " version: "
              + pojo.getVersion()
              + " for object: "
              + getId());
    } // do support pojo.getVersion() > CURRENT_VERSION for forward compatibility
    readFromCurrentOrFutureVersion(pojo);
  }

  private void readFromCurrentOrFutureVersion(FilterIndexPojo pojo) {
    setModifiedSince(pojo.getModifiedSince());
  }
}
