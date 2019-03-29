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
package org.codice.ditto.replication.api.impl.data;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;

public class SyncRequestImpl implements SyncRequest {

  private ReplicatorConfig config;

  private ReplicationStatus status;

  public SyncRequestImpl(ReplicatorConfig config, ReplicationStatus status) {
    this.config = config;
    this.status = status;
  }

  @Override
  public ReplicatorConfig getConfig() {
    return config;
  }

  @Override
  public ReplicationStatus getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SyncRequest) {
      return ((SyncRequest) o).getConfig().getName().equals(config.getName());
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(3, 7).append(this.getConfig().getName()).toHashCode();
  }

  @Override
  public String toString() {
    return String.format("SyncRequestImpl{config=%s, status=%s}", config, status);
  }
}
