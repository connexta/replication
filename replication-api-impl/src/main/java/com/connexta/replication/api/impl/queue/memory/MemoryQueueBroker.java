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
import com.connexta.replication.api.queue.QueueBroker;
import com.connexta.replication.api.queue.SiteQueue;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides a non-persistent implementation for the queue broker that keeps queues and tasks in
 * memory.
 */
public class MemoryQueueBroker implements QueueBroker {
  // TODO: need to monitor and poll site configurations for changes such that when sites are removed
  // the queues are disposed. This might result in composite queues that no longer have queues in
  // them
  /** Cache of all queues created so far keyed by the corresponding site id. */
  private final Map<String, MemorySiteQueue> queues = new ConcurrentHashMap<>();

  private final int queueCapacity;

  /**
   * Instantiates a broker to manage queues for sites with the specified maximum task capacity
   * before a queue starts blocking.
   *
   * @param queueCapacity the capacity of the queue this broker manages
   */
  public MemoryQueueBroker(int queueCapacity) {
    this.queueCapacity = queueCapacity;
  }

  @Override
  public SiteQueue getQueue(String site) {
    return getQueue0(site);
  }

  @Override
  @SuppressWarnings(
      "squid:CommentedOutCodeLine" /* will be removed when we support composite queues */)
  public Queue getQueue(String... sites) {
    if (sites.length == 1) {
      return getQueue(sites[0]);
    }
    throw new UnsupportedOperationException("composite queues are not yet supported");
    // return getQueue(Stream.of(sites));
  }

  @Override
  @SuppressWarnings(
      "squid:CommentedOutCodeLine" /* will be removed when we support composite queues */)
  public Queue getQueue(Stream<String> sites) {
    // return new MemoryCompositeQueue(this, sites);
    final Set<String> set = sites.collect(Collectors.toSet());

    if (set.size() == 1) {
      return getQueue(set.iterator().next());
    }
    throw new UnsupportedOperationException("composite queues are not yet supported");
  }

  Optional<MemorySiteQueue> getQueueIfDefined(String site) {
    return Optional.of(queues.get(site));
  }

  private MemorySiteQueue getQueue0(String site) {
    return queues.computeIfAbsent(site, s -> new MemorySiteQueue(this, s, queueCapacity));
  }
}
