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
import com.connexta.replication.api.queue.SiteQueue;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MemoryQueueBrokerTest {
  private static final int CAPACITY = 2;
  private static final String SITE = "site";
  private static final String SITE2 = "site2";

  private static final MeterRegistry REGISTRY = Mockito.mock(MeterRegistry.class);

  private final MemoryQueueBroker broker =
      new MemoryQueueBroker(MemoryQueueBrokerTest.CAPACITY, MemoryQueueBrokerTest.REGISTRY);

  @Test
  public void testGetQueue() throws Exception {
    Assert.assertThat(
        broker.getQueue(MemoryQueueBrokerTest.SITE), Matchers.not(Matchers.nullValue()));
  }

  @Test
  public void testGetQueueWhenAlreadyDefined() throws Exception {
    final SiteQueue queue = broker.getQueue(MemoryQueueBrokerTest.SITE);

    Assert.assertThat(broker.getQueue(MemoryQueueBrokerTest.SITE), Matchers.sameInstance(queue));
  }

  @Test
  public void testGetQueueWithOneSiteInArrayWhenAlreadyDefined() throws Exception {
    final SiteQueue queue = broker.getQueue(MemoryQueueBrokerTest.SITE);

    Assert.assertThat(
        broker.getQueue(new String[] {MemoryQueueBrokerTest.SITE}), Matchers.sameInstance(queue));
  }

  @Test
  public void testGetQueueWithTwoSitesInArray() throws Exception {
    final SiteQueue queue = broker.getQueue(MemoryQueueBrokerTest.SITE);
    final SiteQueue queue2 = broker.getQueue(MemoryQueueBrokerTest.SITE2);

    final Queue cqueue = broker.getQueue(MemoryQueueBrokerTest.SITE, MemoryQueueBrokerTest.SITE2);

    Assert.assertThat(cqueue, Matchers.isA(MemoryCompositeQueue.class));
    Assert.assertThat(
        ((MemoryCompositeQueue) cqueue).sites().collect(Collectors.toList()),
        Matchers.contains(MemoryQueueBrokerTest.SITE, MemoryQueueBrokerTest.SITE2));
    Assert.assertThat(
        ((MemoryCompositeQueue) cqueue).queues().collect(Collectors.toList()),
        Matchers.contains(queue, queue2));
  }

  @Test
  public void testGetQueueWithOneSiteInStreamWhenAlreadyDefined() throws Exception {
    final SiteQueue queue = broker.getQueue(MemoryQueueBrokerTest.SITE);

    final Queue cqueue = broker.getQueue(Stream.of(MemoryQueueBrokerTest.SITE));

    Assert.assertThat(cqueue, Matchers.isA(MemoryCompositeQueue.class));
    Assert.assertThat(
        ((MemoryCompositeQueue) cqueue).sites().collect(Collectors.toList()),
        Matchers.contains(MemoryQueueBrokerTest.SITE));
    Assert.assertThat(
        ((MemoryCompositeQueue) cqueue).queues().collect(Collectors.toList()),
        Matchers.contains(queue));
  }

  @Test
  public void testGetQueueWithTwoSitesInStream() throws Exception {
    final SiteQueue queue = broker.getQueue(MemoryQueueBrokerTest.SITE);
    final SiteQueue queue2 = broker.getQueue(MemoryQueueBrokerTest.SITE2);

    final Queue cqueue =
        broker.getQueue(Stream.of(MemoryQueueBrokerTest.SITE, MemoryQueueBrokerTest.SITE2));

    Assert.assertThat(cqueue, Matchers.isA(MemoryCompositeQueue.class));
    Assert.assertThat(
        ((MemoryCompositeQueue) cqueue).sites().collect(Collectors.toList()),
        Matchers.contains(MemoryQueueBrokerTest.SITE, MemoryQueueBrokerTest.SITE2));
    Assert.assertThat(
        ((MemoryCompositeQueue) cqueue).queues().collect(Collectors.toList()),
        Matchers.contains(queue, queue2));
  }

  @Test
  public void testGetQueueIfDefinedWhenNotDefined() throws Exception {
    Assert.assertThat(
        broker.getQueueIfDefined(MemoryQueueBrokerTest.SITE), OptionalMatchers.isEmpty());
  }

  @Test
  public void testGetQueueIfDefinedWhenAlreadyDefined() throws Exception {
    final SiteQueue queue = broker.getQueue(MemoryQueueBrokerTest.SITE);

    Assert.assertThat(
        broker.getQueueIfDefined(MemoryQueueBrokerTest.SITE).map(SiteQueue.class::cast),
        OptionalMatchers.isPresentAndIs(queue));
  }
}
