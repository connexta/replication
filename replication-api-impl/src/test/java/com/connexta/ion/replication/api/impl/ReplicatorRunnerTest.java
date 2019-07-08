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
package com.connexta.ion.replication.api.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.Replicator;
import com.connexta.ion.replication.api.SyncRequest;
import com.connexta.ion.replication.api.data.ReplicatorConfig;
import com.connexta.ion.replication.api.persistence.ReplicatorConfigManager;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorRunnerTest {

  private ReplicatorRunner runner;

  @Mock Replicator replicator;

  @Mock ReplicatorConfigManager configManager;

  @Mock ScheduledExecutorService scheduledExecutor;

  private Stream<ReplicatorConfig> configStream;

  @Mock ReplicatorConfig config;

  @Before
  public void setUp() throws Exception {
    runner = new ReplicatorRunner(scheduledExecutor, replicator, configManager, 0);
    configStream = Stream.of(config);
  }

  @Test
  public void init() {
    ArgumentCaptor<Long> period = ArgumentCaptor.forClass(Long.class);
    runner.init();
    verify(scheduledExecutor)
        .scheduleAtFixedRate(any(Runnable.class), anyLong(), period.capture(), any(TimeUnit.class));
    assertThat(period.getValue(), is(TimeUnit.MINUTES.toSeconds(5)));
  }

  @Test
  public void initNonDefaultPeriod() {
    runner = new ReplicatorRunner(scheduledExecutor, replicator, configManager, 1);
    ArgumentCaptor<Long> period = ArgumentCaptor.forClass(Long.class);
    runner.init();
    verify(scheduledExecutor)
        .scheduleAtFixedRate(any(Runnable.class), anyLong(), period.capture(), any(TimeUnit.class));
    assertThat(period.getValue(), is(1L));
  }

  @Test
  public void destroy() {
    runner.destroy();
    verify(scheduledExecutor).shutdownNow();
  }

  @Test
  public void scheduleReplication() throws Exception {
    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);
    when(config.getName()).thenReturn("test");
    when(configManager.objects()).thenReturn(configStream);
    runner.scheduleReplication();
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getConfig().getName(), is("test"));
  }

  @Test
  public void scheduleReplicationWithSuspend() throws Exception {
    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);
    when(config.getName()).thenReturn("test");
    when(config.isSuspended()).thenReturn(true);
    when(configManager.objects()).thenReturn(configStream);
    runner.scheduleReplication();
    verify(replicator, never()).submitSyncRequest(request.capture());
  }

  @Test
  public void scheduleReplicationInterruptException() throws Exception {
    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);
    when(config.getName()).thenReturn("test");
    when(configManager.objects()).thenReturn(configStream);
    doThrow(new InterruptedException()).when(replicator).submitSyncRequest(any(SyncRequest.class));
    runner.scheduleReplication();
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getConfig().getName(), is("test"));
  }
}
