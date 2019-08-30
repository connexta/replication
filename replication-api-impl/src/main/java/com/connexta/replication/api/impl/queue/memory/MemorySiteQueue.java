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

import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.TaskInfo;
import com.connexta.replication.api.queue.QueueBroker;
import com.connexta.replication.api.queue.SiteQueue;
import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

/** Provides an in-memory implementation for a site specific queue. */
// locking behavior is similar to LinkedBlockingQueue such that we can control capacity
// and size across all internal queues and also to properly handle polling from multiple
// different site queues when implementing the composite queue
public class MemorySiteQueue implements MemoryQueue, SiteQueue {
  @VisibleForTesting static final byte MAX_PRIORITY = 9;
  @VisibleForTesting static final byte MIN_PRIORITY = 0;

  private final MemoryQueueBroker broker;

  private final String site;

  /** List of all internal queues indexed by their corresponding priorities. */
  private final List<Deque<MemoryTask>> pendings = new ArrayList<>();

  /** Ordered set of active tasks that are still locked and being worked on. */
  private final Set<MemoryTask> active = new LinkedHashSet<>();

  /** The capacity bound, or Integer.MAX_VALUE if none. */
  private final int capacity;

  /** Current number of tasks. */
  private final AtomicInteger count;

  /** Current number of pending tasks. */
  private final AtomicInteger pendingCount;

  /** Current number of active tasks. */
  private final AtomicInteger activeCount;

  /** Lock held by take, poll, etc. */
  private final ReentrantLock takeLock = new ReentrantLock();

  /** Wait queue for waiting takes. */
  private final Condition notEmpty = takeLock.newCondition();

  /** Lock held by put, offer, etc. */
  private final ReentrantLock putLock = new ReentrantLock();

  /** Wait queue for waiting puts. */
  private final Condition notFull = putLock.newCondition();

  private final MeterRegistry registry;

  private final Counter queuedCounter;
  private final Counter requeuedCounter;
  private final Counter successCounter;
  private final Counter failCounter;
  private final Timer timer;
  private final List<Timer> pendingTimers = new ArrayList<>(MemorySiteQueue.MAX_PRIORITY + 1);
  private final Timer activeTimer;

  /**
   * Instantiates a default in-memory site queue.
   *
   * @param broker the broker for which we are instantiating this queue
   * @param site the corresponding site id
   * @param capacity the capacity of the queue this broker manages
   * @param registry the micrometer registry to report metrics
   */
  public MemorySiteQueue(
      MemoryQueueBroker broker, String site, int capacity, MeterRegistry registry) {
    final Tag tag = Tag.of("site", site);
    final Iterable<Tag> tags = Set.of(tag);

    this.broker = broker;
    this.site = site;
    this.capacity = capacity;
    this.registry = registry;
    this.count = registry.gauge("replication.queue.tasks", tags, new AtomicInteger());
    // no need to gauge this one since the priority gauges below will report that automatically
    this.pendingCount = new AtomicInteger();
    this.activeCount = registry.gauge("replication.queue.active.tasks", tags, new AtomicInteger());
    for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
      final Iterable<Tag> ptags = Set.of(tag, Tag.of("priority", Integer.toString(i)));

      pendings.add(
          registry.gaugeCollectionSize(
              "replication.queue.pending.tasks", ptags, new LinkedList<>()));
      this.pendingTimers.add(registry.timer("replication.queue.pending.timer", ptags));
    }
    this.queuedCounter = registry.counter("replication.queue.queued.tasks", tags);
    this.requeuedCounter = registry.counter("replication.queue.requeued.tasks", tags);
    this.successCounter = registry.counter("replication.queue.success.tasks", tags);
    this.failCounter = registry.counter("replication.queue.fail.tasks", tags);
    this.timer = registry.timer("replication.queue.overall.timer", tags);
    this.activeTimer = registry.timer("replication.queue.active.timer", tags);
  }

  @Override
  public QueueBroker getBroker() {
    return broker;
  }

  @Override
  public String getSite() {
    return site;
  }

  @Override
  public int size() {
    return count.get();
  }

  @Override
  public int pendingSize() {
    return pendingCount.get();
  }

  @Override
  public int activeSize() {
    return activeCount.get();
  }

  @Override
  public int remainingCapacity() {
    return capacity - size();
  }

  @Override
  public void put(TaskInfo info) throws InterruptedException {
    final MemoryTask task = new MemoryTask(info, this, registry.config().clock());
    final Deque<MemoryTask> queue = pendings.get(task.getPriority());
    final int c;

    putLock.lockInterruptibly();
    try {
      // Note that count is used in wait guard even though it is not protected by lock. This works
      // because count can only decrease at this point (all other puts are shut out by lock), and we
      // (or some other waiting put) are signalled if it ever changes from capacity. Similarly for
      // all other uses of count in other wait guards.
      while (remainingCapacity() <= 0) {
        notFull.await();
      }
      queue.addLast(task);
      if (count.incrementAndGet() < capacity) {
        notFull.signal();
      }
      c = pendingCount.getAndIncrement();
    } finally {
      putLock.unlock();
    }
    if (c == 0) {
      signalNotEmpty();
    }
    queuedCounter.increment();
  }

  @Override
  public boolean offer(TaskInfo info, long timeout, TimeUnit unit) throws InterruptedException {
    final MemoryTask task = new MemoryTask(info, this, registry.config().clock());
    final Deque<MemoryTask> queue = pendings.get(task.getPriority());
    long duration = unit.toNanos(timeout);
    final int c;

    putLock.lockInterruptibly();
    try {
      while (remainingCapacity() <= 0) {
        if (duration <= 0L) {
          return false;
        }
        duration = notFull.awaitNanos(duration);
      }
      queue.addLast(task);
      if (count.incrementAndGet() < capacity) {
        notFull.signal();
      }
      c = pendingCount.getAndIncrement();
    } finally {
      putLock.unlock();
    }
    if (c == 0) {
      signalNotEmpty();
    }
    queuedCounter.increment();
    return true;
  }

  @Override
  public boolean offer(TaskInfo info) {
    if (pendingCount.get() == capacity) {
      return false;
    }
    final MemoryTask task = new MemoryTask(info, this, registry.config().clock());
    final Deque<MemoryTask> queue = pendings.get(task.getPriority());
    final int c;

    putLock.lock();
    try {
      if (pendingCount.get() >= capacity) {
        return false;
      }
      queue.addLast(task);
      if (count.incrementAndGet() < capacity) {
        notFull.signal();
      }
      c = pendingCount.getAndIncrement();
    } finally {
      putLock.unlock();
    }
    if (c == 0) {
      signalNotEmpty();
    }
    queuedCounter.increment();
    return true;
  }

  @Override
  public Task take() throws InterruptedException {
    final MemoryTask task;

    takeLock.lockInterruptibly();
    try {
      while (pendingCount.get() == 0) {
        notEmpty.await();
      }
      // cannot return null since pendingCount is not 0, we are looking at all priority queues,
      // and we are locked
      task = pollFirst(MemorySiteQueue.MIN_PRIORITY);
      if (pendingCount.getAndDecrement() > 1) { // still some pending ones so let others know
        notEmpty.signal();
      }
    } finally {
      takeLock.unlock();
    }
    return task;
  }

  @Nullable
  @Override
  public Task poll(long timeout, TimeUnit unit) throws InterruptedException {
    return poll(MemorySiteQueue.MIN_PRIORITY, timeout, unit);
  }

  /**
   * Retrieves and removes the next available highest priority task with a priority equal or greater
   * than the specified one, waiting up to the specified wait time if necessary for one to become
   * available. The returned task is considered locked by the current thread/worker.
   *
   * @param priority the minimum priority for which to retrieve a task (0 to 9)
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return the next available task with the highest priority from this queue or <code>null</code>
   *     if the specified amount time elapsed before a task is available
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   */
  @Nullable
  Task poll(int priority, long timeout, TimeUnit unit) throws InterruptedException {
    final MemoryTask task;
    long duration = unit.toNanos(timeout);

    takeLock.lockInterruptibly();
    try {
      while (pendingCount.get() == 0) {
        if (duration <= 0L) {
          return null;
        }
        duration = notEmpty.awaitNanos(duration);
      }
      // cannot return null since pendingCount is not 0, we are looking at all priority queues,
      // and we are locked
      task = pollFirst(priority);
      if (pendingCount.getAndDecrement() > 1) { // still some pending ones so let others know
        notEmpty.signal();
      }
    } finally {
      takeLock.unlock();
    }
    return task;
  }

  /**
   * Called by {@link MemoryTask} when the task's owner indicates the task should be removed from
   * the queue.
   *
   * @param task the task that should be removed
   * @param whenRemoved a callback that will be called right after having removed the task from the
   *     queue (it is expected that the task durations will be calculated at that point)
   * @throws InterruptedException if the current thread is interrupted while attempting to remove
   *     the active task
   */
  void remove(MemoryTask task, Runnable whenRemoved) throws InterruptedException {
    int c;

    takeLock.lockInterruptibly();
    try {
      active.remove(task);
      activeCount.decrementAndGet();
      c = count.getAndDecrement();
      whenRemoved.run();
      timer.record(task.getDuration().toMillis(), TimeUnit.MILLISECONDS);
      pendingTimers
          .get(task.getPriority())
          .record(task.getPendingDuration().toMillis(), TimeUnit.MILLISECONDS);
      activeTimer.record(task.getActiveDuration().toMillis(), TimeUnit.MILLISECONDS);
    } finally {
      takeLock.unlock();
    }
    if (c == capacity) {
      signalNotFull();
    }
    (task.hasFailed() ? failCounter : successCounter).increment();
  }

  /**
   * Called by {@link MemoryTask} when the task's owner indicates the task should be retried later.
   *
   * @param task the task that needs to be re-queued
   * @param atFront <code>true</code> to re-queue the task at the front of the queue; <code>false
   *     </code> to re-queue at the end
   * @param whenQueued a callback that will be called just before re-inserting the task in the queue
   * @throws InterruptedException if the current thread is interrupted while attempting to re-queue
   *     the active task
   */
  void requeue(MemoryTask task, boolean atFront, Runnable whenQueued) throws InterruptedException {
    takeLock.lockInterruptibly();
    // start by removing it from the active list
    try {
      active.remove(task);
      activeCount.decrementAndGet();
    } finally {
      takeLock.unlock();
    }
    // now re-insert it at the end or front of the queue
    final Deque<MemoryTask> queue = pendings.get(task.getPriority());
    final int c;

    putLock.lock(); // don't allow interruption here as we want to complete the operation
    try {
      whenQueued.run();
      if (atFront) {
        queue.addFirst(task);
      } else {
        queue.addLast(task);
      }
      c = pendingCount.getAndIncrement();
      if (count.get() < capacity) {
        notFull.signal();
      }
    } finally {
      putLock.unlock();
    }
    if (c == 0) {
      signalNotEmpty();
    }
    requeuedCounter.increment();
  }

  @VisibleForTesting
  IntStream pendingSizes() {
    return pendings.stream().mapToInt(Deque::size);
  }

  @VisibleForTesting
  int pendingSize(int priority) {
    return pendings.get(priority).size();
  }

  @VisibleForTesting
  @Nullable
  MemoryTask peekFirst() {
    if (count.get() == 0) {
      return null;
    }
    takeLock.lock();
    try {
      if (count.get() > 0) {
        for (int i = MemorySiteQueue.MAX_PRIORITY; i >= MemorySiteQueue.MIN_PRIORITY; i--) {
          final MemoryTask task = pendings.get(i).peekFirst();

          if (task != null) {
            return task;
          }
        }
      }
      return null;
    } finally {
      takeLock.unlock();
    }
  }

  @VisibleForTesting
  @Nullable
  MemoryTask peekLast() {
    if (count.get() == 0) {
      return null;
    }
    takeLock.lock();
    try {
      if (count.get() > 0) {
        for (int i = MemorySiteQueue.MIN_PRIORITY; i <= MemorySiteQueue.MAX_PRIORITY; i++) {
          final MemoryTask task = pendings.get(i).peekLast();

          if (task != null) {
            return task;
          }
        }
      }
      return null;
    } finally {
      takeLock.unlock();
    }
  }

  @VisibleForTesting
  boolean waitForOfferorsToBeBlocked(long timeout) throws InterruptedException {
    final long start = System.nanoTime();
    final long nanos = TimeUnit.MILLISECONDS.toNanos(timeout);

    while (true) {
      putLock.lockInterruptibly();
      try {
        if (putLock.hasWaiters(notFull)) {
          return true;
        } else if ((System.nanoTime() - start) >= nanos) {
          return false;
        }
        Thread.onSpinWait();
      } finally {
        putLock.unlock();
      }
    }
  }

  @VisibleForTesting
  boolean waitForPullersToBeBlocked(long timeout) throws InterruptedException {
    final long start = System.nanoTime();
    final long nanos = TimeUnit.MILLISECONDS.toNanos(timeout);

    while (true) {
      takeLock.lockInterruptibly();
      try {
        if (takeLock.hasWaiters(notEmpty)) {
          return true;
        } else if ((System.nanoTime() - start) >= nanos) {
          return false;
        }
        Thread.onSpinWait();
      } finally {
        takeLock.unlock();
      }
    }
  }

  /**
   * Retrieves and removes the first task available from the highest priority queue which is greater
   * or equal to the specified priority.
   *
   * <p><i>Note:</i> It is assumed that the caller of this method has acquired the <code>taskLock
   * </code> prior to calling this method and that at least one task is available in the queue (
   * <code>count > 0</code>).
   *
   * @param priority the minimum priority for which to remove a task (0 to 9)
   * @return the first available highest priority task with a priority no less than the specified
   *     priority (task will be locked to the current thread) or <code>null</code> if no tasks are
   *     available at or above the specified priority
   */
  private MemoryTask pollFirst(int priority) {
    for (int i = MemorySiteQueue.MAX_PRIORITY; i >= priority; i--) {
      final MemoryTask task = pendings.get(i).pollFirst();

      if (task != null) {
        active.add(task);
        activeCount.getAndIncrement();
        return task.lock();
      }
    }
    return null;
  }

  /**
   * Signals a waiting take. Called only from put/offer (which do not otherwise ordinarily lock
   * takeLock.)
   */
  private void signalNotEmpty() {
    takeLock.lock();
    try {
      notEmpty.signal();
    } finally {
      takeLock.unlock();
    }
  }

  /** Signals a waiting put. Called only from take/poll. */
  private void signalNotFull() {
    putLock.lock();
    try {
      notFull.signal();
    } finally {
      putLock.unlock();
    }
  }
}
