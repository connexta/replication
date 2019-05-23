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

import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.Status;

public class ReplicationStatusImpl implements ReplicationStatus {

  private final String id;

  private final String replicatorName;

  private volatile Date startTime;

  @Nullable private volatile Date lastSuccess;

  @Nullable private volatile Date lastRun;

  @Nullable private volatile Date lastMetadataModified;

  private volatile long duration = -1;

  private volatile Status status = Status.PENDING;

  private volatile long pushCount = 0;

  private volatile long pullCount = 0;

  private volatile long pushFailCount = 0;

  private volatile long pullFailCount = 0;

  private volatile long pushBytes = 0;

  private volatile long pullBytes = 0;

  public ReplicationStatusImpl(String replicatorName) {
    this.replicatorName = replicatorName;
    this.id = UUID.randomUUID().toString();
  }

  public ReplicationStatusImpl(String id, String replicatorName) {
    this.id = id;
    this.replicatorName = replicatorName;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getReplicatorName() {
    return replicatorName;
  }

  @Override
  public Date getStartTime() {
    return startTime;
  }

  @Override
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  @Override
  public @Nullable Date getLastRun() {
    return lastRun;
  }

  @Override
  public void setLastRun(@Nullable Date lastRun) {
    this.lastRun = lastRun;
  }

  @Override
  public @Nullable Date getLastSuccess() {
    return lastSuccess;
  }

  @Override
  public void setLastSuccess(@Nullable Date lastSuccess) {
    this.lastSuccess = lastSuccess;
  }

  @Override
  public void setLastMetadataModified(Date lastMetadataModified) {
    this.lastMetadataModified = lastMetadataModified;
  }

  @Nullable
  @Override
  public Date getLastMetadataModified() {
    if (lastMetadataModified != null) {
      return lastMetadataModified;
    }

    // Preserve this behavior so that existing configurations will not attempt to re-sync all
    // items. After the first run of an existing config with these changes, last metadata modified
    // will always be used.
    return lastSuccess;
  }

  @Override
  public void markStartTime() {
    this.startTime = new Date();
  }

  /** @return the duration of the replication in seconds */
  @Override
  public long getDuration() {
    if (startTime != null && duration < 0) {
      return (System.currentTimeMillis() - startTime.getTime()) / 1000;
    }
    return duration;
  }

  @Override
  public void setDuration(long duration) {
    this.duration = duration;
  }

  @Override
  public void setDuration() {
    duration = (System.currentTimeMillis() - startTime.getTime()) / 1000;
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public long getPushCount() {
    return pushCount;
  }

  @Override
  public void setPushCount(long count) {
    this.pushCount = count;
  }

  @Override
  public long getPullCount() {
    return pullCount;
  }

  @Override
  public void setPullCount(long count) {
    this.pullCount = count;
  }

  @Override
  public long getPushFailCount() {
    return pushFailCount;
  }

  @Override
  public void setPushFailCount(long count) {
    this.pushFailCount = count;
  }

  @Override
  public long getPullFailCount() {
    return pullFailCount;
  }

  @Override
  public void setPullFailCount(long count) {
    this.pullFailCount = count;
  }

  @Override
  public long getPushBytes() {
    return pushBytes;
  }

  @Override
  public void setPushBytes(long pushBytes) {
    this.pushBytes = pushBytes;
  }

  @Override
  public long getPullBytes() {
    return pullBytes;
  }

  @Override
  public void setPullBytes(long pullBytes) {
    this.pullBytes = pullBytes;
  }

  @Override
  public String toString() {
    return String.format(
        "ReplicationStatus{id='%s', replicatorName='%s', startTime=%s, duration=%d, status=%s, pushCount=%d, pullCount=%d, pushFailCount=%d, pullFailCount=%d, pushBytes=%d, pullBytes=%d, lastMetadataModified=%s}",
        id,
        replicatorName,
        startTime,
        duration,
        status,
        pushCount,
        pullCount,
        pushFailCount,
        pullFailCount,
        pushBytes,
        pullBytes,
        lastMetadataModified);
  }

  @Override
  public void incrementCount() {
    if (status.equals(Status.PULL_IN_PROGRESS)) {
      pullCount++;
    } else {
      pushCount++;
    }
  }

  @Override
  public void incrementFailure() {
    if (status.equals(Status.PULL_IN_PROGRESS)) {
      pullFailCount++;
    } else {
      pushFailCount++;
    }
  }

  @Override
  public void incrementBytesTransferred(long numBytes) {
    if (status.equals(Status.PULL_IN_PROGRESS)) {
      pullBytes += numBytes;
    } else {
      pushBytes += numBytes;
    }
  }
}
