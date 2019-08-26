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

import com.connexta.replication.api.SyncRequest;
import com.connexta.replication.api.data.Filter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/** Simple SyncRequest implementation. */
public class SyncRequestImpl implements SyncRequest {

  private Filter filter;

  /**
   * Creates a new SyncRequest.
   *
   * @param filter the {@link Filter} which will be used for replicating
   */
  public SyncRequestImpl(Filter filter) {
    this.filter = filter;
  }

  @Override
  public Filter getFilter() {
    return filter;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (o instanceof SyncRequest) {
      return ((SyncRequest) o).getFilter().getName().equals(filter.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(3, 7).append(this.getFilter().getName()).toHashCode();
  }

  @Override
  public String toString() {
    return String.format("SyncRequestImpl{filter=%s}", filter);
  }
}
