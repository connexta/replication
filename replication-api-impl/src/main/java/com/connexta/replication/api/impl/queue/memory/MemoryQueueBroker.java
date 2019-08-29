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
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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

  private final MeterRegistry meterRegistry;

  /**
   * Instantiates a broker to manage queues for sites with the specified maximum task capacity
   * before a queue starts blocking.
   *
   * @param queueCapacity the capacity of the queue this broker manages
   * @param meterRegistry the micrometer registry to report metrics
   */
  public MemoryQueueBroker(int queueCapacity, MeterRegistry meterRegistry) {
    this.queueCapacity = queueCapacity;
    this.meterRegistry = meterRegistry;
  }

  @Override
  public SiteQueue getQueue(String site) {
    return getSiteQueue(site);
  }

  @Override
  public Queue getQueue(String... sites) {
    if (sites.length == 1) {
      return getQueue(sites[0]);
    }
    return getQueue(Stream.of(sites));
  }

  @Override
  public Queue getQueue(Stream<String> sites) {
    return new MemoryCompositeQueue(this, sites);
  }

  /**
   * Gets a queue for a given site.
   *
   * <p>A new queue should be deployed if no queue currently exist for the specified site.
   *
   * @param site the ID of the site for which to get a queue
   * @return the site queue to use for the specified site
   */
  public MemorySiteQueue getSiteQueue(String site) {
    return queues.computeIfAbsent(
        site, s -> new MemorySiteQueue(this, s, queueCapacity, meterRegistry));
  }

  /**
   * Gets a queue for a given site if it is currently defined.
   *
   * @param site the ID of the site for which to get a queue
   * @return the site queue to use for the specified site or empty if none already defined
   */
  public Optional<MemorySiteQueue> getSiteQueueIfDefined(String site) {
    return Optional.ofNullable(queues.get(site));
  }
}
