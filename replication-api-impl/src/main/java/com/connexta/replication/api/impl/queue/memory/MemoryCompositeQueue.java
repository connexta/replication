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

import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.queue.QueueBroker;
import com.connexta.replication.api.queue.QueueException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * A composite queue is an artifact created around multiple site queues allowing a worker to
 * retrieve tasks from any of the queues based on task priorities.
 */
public class MemoryCompositeQueue implements MemoryQueue {
  private final MemoryQueueBroker broker;
  private final Set<String> sites;

  public MemoryCompositeQueue(MemoryQueueBroker broker, Stream<String> sites) {
    this.broker = broker;
    this.sites = sites.collect(Collectors.toSet());
  }

  @Override
  public QueueBroker getBroker() {
    return broker;
  }

  /**
   * Gets all site queues that are compounded together.
   *
   * @return a stream of all site queues compounded together (never empty)
   */
  public Stream<MemorySiteQueue> queues() {
    return sites.stream().map(broker::getQueueIfDefined).flatMap(Optional::stream);
  }

  /**
   * Gets all sites that have their queues compounded together.
   *
   * @return a stream of all site ids that have their queues compounded together (never empty)
   */
  public Stream<String> sites() {
    return sites.stream();
  }

  /**
   * Gets a compounded queue for a given site.
   *
   * @param site the site for which to get a queue that was compounded
   * @return the site queue to use for the specified site or empty if no queue was compounded for
   *     the specified site
   */
  public Optional<MemorySiteQueue> getQueue(String site) {
    return sites.contains(site) ? broker.getQueueIfDefined(site) : Optional.empty();
  }

  @Override
  public int size() {
    return queues().mapToInt(MemorySiteQueue::size).sum();
  }

  @Override
  public int pendingSize() {
    return queues().mapToInt(MemorySiteQueue::pendingSize).sum();
  }

  @Override
  public int activeSize() {
    return queues().mapToInt(MemorySiteQueue::activeSize).sum();
  }

  @Override
  public int remainingCapacity() {
    return queues().mapToInt(MemorySiteQueue::remainingCapacity).sum();
  }

  @Override
  public Task take() throws InterruptedException {
    throw new QueueException("not yet supported");
  }

  @Nullable
  @Override
  public Task poll(long timeout, TimeUnit unit) throws InterruptedException {
    throw new QueueException("not yet supported");
  }
}
