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

import com.connexta.ion.replication.api.ReplicationItem;
import com.connexta.ion.replication.api.Replicator;
import com.connexta.ion.replication.api.Status;
import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registers metrics and a callback to the {@link Replicator#registerCompletionCallback(Consumer)}
 * to increment metrics based on the resulting {@link ReplicationItem}s.
 */
public class ReplicationMetrics {

  private final MeterRegistry meterRegistry;

  /**
   * Creates a new {@code ReplicationMetrics}.
   *
   * @param replicator the {@link Replicator} to receive completed {@link ReplicationItem}s from.
   * @param meterRegistry registry for accessing meters
   */
  public ReplicationMetrics(Replicator replicator, MeterRegistry meterRegistry) {
    replicator.registerCompletionCallback(this::handleItem);
    this.meterRegistry = meterRegistry;
  }

  /** Increments certain meters based on the incoming {@link ReplicationItem}. */
  @VisibleForTesting
  void handleItem(ReplicationItem item) {
    Iterable<Tag> itemTags = getTagsFor(item);
    Status status = item.getStatus();
    if (Status.SUCCESS.equals(status)) {
      meterRegistry.counter("replication_transfer_success_items", itemTags).increment();
      meterRegistry
          .counter("replication_transfer_resource_bytes", itemTags)
          .increment(item.getResourceSize());
      meterRegistry
          .counter("replication_transfer_metadata_bytes", itemTags)
          .increment(item.getMetadataSize());
    } else {
      meterRegistry.counter("replication_transfer_fail_items", itemTags).increment();
    }
  }

  private Iterable<Tag> getTagsFor(ReplicationItem item) {
    return List.of(
        Tag.of("source", item.getSource()), Tag.of("destination", item.getDestination()));
  }
}
