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
import javax.annotation.Nullable;

/**
 * Represents the status of a replication config run including metrics. This can represent a single
 * run instance or a summation of many runs
 */
public interface ReplicationStatus {

  /**
   * Get the status instance ID. Will be globally unique.
   *
   * @return The string ID
   */
  String getId();

  /**
   * Get the name of the associated replication configuration
   *
   * @return The replicator name
   */
  String getReplicatorName();

  /**
   * Get the time this configuration was first run
   *
   * @return
   */
  Date getStartTime();

  void setStartTime(Date startTime);

  /** Sets the start time to now */
  void markStartTime();

  /**
   * Get the Date of the last time the referenced configuration was run
   *
   * @return
   */
  @Nullable
  Date getLastRun();

  void setLastRun(@Nullable Date lastRun);

  /**
   * Get the Date of the last time the referenced configuration was successfully run
   *
   * @return
   */
  @Nullable
  Date getLastSuccess();

  void setLastSuccess(@Nullable Date lastSuccess);

  /**
   * See {@link #getLastMetadataModified()}.
   *
   * @param lastMetadataModified the metadata's modified date
   */
  void setLastMetadataModified(Date lastMetadataModified);

  /**
   * A {@link Date} which represents the modified date of the last metadata that was attempted to be
   * replicated. If available, this should be used when determining what metadata to replicate.
   *
   * @return the {@link Date}
   */
  @Nullable
  Date getLastMetadataModified();

  /**
   * Gets the runtime duration of the referenced configuration in seconds
   *
   * @return
   */
  long getDuration();

  void setDuration(long duration);

  /** Sets the duration based on the start time and the current time */
  void setDuration();

  /**
   * Gets the {@link Status} of the referenced configuration
   *
   * @return
   */
  Status getStatus();

  void setStatus(Status status);

  /**
   * Get the total number of items push (source -> destination)
   *
   * @return
   */
  long getPushCount();

  void setPushCount(long count);

  /**
   * Get the total number of items pulled (destination -> source)
   *
   * @return
   */
  long getPullCount();

  void setPullCount(long count);

  /**
   * Get the number of items that failed to be pushed
   *
   * @return
   */
  long getPushFailCount();

  void setPushFailCount(long count);

  /**
   * Get the number of items that failed to be pulled
   *
   * @return
   */
  long getPullFailCount();

  void setPullFailCount(long count);

  /**
   * Get the number of bytes pushed
   *
   * @return
   */
  long getPushBytes();

  void setPushBytes(long pushBytes);

  /**
   * Get the number of bytes pulled
   *
   * @return
   */
  long getPullBytes();

  void setPullBytes(long pullBytes);

  /** Increment the pull/push count based on the current {@link Status} */
  void incrementCount();

  /** Increment the pull/push failure count based on the current {@link Status} */
  void incrementFailure();

  /** Increment the pull/push bytes based on the current {@link Status} */
  void incrementBytesTransferred(long numBytes);
}
