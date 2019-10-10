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
package com.connexta.replication.api.impl.data;

import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.Task.State;
import com.connexta.replication.api.data.TaskInfo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractTaskImplTest {
  private static final String ID = "id";
  private static final byte PRIORITY = 3;
  private static final String INTEL_ID = "intel.id";
  private static final OperationType OPERATION = OperationType.DELETE;
  private static final int ATTEMPTS = 3;
  private static final State STATE = State.ACTIVE;
  private static final Instant ORIGINAL_QUEUED_TIME = Instant.now().minusSeconds(120L);
  private static final Instant QUEUED_TIME = Instant.now().minusSeconds(450L);
  private static final long START_TIME = 23345L;
  private static final Instant LAST_MODIFIED = Instant.MIN;
  private static final ResourceInfo RESOURCE = Mockito.mock(ResourceInfo.class);
  private static final MetadataInfo METADATA = Mockito.mock(MetadataInfo.class);

  private static final Duration DURATION2 = Duration.ofSeconds(5L);
  private static final Duration DURATION3 = Duration.ofSeconds(8L);
  private static final Duration DURATION4 = Duration.ofSeconds(17L);
  private static final Duration DURATION5 = Duration.ofSeconds(24L);
  private static final long NANOS = System.nanoTime();
  private static final long NANOS2 =
      AbstractTaskImplTest.NANOS + AbstractTaskImplTest.DURATION2.toNanos();
  private static final long NANOS3 =
      AbstractTaskImplTest.NANOS2 + AbstractTaskImplTest.DURATION3.toNanos();
  private static final long NANOS4 =
      AbstractTaskImplTest.NANOS3 + AbstractTaskImplTest.DURATION4.toNanos();
  private static final long NANOS5 =
      AbstractTaskImplTest.NANOS4 + AbstractTaskImplTest.DURATION5.toNanos();
  // using System.currentTimeMillis() ensures the same resolution when testing
  // as how the implementation currently creates Instant objects from a Clock
  private static final Instant NOW = Instant.ofEpochMilli(System.currentTimeMillis());
  private static final Instant THEN2 = AbstractTaskImplTest.NOW.plusMillis(NANOS2);
  private static final Instant THEN3 = AbstractTaskImplTest.NOW.plusMillis(NANOS3);
  private static final Instant THEN4 = AbstractTaskImplTest.NOW.plusMillis(NANOS4);
  private static final Instant THEN5 = AbstractTaskImplTest.NOW.plusMillis(NANOS5);

  private final TaskInfo info = Mockito.mock(TaskInfo.class);

  private AbstractTaskImpl task;

  @Before
  public void setup() throws Exception {
    Mockito.when(info.getPriority()).thenReturn(AbstractTaskImplTest.PRIORITY);
    task =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));
    task.setId(AbstractTaskImplTest.ID);
    task.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task.setState(AbstractTaskImplTest.STATE);
    task.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task.setStartTime(AbstractTaskImplTest.START_TIME);
    task.setDuration(AbstractTaskImplTest.DURATION2);
    task.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task.setActiveDuration(AbstractTaskImplTest.DURATION4);
  }

  @Test
  public void testConstructorWithTaskInfo() throws Exception {
    final AbstractTaskImpl task =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(
                AbstractTaskImplTest.NOW, // creation
                AbstractTaskImplTest.NANOS, // creation
                AbstractTaskImplTest.NANOS2, // when getDuration() is called
                AbstractTaskImplTest.NANOS2, // when getPendingDuration() is called
                AbstractTaskImplTest.NANOS2)); // when getActiveDuration() is called

    Assert.assertThat(task.getPriority(), Matchers.equalTo(AbstractTaskImplTest.PRIORITY));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(AbstractTaskImplTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(AbstractTaskImplTest.NOW));
    Assert.assertThat(task.getDuration(), Matchers.equalTo(AbstractTaskImplTest.DURATION2));
    Assert.assertThat(task.getPendingDuration(), Matchers.equalTo(AbstractTaskImplTest.DURATION2));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(Duration.ZERO));
    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
  }

  @Test
  public void testConstructorWhenPriorityIsMoreThanMax() throws Exception {
    Mockito.when(info.getPriority()).thenReturn((byte) (TaskInfoImpl.MAX_PRIORITY + 1));

    final AbstractTaskImpl task =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    Assert.assertThat(
        task.getPriority(), Matchers.equalTo(Byte.valueOf(TaskInfoImpl.MAX_PRIORITY)));
  }

  @Test
  public void testConstructorWhenPriorityIsLessThanMin() throws Exception {
    Mockito.when(info.getPriority()).thenReturn((byte) (TaskInfoImpl.MIN_PRIORITY - 1));

    final AbstractTaskImpl task =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    Assert.assertThat(
        task.getPriority(), Matchers.equalTo(Byte.valueOf(TaskInfoImpl.MIN_PRIORITY)));
  }

  @Test
  public void testGetIntelId() throws Exception {
    Mockito.when(info.getIntelId()).thenReturn(AbstractTaskImplTest.INTEL_ID);

    Assert.assertThat(task.getIntelId(), Matchers.equalTo(AbstractTaskImplTest.INTEL_ID));

    Mockito.verify(info).getIntelId();
  }

  @Test
  public void testGetOperation() throws Exception {
    Mockito.when(info.getOperation()).thenReturn(AbstractTaskImplTest.OPERATION);

    Assert.assertThat(task.getOperation(), Matchers.equalTo(AbstractTaskImplTest.OPERATION));

    Mockito.verify(info).getOperation();
  }

  @Test
  public void testGetLastModified() throws Exception {
    Mockito.when(info.getLastModified()).thenReturn(AbstractTaskImplTest.LAST_MODIFIED);

    Assert.assertThat(task.getLastModified(), Matchers.equalTo(AbstractTaskImplTest.LAST_MODIFIED));

    Mockito.verify(info).getLastModified();
  }

  @Test
  public void testGetResource() throws Exception {
    Mockito.when(info.getResource()).thenReturn(Optional.of(AbstractTaskImplTest.RESOURCE));

    Assert.assertThat(
        task.getResource(), OptionalMatchers.isPresentAndIs(AbstractTaskImplTest.RESOURCE));

    Mockito.verify(info).getResource();
  }

  @Test
  public void testGetResourceWhenEmpty() throws Exception {
    Assert.assertThat(task.getResource(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testMetadatas() throws Exception {
    Mockito.when(info.metadatas()).thenReturn(Stream.of(AbstractTaskImplTest.METADATA));

    Assert.assertThat(
        task.metadatas().collect(Collectors.toList()),
        Matchers.contains(AbstractTaskImplTest.METADATA));

    Mockito.verify(info).metadatas();
  }

  @Test
  public void testMetadatasWhenEmpty() throws Exception {
    Assert.assertThat(
        task.metadatas().collect(Collectors.toList()),
        Matchers.emptyCollectionOf(MetadataInfo.class));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.hashCode0(), Matchers.equalTo(task2.hashCode0()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID + "2");
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.hashCode0(), Matchers.not(Matchers.equalTo(task2.hashCode0())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(task.equals0(task), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(task.equals0(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with
                                               something else than expected */)
  @Test
  public void testEqualsWhenNotAnAbstractTaskImpl() throws Exception {
    Assert.assertThat(task.equals0("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID + "2");
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenInfoIsDifferent() throws Exception {
    final TaskInfo info2 = Mockito.mock(TaskInfo.class);

    Mockito.when(info.getPriority()).thenReturn((byte) (AbstractTaskImplTest.PRIORITY + 2));
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info2,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenTotalAttemptsIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS + 2);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenStateIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(State.FAILED);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenOriginalQueuedTimeIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME.plusMillis(2L));
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenQueuedTimeIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME.plusMillis(2L));
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenStartTimeIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME + 2L);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDurationIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2.plusMillis(2L));
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenPendingDurationIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3.plusMillis(2L));
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4);

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenActiveDurationIsDifferent() throws Exception {
    final AbstractTaskImpl task2 =
        AbstractTaskImplTest.newTask(
            info,
            AbstractTaskImplTest.mockClock(AbstractTaskImplTest.NOW, AbstractTaskImplTest.NANOS));

    task2.setId(AbstractTaskImplTest.ID);
    task2.setTotalAttempts(AbstractTaskImplTest.ATTEMPTS);
    task2.setState(AbstractTaskImplTest.STATE);
    task2.setOriginalQueuedTime(AbstractTaskImplTest.ORIGINAL_QUEUED_TIME);
    task2.setQueuedTime(AbstractTaskImplTest.QUEUED_TIME);
    task2.setStartTime(AbstractTaskImplTest.START_TIME);
    task2.setDuration(AbstractTaskImplTest.DURATION2);
    task2.setPendingDuration(AbstractTaskImplTest.DURATION3);
    task2.setActiveDuration(AbstractTaskImplTest.DURATION4.plusMillis(2L));

    Assert.assertThat(task.equals0(task2), Matchers.not(Matchers.equalTo(true)));
  }

  private static Clock mockClock(Instant wallTime, long monotonicTime, long... monotonicTimes) {
    return AbstractTaskImplTest.mockClock(
        wallTime, (Instant[]) null, monotonicTime, monotonicTimes);
  }

  private static Clock mockClock(
      Instant wallTime, Instant wallTime2, long monotonicTime, long... monotonicTimes) {
    return AbstractTaskImplTest.mockClock(
        wallTime, new Instant[] {wallTime2}, monotonicTime, monotonicTimes);
  }

  private static Clock mockClock(
      Instant wallTime,
      @Nullable Instant[] wallTimes,
      long monotonicTime,
      @Nullable long... monotonicTimes) {
    final Clock clock = Mockito.mock(Clock.class);

    if (ArrayUtils.isNotEmpty(wallTimes)) {
      Mockito.when(clock.wallTime())
          .thenReturn(
              wallTime.toEpochMilli(),
              Stream.of(wallTimes).map(Instant::toEpochMilli).toArray(Long[]::new))
          // poor's man way of achieving verifies without over-complicating the test method
          .thenThrow(new AssertionError("unexpected call to clock.wallTime()"));
    } else {
      Mockito.when(clock.wallTime())
          .thenReturn(wallTime.toEpochMilli())
          // poor's man way of achieving verifies without over-complicating the test method
          .thenThrow(new AssertionError("unexpected call to clock.wallTime()"));
    }
    if (ArrayUtils.isNotEmpty(monotonicTimes)) {
      Mockito.when(clock.monotonicTime())
          .thenReturn(monotonicTime, ArrayUtils.toObject(monotonicTimes))
          // poor's man way of achieving verifies without over-complicating the test method
          .thenThrow(new AssertionError("unexpected call to clock.monotonicTime()"));
    } else {
      Mockito.when(clock.monotonicTime())
          .thenReturn(monotonicTime)
          // poor's man way of achieving verifies without over-complicating the test method
          .thenThrow(new AssertionError("unexpected call to clock.monotonicTime()"));
    }
    return clock;
  }

  private static AbstractTaskImpl newTask(TaskInfo info, Clock clock) {
    return Mockito.mock(
        AbstractTaskImpl.class,
        Mockito.withSettings()
            .useConstructor(info, clock)
            .defaultAnswer(Mockito.CALLS_REAL_METHODS));
  }
}
