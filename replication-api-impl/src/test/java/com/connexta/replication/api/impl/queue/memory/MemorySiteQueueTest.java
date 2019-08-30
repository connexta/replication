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
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.codice.junit.ClearInterruptions;
import org.codice.junit.rules.MethodRuleAnnotationProcessor;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class MemorySiteQueueTest {
  private static final String SITE = "siteId";
  private static final int CAPACITY = 2;
  private static final byte LOW_PRIORITY = MemorySiteQueue.MIN_PRIORITY + 2;
  private static final byte PRIORITY = MemorySiteQueueTest.LOW_PRIORITY + 3;
  private static final byte HIGH_PRIORITY = MemorySiteQueueTest.PRIORITY + 1;

  private static final MemoryQueueBroker BROKER = Mockito.mock(MemoryQueueBroker.class);
  private static final MeterRegistry REGISTRY =
      Mockito.mock(MeterRegistry.class, Mockito.RETURNS_DEEP_STUBS);

  @BeforeClass
  public static void setupClass() {
    Mockito.doAnswer(MemorySiteQueueTest.returnGaugedObject(2))
        .when(MemorySiteQueueTest.REGISTRY)
        .gauge(Mockito.any(), Mockito.any(Iterable.class), Mockito.any(Number.class));
    Mockito.doAnswer(MemorySiteQueueTest.returnGaugedObject(2))
        .when(MemorySiteQueueTest.REGISTRY)
        .gaugeCollectionSize(Mockito.any(), Mockito.any(Iterable.class), Mockito.any());
  }

  @Rule public final MethodRuleAnnotationProcessor processor = new MethodRuleAnnotationProcessor();

  private final MemorySiteQueue queue =
      new MemorySiteQueue(
          MemorySiteQueueTest.BROKER,
          MemorySiteQueueTest.SITE,
          MemorySiteQueueTest.CAPACITY,
          MemorySiteQueueTest.REGISTRY);

  final TaskInfo minInfo = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueue.MIN_PRIORITY);
  final TaskInfo lowInfo = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueueTest.LOW_PRIORITY);
  final TaskInfo info = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueueTest.PRIORITY);
  final TaskInfo info2 = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueueTest.PRIORITY);
  final TaskInfo highInfo = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueueTest.HIGH_PRIORITY);
  final TaskInfo maxInfo = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueue.MAX_PRIORITY);

  final AtomicReference<AssertionError> error = new AtomicReference<>();
  final AtomicReference<Thread> thread = new AtomicReference<>();

  @After
  public void cleanup() {
    final AssertionError error = this.error.get();

    if (error != null) {
      throw error;
    }
    final Thread thread = this.thread.get();

    if (thread != null) {
      thread.interrupt();
    }
  }

  @Test
  public void testConstructor() throws Exception {
    Assert.assertThat(queue.getBroker(), Matchers.sameInstance(MemorySiteQueueTest.BROKER));
    Assert.assertThat(queue.getSite(), Matchers.equalTo(MemorySiteQueueTest.SITE));
    Assert.assertThat(queue.size(), Matchers.equalTo(0));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(0));
    queue.pendingSizes().forEach(s -> Assert.assertThat(s, Matchers.equalTo(0)));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY));
  }

  @Test
  public void testPut() throws Exception {
    queue.put(info);

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(1));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo((i == info.getPriority()) ? 1 : 0));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));

    final MemoryTask task = queue.peekFirst();

    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testPutWithHigherPriority() throws Exception {
    queue.put(info);

    queue.put(highInfo);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == highInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(highInfo));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
  }

  @Test
  public void testPutWithMaxPriority() throws Exception {
    queue.put(info);

    queue.put(maxInfo);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == maxInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(maxInfo));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
  }

  @Test
  public void testPutWithMoreThanMaxPriority() throws Exception {
    final TaskInfo info2 = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueue.MAX_PRIORITY + 1);

    queue.put(info);

    queue.put(info2);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(
              ((i == info.getPriority()) || (i == MemorySiteQueue.MAX_PRIORITY) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info2));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(
        queue.peekFirst().getPriority(),
        Matchers.equalTo(Byte.valueOf(MemorySiteQueue.MAX_PRIORITY)));
  }

  @Test
  public void testPutWithLowerPriority() throws Exception {
    queue.put(info);

    queue.put(lowInfo);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == lowInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(lowInfo));
  }

  @Test
  public void testPutWithMinPriority() throws Exception {
    queue.put(info);

    queue.put(minInfo);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == minInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(minInfo));
  }

  @Test
  public void testPutWithLessThanMinPriority() throws Exception {
    final TaskInfo info2 = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueue.MIN_PRIORITY - 1);

    queue.put(info);

    queue.put(info2);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(
              ((i == info.getPriority()) || (i == MemorySiteQueue.MIN_PRIORITY) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info2));
    Assert.assertThat(
        queue.peekLast().getPriority(),
        Matchers.equalTo(Byte.valueOf(MemorySiteQueue.MIN_PRIORITY)));
  }

  @Test
  public void testPutWithSamePriority() throws Exception {
    queue.put(info);

    queue.put(info2);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo((i == info.getPriority() ? 2 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info2));
  }

  @Test
  @ClearInterruptions
  public void testPutBlocksWhenCapacityReached() throws Exception {
    queue.put(info);
    queue.put(highInfo);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));

    putInQueueAndFailIfItDoesNotBlockForever(maxInfo);
    // at this point the other thread is blocked

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));
  }

  @Test
  @ClearInterruptions
  public void testPutUnblocksOnceTaskIsCompleted() throws Exception {
    queue.put(info);
    queue.put(highInfo);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));

    final Thread thread = putInQueueAndFailIfItNeverBlocks(maxInfo);
    // at this point the other thread is blocked

    // pull and finish a task to unblock the other thread
    queue.poll(0L, TimeUnit.MILLISECONDS).complete();
    // wait for the other thread to complete
    MemorySiteQueueTest.joinUnInterruptibly(thread, 5000L);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));
  }

  @Test
  public void testOffer() throws Exception {
    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(1));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo((i == info.getPriority()) ? 1 : 0));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));

    final MemoryTask task = queue.peekFirst();

    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testOfferWithHigherPriority() throws Exception {
    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(highInfo), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == highInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(highInfo));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
  }

  @Test
  public void testOfferWithMaxPriority() throws Exception {
    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(maxInfo), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == maxInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(maxInfo));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
  }

  @Test
  public void testOfferWithMoreThanMaxPriority() throws Exception {
    final TaskInfo info2 = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueue.MAX_PRIORITY + 1);

    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(info2), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(
              ((i == info.getPriority()) || (i == MemorySiteQueue.MAX_PRIORITY) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info2));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(
        queue.peekFirst().getPriority(),
        Matchers.equalTo(Byte.valueOf(MemorySiteQueue.MAX_PRIORITY)));
  }

  @Test
  public void testOfferWithLowerPriority() throws Exception {
    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(lowInfo), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == lowInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(lowInfo));
  }

  @Test
  public void testOfferWithMinPriority() throws Exception {
    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(minInfo), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == minInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(minInfo));
  }

  @Test
  public void testOfferWithLessThanMinPriority() throws Exception {
    final TaskInfo info2 = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueue.MIN_PRIORITY - 1);

    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(info2), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(
              ((i == info.getPriority()) || (i == MemorySiteQueue.MIN_PRIORITY) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info2));
    Assert.assertThat(
        queue.peekLast().getPriority(),
        Matchers.equalTo(Byte.valueOf(MemorySiteQueue.MIN_PRIORITY)));
  }

  @Test
  public void testOfferWithSamePriority() throws Exception {
    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(info2), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo((i == info.getPriority() ? 2 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info2));
  }

  @Test
  public void testOfferFailsWhenCapacityReached() throws Exception {
    Assert.assertThat(queue.offer(info), Matchers.equalTo(true));
    Assert.assertThat(queue.offer(highInfo), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));

    Assert.assertThat(queue.offer(maxInfo), Matchers.equalTo(false));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));
  }

  @Test
  public void testOfferWithTimeout() throws Exception {
    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(1));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo((i == info.getPriority()) ? 1 : 0));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));

    final MemoryTask task = queue.peekFirst();

    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testOfferWithTimeoutAndHigherPriority() throws Exception {
    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(highInfo, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == highInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(highInfo));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
  }

  @Test
  public void testOfferWithTimeoutAndMaxPriority() throws Exception {
    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(maxInfo, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == maxInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(maxInfo));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
  }

  @Test
  public void testOfferWithTimeoutAndMoreThanMaxPriority() throws Exception {
    final TaskInfo info2 = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueue.MAX_PRIORITY + 1);

    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(info2, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(
              ((i == info.getPriority()) || (i == MemorySiteQueue.MAX_PRIORITY) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info2));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(
        queue.peekFirst().getPriority(),
        Matchers.equalTo(Byte.valueOf(MemorySiteQueue.MAX_PRIORITY)));
  }

  @Test
  public void testOfferWithTimeoutAndLowerPriority() throws Exception {
    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(lowInfo, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == lowInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(lowInfo));
  }

  @Test
  public void testOfferWithTimeoutAndMinPriority() throws Exception {
    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(minInfo, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(((i == info.getPriority()) || (i == minInfo.getPriority()) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(minInfo));
  }

  @Test
  public void testOfferWithTimeoutAndLessThanMinPriority() throws Exception {
    final TaskInfo info2 = MemorySiteQueueTest.mockTaskInfo(MemorySiteQueue.MIN_PRIORITY - 1);

    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(info2, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(
          queue.pendingSize(i),
          Matchers.equalTo(
              ((i == info.getPriority()) || (i == MemorySiteQueue.MIN_PRIORITY) ? 1 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info2));
    Assert.assertThat(
        queue.peekLast().getPriority(),
        Matchers.equalTo(Byte.valueOf(MemorySiteQueue.MIN_PRIORITY)));
  }

  @Test
  public void testOfferWithTimeoutAndSamePriority() throws Exception {
    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.offer(info2, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo((i == info.getPriority() ? 2 : 0)));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 2));
    Assert.assertThat(queue.peekFirst().getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(queue.peekLast().getInfo(), Matchers.sameInstance(info2));
  }

  @Test
  public void testOfferWithTimeoutTimesOutWhenCapacityReached() throws Exception {
    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));
    Assert.assertThat(queue.offer(highInfo, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));

    // make sure the timeout is more than 0L such that it will get us through the offer() method
    // where the initial check will result in more time available to wait
    Assert.assertThat(queue.offer(maxInfo, 1L, TimeUnit.NANOSECONDS), Matchers.equalTo(false));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));
  }

  @Test
  @ClearInterruptions
  public void testOfferWithTimeoutUnblocksOnceTaskIsCompleted() throws Exception {
    Assert.assertThat(queue.offer(info, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));
    Assert.assertThat(queue.offer(highInfo, 0L, TimeUnit.MILLISECONDS), Matchers.equalTo(true));

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));

    final Thread thread = offerInQueueAndFailIfTimesOutOrIfItNeverBlocks(maxInfo);
    // at this point the other thread is blocked

    // pull and finish a task to unblock the other thread
    queue.poll(0L, TimeUnit.MILLISECONDS).complete();
    // wait for the other thread to complete
    MemorySiteQueueTest.joinUnInterruptibly(thread, 5000L);

    Assert.assertThat(queue.size(), Matchers.equalTo(2));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(2));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(0));
  }

  @Test
  public void testTake() throws Exception {
    queue.put(info);

    final MemoryTask task = (MemoryTask) queue.take();

    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.ACTIVE));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(true));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isPresentAndIs(Thread.currentThread()));

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(0));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo(0));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(1));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));
  }

  @Test
  @ClearInterruptions
  public void testTakeBlocksWhenNoTasksAreAvailable() throws Exception {
    takeFromQueueAndFailIfItDoesNotBlockForever();
    // at this point the other thread is blocked

    Assert.assertThat(queue.size(), Matchers.equalTo(0));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY));
  }

  @Test
  @ClearInterruptions
  public void testTakeUnblocksOnceTaskIsPending() throws Exception {
    final Thread thread = takeFromQueueAndFailIfItNeverBlocks(info);
    // at this point the other thread is blocked

    // offer a new task to unblock the other thread
    queue.put(info);
    // wait for the other thread to complete
    MemorySiteQueueTest.joinUnInterruptibly(thread, 5000L);

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(1));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));
  }

  @Test
  public void testPoll() throws Exception {
    queue.put(info);

    final MemoryTask task = (MemoryTask) queue.poll(0L, TimeUnit.MILLISECONDS);

    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.ACTIVE));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(true));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isPresentAndIs(Thread.currentThread()));

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(0));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo(0));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(1));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));
  }

  @Test
  public void testPollTimesOutWhenNoTasksAreAvailable() throws Exception {
    // make sure the timeout is more than 0L such that it will get us through the poll() method
    // where the initial check will result in more time available to wait
    Assert.assertThat(queue.poll(1L, TimeUnit.NANOSECONDS), Matchers.nullValue());

    Assert.assertThat(queue.size(), Matchers.equalTo(0));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY));
  }

  @Test
  @ClearInterruptions
  public void testPollUnblocksOnceTaskIsOffered() throws Exception {
    final Thread thread = pollFromQueueAndFailIfItTimesOutOrIfItNeverBlocks(info);
    // at this point the other thread is blocked

    // put a task in the queue to unblock the other thread
    queue.put(info);
    // wait for the other thread to complete
    MemorySiteQueueTest.joinUnInterruptibly(thread, 5000L);

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(0));
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(1));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));
  }

  @Test
  public void testRequeueAtFront() throws Exception {
    queue.put(info);

    queue.take().unlock();

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(1));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo((i == info.getPriority()) ? 1 : 0));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));

    final MemoryTask task = queue.peekFirst();

    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testRequeueAtEnd() throws Exception {
    queue.put(info);

    queue.take().fail(ErrorCode.SITE_TIMEOUT);

    Assert.assertThat(queue.size(), Matchers.equalTo(1));
    Assert.assertThat(queue.pendingSize(), Matchers.equalTo(1));
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      Assert.assertThat(queue.pendingSize(i), Matchers.equalTo((i == info.getPriority()) ? 1 : 0));
    }
    Assert.assertThat(queue.activeSize(), Matchers.equalTo(0));
    Assert.assertThat(
        queue.remainingCapacity(), Matchers.equalTo(MemorySiteQueueTest.CAPACITY - 1));

    final MemoryTask task = queue.peekFirst();

    Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(2));
    Assert.assertThat(task.getState(), Matchers.equalTo(State.PENDING));
    Assert.assertThat(task.isLocked(), Matchers.equalTo(false));
    Assert.assertThat(task.getOwner(), OptionalMatchers.isEmpty());
  }

  private Thread putInQueueAndFailIfItDoesNotBlockForever(TaskInfo info)
      throws InterruptedException {
    final Thread main = Thread.currentThread();
    final Thread thread =
        new Thread(
            () -> {
              try {
                queue.put(info);
                Assert.fail("MemorySiteQueue.put() should have blocked forever");
              } catch (InterruptedException e) { // ignore
              } catch (AssertionError e) {
                error.compareAndSet(null, e);
                main.interrupt();
              }
            });

    thread.setDaemon(true);
    this.thread.set(thread);
    thread.start();
    if (!queue.waitForOfferorsToBeBlocked(5000L)) {
      Assert.fail("timed out waiting for MemorySiteQueue.put() to block");
    }
    return thread;
  }

  private Thread putInQueueAndFailIfItNeverBlocks(TaskInfo info) throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Thread main = Thread.currentThread();
    final Thread thread =
        new Thread(
            () -> {
              try {
                queue.put(info);
                if (latch.getCount() != 0) {
                  Assert.fail("MemorySiteQueue.put() should have blocked");
                }
              } catch (InterruptedException e) { // ignore
              } catch (AssertionError e) {
                error.compareAndSet(null, e);
                main.interrupt();
              }
            });

    thread.setDaemon(true);
    this.thread.set(thread);
    thread.start();
    if (!queue.waitForOfferorsToBeBlocked(5000L)) {
      Assert.fail("timed out waiting for MemorySiteQueue.put() to block");
    }
    // let the other thread know to no longer fail once it unblocks
    latch.countDown();
    return thread;
  }

  private Thread offerInQueueAndFailIfTimesOutOrIfItNeverBlocks(TaskInfo info)
      throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Thread main = Thread.currentThread();
    final Thread thread =
        new Thread(
            () -> {
              try {
                if (!queue.offer(info, 5000L, TimeUnit.MILLISECONDS)) {
                  Assert.fail("MemorySiteQueue.offer() timed out");
                } else if (latch.getCount() != 0) {
                  Assert.fail("MemorySiteQueue.offer() should have blocked");
                }
              } catch (InterruptedException e) { // ignore
              } catch (AssertionError e) {
                error.compareAndSet(null, e);
                main.interrupt();
              }
            });

    thread.setDaemon(true);
    this.thread.set(thread);
    thread.start();
    if (!queue.waitForOfferorsToBeBlocked(5000L)) {
      Assert.fail("timed out waiting for MemorySiteQueue.offer() to block");
    }
    // let the other thread know to no longer fail once it unblocks
    latch.countDown();
    return thread;
  }

  private Thread takeFromQueueAndFailIfItDoesNotBlockForever() throws InterruptedException {
    final Thread main = Thread.currentThread();
    final Thread thread =
        new Thread(
            () -> {
              try {
                queue.take();
                Assert.fail("MemorySiteQueue.take() should have blocked forever");
              } catch (InterruptedException e) { // ignore
              } catch (AssertionError e) {
                error.compareAndSet(null, e);
                main.interrupt();
              }
            });

    thread.setDaemon(true);
    this.thread.set(thread);
    thread.start();
    if (!queue.waitForPullersToBeBlocked(5000L)) {
      Assert.fail("timed out waiting for MemorySiteQueue.take() to block");
    }
    return thread;
  }

  private Thread takeFromQueueAndFailIfItNeverBlocks(TaskInfo info) throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Thread main = Thread.currentThread();
    final Thread thread =
        new Thread(
            () -> {
              try {
                final MemoryTask task = (MemoryTask) queue.take();

                if (latch.getCount() != 0) {
                  Assert.fail("MemorySiteQueue.take() should have blocked");
                }
                Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
                Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
                Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
                Assert.assertThat(task.getState(), Matchers.equalTo(State.ACTIVE));
                Assert.assertThat(task.isLocked(), Matchers.equalTo(true));
                Assert.assertThat(
                    task.getOwner(), OptionalMatchers.isPresentAndIs(Thread.currentThread()));
              } catch (InterruptedException e) { // ignore
              } catch (AssertionError e) {
                error.compareAndSet(null, e);
                main.interrupt();
              }
            });

    thread.setDaemon(true);
    this.thread.set(thread);
    thread.start();
    if (!queue.waitForPullersToBeBlocked(5000L)) {
      Assert.fail("timed out waiting for MemorySiteQueue.take() to block");
    }
    // let the other thread know to no longer fail once it unblocks
    latch.countDown();
    return thread;
  }

  private Thread pollFromQueueAndFailIfItTimesOutOrIfItNeverBlocks(TaskInfo info)
      throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final Thread main = Thread.currentThread();
    final Thread thread =
        new Thread(
            () -> {
              try {
                final MemoryTask task = (MemoryTask) queue.poll(5000L, TimeUnit.MILLISECONDS);

                if (task == null) {
                  Assert.fail("MemorySiteQueue.poll() timed out");
                } else if (latch.getCount() != 0) {
                  Assert.fail("MemorySiteQueue.poll() should have blocked");
                }
                Assert.assertThat(task.getInfo(), Matchers.sameInstance(info));
                Assert.assertThat(task.getPriority(), Matchers.equalTo(info.getPriority()));
                Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(1));
                Assert.assertThat(task.getState(), Matchers.equalTo(State.ACTIVE));
                Assert.assertThat(task.isLocked(), Matchers.equalTo(true));
                Assert.assertThat(
                    task.getOwner(), OptionalMatchers.isPresentAndIs(Thread.currentThread()));
              } catch (InterruptedException e) { // ignore
              } catch (AssertionError e) {
                error.compareAndSet(null, e);
                main.interrupt();
              }
            });

    thread.setDaemon(true);
    this.thread.set(thread);
    thread.start();
    if (!queue.waitForPullersToBeBlocked(5000L)) {
      Assert.fail("timed out waiting for MemorySiteQueue.poll() to block");
    }
    // let the other thread know to no longer fail once it unblocks
    latch.countDown();
    return thread;
  }

  private static void joinUnInterruptibly(Thread thread, long timeout) {
    try {
      thread.join(timeout);
    } catch (InterruptedException e) { // ignore
    }
  }

  private static TaskInfo mockTaskInfo(int priority) {
    final TaskInfo info = Mockito.mock(TaskInfo.class);

    Mockito.when(info.getPriority()).thenReturn((byte) priority);
    return info;
  }

  private static <T> Answer<T> returnGaugedObject(int index) {
    return i -> (T) i.getArgument(index);
  }
}
