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
package com.connexta.ion.replication.api.impl.data;

import com.connexta.ion.replication.api.Status;
import com.connexta.ion.replication.api.data.ReplicationStatus;
import java.util.Date;
import javax.annotation.Nullable;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(collection = "replication_status")
public class ReplicationStatusImpl extends AbstractPersistable implements ReplicationStatus {

  private static final String STRING = "_txt";

  private static final String DATE = "_tdt";

  private static final String LONG = "_lng";

  @Indexed(name = "replicator-id" + STRING)
  private String replicatorId;

  @Indexed(name = "start-time" + DATE)
  private Date startTime;

  @Indexed(name = "last-success" + DATE)
  @Nullable
  private Date lastSuccess;

  @Indexed(name = "last-run" + DATE)
  @Nullable
  private Date lastRun;

  @Indexed(name = "duration" + LONG)
  private long duration = -1;

  @Indexed(name = "status" + STRING, type = "string")
  private Status status = Status.PENDING;

  @Indexed(name = "push-count" + LONG)
  private long pushCount = 0;

  @Indexed(name = "pull-count" + LONG)
  private long pullCount = 0;

  @Indexed(name = "push-fail-count" + LONG)
  private long pushFailCount = 0;

  @Indexed(name = "pull-fail-count" + LONG)
  private long pullFailCount = 0;

  @Indexed(name = "push-bytes" + LONG)
  private long pushBytes = 0;

  @Indexed(name = "pull-bytes" + LONG)
  private long pullBytes = 0;

  @Indexed(name = "last-metadata-modified" + DATE)
  @Nullable
  private Date lastMetadataModified;

  /** 1 - initial version. */
  private static final int CURRENT_VERSION = 1;

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
    if (lastMetadataModified != null) {
      return lastMetadataModified;
    }

    // Preserve this behavior so that existing configurations will not attempt to re-sync all
    // items. After the first run of an existing config with these changes, last metadata modified
    // will always be used.
    return lastSuccess;
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
        "ReplicationStatusImpl{id='%s', replicatorId='%s', startTime=%s, duration=%d, status=%s, pushCount=%d, pullCount=%d, pushFailCount=%d, pullFailCount=%d, pushBytes=%d, pullBytes=%d, lastMetadataModified=%s, lastSuccess=%s, lastRun=%s, version=%s}",
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
        pullBytes,
        lastMetadataModified,
        lastSuccess,
        lastRun,
        getVersion());
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
