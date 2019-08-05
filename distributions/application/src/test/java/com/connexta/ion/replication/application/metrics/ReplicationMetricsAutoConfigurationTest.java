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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import com.connexta.ion.replication.api.Replicator;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

public class ReplicationMetricsAutoConfigurationTest {

  private final AnnotationConfigApplicationContext context =
      new AnnotationConfigApplicationContext();

  @Before
  public void cleanUp() {
    context.close();
  }

  @Test
  public void autoConfiguresReplicationMetrics() {
    registerAndRefresh();
    assertThat(context.getBean(ReplicationMetrics.class), is(notNullValue()));
  }

  private void registerAndRefresh() {
    this.context.register(
        MeterRegistryConfiguration.class,
        ReplicatorConfiguration.class,
        ReplicationMetricsAutoConfiguration.class);
    this.context.refresh();
  }

  @TestConfiguration
  static class MeterRegistryConfiguration {
    @Bean
    public MeterRegistry meterRegistry() {
      return mock(MeterRegistry.class);
    }
  }

  @TestConfiguration
  static class ReplicatorConfiguration {
    @Bean
    public Replicator replicator() {
      return mock(Replicator.class);
    }
  }
}
