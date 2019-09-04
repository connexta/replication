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
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.TaskInfo;
import com.connexta.replication.api.impl.queue.AbstractTask;
import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

/** Provides an in-memory implement for the {@link Task} interface. */
public class MemoryTask extends AbstractTask {
  private final byte priority;
  private final Clock clock;
  private final MemorySiteQueue queue;

  private volatile int attempts = 1;

  private volatile State state = State.PENDING;

  /**
   * The error code associated with the completion of this task. Will be <code>null</code> until it
   * fails at which point it will be used to tracked the reason for its failure. Will be reset to
   * <code>null</code> when picked up for processing again.
   */
  @Nullable private volatile ErrorCode code = null;

  @Nullable private volatile String reason = null;

  private final Instant originalQueuedTime;
  private volatile long queueTime;

  // use to track duration - all in nanoseconds
  private volatile long startTime;
  private volatile Duration duration = Duration.ZERO;
  private volatile Duration pendingDuration = Duration.ZERO;
  private volatile Duration activeDuration = Duration.ZERO;

  private final ReentrantLock lock = new ReentrantLock();
  @Nullable private volatile Thread owner = null;

  /**
   * Creates a new task for the corresponding task information as it is being queued to a given
   * queue.
   *
   * @param info the corresponding task info
   * @param queue the queue where the task is being queued.
   * @param clock the clock to use for retrieving wall and monotonic times
   */
  MemoryTask(TaskInfo info, MemorySiteQueue queue, Clock clock) {
    super(info);
    this.priority =
        (byte)
            Math.min(
                Math.max(info.getPriority(), MemorySiteQueue.MIN_PRIORITY),
                MemorySiteQueue.MAX_PRIORITY);
    this.clock = clock;
    this.queue = queue;
    final long now = clock.wallTime();

    this.startTime = clock.monotonicTime();
    this.queueTime = now;
    this.originalQueuedTime = Instant.ofEpochMilli(now);
  }

  @Override
  public byte getPriority() {
    return priority;
  }

  @Override
  public int getTotalAttempts() {
    return attempts;
  }

  @Override
  public State getState() {
    return state;
  }

  /**
   * Gets the optional error code indicating why the last processing of the task failed.
   *
   * <p><i>Note:</i> A task that fails for reasons that allow for it to be retried later will report
   * an error until the task is picked up again by a worker for a subsequent attempt, at which point
   * the error will be cleared automatically.
   *
   * @return the error code associated with the last processing attempt or empty if the processing
   *     of the task completed successfully or if it is still being processed
   */
  public Optional<ErrorCode> getCode() {
    return Optional.ofNullable(code);
  }

  /**
   * Gets the optional reason providing additional information to the error code (see {@link
   * #getCode()}) as to why the last processing of the task failed.
   *
   * <p><i>Note:</i> The returned value might be cleared right after calling this method if the task
   * is being picked up for a subsequent attempt.
   *
   * @return the reason associated with the last failed processing attempt or empty if the
   *     processing of the task completed successfully if it is still being processed or if no
   *     reason for the failure was provided
   */
  public Optional<String> getReason() {
    return Optional.ofNullable(reason);
  }

  @Override
  public Instant getOriginalQueuedTime() {
    return originalQueuedTime;
  }

  @Override
  public Instant getQueuedTime() {
    return Instant.ofEpochMilli(queueTime);
  }

  @Override
  public Duration getDuration() {
    final long s = this.startTime;
    final Duration d = this.duration;

    switch (state) {
      case PENDING:
      case ACTIVE:
        return d.plusNanos(clock.monotonicTime() - s);
      case FAILED:
      case SUCCESSFUL:
      default:
        return d;
    }
  }

  @Override
  public Duration getPendingDuration() {
    final long s = this.startTime;
    final Duration d = this.pendingDuration;

    switch (state) {
      case PENDING:
        return d.plusNanos(clock.monotonicTime() - s);
      case ACTIVE:
      case FAILED:
      case SUCCESSFUL:
      default:
        return d;
    }
  }

  @Override
  public Duration getActiveDuration() {
    final long s = this.startTime;
    final Duration d = this.activeDuration;

    switch (state) {
      case ACTIVE:
        return d.plusNanos(clock.monotonicTime() - s);
      case PENDING:
      case FAILED:
      case SUCCESSFUL:
      default:
        return d;
    }
  }

  @Override
  public boolean isLocked() {
    return lock.isHeldByCurrentThread();
  }

  /**
   * Gets the thread that is currently processing this task.
   *
   * <p><i>Note:</i> This is a best effort since ownership can change at any point in time.
   *
   * @return the thread currently processing the task or empty if the task is not currently being
   *     processed
   */
  public Optional<Thread> getOwner() {
    return Optional.ofNullable(owner);
  }

  @Override
  public void unlock() throws InterruptedException {
    verifyCompletionAndOwnership();
    // requeue in front of the queue and leave queue time as is since the owner didn't work on it
    // and is just unlocking it for another worker to pick it up
    queue.requeue(this, true, () -> requeued(null, null));
  }

  @Override
  public void complete() throws InterruptedException {
    verifyCompletionAndOwnership();
    queue.remove(this, () -> removed(State.SUCCESSFUL, null, null));
  }

  @Override
  public void fail(ErrorCode code) throws InterruptedException {
    fail(code, null);
  }

  @Override
  public void fail(ErrorCode code, @Nullable String reason) throws InterruptedException {
    verifyCompletionAndOwnership();
    if (code.shouldBeRetried()) {
      // re-queue at end of the queue and update it once it is queued
      queue.requeue(this, false, () -> requeued(code, reason));
    } else { // done so just remove the task and mark it complete
      queue.remove(this, () -> removed(State.FAILED, code, reason));
    }
  }

  /**
   * Called by the associated {@link MemorySiteQueue} to lock this task.
   *
   * @return this for chaining
   */
  MemoryTask lock() {
    final long now = clock.monotonicTime();
    final long d = now - startTime;

    this.state = State.ACTIVE;
    this.code = null;
    this.reason = null;
    this.startTime = now;
    this.duration = duration.plusNanos(d);
    this.pendingDuration = pendingDuration.plusNanos(d);
    lock.lock();
    this.owner = Thread.currentThread();
    return this;
  }

  @VisibleForTesting
  TaskInfo getInfo() {
    return info;
  }

  /**
   * Callback passed to the {@link MemorySiteQueue} when removing a task to update the internal
   * state of the task after the task was actually removed from the queue.
   *
   * @param state the new state to record
   * @param code the new error code to record or <code>null</code> if there was no failure
   * @param reason a new reason to record or <code>null</code> if none provided
   */
  private void removed(State state, @Nullable ErrorCode code, @Nullable String reason) {
    final long now = clock.monotonicTime();
    final long d = now - startTime;

    this.state = state;
    this.code = code;
    this.reason = reason;
    this.startTime = 0L;
    this.duration = duration.plusNanos(d);
    this.activeDuration = activeDuration.plusNanos(d);
    lock.unlock(); // should not fail since we already checked in the complete() and fail() methods
    this.owner = null;
  }

  /**
   * Callback passed to the {@link MemorySiteQueue} when re-queuing a task to update the internal
   * state of the task after the task is about to be re-added to the queue.
   *
   * @param code the new error code to record or <code>null</code> if there was no failure
   * @param reason a new reason to record or <code>null</code> if none provided
   */
  private void requeued(@Nullable ErrorCode code, @Nullable String reason) {
    final long now = clock.monotonicTime();
    final long d = now - startTime;

    this.state = State.PENDING;
    this.code = code;
    this.reason = reason;
    if (code != null) {
      // only increment attempt and update queue time if it is requeued because of error
      this.attempts++;
      this.queueTime = clock.wallTime();
    }
    this.startTime = now;
    this.duration = duration.plusNanos(d);
    this.activeDuration = activeDuration.plusNanos(d);
    lock.unlock(); // should not fail since we already checked in the unlock() and fail() methods
    this.owner = null;
  }

  private void verifyCompletionAndOwnership() {
    if (wasSuccessful()) {
      throw new IllegalStateException("task already completed successfully");
    } else if (hasFailed()) {
      throw new IllegalStateException("task already failed");
    } else if (Thread.currentThread() != owner) {
      throw new IllegalMonitorStateException();
    }
  }
}
