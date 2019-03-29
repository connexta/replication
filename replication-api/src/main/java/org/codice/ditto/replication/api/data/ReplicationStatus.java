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
package org.codice.ditto.replication.api.data;

import java.util.Date;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.Status;

/**
 * Represents the status of a replication config run including metrics. This can represent a single
 * run instance or a summation of many runs
 */
public interface ReplicationStatus extends Persistable {

  /**
   * Gets the status instance ID. Will be globally unique.
   *
   * @return The string ID
   */
  String getId();

  /**
   * Gets the unique identifier of the replication configuration for which this status holds data.
   *
   * @return The replicator ID
   */
  String getReplicatorId();

  /**
   * Sets the unique identifier of the replication configuration for which this status holds data.
   *
   * @param replicatorId the ID of the replication configuration
   */
  void setReplicatorId(String replicatorId);

  /**
   * Gets the time this configuration was first run
   *
   * @return the start time
   */
  Date getStartTime();

  /**
   * Sets the time this configuration was first run
   *
   * @param startTime the start time
   */
  void setStartTime(Date startTime);

  /** Sets the start time to now */
  void markStartTime();

  /**
   * Gets the Date of the last time the referenced configuration was run
   *
   * @return the date at which the referenced configuration was last run
   */
  @Nullable
  Date getLastRun();

  /**
   * Sets the Date of the last time the referenced configuration was run
   *
   * @param lastRun the date at which the refernced configuration was last run
   */
  void setLastRun(@Nullable Date lastRun);

  /**
   * Gets the Date of the last time the referenced configuration was successfully run
   *
   * @return the last time the referenced configuration was successfully run
   */
  @Nullable
  Date getLastSuccess();

  /**
   * Sets the Date of the last time the referenced configuration was successfully run
   *
   * @param lastSuccess the last time the referenced configuration was successfully run
   */
  void setLastSuccess(@Nullable Date lastSuccess);

  /**
   * Gets the runtime duration of the referenced configuration in seconds
   *
   * @return the runtime duration
   */
  long getDuration();

  /**
   * Sets the runtime duration of the referenced configuration in seconds
   *
   * @param duration the runtime duration
   */
  void setDuration(long duration);

  /** Sets the duration based on the start time and the current time */
  void setDuration();

  /**
   * Gets the {@link Status} of the referenced configuration
   *
   * @return the status of the configuration
   */
  Status getStatus();

  /**
   * Sets the {@link Status} of the referenced configuration
   *
   * @param status the status of the configuration
   */
  void setStatus(Status status);

  /**
   * Gets the total number of items pushed (source -> destination)
   *
   * @return the number of items pushed
   */
  long getPushCount();

  /**
   * Sets the total number of items pushed (source -> destination)
   *
   * @param count the number of items pushed
   */
  void setPushCount(long count);

  /**
   * Gets the total number of items pulled (destination -> source)
   *
   * @return the number of items pulled
   */
  long getPullCount();

  /**
   * Sets the total number of items pulled (destination -> source)
   *
   * @param count the number of items pulled
   */
  void setPullCount(long count);

  /**
   * Gets the number of items that failed to be pushed
   *
   * @return the number of items that failed to be pushed
   */
  long getPushFailCount();

  /**
   * Sets the number of items that failed to be pushed
   *
   * @param count the number of items that failed to be pushed
   */
  void setPushFailCount(long count);

  /**
   * Gets the number of items that failed to be pulled
   *
   * @return the number of items that failed to be pulled
   */
  long getPullFailCount();

  /**
   * Sets the number of items that failed to be pulled
   *
   * @param count the number of items that failed to be pulled
   */
  void setPullFailCount(long count);

  /**
   * Gets the number of bytes pushed
   *
   * @return the number of bytes pushed
   */
  long getPushBytes();

  /**
   * Sets the number of bytes pushed
   *
   * @param pushBytes the number of bytes pushed
   */
  void setPushBytes(long pushBytes);

  /**
   * Gets the number of bytes pulled
   *
   * @return the number of bytes pulled
   */
  long getPullBytes();

  /**
   * Sets the number of bytes pulled
   *
   * @param pullBytes the number of bytes pulled
   */
  void setPullBytes(long pullBytes);

  /** Increments the pull/push count based on the current {@link Status} */
  void incrementCount();

  /** Increments the pull/push failure count based on the current {@link Status} */
  void incrementFailure();

  /** Increments the pull/push bytes based on the current {@link Status} */
  void incrementBytesTransferred(long numBytes);

  /**
   * Consolidates the info contained in the given status with the current status info.
   *
   * @param newStatus The new status to be merged with the current status.
   */
  void addStats(ReplicationStatus newStatus);
}
