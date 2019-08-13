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
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * A queue broker defines the point of entry for retrieving references to queues and for monitoring
 * multiple queues at the same time for tasks to be processed.
 */
public interface QueueBroker {
  /**
   * Gets a queue for a given site.
   *
   * <p>A new queue should be deployed if no queue currently exist for the specified site.
   *
   * @param site the site for which to get a queue
   * @return the queue to use for the specified site
   * @throws QueueException if an error occurred while performing the operation
   */
  public Queue getQueue(String site) throws QueueException;

  /**
   * Retrieves the next available highest priority task from any of the specified queues, waiting if
   * necessary until a task becomes available. The returned task is considered locked by the current
   * thread/worker.
   *
   * @param queues the set of queues to monitor in order to retrieve the next available high
   *     priority task
   * @return the next available task with the highest priority from one of the specified queue
   * @throws IllegalArgumentException if no queues are specified
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   * @throws QueueException if an error occurred while performing the operation
   */
  public Task take(Queue... queues) throws QueueException, InterruptedException;

  /**
   * Retrieves the next available highest priority task from any of the specified queues, waiting if
   * necessary until a task becomes available. The returned task is considered locked by the current
   * thread/worker.
   *
   * @param queues the set of queues to monitor in order to retrieve the next available high
   *     priority task
   * @return the next available task with the highest priority from one of the specified queue
   * @throws IllegalArgumentException if no queues are specified
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   * @throws QueueException if an error occurred while performing the operation
   */
  public Task take(Stream<Queue> queues) throws QueueException, InterruptedException;

  /**
   * Retrieves and removes the next available highest priority task from any of the specified
   * queues, waiting up to the specified wait time if necessary for a task to become available. The
   * returned task is considered locked by the current thread/worker.
   *
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @param queues the set of queues to monitor in order to retrieve the next available high
   *     priority task
   * @return the next available task with the highest priority from this queue or <code>null</code>
   *     if the specified amount time elapsed before a task is available
   * @throws IllegalArgumentException if no queues are specified
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   * @throws QueueException if an error occurred while performing the operation
   */
  @Nullable
  public Task poll(long timeout, TimeUnit unit, Queue... queues)
      throws QueueException, InterruptedException;

  /**
   * Retrieves and removes the next available highest priority task from any of the specified
   * queues, waiting up to the specified wait time if necessary for a task to become available. The
   * returned task is considered locked by the current thread/worker.
   *
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @param queues the set of queues to monitor in order to retrieve the next available high
   *     priority task
   * @return the next available task with the highest priority from this queue or <code>null</code>
   *     if the specified amount time elapsed before a task is available
   * @throws IllegalArgumentException if no queues are specified
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   * @throws QueueException if an error occurred while performing the operation
   */
  @Nullable
  public Task poll(long timeout, TimeUnit unit, Stream<Queue> queues)
      throws QueueException, InterruptedException;
}
