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
package com.connexta.replication.api.impl.queue.memory;

import com.connexta.replication.api.data.ErrorCode;
import com.connexta.replication.api.data.Task.State;
import com.connexta.replication.api.data.TaskInfo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class MemoryTaskTest {
  private static final byte PRIORITY = 3;
  private static final Duration DURATION2 = Duration.ofSeconds(5L);
  private static final Duration DURATION3 = Duration.ofSeconds(8L);
  private static final Duration DURATION4 = Duration.ofSeconds(17L);
  private static final Duration DURATION5 = Duration.ofSeconds(24L);
  private static final long NANOS = System.nanoTime();
  private static final long NANOS2 = MemoryTaskTest.NANOS + MemoryTaskTest.DURATION2.toNanos();
  private static final long NANOS3 = MemoryTaskTest.NANOS2 + MemoryTaskTest.DURATION3.toNanos();
  private static final long NANOS4 = MemoryTaskTest.NANOS3 + MemoryTaskTest.DURATION4.toNanos();
  private static final long NANOS5 = MemoryTaskTest.NANOS4 + MemoryTaskTest.DURATION5.toNanos();
  // using System.currentTimeMillis() ensures the same resolution when testing
  // as how the implementation currently creates Instant objects from a Clock
  private static final Instant NOW = Instant.ofEpochMilli(System.currentTimeMillis());
  private static final Instant THEN2 = MemoryTaskTest.NOW.plusMillis(NANOS2);
  private static final Instant THEN3 = MemoryTaskTest.NOW.plusMillis(NANOS3);
  private static final Instant THEN4 = MemoryTaskTest.NOW.plusMillis(NANOS4);
  private static final Instant THEN5 = MemoryTaskTest.NOW.plusMillis(NANOS5);
  private static final String REASON = "SomeIntelligentReason";

  private final TaskInfo info = Mockito.mock(TaskInfo.class);
  private final MemorySiteQueue queue = Mockito.mock(MemorySiteQueue.class);

  @Before
  public void setup() throws Exception {
    Mockito.when(info.getPriority()).thenReturn(MemoryTaskTest.PRIORITY);
    Mockito.doAnswer(MemoryTaskTest.runCallback())
        .when(queue)
        .requeue(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
    Mockito.doAnswer(MemoryTaskTest.runCallback()).when(queue).remove(Mockito.any(), Mockito.any());
  }

  @Test
  public void testConstructorWhenInitiallyQueued() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when getDuration() is called
            MemoryTaskTest.NANOS2, // when getPendingDuration() is called
            MemoryTaskTest.NANOS2); // when getActiveDuration() is called
    final MemoryTask task = new MemoryTask(info, queue, clock);

    Assert.assertThat(task.getPriority(), Matchers.equalTo(MemoryTaskTest.PRIORITY));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getCode(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getDuration(), Matchers.equalTo(MemoryTaskTest.DURATION2));
    Assert.assertThat(task.getPendingDuration(), Matchers.equalTo(MemoryTaskTest.DURATION2));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(Duration.ZERO));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testLockOnFirstAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3, // when getDuration() is called
            MemoryTaskTest.NANOS3, // when getPendingDuration() is called
            MemoryTaskTest.NANOS3); // when getActiveDuration() is called
    final MemoryTask task = new MemoryTask(info, queue, clock);

    Assert.assertThat(task.lock(), Matchers.sameInstance(task));

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.ACTIVE));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getCode(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION3)));
    Assert.assertThat(task.getPendingDuration(), Matchers.equalTo(MemoryTaskTest.DURATION2));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(MemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(true));
    Assert.assertThat(
        task.getOwner(),
        OptionalMatchers.isPresentAnd(Matchers.sameInstance(Thread.currentThread())));
  }

  @Test
  public void testUnlockOnFirstAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.THEN3, // when requeued
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3, // when unlock() is called and task is requeued to become pending
            MemoryTaskTest.NANOS4, // when getDuration() is called
            MemoryTaskTest.NANOS4, // when getPendingDuration() is called
            MemoryTaskTest.NANOS4); // when getActiveDuration() is called
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.unlock();

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getCode(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            MemoryTaskTest.DURATION2
                .plus(MemoryTaskTest.DURATION3)
                .plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getPendingDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(MemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.eq(task), Mockito.eq(true), Mockito.notNull());
    Mockito.verify(queue, Mockito.never()).remove(Mockito.any(), Mockito.any());
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void testUnlockWhenNotOwner() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    lockFromOtherThread(task);

    task.unlock();
  }

  @Test(expected = IllegalStateException.class)
  public void testUnlockWhenAlreadyCompleted() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    task.lock();
    task.complete();

    task.unlock();
  }

  @Test(expected = IllegalStateException.class)
  public void testUnlockWhenAlreadyFailed() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    task.lock();
    task.fail(ErrorCode.UNKNOWN_ERROR);

    task.unlock();
  }

  @Test
  public void testCompleteOnFirstAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3); // when complete() is called and task is removed
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.complete();

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.SUCCESSFUL));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(true));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getCode(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION3)));
    Assert.assertThat(task.getPendingDuration(), Matchers.equalTo(MemoryTaskTest.DURATION2));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(MemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue, Mockito.never())
        .requeue(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test
  public void testCompleteAfterFirstUnlocked() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3, // when unlock() is called and task is requeued to become pending
            MemoryTaskTest.NANOS4, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS5); // when complete() is called and task is removed
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.unlock();
    task.lock();
    task.complete();

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.SUCCESSFUL));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(true));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getCode(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            MemoryTaskTest.DURATION2
                .plus(MemoryTaskTest.DURATION3)
                .plus(MemoryTaskTest.DURATION4)
                .plus(MemoryTaskTest.DURATION5)));
    Assert.assertThat(
        task.getPendingDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getActiveDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION3.plus(MemoryTaskTest.DURATION5)));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.any(), Mockito.eq(true), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test
  public void testCompleteAfterSecondAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.THEN3, // after requeued
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3, // when fail() is called and task is requeued to become pending
            MemoryTaskTest.NANOS4, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS5); // when complete() is called and task is removed
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.fail(ErrorCode.SITE_TIMEOUT);
    task.lock();
    task.complete();

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.SUCCESSFUL));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(true));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getCode(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.THEN3));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            MemoryTaskTest.DURATION2
                .plus(MemoryTaskTest.DURATION3)
                .plus(MemoryTaskTest.DURATION4)
                .plus(MemoryTaskTest.DURATION5)));
    Assert.assertThat(
        task.getPendingDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getActiveDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION3.plus(MemoryTaskTest.DURATION5)));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.any(), Mockito.eq(false), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void testCompleteWhenNotOwner() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    lockFromOtherThread(task);

    task.complete();
  }

  @Test(expected = IllegalStateException.class)
  public void testCompleteWhenAlreadyCompleted() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    task.lock();
    task.complete();

    task.complete();
  }

  @Test(expected = IllegalStateException.class)
  public void testCompleteWhenAlreadyFailed() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    task.lock();
    task.fail(ErrorCode.UNKNOWN_ERROR);

    task.complete();
  }

  @Test
  public void testFailWhenRetryableAndWithNoReasonOnFirstAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.THEN3, // when requeued
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3, // when fail() is called and task is requeued to become pending
            MemoryTaskTest.NANOS4, // when getDuration() is called
            MemoryTaskTest.NANOS4, // when getPendingDuration() is called
            MemoryTaskTest.NANOS4); // when getActiveDuration() is called
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.fail(ErrorCode.SITE_TIMEOUT);

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getCode(), OptionalMatchers.isPresentAndIs(ErrorCode.SITE_TIMEOUT));
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.THEN3));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            MemoryTaskTest.DURATION2
                .plus(MemoryTaskTest.DURATION3)
                .plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getPendingDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(MemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.eq(task), Mockito.eq(false), Mockito.notNull());
    Mockito.verify(queue, Mockito.never()).remove(Mockito.any(), Mockito.any());
  }

  @Test
  public void testFailWhenRetryableAndWithReasonOnFirstAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.THEN3, // when requeued
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3, // when fail() is called and task is requeued to become pending
            MemoryTaskTest.NANOS4, // when getDuration() is called
            MemoryTaskTest.NANOS4, // when getPendingDuration() is called
            MemoryTaskTest.NANOS4); // when getActiveDuration() is called
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.fail(ErrorCode.SITE_TIMEOUT, MemoryTaskTest.REASON);

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(false));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(false));
    Assert.assertThat(task.getCode(), OptionalMatchers.isPresentAndIs(ErrorCode.SITE_TIMEOUT));
    Assert.assertThat(task.getReason(), OptionalMatchers.isPresentAndIs(MemoryTaskTest.REASON));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.THEN3));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            MemoryTaskTest.DURATION2
                .plus(MemoryTaskTest.DURATION3)
                .plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getPendingDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(MemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.eq(task), Mockito.eq(false), Mockito.notNull());
    Mockito.verify(queue, Mockito.never()).remove(Mockito.any(), Mockito.any());
  }

  @Test
  public void testFailWhenNotRetryableAndWithNoReasonOnFirstAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3); // when fail() is called and task is removed
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.fail(ErrorCode.UNKNOWN_ERROR);

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.FAILED));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(true));
    Assert.assertThat(task.getCode(), OptionalMatchers.isPresentAndIs(ErrorCode.UNKNOWN_ERROR));
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION3)));
    Assert.assertThat(task.getPendingDuration(), Matchers.equalTo(MemoryTaskTest.DURATION2));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(MemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue, Mockito.never())
        .requeue(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test
  public void testFailWhenNotRetryableAndWithReasonOnFirstAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3); // when fail() is called and task is removed
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.fail(ErrorCode.UNKNOWN_ERROR, MemoryTaskTest.REASON);

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.FAILED));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(true));
    Assert.assertThat(task.getCode(), OptionalMatchers.isPresentAndIs(ErrorCode.UNKNOWN_ERROR));
    Assert.assertThat(task.getReason(), OptionalMatchers.isPresentAndIs(MemoryTaskTest.REASON));
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION3)));
    Assert.assertThat(task.getPendingDuration(), Matchers.equalTo(MemoryTaskTest.DURATION2));
    Assert.assertThat(task.getActiveDuration(), Matchers.equalTo(MemoryTaskTest.DURATION3));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue, Mockito.never())
        .requeue(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test
  public void testFailWhenNotRetryableAndAfterSecondAttempt() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(
            MemoryTaskTest.NOW, // creation
            MemoryTaskTest.THEN3, // when requeued
            MemoryTaskTest.NANOS, // creation
            MemoryTaskTest.NANOS2, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS3, // when fail() is called and task is requeued to become pending
            MemoryTaskTest.NANOS4, // when lock() is called and task becomes active
            MemoryTaskTest.NANOS5); // when fail() is called and task is removed
    final MemoryTask task = new MemoryTask(info, queue, clock);

    task.lock();
    task.fail(ErrorCode.SITE_TIMEOUT, MemoryTaskTest.REASON);
    task.lock();
    task.fail(ErrorCode.UNKNOWN_ERROR);

    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.FAILED));
    Assert.assertThat(task.isCompleted(), Matchers.equalTo(true));
    Assert.assertThat(task.wasSuccessful(), Matchers.equalTo(false));
    Assert.assertThat(task.hasFailed(), Matchers.equalTo(true));
    Assert.assertThat(task.getCode(), OptionalMatchers.isPresentAndIs(ErrorCode.UNKNOWN_ERROR));
    Assert.assertThat(task.getReason(), OptionalMatchers.isEmpty());
    Assert.assertThat(task.getOriginalQueuedTime(), Matchers.equalTo(MemoryTaskTest.NOW));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(MemoryTaskTest.THEN3));
    Assert.assertThat(
        task.getDuration(),
        Matchers.equalTo(
            MemoryTaskTest.DURATION2
                .plus(MemoryTaskTest.DURATION3)
                .plus(MemoryTaskTest.DURATION4)
                .plus(MemoryTaskTest.DURATION5)));
    Assert.assertThat(
        task.getPendingDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION2.plus(MemoryTaskTest.DURATION4)));
    Assert.assertThat(
        task.getActiveDuration(),
        Matchers.equalTo(MemoryTaskTest.DURATION3.plus(MemoryTaskTest.DURATION5)));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());

    Mockito.verify(queue).requeue(Mockito.any(), Mockito.eq(false), Mockito.any());
    Mockito.verify(queue).remove(Mockito.eq(task), Mockito.any());
  }

  @Test(expected = IllegalMonitorStateException.class)
  public void testFailWhenNotOwner() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    lockFromOtherThread(task);

    task.fail(ErrorCode.UNKNOWN_ERROR);
  }

  @Test(expected = IllegalStateException.class)
  public void testFailWhenAlreadyCompleted() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    task.lock();
    task.complete();

    task.fail(ErrorCode.UNKNOWN_ERROR);
  }

  @Test(expected = IllegalStateException.class)
  public void testFailWhenAlreadyFailed() throws Exception {
    final MemoryTask task = new MemoryTask(info, queue, Mockito.mock(Clock.class));

    task.lock();
    task.fail(ErrorCode.UNKNOWN_ERROR);

    task.fail(ErrorCode.UNKNOWN_ERROR);
  }

  private void lockFromOtherThread(MemoryTask task) throws InterruptedException {
    final Thread other = new Thread(task::lock);

    other.setDaemon(true);
    other.start();
    other.join();

    Assert.assertThat(task.getOwner(), OptionalMatchers.isPresentAndIs(other));
  }

  private static Clock mockClock(Instant wallTime, long monotonicTime, long... monotonicTimes) {
    return MemoryTaskTest.mockClock(wallTime, (Instant[]) null, monotonicTime, monotonicTimes);
  }

  private static Clock mockClock(
      Instant wallTime, Instant wallTime2, long monotonicTime, long... monotonicTimes) {
    return MemoryTaskTest.mockClock(
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

  private static Answer<Void> runCallback() {
    return i -> {
      for (final Object o : i.getArguments()) {
        if (o instanceof Runnable) {
          ((Runnable) o).run();
          break;
        }
      }
      return null;
    };
  }
}
