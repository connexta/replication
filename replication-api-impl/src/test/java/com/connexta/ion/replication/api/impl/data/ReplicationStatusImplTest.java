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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.connexta.ion.replication.api.Status;
import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;
import java.util.Date;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

public class ReplicationStatusImplTest {

  private ReplicationStatusImpl status;

  @Before
  public void setup() {
    status = loadStatus(new ReplicationStatusImpl(), 1);
  }

  @Test
  public void getters() {
    assertThat(status.getReplicatorId(), is("id1"));
    assertThat(status.getStartTime(), is(new Date(1)));
    assertThat(status.getLastSuccess(), is(new Date(1)));
    assertThat(status.getLastRun(), is(new Date(1)));
    assertThat(status.getDuration(), is(1L));
    assertThat(status.getStatus(), is(Status.values()[1]));
    assertThat(status.getPushCount(), is(1L));
    assertThat(status.getPullCount(), is(1L));
    assertThat(status.getPushFailCount(), is(1L));
    assertThat(status.getPullFailCount(), is(1L));
    assertThat(status.getPushBytes(), is(1L));
    assertThat(status.getPullBytes(), is(1L));
  }

  @Test
  public void testToString() {
    ToStringVerifier.forClass(ReplicationStatusImpl.class)
        .withClassName(NameStyle.SIMPLE_NAME)
        .verify();
  }

  @Test
  public void incrementCountPush() {
    status.setStatus(Status.PUSH_IN_PROGRESS);
    status.incrementCount();
    assertThat(status.getPushCount(), is(2L));
    assertThat(status.getPullCount(), is(1L));
  }

  @Test
  public void incrementCountPull() {
    status.setStatus(Status.PULL_IN_PROGRESS);
    status.incrementCount();
    assertThat(status.getPullCount(), is(2L));
    assertThat(status.getPushCount(), is(1L));
  }

  @Test
  public void incrementFailurePush() {
    status.setStatus(Status.PUSH_IN_PROGRESS);
    status.incrementFailure();
    assertThat(status.getPushFailCount(), is(2L));
    assertThat(status.getPullFailCount(), is(1L));
  }

  @Test
  public void incrementFailurePull() {
    status.setStatus(Status.PULL_IN_PROGRESS);
    status.incrementFailure();
    assertThat(status.getPullFailCount(), is(2L));
    assertThat(status.getPushFailCount(), is(1L));
  }

  @Test
  public void incrementBytesTransferredPush() {
    status.setStatus(Status.PUSH_IN_PROGRESS);
    status.incrementBytesTransferred(1);
    assertThat(status.getPushBytes(), is(2L));
    assertThat(status.getPullBytes(), is(1L));
  }

  @Test
  public void incrementBytesTransferredPull() {
    status.setStatus(Status.PULL_IN_PROGRESS);
    status.incrementBytesTransferred(1);
    assertThat(status.getPullBytes(), is(2L));
    assertThat(status.getPushBytes(), is(1L));
  }

  @Test
  public void addStatus() {
    ReplicationStatusImpl base = loadStatus(new ReplicationStatusImpl(), 1);
    status = loadStatus(new ReplicationStatusImpl(), 2);
    status.setStatus(Status.SUCCESS);
    base.addStats(status);
    assertThat(base.getStartTime(), is(new Date(1)));
    assertThat(base.getLastSuccess(), is(new Date(2)));
    assertThat(base.getLastRun(), is(status.getStartTime()));
    assertThat(base.getDuration(), is(3L));
    assertThat(base.getStatus(), is(status.getStatus()));
    assertThat(base.getPushCount(), is(3L));
    assertThat(base.getPullCount(), is(3L));
    assertThat(base.getPushFailCount(), is(3L));
    assertThat(base.getPullFailCount(), is(3L));
    assertThat(base.getPushBytes(), is(3L));
    assertThat(base.getPullBytes(), is(3L));
  }

  @Test
  public void addStatusNullLastRun() {
    ReplicationStatusImpl base = loadStatus(new ReplicationStatusImpl(), 1);
    base.setLastRun(null);
    base.setLastSuccess(null);
    status = loadStatus(new ReplicationStatusImpl(), 2);
    status.setStatus(Status.SUCCESS);
    base.addStats(status);
    assertThat(base.getLastRun(), is(status.getStartTime()));
    assertThat(base.getStatus(), is(status.getStatus()));
    assertThat(base.getStartTime(), is(new Date(1)));
    assertThat(base.getLastSuccess(), is(new Date(2)));
  }

  @Test
  public void addStatusThatOccurredBeforeBaseStatus() {
    ReplicationStatusImpl base = loadStatus(new ReplicationStatusImpl(), 2);
    base.addStats(status);
    assertThat(base.getLastRun(), is(new Date(2)));
    assertThat(base.getStatus(), is(Status.values()[2]));
    assertThat(base.getStartTime(), is(new Date(1)));
    assertThat(base.getLastSuccess(), is(new Date(2)));
  }

  @Test
  public void addSuccessfulStatusThatOccurredBeforeBaseStatus() {
    ReplicationStatusImpl base = loadStatus(new ReplicationStatusImpl(), 2);
    status.setStatus(Status.SUCCESS);
    base.addStats(status);
    assertThat(base.getLastRun(), is(new Date(2)));
    assertThat(base.getStatus(), is(Status.values()[2]));
    assertThat(base.getStartTime(), is(new Date(1)));
    assertThat(base.getLastSuccess(), is(new Date(2)));
  }

  @Test
  public void testNoLastMetadataModifiedReturnsLastSuccess() {
    final Date lastSuccess = new Date(5);
    status.setLastSuccess(lastSuccess);
    assertThat(status.getLastMetadataModified(), is(lastSuccess));
  }

  @Test
  public void testLastMetadataModifiedPresent() {
    Random r = new Random();
    final Date lastSuccess = new Date(r.nextInt());
    final Date lastMetadataModified = new Date(r.nextInt());
    status.setLastSuccess(lastSuccess);
    status.setLastMetadataModified(lastMetadataModified);

    assertThat(status.getLastMetadataModified(), is(lastMetadataModified));
  }

  private ReplicationStatusImpl loadStatus(ReplicationStatusImpl status, int num) {
    status.setReplicatorId("id" + num);
    status.setStartTime(new Date(num));
    status.setLastSuccess(new Date(num));
    status.setLastRun(new Date(num));
    status.setDuration(num);
    status.setStatus(Status.values()[num]);
    status.setPushCount(num);
    status.setPullCount(num);
    status.setPushFailCount(num);
    status.setPullFailCount(num);
    status.setPushBytes(num);
    status.setPullBytes(num);
    return status;
  }
}
