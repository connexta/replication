package com.connexta.ion.replication.application.metrics;

import com.connexta.ion.replication.api.ReplicationItem;
import com.connexta.ion.replication.api.Replicator;
import com.connexta.ion.replication.api.Status;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationMetrics implements MeterBinder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationMetrics.class);

  private final Iterable<Tag> tags;

  private Counter itemsTransferred;

  private Counter itemsFailed;

  private Counter resourceBytes;

  private Counter metadataBytes;

  private Gauge transferRate;

  private Timer throughput;

  public ReplicationMetrics(Replicator replicator) {
    this(replicator, List.of());
  }

  public ReplicationMetrics(Replicator replicator, Iterable<Tag> tags) {
    this.tags = tags;
    replicator.registerCompletionCallback(createCompletionCallback());
  }

  @Override
  public void bindTo(@NonNull MeterRegistry meterRegistry) {
    LOGGER.info("Binding replication meters");
    Counter.builder("replication.transfer.success")
        .description("Number of metadata/resources successfully transferred between sites")
        .baseUnit("items")
        .tags(tags)
        .register(meterRegistry);

    Counter.builder("replication.transfer.fail")
        .description("Number of metadata/resources failed to be transferred between sites")
        .baseUnit("items")
        .tags(tags)
        .register(meterRegistry);
  }

  private Consumer<ReplicationItem> createCompletionCallback() {
    return item -> {
      Iterable<Tag> itemTags = getTagsFor(item);
      Status status = item.getStatus();
      if (Status.SUCCESS.equals(status)) {
        itemsTransferred = Metrics.counter("replication.transfer.success", itemTags);
        itemsTransferred.increment();

        // these counters are not working like the other two for some reason
        Counter.builder("replication.transfer.resource")
            .description("Number of resource bytes transferred between sites")
            .baseUnit("bytes")
            .tags(itemTags)
            .register(Metrics.globalRegistry)
            .increment(item.getResourceSize());

        Counter.builder("replication.transfer.metadata")
            .description("Number of metadata bytes transferred between sites")
            .baseUnit("bytes")
            .tags(itemTags)
            .register(Metrics.globalRegistry)
            .increment(item.getMetadataSize());
      } else {
        itemsFailed = Metrics.counter("replication.transfer.fail", itemTags);
        itemsFailed.increment();
      }
    };
  }

  private Iterable<Tag> getTagsFor(ReplicationItem item) {
    return List.of(
        Tag.of("source", item.getSource()), Tag.of("destination", item.getDestination()));
  }
}
