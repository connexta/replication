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
import com.connexta.replication.api.data.ReplicatorConfig;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/** Simple SyncRequest implementation. */
public class SyncRequestImpl implements SyncRequest {

  private ReplicatorConfig config;

  /**
   * Creates a new SyncRequest.
   *
   * @param config the {@link ReplicatorConfig} which will be used for replicating
   */
  public SyncRequestImpl(ReplicatorConfig config) {
    this.config = config;
  }

  @Override
  public ReplicatorConfig getConfig() {
    return config;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (o instanceof SyncRequest) {
      return ((SyncRequest) o).getConfig().getName().equals(config.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(3, 7).append(this.getConfig().getName()).toHashCode();
  }

  @Override
  public String toString() {
    return String.format("SyncRequestImpl{config=%s}", config);
  }
}
