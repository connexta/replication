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
 * A queue which is specific to a site and which defines all possible operations that can be
 * performed. <code>SiteQueue</code>s will hold tasks which will specify intel that either needs to
 * be harvested from, retrieved from, or replicated to. For example:
 *
 * <ul>
 *   <li>a Tactical Ion or DDF-based site we replicate from/to
 *   <li>a DDF-based site we are harvesting from or replicating to
 *   <li>a Regional Ion site to which we are replicating our local intel to
 *   <li>the local Ion site (typically will be used for delete operations only)
 * </ul>
 */
public interface SiteQueue extends Queue {
  /**
   * Gets the identifier of the site this queue is for.
   *
   * @return the site id this queue is for
   */
  public String getSite();

  /**
   * Inserts the specified task with the given priority into this queue if it is possible to do so
   * immediately without violating capacity restrictions.
   *
   * @param task the task to be added to the queue
   * @return <code>true</code> if the task was added to this queue; <code>false</code> otherwise
   * @throws QueueException if an error occurred while performing the operation
   */
  public boolean offer(TaskInfo task) throws QueueException;

  /**
   * Inserts the specified task with the given priority into this queue, waiting up to the specified
   * maximum amount of time if necessary for space to become available.
   *
   * @param task the task to be added to the queue
   * @param timeout how long to wait before giving up, in units of <code>unit</code>
   * @param unit a {@code TimeUnit} determining how to interpret the <code>timeout</code> parameter
   * @return <code>true</code> if the task was added to this queue; <code>false</code> otherwise
   * @throws InterruptedException if the current thread was interrupted while waiting to insert the
   *     task
   * @throws QueueException if an error occurred while performing the operation
   */
  public boolean offer(TaskInfo task, long timeout, TimeUnit unit)
      throws QueueException, InterruptedException;

  /**
   * Inserts the specified task with the given priority into this queue, waiting if necessary for
   * space to become available.
   *
   * @param task the task to be added to the queue
   * @throws InterruptedException if the current thread was interrupted while waiting to insert the
   *     task
   * @throws QueueException if an error occurred while performing the operation
   */
  public void put(TaskInfo task) throws QueueException, InterruptedException;
}
