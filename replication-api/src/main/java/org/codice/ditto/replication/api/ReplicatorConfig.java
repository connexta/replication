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

import javax.annotation.Nullable;

public interface ReplicatorConfig {

  /**
   * Get the unique id for this configuration
   *
   * @return configuration id
   */
  String getId();

  /**
   * Get the human readable name for this configuration
   *
   * @return configuration name
   */
  String getName();

  /**
   * Get the direction of this replication (push/pull/both)
   *
   * @return The replication direction
   */
  Direction getDirection();

  /**
   * Get the type of replication to be performed metacard or resource. Resource replication includes
   * metacard replication.
   *
   * @return The replication type
   */
  ReplicationType getReplicationType();

  /**
   * Get the source site id associated with this configuration
   *
   * @return source id
   */
  String getSource();

  /**
   * Get the destination site id associated with this configuration
   *
   * @return destination id
   */
  String getDestination();

  /**
   * Get the cql filter used for determining the data set to replicate
   *
   * @return replication cql filter
   */
  String getCql();

  /**
   * Get a short description for this configuration
   *
   * @return configuration description
   */
  @Nullable
  String getDescription();

  /**
   * Returns the amount of times a {@link ReplicationItem} will attempt to be retried if it
   * previously failed to replicate.
   *
   * @return failure retry count
   */
  int getFailureRetryCount();

  /**
   * Gets the suspended state of this config. Suspended configs will not be run.
   *
   * @return boolean indicating if this config is suspended.
   */
  boolean isSuspended();

  /**
   * Get the version of this configuration
   *
   * @return integer version of the configuration
   */
  int getVersion();
}
