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
package org.codice.ditto.replication.api;

import java.util.Date;

/** Represents a resource that replication has replicated or attempted to replicate. */
public interface ReplicationItem {

  /**
   * @return id for which {@link org.codice.ditto.replication.api.data.Metadata} this item
   *     represents
   */
  String getId();

  /** @return this last time the resource associated with this item modified */
  Date getResourceModified();

  /**
   * @return the last time the {@link org.codice.ditto.replication.api.data.Metadata} associated
   *     with this item was modified
   */
  Date getMetadataModified();

  /**
   * @return the name for the source {@link NodeAdapter} the metadata/resource was being replicated
   *     from.
   */
  String getSource();

  /**
   * @return the name for the destination {@link NodeAdapter} the metadata/resource was being
   *     replicated to.
   */
  String getDestination();

  /**
   * @return the id of the {@link org.codice.ditto.replication.api.data.ReplicatorConfig} this item
   *     belongs to
   */
  String getConfigId();

  /**
   * Represents the amount of times an operation between the source and destination {@link
   * NodeAdapter}s failed.
   *
   * @return the failure count
   */
  int getFailureCount();

  /** Increments the failure count by 1. See {@link #getFailureCount()}. */
  void incrementFailureCount();
}
