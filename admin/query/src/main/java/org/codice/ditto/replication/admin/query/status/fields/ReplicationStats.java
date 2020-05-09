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
package org.codice.ditto.replication.admin.query.status.fields;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.common.fields.base.BaseObjectField;
import org.codice.ddf.admin.common.fields.base.scalar.LongField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.replications.fields.Iso8601Field;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationStatus;

public class ReplicationStats extends BaseObjectField {

  private static final String FIELD_NAME = "stats";

  private static final String FIELD_TYPE_NAME = "ReplicationStats";

  private static final String DESCRIPTION = "Contains various statistics of a replication's run.";

  private PidField pid;

  private Iso8601Field startTime;

  private Iso8601Field lastRun;

  private Iso8601Field lastSuccess;

  private LongField duration;

  private ReplicationStatus status;

  private LongField pushCount;

  private LongField pullCount;

  private LongField pushFailCount;

  private LongField pullFailCount;

  private LongField pushBytes;

  private LongField pullBytes;

  public ReplicationStats() {
    super(FIELD_NAME, FIELD_TYPE_NAME, DESCRIPTION);
    pid = new PidField();
    startTime = new Iso8601Field("startTime");
    lastRun = new Iso8601Field("lastRun");
    lastSuccess = new Iso8601Field("lastSuccess");
    duration = new LongField("duration");
    status = new ReplicationStatus();
    pushCount = new LongField("pushCount");
    pullCount = new LongField("pullCount");
    pushFailCount = new LongField("pushFailCount");
    pullFailCount = new LongField("pullFailCount");
    pushBytes = new LongField("pushBytes");
    pullBytes = new LongField("pullBytes");

    pid.isRequired(true);
    startTime.isRequired(true);
    lastRun.isRequired(true);
    duration.isRequired(true);
    status.isRequired(true);
    pushCount.isRequired(true);
    pullCount.isRequired(true);
    pushFailCount.isRequired(true);
    pullFailCount.isRequired(true);
    pushBytes.isRequired(true);
    pullBytes.isRequired(true);
  }

  @Override
  public List<Field> getFields() {
    return ImmutableList.of(
        pid,
        startTime,
        lastRun,
        lastSuccess,
        duration,
        status,
        pushCount,
        pullCount,
        pushFailCount,
        pullFailCount,
        pushBytes,
        pullBytes);
  }

  public void setPid(String pid) {
    this.pid.setValue(pid);
  }

  public String getPid() {
    return this.pid.getValue();
  }

  public void setStartTime(Date date) {
    this.startTime.setValue(date.toInstant());
  }

  public String getStartTime() {
    return this.startTime.getValue();
  }

  public void setLastRun(Date date) {
    this.lastRun.setValue(date.toInstant());
  }

  public String getLastRun() {
    return this.lastRun.getValue();
  }

  public void setLastSuccess(@Nullable Date date) {
    if (date != null) {
      this.lastSuccess.setValue(date.toInstant());
    }
  }

  public String getLastSuccess() {
    return this.lastSuccess.getValue();
  }

  public void setDuration(long duration) {
    this.duration.setValue(duration);
  }

  public long getDuration() {
    if (this.duration != null) {
      return this.duration.getValue();
    }
    return 0L;
  }

  public void setStatus(String replicationStatus) {
    this.status.setValue(replicationStatus);
  }

  public String getStatus() {
    return this.status.getValue();
  }

  public void setPushCount(long pushCount) {
    this.pushCount.setValue(pushCount);
  }

  public long getPushCount() {
    return this.pushCount.getValue();
  }

  public void setPullCount(long pullCount) {
    this.pullCount.setValue(pullCount);
  }

  public long getPullCount() {
    return this.pullCount.getValue();
  }

  public void setPushFailCount(long pushFailCount) {
    this.pushFailCount.setValue(pushFailCount);
  }

  public long getPushFailCount() {
    return this.pushFailCount.getValue();
  }

  public void setPullFailCount(long pullFailCount) {
    this.pullFailCount.setValue(pullFailCount);
  }

  public long getPullFailCount() {
    return this.pullFailCount.getValue();
  }

  public void setPushBytes(long pushBytes) {
    this.pushBytes.setValue(pushBytes);
  }

  public long getPushBytes() {
    return this.pushBytes.getValue();
  }

  public void setPullBytes(long pullBytes) {
    this.pullBytes.setValue(pullBytes);
  }

  public long getPullBytes() {
    return this.pullBytes.getValue();
  }
}
