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
package com.connexta.ion.replication.api.data;

import java.util.Date;
import javax.annotation.Nullable;

/**
 * A ReplicatorConfig holds information about how replication should be performed such as the sites
 * to replicate between and the filter specifying what to replicate.
 */
public interface ReplicatorConfig extends Persistable {

  /**
   * Get the human readable name for this configuration
   *
   * @return configuration name
   */
  String getName();

  /**
   * Set the human readable name for this configuration
   *
   * @param name the new configuration name
   */
  void setName(String name);

  /**
   * Get the source site id associated with this configuration
   *
   * @return source id
   */
  String getSource();

  /**
   * Set the source site id associated with this configuration
   *
   * @param sourceId the id for the source site
   */
  void setSource(String sourceId);

  /**
   * Get the destination site id associated with this configuration
   *
   * @return destination id
   */
  String getDestination();

  /**
   * Set the destination site id associated with this configuration
   *
   * @param destinationId the id for the destination site
   */
  void setDestination(String destinationId);

  /**
   * Get the filter used for determining the data set to replicate
   *
   * @return replication cql filter
   */
  String getFilter();

  /**
   * Set the filter used for determining the data set to replicate
   *
   * @param filter the new filter to use
   */
  void setFilter(String filter);

  /**
   * Tells whether this replication is configured to be bidirectional or not.
   *
   * @return True if this replication is bidirectional, otherwise, False.
   */
  boolean isBidirectional();

  /**
   * Set whether or not this configuration is bidirectional
   *
   * @param bidirectional boolean indicating whether this configuration is bidirectional or not
   */
  void setBidirectional(boolean bidirectional);

  /**
   * Get a short description for this configuration
   *
   * @return configuration description
   */
  @Nullable
  String getDescription();

  /**
   * Set the description for this configuration
   *
   * @param description the new description
   */
  void setDescription(@Nullable String description);

  /**
   * Returns the amount of times a {@link com.connexta.ion.replication.api.ReplicationItem} will
   * attempt to be retried if it previously failed to replicate.
   *
   * @return failure retry count
   */
  int getFailureRetryCount();

  /**
   * Sets how many attempts will be made to replicate a {@link
   * com.connexta.ion.replication.api.ReplicationItem} that fails to replicate.
   *
   * @param retries the number of any item that fails to replicate should be retried.
   */
  void setFailureRetryCount(int retries);

  /**
   * Gets the suspended state of this config. Suspended configs will not be run.
   *
   * @return boolean indicating if this config is suspended.
   */
  boolean isSuspended();

  /**
   * Sets the suspended state of this config. Suspended configs will not be run.
   *
   * @param suspended the suspended state to give this config
   */
  void setSuspended(boolean suspended);

  /**
   * @return the date at which the configuration was last run, or {@code null} if it hasn't been run
   */
  @Nullable
  Date getLastRun();

  /** @param lastRun the date at which the configuration was last run */
  void setLastRun(@Nullable Date lastRun);

  /**
   * @return the last time the configuration was successfully run, or {@code null} if there has been
   *     no successful run
   */
  @Nullable
  Date getLastSuccess();

  /**
   * See {@link #getLastMetadataModified()}.
   *
   * @param lastMetadataModified the {@link Metadata}'s modified date
   */
  void setLastMetadataModified(@Nullable Date lastMetadataModified);

  /**
   * A {@link Date} which represents the modified date of the last {@link Metadata} that was
   * attempted to be replicated. If available, this should be used by the {@link
   * QueryRequest#getModifiedAfter()}.
   *
   * @return the {@link Date}, or {@code null} if no metadata has attempted to be replicated
   */
  @Nullable
  Date getLastMetadataModified();

  /** @param lastSuccess the last time the configuration was successfully run */
  void setLastSuccess(@Nullable Date lastSuccess);
}
