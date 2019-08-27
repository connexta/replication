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

import com.connexta.replication.api.queue.Queue;

/** Base interface for all in-memory implementations of the {@link Queue} interface. */
public interface MemoryQueue extends Queue {
  /**
   * Gets the number of tasks in this queue.
   *
   * @return the number of tasks in this queue
   */
  public int size();

  /**
   * Gets the number of tasks that are pending in this queue.
   *
   * @return the number of pending tasks in this queue
   */
  public abstract int pendingSize();

  /**
   * Gets the number of tasks that were queued in this queue and that are currently active.
   *
   * @return the number of active tasks in this queue
   */
  public abstract int activeSize();

  /**
   * Gets the number of additional tasks that this queue can ideally (in the absence of memory or
   * resource constraints) accept without blocking. This is always equal to the initial capacity of
   * this queue less the current <code>size</code> of this queue.
   *
   * <p><i>Note:</i> You <em>cannot</em> always tell if an attempt to insert an element will succeed
   * by inspecting {@link #remainingCapacity} because it may be the case that another thread is
   * about to insert or remove an element.
   *
   * @return the current remaining capacity
   */
  public abstract int remainingCapacity();
}
