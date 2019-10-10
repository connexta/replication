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
package com.connexta.replication.api.data;

import com.connexta.replication.api.queue.QueueException;
import com.connexta.replication.api.queue.SiteQueue;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Represents a task that was queued for later processing by a worker.
 *
 * <p>When a task is first queued, its total attempts counter will report <code>0</code> and its
 * queued time and original queued time will be set to the same time (i.e. the current time when the
 * task is queued via one of {@link SiteQueue#offer(TaskInfo)}), {@link SiteQueue#offer(TaskInfo,
 * long, TimeUnit)}, or {@link SiteQueue#put(TaskInfo)}. If the task fails and a decision is made by
 * the queue broker to put it back into the queue, a new task will be created with its total
 * attempts counter increased by <code>1</code>. The original queued time will be copied from this
 * task and its queued time will be set to the current time when the new created task will be
 * queued.
 */
public interface Task extends TaskInfo {
  /**
   * Gets the number of times this particular task has been attempted. When a task fails for a
   * reason that doesn't preclude it from being re-tried and it gets re-queued by the worker, its
   * total attempts counter will automatically be incremented as it is re-added to the associated
   * queue for later reprocessing.
   *
   * <p><i>Note:</i> This method will return <code>1</code> when first queued to indicate it is in
   * its first attempt.
   *
   * @return the total number of times this task has been attempted so far
   */
  public int getTotalAttempts();

  /**
   * Gets the current state of the task.
   *
   * @return the current task state
   */
  public State getState();

  /**
   * Returns the queue that this task is in.
   *
   * @return the site queue this task is in
   */
  public SiteQueue getQueue();

  /**
   * Checks if this task has been marked completed.
   *
   * <p><i>Note:</i> A task that fails for reasons that allows for it to be retried later will not
   * be reported as completed. It will be reported as completed for any other failure reasons.
   *
   * @return <code>true</code> if this task has been marked completed; <code>false</code> otherwise
   */
  public default boolean isCompleted() {
    final State state = getState();

    return (state == State.SUCCESSFUL) || (state == State.FAILED);
  }

  /**
   * Checks if this task has successfully completed.
   *
   * @return <code>true</code> if this task has successfully completed; <code>false</code> otherwise
   */
  public default boolean wasSuccessful() {
    return (getState() == State.SUCCESSFUL);
  }

  /**
   * Checks if this task has completed with a failure that doesn't allow for it to be retried later.
   *
   * <p><i>Note:</i> A task that fails for reasons that allows for it to be retried later will not
   * be reported as failed.
   *
   * @return <code>true</code> if this task has completed with a failure; <code>false</code>
   *     otherwise
   */
  public default boolean hasFailed() {
    return (getState() == State.FAILED);
  }

  /**
   * Gets the time when the first attempted task was queued.
   *
   * @return the time when the first attempted task was queued from the epoch
   */
  public Instant getOriginalQueuedTime();

  /**
   * Gets the time when the task was queued. This will be the same time as reported by {@link
   * #getOriginalQueuedTime()} if the task has never been requeued after a failed attempt.
   *
   * @return the time when this task was queued from the epoch
   */
  public Instant getQueuedTime();

  /**
   * Gets the total amount of time since this task has been created until it has completed
   * successfully or not.
   *
   * <p><i>Note:</i> The reported value will keep changing if this task has not yet completed.
   *
   * @return the total duration for this task
   */
  public Duration getDuration();

  /**
   * Gets the total amount of time this task has been waiting in queue (not counting the time it was
   * actively being processed).
   *
   * <p><i>Note:</i> The reported value will keep changing if this task is currently pending in
   * queue.
   *
   * @return the total duration this task was in queue waiting
   */
  public Duration getPendingDuration();

  /**
   * Gets the total amount of time this task has been actively being processed.
   *
   * <p><i>Note:</i> The reported value will keep changing if this task is currently being
   * processed.
   *
   * @return the total duration this task was actively being processed
   */
  public Duration getActiveDuration();

  /**
   * Checks if this task is still locked by the current thread/worker. A task will automatically be
   * unlocked once one of {@link #unlock()}, {@link #complete()}, {@link #fail(ErrorCode)}, or
   * {@link #fail(ErrorCode, String)} has been called on it.
   *
   * @return <code>true</code> if the current thread/worker owns the lock to this task; <code>false
   *     </code> otherwise
   */
  public boolean isLocked();

  /**
   * Attempts to unlock this task. An unlocked task will be picked up by the next worker attempting
   * to poll for a task from the associated queue.
   *
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(ErrorCode)}, or {@link #fail(ErrorCode, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws QueueException if an error occurred while performing the operation
   */
  public void unlock() throws InterruptedException;

  /**
   * Reports the successful completion of this task. The task will automatically be unlocked and
   * removed from the associated queue.
   *
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(ErrorCode)}, or {@link #fail(ErrorCode, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently or no longer locked
   * @throws InterruptedException if the thread was interrupted while attempting to mark the task as
   *     completed
   * @throws QueueException if an error occurred while performing the operation
   */
  public void complete() throws InterruptedException;

  /**
   * Reports the failed completion of this task. The task will automatically be unlocked and either
   * removed from or re-queued into the associated queue based on the error code provided. The task
   * will automatically be unlocked from the associated queue upon return.
   *
   * <p>Calling this method will block if the task needs to be re-queued based on the provided error
   * code and there is a need to wait for space to become available on the queue.
   *
   * @param code the error code for the failure
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(ErrorCode)}, or {@link #fail(ErrorCode, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently or no longer locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws QueueException if an error occurred while performing the operation
   */
  public void fail(ErrorCode code) throws InterruptedException;

  /**
   * Reports the failed completion of this task. The task will automatically be unlocked and either
   * removed from or re-queued into the associated queue based on the error code provided. The task
   * will automatically be unlocked from the associated queue upon return.
   *
   * <p>Calling this method will block if the task needs to be re-queued based on the provided error
   * code and there is a need to wait for space to become available on the queue.
   *
   * @param code the error code for the failure
   * @param reason a reason associated with the failure (used for traceability)
   * @throws IllegalStateException if the task was already marked completed via one of {@link
   *     #complete()}, {@link #fail(ErrorCode)}, or {@link #fail(ErrorCode, String)}
   * @throws IllegalMonitorStateException if the current thread/worker doesn't own the lock to the
   *     task or if the task is not currently or no longer locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws QueueException if an error occurred while performing the operation
   */
  public void fail(ErrorCode code, String reason) throws InterruptedException;

  /** The various states a task can be in. */
  public enum State {
    /** Indicates the task is currently in queue awaiting to be picked up to be processed. */
    PENDING,

    /** Indicates the task is currently being processed. */
    ACTIVE,

    /** Indicates the task failed to be processed and will not be retried. */
    FAILED,

    /** Indicates the task processing has successfully completed. */
    SUCCESSFUL,

    /**
     * The unknown value is used for forward compatibility where the current code might not be able
     * to understand a new task state and would map this new site to <code>UNKNOWN</code> and most
     * likely ignore it.
     */
    UNKNOWN
  }
}
