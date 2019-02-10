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
package org.codice.ditto.replication.api.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.SyncRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorRunnerTest {

  ReplicatorRunner runner;

  @Mock Security security;

  @Mock Replicator replicator;

  @Mock ReplicatorConfigLoader configLoader;

  @Mock ScheduledExecutorService scheduledExecutor;

  List<ReplicatorConfig> configList = new ArrayList<>();

  @Mock ReplicatorConfig config;

  @Before
  public void setUp() throws Exception {
    when(security.runWithSubjectOrElevate(any(Callable.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, Callable.class).call());
    when(security.runAsAdmin(any(PrivilegedAction.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, PrivilegedAction.class).run());
    runner = new ReplicatorRunner(scheduledExecutor, replicator, configLoader, security);
    configList.add(config);
  }

  @After
  public void after() {
    System.clearProperty("org.codice.replication.period");
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
    ArgumentCaptor<Long> period = ArgumentCaptor.forClass(Long.class);
    System.setProperty("org.codice.replication.period", "1");
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
    when(configLoader.getAllConfigs()).thenReturn(configList);
    runner.scheduleReplication();
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getConfig().getName(), is("test"));
  }

  @Test
  public void scheduleReplicationInterruptException() throws Exception {
    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);
    when(config.getName()).thenReturn("test");
    when(configLoader.getAllConfigs()).thenReturn(configList);
    doThrow(new InterruptedException()).when(replicator).submitSyncRequest(any(SyncRequest.class));
    runner.scheduleReplication();
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getConfig().getName(), is("test"));
  }

  @Test
  public void scheduleReplicationAsSystemUser() throws Exception {
    runner.replicateAsSystemUser();
    verify(security).runAsAdmin(any(PrivilegedAction.class));
    verify(security).runWithSubjectOrElevate(any(Callable.class));
  }
}
