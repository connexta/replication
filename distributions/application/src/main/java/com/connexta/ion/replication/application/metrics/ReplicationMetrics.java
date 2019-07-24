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
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registers metrics and a callback to the {@link Replicator#registerCompletionCallback(Consumer)}
 * to increment metrics based on the resulting {@link ReplicationItem}s.
 */
public class ReplicationMetrics implements MeterBinder {

  /**
   * Creates a new {@code ReplicationMetrics}.
   *
   * @param replicator the {@link Replicator} to receive completed {@link ReplicationItem}s from.
   */
  public ReplicationMetrics(Replicator replicator) {
    replicator.registerCompletionCallback(this::handleItem);
  }

  // todo: enable registration here when tags are figured out.
  @Override
  public void bindTo(@NonNull MeterRegistry meterRegistry) {
    //    LOGGER.info("Binding replication meters");
    //    Counter.builder("replication.transfer.success.items")
    //        .description("Number of metadata/resources successfully transferred between sites")
    //        .register(meterRegistry);
    //
    //    Counter.builder("replication.transfer.fail.items")
    //        .description("Number of metadata/resources failed to be transferred between sites")
    //        .register(meterRegistry);
    //
    //    Counter.builder("replication.transfer.resource.bytes")
    //        .description("Number of resource bytes transferred between sites")
    //        .register(meterRegistry);
    //
    //    Counter.builder("replication.transfer.metadata.bytes")
    //        .description("Number of metadata bytes transferred between sites")
    //        .register(meterRegistry);
  }

  /** Increments certain meters based on the incoming {@link ReplicationItem}. */
  @VisibleForTesting
  void handleItem(ReplicationItem item) {
    Iterable<Tag> itemTags = getTagsFor(item);
    Status status = item.getStatus();
    if (Status.SUCCESS.equals(status)) {
      Metrics.counter("replication.transfer.success.items", itemTags).increment();
      Metrics.counter("replication.transfer.resource.bytes", itemTags)
          .increment(item.getResourceSize());
      Metrics.counter("replication.transfer.metadata.bytes", itemTags)
          .increment(item.getMetadataSize());
    } else {
      Metrics.counter("replication.transfer.fail.items", itemTags).increment();
    }
  }

  private Iterable<Tag> getTagsFor(ReplicationItem item) {
    return List.of(
        Tag.of("source", item.getSource()), Tag.of("destination", item.getDestination()));
  }
}
