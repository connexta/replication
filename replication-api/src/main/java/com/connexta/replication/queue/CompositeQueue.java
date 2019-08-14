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

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A composite queue is an artifact created around multiple {@link SiteQueue}s allowing a worker to
 * retrieve tasks from any of the queues based on task priorities.
 */
// TODO: This interface will move out of the API into the implementation once we start working on
// this ticket
public interface CompositeQueue extends Queue {
  /**
   * Gets all site queues that are compounded together.
   *
   * @return a stream of all site queues compounded together (never empty)
   */
  public Stream<SiteQueue> queues();

  /**
   * Gets all sites that have their queues compounded together.
   *
   * @return a stream of all site ids that have their queues compounded together (never empty)
   */
  public Stream<String> sites();

  /**
   * Gets a compounded queue for a given site.
   *
   * @param site the site for which to get a queue that was compounded
   * @return the site queue to use for the specified site or empty if no queue was not compounded
   *     for the specified site
   */
  public Optional<SiteQueue> getQueue(String site);
}
