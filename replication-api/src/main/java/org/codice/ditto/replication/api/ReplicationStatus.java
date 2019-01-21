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
import java.util.UUID;

public class ReplicationStatus {

  private final String id;

  private final String replicatorName;

  private Date startTime;

  private long duration = -1;

  private Status status = Status.PENDING;

  private long pushCount = 0;

  private long pullCount = 0;

  private long pushFailCount = 0;

  private long pullFailCount = 0;

  private long pushBytes = 0;

  private long pullBytes = 0;

  public ReplicationStatus(String replicatorName) {
    this.replicatorName = replicatorName;
    this.id = UUID.randomUUID().toString();
  }

  public ReplicationStatus(String id, String replicatorName) {
    this.id = id;
    this.replicatorName = replicatorName;
  }

  public String getId() {
    return id;
  }

  public String getReplicatorName() {
    return replicatorName;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public void markStartTime() {
    this.startTime = new Date();
  }

  /** @return the duration of the replication in seconds */
  public long getDuration() {
    if (startTime != null && duration < 0) {
      return (System.currentTimeMillis() - startTime.getTime()) / 1000;
    }
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public void setDuration() {
    duration = (System.currentTimeMillis() - startTime.getTime()) / 1000;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public long getPushCount() {
    return pushCount;
  }

  public void setPushCount(long count) {
    this.pushCount = count;
  }

  public long getPullCount() {
    return pullCount;
  }

  public void setPullCount(long count) {
    this.pullCount = count;
  }

  public long getPushFailCount() {
    return pushFailCount;
  }

  public void setPushFailCount(long count) {
    this.pushFailCount = count;
  }

  public long getPullFailCount() {
    return pullFailCount;
  }

  public void setPullFailCount(long count) {
    this.pullFailCount = count;
  }

  public long getPushBytes() {
    return pushBytes;
  }

  public void setPushBytes(long pushBytes) {
    this.pushBytes = pushBytes;
  }

  public long getPullBytes() {
    return pullBytes;
  }

  public void setPullBytes(long pullBytes) {
    this.pullBytes = pullBytes;
  }

  @Override
  public String toString() {
    return String.format(
        "ReplicationStatus{id='%s', replicatorName='%s', startTime=%s, duration=%d, status=%s, pushCount=%d, pullCount=%d, pushFailCount=%d, pullFailCount=%d, pushBytes=%d, pullBytes=%d}",
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
        pullBytes);
  }
}
