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
import org.junit.Test;
import org.mockito.Mockito;

public class MemoryTaskTest {
  // using System.currentTimeMillis() ensures the same resolution when testing
  // as how the implementation currently creates Instant objects from a Clock
  private static final Instant NOW = Instant.ofEpochMilli(System.currentTimeMillis());
  private static final Duration DURATION2 = Duration.ofSeconds(5L);
  private static final long NANOS = System.nanoTime();
  private static final long NANOS2 = MemoryTaskTest.NANOS + MemoryTaskTest.DURATION2.toNanos();

  private final TaskInfo info = Mockito.mock(TaskInfo.class);
  private final MemorySiteQueue queue = Mockito.mock(MemorySiteQueue.class);

  @Test
  public void testConstructorWhenInitiallyQueued() throws Exception {
    final Clock clock =
        MemoryTaskTest.mockClock(MemoryTaskTest.NOW, MemoryTaskTest.NANOS, MemoryTaskTest.NANOS2);
    final MemoryTask task = new MemoryTask(info, queue, clock);

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
  public void testLockOnFirstAttempt() throws Exception {}

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
              Stream.of(wallTimes).map(Instant::toEpochMilli).toArray(Long[]::new));
    } else {
      Mockito.when(clock.wallTime()).thenReturn(wallTime.toEpochMilli());
    }
    if (ArrayUtils.isNotEmpty(monotonicTimes)) {
      Mockito.when(clock.monotonicTime())
          .thenReturn(monotonicTime, ArrayUtils.toObject(monotonicTimes));
    } else {
      Mockito.when(clock.monotonicTime()).thenReturn(monotonicTime);
    }
    return clock;
  }
}
