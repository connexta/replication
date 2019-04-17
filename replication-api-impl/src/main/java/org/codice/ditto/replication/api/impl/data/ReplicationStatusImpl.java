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
import java.util.Map;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.data.ReplicationStatus;

public class ReplicationStatusImpl extends AbstractPersistable implements ReplicationStatus {

  public static final String PERSISTENCE_TYPE = "replication_status";

  public static final String REPLICATOR_ID = "replicator-id";

  public static final String START_TIME = "start-time";

  public static final String LAST_SUCCESS = "last-success";

  public static final String LAST_RUN = "last-run";

  public static final String DURATION = "duration";

  public static final String STATUS = "status";

  public static final String PUSH_COUNT = "push-count";

  public static final String PULL_COUNT = "pull-count";

  public static final String PUSH_FAIL_COUNT = "push-fail-count";

  public static final String PULL_FAIL_COUNT = "pull-fail-count";

  public static final String PUSH_BYTES = "push-bytes";

  public static final String PULL_BYTES = "pull-bytes";

  public static final String LAST_METADATA_MODIFIED = "last-metadata-modified";

  private String replicatorId;

  private Date startTime;

  @Nullable private Date lastSuccess;

  @Nullable private Date lastRun;

  private long duration = -1;

  private Status status = Status.PENDING;

  private long pushCount = 0;

  private long pullCount = 0;

  private long pushFailCount = 0;

  private long pullFailCount = 0;

  private long pushBytes = 0;

  private long pullBytes = 0;

  @Nullable private Date lastMetadataModified;

  /** 1 - initial version. */
  public static final int CURRENT_VERSION = 1;

  public ReplicationStatusImpl() {
    super.setVersion(CURRENT_VERSION);
  }

  @Override
  public String getReplicatorId() {
    return replicatorId;
  }

  @Override
  public void setReplicatorId(String replicatorId) {
    this.replicatorId = replicatorId;
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
  public void setLastMetadataModified(Date lastMetadataModified) {
    this.lastMetadataModified = lastMetadataModified;
  }

  @Nullable
  @Override
  public Date getLastMetadataModified() {
    return lastMetadataModified;
  }

  @Override
  public void setLastSuccess(@Nullable Date lastSuccess) {
    this.lastSuccess = lastSuccess;
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
        "ReplicationStatus{id='%s', replicatorId='%s', startTime=%s, duration=%d, status=%s, pushCount=%d, pullCount=%d, pushFailCount=%d, pullFailCount=%d, pushBytes=%d, pullBytes=%d}",
        getId(),
        replicatorId,
        startTime,
        duration,
        status,
        pushCount,
        pullCount,
        pushFailCount,
        pullFailCount,
        pushBytes,
        pullBytes);
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

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> result = super.toMap();
    result.put(REPLICATOR_ID, getReplicatorId());
    result.put(START_TIME, getStartTime());
    result.put(LAST_SUCCESS, getLastSuccess());
    result.put(LAST_RUN, getLastRun());
    result.put(DURATION, getDuration());
    result.put(STATUS, getStatus());
    result.put(PUSH_COUNT, getPushCount());
    result.put(PULL_COUNT, getPullCount());
    result.put(PUSH_FAIL_COUNT, getPushFailCount());
    result.put(PULL_FAIL_COUNT, getPullFailCount());
    result.put(PUSH_BYTES, getPushBytes());
    result.put(PULL_BYTES, getPullBytes());
    result.put(LAST_METADATA_MODIFIED, getLastMetadataModified());
    return result;
  }

  @Override
  public void fromMap(Map<String, Object> properties) {
    super.fromMap(properties);
    setReplicatorId((String) properties.get(REPLICATOR_ID));
    setStartTime((Date) properties.get(START_TIME));
    setLastSuccess((Date) properties.get(LAST_SUCCESS));
    setLastRun((Date) properties.get(LAST_RUN));
    setDuration((Long) properties.get(DURATION));
    setStatus(Status.valueOf((String) properties.get(STATUS)));
    setPushCount((Long) properties.get(PUSH_COUNT));
    setPullCount((Long) properties.get(PULL_COUNT));
    setPushFailCount((Long) properties.get(PUSH_FAIL_COUNT));
    setPullFailCount((Long) properties.get(PULL_FAIL_COUNT));
    setPushBytes((Long) properties.get(PUSH_BYTES));
    setPullBytes((Long) properties.get(PULL_BYTES));
    setLastMetadataModified((Date) properties.get(LAST_METADATA_MODIFIED));
  }

  @Override
  public void addStats(ReplicationStatus newStatus) {
    setPushCount(getPushCount() + newStatus.getPushCount());
    setPushBytes(getPushBytes() + newStatus.getPushBytes());
    setPushFailCount(getPushFailCount() + newStatus.getPushFailCount());
    setPullCount(getPullCount() + newStatus.getPullCount());
    setPullBytes(getPullBytes() + newStatus.getPullBytes());
    setPullFailCount(getPullFailCount() + newStatus.getPullFailCount());
    if (getLastRun() == null || newStatus.getStartTime().after(getLastRun())) {
      setLastRun(newStatus.getStartTime());
      setStatus(newStatus.getStatus());
    }

    if (newStatus.getLastMetadataModified() != null) {
      setLastMetadataModified(newStatus.getLastMetadataModified());
    }

    if (getStartTime().after(newStatus.getStartTime())) {
      setStartTime(newStatus.getStartTime());
    }

    setDuration(getDuration() + newStatus.getDuration());

    if (newStatus.getStatus().equals(Status.SUCCESS)
        && (getLastSuccess() == null || newStatus.getStartTime().after(getLastSuccess()))) {
      setLastSuccess(newStatus.getStartTime());
    }
  }
}
