package com.connexta.ion.replication.application.metrics;

import com.connexta.ion.replication.api.Replicator;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Enable auto configuration for Replication related metrics. */
@Configuration
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@ConditionalOnClass(MeterRegistry.class)
public class ReplicationMetricsAutoConfiguration {

  @Bean
  @ConditionalOnProperty(value = "management.metrics.enable.replication", matchIfMissing = true)
  @ConditionalOnMissingBean
  public ReplicationMetrics replicationMetrics(Replicator replicator) {
    return new ReplicationMetrics(replicator);
  }
}
