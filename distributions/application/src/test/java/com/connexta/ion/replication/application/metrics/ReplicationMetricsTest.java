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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.ReplicationItem;
import com.connexta.ion.replication.api.Replicator;
import com.connexta.ion.replication.api.Status;
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

  @Before
  public void setup() {
    replicationMetrics = new ReplicationMetrics(replicator, meterRegistry);
  }

  @Test
  public void testMetricsCallbackRegistered() {
    verify(replicator).registerCompletionCallback(any(Consumer.class));
  }

  @Test
  public void testReplicationItemSuccess() {
    // setup
    ReplicationItem item = mockItem(Status.SUCCESS);
    Iterable<Tag> tags = mockTags();
    Counter transferSuccess = mock(Counter.class);
    Counter resourceBytes = mock(Counter.class);
    Counter metadataBytes = mock(Counter.class);
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
  public void testReplicationItemFailure() {
    // setup
    ReplicationItem item = mockItem(Status.FAILURE);
    Iterable<Tag> tags = mockTags();
    Counter transferFail = mock(Counter.class);
    when(meterRegistry.counter(TRANSFER_FAIL_METER, tags)).thenReturn(transferFail);

    // when
    replicationMetrics.handleItem(item);

    // then
    verify(transferFail).increment();
  }

  private ReplicationItem mockItem(Status status) {
    ReplicationItem item = mock(ReplicationItem.class);
    when(item.getStatus()).thenReturn(status);
    when(item.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(item.getResourceSize()).thenReturn(RESOURCE_SIZE);
    when(item.getSource()).thenReturn(SOURCE);
    when(item.getDestination()).thenReturn(DESTINATION);
    return item;
  }

  private Iterable<Tag> mockTags() {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("source", SOURCE));
    tags.add(Tag.of("destination", DESTINATION));
    return tags;
  }
}
