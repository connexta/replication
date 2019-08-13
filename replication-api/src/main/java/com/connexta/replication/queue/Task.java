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
package com.connexta.replication.queue;

import java.util.concurrent.TimeUnit;

/**
 * Represents a task that was queued for later processing by a worker.
 *
 * <p>When a task is first queued, its total attempts counter will report <code>0</code> and its
 * queued time and original queued time will be set to the same time (i.e. the current time when the
 * task is queued via one of {@link Queue#offer(TaskInfo)}), {@link Queue#offer(TaskInfo, long,
 * TimeUnit)}, or {@link Queue#put(TaskInfo)}. If the task fails and a decision is made to put it
 * back into the queue via one of {@link #offerForRetry(PriorityLevel)}, {@link
 * #offerForRetry(PriorityLevel, long, TimeUnit)}, or {@link #putForRetry(PriorityLevel)}, a new
 * task will be created with its total attempts counter increased by <code>1</code>. The original
 * queued time will be copied from this task and its queued time will be set to the current time
 * when the new created task will be queued.
 */
public interface Task extends TaskInfo {
  /**
   * Gets the queue from which this task was retrieved.
   *
   * @return the queue from which this task was retrieved
   */
  public Queue getQueue();

  /**
   * Gets the time when the first attempted task was queued.
   *
   * @return the time when the first attempted task was queued in milliseconds from the epoch
   */
  public long getOriginalQueuedTime();

  /**
   * Gets the time when the task was queued. This will be the same time as reported by {@link
   * #getOriginalQueuedTime()} if the task has not yet been attempted ({@link #getTotalAttempts()}
   * returns <code>0</code>).
   *
   * @return the time when this task was queued in milliseconds from the epoch
   */
  public long getQueuedTime();

  /**
   * Gets the total number of times this particular task has been attempted. When a task fails for a
   * reason that doesn't preclude it from being re-tried and it gets re-queued by the worker, its
   * total attempts counter will automatically be incremented as it is re-added to the associated
   * queue for later reprocessing.
   *
   * @return the total number of times this task has been attempted so far
   */
  public int getTotalAttempts();

  /**
   * Checks if this task is still locked by the current thread/worker. A task will automatically be
   * unlocked once one of {@link #unlock()}, {@link #complete()}, {@link #fail()}, {@link
   * #offerForRetry(PriorityLevel)}, {@link #offerForRetry(PriorityLevel, long, TimeUnit)}, or
   * {@link #putForRetry(PriorityLevel)} has been called on it.
   *
   * @return <code>true</code> if the current thread/worker owns the lock to this task; <code>false
   *     </code> otherwise
   */
  public boolean isLocked();

  /**
   * Attempts to unlock this task. An unlock task will be picked up by the next worker attempting to
   * poll for a task from the associated queue.
   *
   * @throws IllegalStateException if the current thread/worker doesn't own the lock to the task or
   *     if the task is not current locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws QueueException if an error occurred while performing the operation
   */
  public void unlock() throws QueueException, InterruptedException;

  /**
   * Reports the successful completion of this task. The task will automatically be unlocked and
   * removed from the associated queue.
   *
   * @throws IllegalStateException if the current thread/worker doesn't own the lock to the task or
   *     if the task is not currently or no longer locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws QueueException if an error occurred while performing the operation
   */
  public void complete() throws QueueException, InterruptedException;

  /**
   * Reports the failed completion of this task. The task will automatically be unlocked and removed
   * from the associated queue. Such a task will not be re-queued for another attempt at being
   * processed.
   *
   * @throws IllegalStateException if the current thread/worker doesn't own the lock to the task or
   *     if the task is not currently or no longer locked
   * @throws InterruptedException if the thread was interrupted while attempting to unlock the task
   * @throws QueueException if an error occurred while performing the operation
   */
  public void fail() throws QueueException, InterruptedException;

  /**
   * Re-queues this task with the specified priority level into the associated queue for another
   * attempt at being processed by re-inserting it into this queue if it is possible to do so
   * immediately without violating capacity restrictions. This will typically be called by a worker
   * after a failure occurs which permits another attempt at processing the task.
   *
   * @param level the new level for the new re-queued task
   * @return <code>true</code> if the task was removed and re-added to this queue (only then will
   *     the task be unlocked); <code>false</code> otherwise (the task is still considered locked)
   * @throws IllegalStateException if the current thread/worker doesn't own the lock to the task or
   *     if the task is not currently or no longer locked
   * @throws QueueException if an error occurred while performing the operation
   */
  public boolean offerForRetry(PriorityLevel level) throws QueueException;

  /**
   * Re-queues this task with the given priority into the associated queue, waiting up to the
   * specified maximum amount of time if necessary for space to become available. This will
   * typically be called by a worker after a failure occurs which permits another attempt at
   * processing the task.
   *
   * @param level the new level for the new re-queued task
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return <code>true</code> if the task was re-added to this queue (only then will the task be
   *     unlocked); <code>false</code> otherwise (the task is still considered locked)
   * @throws InterruptedException if the current thread was interrupted while waiting to re-insert
   *     the task
   * @throws QueueException if an error occurred while performing the operation
   */
  public boolean offerForRetry(PriorityLevel level, long timeout, TimeUnit unit)
      throws QueueException, InterruptedException;

  /**
   * Re-queues this task with the given priority into the associated queue, waiting if necessary for
   * space to become available. This will typically be called by a worker after a failure occurs
   * which permits another attempt at processing the task. The task will be considered unlocked upon
   * normal return rom this method
   *
   * @throws InterruptedException if the current thread was interrupted while waiting to re-insert
   *     the task
   * @throws QueueException if an error occurred while performing the operation
   */
  public void putForRetry(PriorityLevel level) throws QueueException, InterruptedException;
}
