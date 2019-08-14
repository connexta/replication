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
import javax.annotation.Nullable;

/**
 * Base interface for all type of queues ({@link SiteQueue} and {@link CompositeQueue}) on which
 * tasks can be retrieved
 */
public interface Queue {
  /**
   * Gets the broker used to retrieved this queue.
   *
   * @return the associated broker
   */
  public QueueBroker getBroker();

  /**
   * Retrieves the next available highest priority task, waiting if necessary until one becomes
   * available. The returned task is considered locked by the current thread/worker.
   *
   * @return the next available task with the highest priority from this queue
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   * @throws QueueException if an error occurred while performing the operation
   */
  public Task take() throws QueueException, InterruptedException;

  /**
   * Retrieves and removes the next available highest priority task, waiting up to the specified
   * wait time if necessary for one to become available. The returned task is considered locked by
   * the current thread/worker.
   *
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return the next available task with the highest priority from this queue or <code>null</code>
   *     if the specified amount time elapsed before a task is available
   * @throws InterruptedException if the current thread was interrupted while waiting for a task to
   *     be returned
   * @throws QueueException if an error occurred while performing the operation
   */
  @Nullable
  public Task poll(long timeout, TimeUnit unit) throws QueueException, InterruptedException;
}
