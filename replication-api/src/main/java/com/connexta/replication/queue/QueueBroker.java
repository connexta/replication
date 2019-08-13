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

import java.util.stream.Stream;

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
   * @return the site queue to use for the specified site
   * @throws QueueException if an error occurred while performing the operation
   */
  public SiteQueue getQueue(String site) throws QueueException;

  /**
   * Gets a compounded queue for the specified set of sites. Polling from the returned queue will
   * attempt to find the first available task based on priority level. There is no priority
   * established between sites.
   *
   * <p>New queues should be deployed if none currently exist for a given site.
   *
   * @param sites the sites for which to compound all their corresponding queues
   * @return a queue object which compounds all specified site queues together
   * @throws QueueException if an error occurred while performing the operation
   */
  public CompoundQueue getQueues(String... sites) throws QueueException;

  /**
   * Gets a compounded queue for the specified set of sites. Polling from the returned queue will
   * attempt to find the first available task based on priority level. There is no priority
   * established between sites.
   *
   * <p>New queues should be deployed if none currently exist for a given site.
   *
   * @param sites the sites for which to compound all their corresponding queues
   * @return a queue object which compounds all specified site queues together
   * @throws QueueException if an error occurred while performing the operation
   */
  public CompoundQueue getQueues(Stream<String> sites) throws QueueException;
}
