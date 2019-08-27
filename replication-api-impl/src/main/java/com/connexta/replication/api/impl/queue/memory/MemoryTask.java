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
import com.connexta.replication.api.queue.QueueException;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

/** Provides an in-memory implement for the {@link Task} interface. */
public class MemoryTask extends AbstractTask {
  private final MemorySiteQueue queue;
  private final Instant originalQueuedTime;
  private volatile Instant queueTime;
  private volatile int attempts;
  private final ReentrantLock lock = new ReentrantLock();
  @Nullable private volatile Thread owner = null;

  /** Flag indicating if the task was completed either successfully or failed. */
  private volatile boolean completed;

  /**
   * The error code associated with the completion of this task. Will be <code>null</code> until it
   * fails at which point it will be used to tracked the reason for its failure.
   */
  @Nullable private volatile ErrorCode code;

  @Nullable private volatile String reason;

  /**
   * Creates a new task for the corresponding task information as it is being queued to a given
   * queue.
   *
   * @param info the corresponding task info
   * @param queue the queue where the task is being queued.
   */
  MemoryTask(TaskInfo info, MemorySiteQueue queue) {
    super(info);
    this.queue = queue;
    this.queueTime = Instant.now();
    this.originalQueuedTime = queueTime;
    this.attempts = 1;
    this.completed = false;
    this.code = null;
    this.reason = null;
  }

  @Override
  public Instant getOriginalQueuedTime() {
    return originalQueuedTime;
  }

  @Override
  public Instant getQueuedTime() {
    return queueTime;
  }

  @Override
  public int getTotalAttempts() {
    return attempts;
  }

  @Override
  public boolean isLocked() {
    return lock.isHeldByCurrentThread();
  }

  @Override
  public void unlock() throws InterruptedException {
    if (completed) {
      throw new IllegalStateException("task already completed or failed");
    }
    if (Thread.currentThread() != owner) {
      throw new IllegalMonitorStateException();
    }
    // requeue in front of the queue and leave queue time as is since the owner didn't work on it
    // and is just unlocking it for another worker to pick it up
    queue.requeue(this, true, lock::unlock);
  }

  @Override
  public void complete() throws QueueException, InterruptedException {
    if (completed) {
      throw new IllegalStateException("task already completed or failed");
    }
    if (Thread.currentThread() != owner) {
      throw new IllegalMonitorStateException();
    }
    queue.remove(this);
    this.completed = true;
    this.code = null;
    this.reason = null;
    lock.unlock(); // should not fail since we already checked above
    this.owner = null;
  }

  @Override
  public void fail(ErrorCode code) throws QueueException, InterruptedException {
    fail(code, null);
  }

  @Override
  public void fail(ErrorCode code, @Nullable String reason)
      throws QueueException, InterruptedException {
    if (completed) {
      throw new IllegalStateException("task already completed or failed");
    }
    if (Thread.currentThread() != owner) {
      throw new IllegalMonitorStateException();
    }
    if (code.shouldBeRetried()) { // re-queue at end of the queue and update it once it is queued
      queue.requeue(this, false, () -> requeued(code, reason));
    } else { // done so just remove the task and mark it complete
      queue.remove(this);
      this.completed = true;
      this.code = code;
      this.reason = reason;
      lock.unlock(); // should not fail since we already checked above
      this.owner = null;
    }
  }

  /**
   * Called by the associated {@link MemorySiteQueue} to lock this task.
   *
   * @return this for chaining
   */
  MemoryTask lock() {
    lock.lock();
    this.owner = Thread.currentThread();
    this.completed = false;
    this.code = null;
    this.reason = null;
    return this;
  }

  /**
   * Callback passed to the {@link MemorySiteQueue} when re-queuing a failed task to update the
   * internal state of the task after the task is about to be re-added to the queue.
   *
   * @param code the error code for the failure
   * @param reason a reason associated with the failure (used for traceability)
   */
  private void requeued(ErrorCode code, @Nullable String reason) {
    this.completed = false;
    this.code = code;
    this.reason = reason;
    this.queueTime = Instant.now();
    this.attempts++;
    lock.unlock(); // should not fail since we already checked above as it is assumed that the
    //                fail() method would have first checked
    this.owner = null;
  }
}
