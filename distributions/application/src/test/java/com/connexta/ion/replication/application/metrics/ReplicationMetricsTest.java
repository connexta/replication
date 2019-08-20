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
package com.connexta.ion.replication.application.metrics;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.Action;
import com.connexta.ion.replication.api.Replicator;
import com.connexta.ion.replication.api.Status;
import com.connexta.replication.api.data.ReplicationItem;
import com.google.common.cache.Cache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationMetricsTest {

  private static final String TRANSFER_SUCCESS_METER = "replication_transfer_success_items";

  private static final String TRANSFER_RESOURCE_BYTES_METER = "replication_transfer_resource_bytes";

  private static final String TRANSFER_METADATA_BYTES_METER = "replication_transfer_metadata_bytes";

  private static final String TRANSFER_FAIL_METER = "replication_transfer_fail_items";

  private static final String SOURCE = "source";

  private static final String DESTINATION = "destination";

  private static final long RESOURCE_SIZE = 1;

  private static final long METADATA_SIZE = 2;

  @Mock MeterRegistry meterRegistry;

  @Mock Replicator replicator;

  private ReplicationMetrics replicationMetrics;

  private Counter transferSuccess;

  private Counter resourceBytes;

  private Counter metadataBytes;

  @Before
  public void setup() {
    replicationMetrics = new ReplicationMetrics(replicator, meterRegistry);
    transferSuccess = mock(Counter.class);
    resourceBytes = mock(Counter.class);
    metadataBytes = mock(Counter.class);
  }

  @Test
  public void testMetricsCallbackRegistered() {
    verify(replicator).registerCompletionCallback(any(Consumer.class));
  }

  @Test
  public void testReplicationItemSuccessCreate() {
    // setup
    ReplicationItem item = mockItem(Status.SUCCESS, Action.CREATE);
    Iterable<Tag> tags = mockTags(Action.CREATE);
    when(meterRegistry.counter(TRANSFER_SUCCESS_METER, tags)).thenReturn(transferSuccess);
    when(meterRegistry.counter(TRANSFER_RESOURCE_BYTES_METER, tags)).thenReturn(resourceBytes);
    when(meterRegistry.counter(TRANSFER_METADATA_BYTES_METER, tags)).thenReturn(metadataBytes);

    // when
    replicationMetrics.handleItem(item);

    // then
    verify(transferSuccess).increment();
    verify(resourceBytes).increment(RESOURCE_SIZE);
    verify(metadataBytes).increment(METADATA_SIZE);
  }

  @Test
  public void testReplicationItemSuccessUpdate() {
    // setup
    ReplicationItem item = mockItem(Status.SUCCESS, Action.UPDATE);
    Iterable<Tag> tags = mockTags(Action.UPDATE);
    when(meterRegistry.counter(TRANSFER_SUCCESS_METER, tags)).thenReturn(transferSuccess);
    when(meterRegistry.counter(TRANSFER_RESOURCE_BYTES_METER, tags)).thenReturn(resourceBytes);
    when(meterRegistry.counter(TRANSFER_METADATA_BYTES_METER, tags)).thenReturn(metadataBytes);

    // when
    replicationMetrics.handleItem(item);

    // then
    verify(transferSuccess).increment();
    verify(resourceBytes).increment(RESOURCE_SIZE);
    verify(metadataBytes).increment(METADATA_SIZE);
  }

  @Test
  public void testReplicationItemSuccessDelete() {
    // setup
    ReplicationItem item = mockItem(Status.SUCCESS, Action.DELETE);
    Iterable<Tag> tags = mockTags(Action.DELETE);
    when(meterRegistry.counter(TRANSFER_SUCCESS_METER, tags)).thenReturn(transferSuccess);
    when(meterRegistry.counter(TRANSFER_RESOURCE_BYTES_METER, tags)).thenReturn(resourceBytes);
    when(meterRegistry.counter(TRANSFER_METADATA_BYTES_METER, tags)).thenReturn(metadataBytes);

    // when
    replicationMetrics.handleItem(item);

    // then
    verify(transferSuccess).increment();
    verify(resourceBytes, never()).increment(anyLong());
    verify(metadataBytes, never()).increment(anyLong());
  }

  @Test
  public void testReplicationItemFailure() {
    // setup
    ReplicationItem item = mockItem(Status.FAILURE, Action.CREATE);
    Iterable<Tag> tags = mockTags(Action.CREATE);
    Counter transferFail = mock(Counter.class);
    when(meterRegistry.counter(TRANSFER_FAIL_METER, tags)).thenReturn(transferFail);

    // when
    replicationMetrics.handleItem(item);

    // then
    verify(transferFail).increment();
  }

  @Test
  public void testTagsAreCached() {
    // setup
    Cache cache = mock(Cache.class);
    ReplicationMetrics replicationMetrics =
        new ReplicationMetrics(replicator, meterRegistry, cache);
    ReplicationItem item = mockItem(Status.SUCCESS, Action.CREATE);
    Iterable<Tag> tags = mockTags(Action.CREATE);
    when(meterRegistry.counter(TRANSFER_SUCCESS_METER, tags)).thenReturn(transferSuccess);
    when(meterRegistry.counter(TRANSFER_RESOURCE_BYTES_METER, tags)).thenReturn(resourceBytes);
    when(meterRegistry.counter(TRANSFER_METADATA_BYTES_METER, tags)).thenReturn(metadataBytes);
    final String key = SOURCE + DESTINATION + Action.CREATE.name();
    when(cache.getIfPresent(key)).thenReturn(null).thenReturn(tags);

    // when called twice it should only put once in the cache
    replicationMetrics.handleItem(item);
    replicationMetrics.handleItem(item);

    // then
    verify(cache).put(anyString(), any(Iterable.class));
  }

  private ReplicationItem mockItem(Status status, Action action) {
    ReplicationItem item = mock(ReplicationItem.class);
    when(item.getStatus()).thenReturn(status);
    when(item.getAction()).thenReturn(action);
    when(item.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(item.getResourceSize()).thenReturn(RESOURCE_SIZE);
    when(item.getSource()).thenReturn(SOURCE);
    when(item.getDestination()).thenReturn(DESTINATION);
    return item;
  }

  private Iterable<Tag> mockTags(Action action) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("source", SOURCE));
    tags.add(Tag.of("destination", DESTINATION));
    tags.add(Tag.of("action", action.toString()));
    return tags;
  }
}
