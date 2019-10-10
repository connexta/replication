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
package com.connexta.replication.api.impl.persistence.pojo;

import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.Task.State;
import java.time.Duration;
import java.time.Instant;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TaskPojoTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final int ATTEMPTS = 5;
  private static final String STATE = State.PENDING.name();
  private static final Instant ORIGINAL_QUEUED_TIME = Instant.ofEpochMilli(10L);
  private static final Instant QUEUED_TIME = Instant.ofEpochMilli(10000L);
  private static final long FROZEN_TIME = 23453L;
  private static final Duration DURATION = Duration.ofMinutes(20L);
  private static final Duration PENDING_DURATION = Duration.ofMinutes(10L);
  private static final Duration ACTIVE_DURATION = Duration.ofMinutes(5L);

  private static final int TASK_INFO_VERSION = 1;
  private static final String TASK_INFO_ID = "1234";
  private static final byte TASK_INFO_PRIORITY = 3;
  private static final String TASK_INFO_INTEL_ID = "intel.id";
  private static final String TASK_INFO_ = OperationType.HARVEST.name();
  private static final Instant TASK_INFO_LAST_MODIFIED = Instant.now().minusSeconds(5L);

  private static final int RESOURCE_VERSION = 2;
  private static final String RESOURCE_ID = "12345";
  private static final Instant RESOURCE_LAST_MODIFIED = Instant.now().minusSeconds(10L);
  private static final long RESOURCE_SIZE = 1234L;
  private static final String RESOURCE_URI = "https://some.uri.com";

  private static final int METADATA_VERSION = 3;
  private static final String METADATA_ID = "123456";
  private static final Instant METADATA_LAST_MODIFIED = Instant.now().minusSeconds(30L);
  private static final long METADATA_SIZE = 12345L;
  private static final String METADATA_TYPE = "ddms";

  private static final ResourceInfoPojo RESOURCE =
      new ResourceInfoPojo()
          .setVersion(TaskPojoTest.RESOURCE_VERSION)
          .setId(TaskPojoTest.RESOURCE_ID)
          .setLastModified(TaskPojoTest.RESOURCE_LAST_MODIFIED)
          .setSize(TaskPojoTest.RESOURCE_SIZE)
          .setUri(TaskPojoTest.RESOURCE_URI);

  private static final MetadataInfoPojo<?> METADATA =
      new MetadataInfoPojo<>()
          .setVersion(TaskPojoTest.METADATA_VERSION)
          .setId(TaskPojoTest.METADATA_ID)
          .setLastModified(TaskPojoTest.METADATA_LAST_MODIFIED)
          .setSize(TaskPojoTest.METADATA_SIZE)
          .setType(TaskPojoTest.METADATA_TYPE);

  private static final TaskInfoPojo TASK_INFO =
      new TaskInfoPojo()
          .setVersion(TaskPojoTest.TASK_INFO_VERSION)
          .setId(TaskPojoTest.TASK_INFO_ID)
          .setPriority(TaskPojoTest.TASK_INFO_PRIORITY)
          .setIntelId(TaskPojoTest.TASK_INFO_INTEL_ID)
          .setOperation(TaskPojoTest.TASK_INFO_)
          .setLastModified(TaskPojoTest.TASK_INFO_LAST_MODIFIED)
          .setResource(TaskPojoTest.RESOURCE)
          .addMetadata(TaskPojoTest.METADATA);

  private static final TaskPojo POJO =
      new TaskPojo()
          .setVersion(TaskPojoTest.VERSION)
          .setId(TaskPojoTest.ID)
          .setTotalAttempts(TaskPojoTest.ATTEMPTS)
          .setState(TaskPojoTest.STATE)
          .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
          .setQueuedTime(TaskPojoTest.QUEUED_TIME)
          .setFrozenTime(TaskPojoTest.FROZEN_TIME)
          .setDuration(TaskPojoTest.DURATION)
          .setPendingDuration(TaskPojoTest.PENDING_DURATION)
          .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
          .setInfo(TaskPojoTest.TASK_INFO);

  @Test
  public void testSetAndGetId() throws Exception {
    final TaskPojo pojo = new TaskPojo().setId(TaskPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(TaskPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final TaskPojo pojo = new TaskPojo().setVersion(TaskPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(TaskPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetTotalAttempts() throws Exception {
    final TaskPojo pojo = new TaskPojo().setTotalAttempts(TaskPojoTest.ATTEMPTS);

    Assert.assertThat(pojo.getTotalAttempts(), Matchers.equalTo(TaskPojoTest.ATTEMPTS));
  }

  @Test
  public void testSetAndGetState() throws Exception {
    final TaskPojo pojo = new TaskPojo().setState(TaskPojoTest.STATE);

    Assert.assertThat(pojo.getState(), Matchers.equalTo(TaskPojoTest.STATE));
  }

  @Test
  public void testSetAndGetStateWithEnum() throws Exception {
    final TaskPojo pojo = new TaskPojo().setState(State.ACTIVE);

    Assert.assertThat(pojo.getState(), Matchers.equalTo(State.ACTIVE.name()));
  }

  @Test
  public void testSetAndGetOriginalQueuedTime() throws Exception {
    final TaskPojo pojo = new TaskPojo().setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME);

    Assert.assertThat(
        pojo.getOriginalQueuedTime(), Matchers.equalTo(TaskPojoTest.ORIGINAL_QUEUED_TIME));
  }

  @Test
  public void testSetAndGetQueuedTime() throws Exception {
    final TaskPojo pojo = new TaskPojo().setQueuedTime(TaskPojoTest.QUEUED_TIME);

    Assert.assertThat(pojo.getQueuedTime(), Matchers.equalTo(TaskPojoTest.QUEUED_TIME));
  }

  @Test
  public void testSetAndGetFrozenTime() throws Exception {
    final TaskPojo pojo = new TaskPojo().setFrozenTime(TaskPojoTest.FROZEN_TIME);

    Assert.assertThat(pojo.getFrozenTime(), Matchers.equalTo(TaskPojoTest.FROZEN_TIME));
  }

  @Test
  public void testSetAndGetDuration() throws Exception {
    final TaskPojo pojo = new TaskPojo().setDuration(TaskPojoTest.DURATION);

    Assert.assertThat(pojo.getDuration(), Matchers.equalTo(TaskPojoTest.DURATION));
  }

  @Test
  public void testSetAndGetPendingDuration() throws Exception {
    final TaskPojo pojo = new TaskPojo().setPendingDuration(TaskPojoTest.PENDING_DURATION);

    Assert.assertThat(pojo.getPendingDuration(), Matchers.equalTo(TaskPojoTest.PENDING_DURATION));
  }

  @Test
  public void testSetAndGetActiveDuration() throws Exception {
    final TaskPojo pojo = new TaskPojo().setActiveDuration(TaskPojoTest.ACTIVE_DURATION);

    Assert.assertThat(pojo.getActiveDuration(), Matchers.equalTo(TaskPojoTest.ACTIVE_DURATION));
  }

  @Test
  public void testSetAndGetInfo() throws Exception {
    final TaskPojo pojo = new TaskPojo().setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(pojo.getInfo(), Matchers.equalTo(TaskPojoTest.TASK_INFO));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID + "2")
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(
        TaskPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(TaskPojoTest.POJO.equals(TaskPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(TaskPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotATaskPojo() throws Exception {
    Assert.assertThat(TaskPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID + "2")
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION + 2)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenTotalAttemptsIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS + 2)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenStateIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE + "2")
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenOriginalQueuedTimeIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME.plusMillis(2L))
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);
    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenQueuedTimeIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME.plusMillis(2L))
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenFrozenTimeTimeIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME + 2L)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDurationIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION.plusMillis(2L))
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenPendingDurationIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION.plusMillis(2L))
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenActiveDurationIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION.plusMillis(2L))
            .setInfo(TaskPojoTest.TASK_INFO);

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenInfoIsDifferent() throws Exception {
    final TaskPojo pojo2 =
        new TaskPojo()
            .setVersion(TaskPojoTest.VERSION)
            .setId(TaskPojoTest.ID)
            .setTotalAttempts(TaskPojoTest.ATTEMPTS)
            .setState(TaskPojoTest.STATE)
            .setOriginalQueuedTime(TaskPojoTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoTest.FROZEN_TIME)
            .setDuration(TaskPojoTest.DURATION)
            .setPendingDuration(TaskPojoTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoTest.ACTIVE_DURATION)
            .setInfo(
                new TaskInfoPojo()
                    .setVersion(TaskPojoTest.TASK_INFO_VERSION)
                    .setId(TaskPojoTest.TASK_INFO_ID + "2")
                    .setPriority(TaskPojoTest.TASK_INFO_PRIORITY)
                    .setIntelId(TaskPojoTest.TASK_INFO_INTEL_ID)
                    .setOperation(TaskPojoTest.TASK_INFO_)
                    .setLastModified(TaskPojoTest.TASK_INFO_LAST_MODIFIED)
                    .setResource(TaskPojoTest.RESOURCE)
                    .addMetadata(TaskPojoTest.METADATA));

    Assert.assertThat(TaskPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
