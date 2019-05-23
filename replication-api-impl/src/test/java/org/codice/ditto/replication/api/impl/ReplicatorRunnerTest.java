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
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PrivilegedAction;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.Heartbeater;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.codice.junit.ClearInterruptions;
import org.codice.junit.RestoreSystemProperties;
import org.codice.junit.rules.MethodRuleAnnotationProcessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorRunnerTest {

  private static final String SOURCE_ID = "sourceId";

  private static final String DESTINATION_ID = "destinationId";

  private static final String URL1 = "url1";
  private static final String URL2 = "url2";
  private static final String VERIFIED_PREFIX = "verified_";
  private static final String VERIFIED_URL1 =
      ReplicatorRunnerTest.VERIFIED_PREFIX + ReplicatorRunnerTest.URL1;
  private static final String VERIFIED_URL2 =
      ReplicatorRunnerTest.VERIFIED_PREFIX + ReplicatorRunnerTest.URL2;

  @Rule public final MethodRuleAnnotationProcessor processor = new MethodRuleAnnotationProcessor();

  private ReplicatorRunner runner;

  @Mock Security security;

  @Mock Replicator replicator;

  @Mock ReplicatorConfigManager configManager;

  @Mock ScheduledExecutorService scheduledExecutor;

  @Mock ReplicatorConfig config;

  @Mock SiteManager siteManager;

  @Mock Heartbeater heartbeater;

  @Before
  public void setUp() throws Exception {
    when(security.runWithSubjectOrElevate(any(Callable.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, Callable.class).call());
    when(security.runAsAdmin(any(PrivilegedAction.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, PrivilegedAction.class).run());
    runner =
        new ReplicatorRunner(
            scheduledExecutor, replicator, heartbeater, configManager, siteManager, security);
  }

  @Test
  public void init() {
    ArgumentCaptor<Long> period = ArgumentCaptor.forClass(Long.class);

    runner.init();

    verify(scheduledExecutor, times(2))
        .scheduleAtFixedRate(any(Runnable.class), anyLong(), period.capture(), any(TimeUnit.class));

    assertThat(
        period.getAllValues(),
        contains(TimeUnit.MINUTES.toSeconds(5), TimeUnit.MINUTES.toSeconds(1)));
  }

  @RestoreSystemProperties
  @Test
  public void initNonDefaultPeriod() {
    ArgumentCaptor<Long> period = ArgumentCaptor.forClass(Long.class);

    System.setProperty("org.codice.replication.period", "1");
    System.setProperty("org.codice.replication.heartbeat.period", "2");

    runner.init();

    verify(scheduledExecutor, times(2))
        .scheduleAtFixedRate(any(Runnable.class), anyLong(), period.capture(), any(TimeUnit.class));

    assertThat(period.getAllValues(), contains(1L, 2L));
  }

  @Test
  public void destroy() {
    runner.destroy();
    verify(scheduledExecutor).shutdownNow();
  }

  @Test
  public void scheduleReplication() throws Exception {
    ReplicationSite source = mockSite(SOURCE_ID, false, URL1, VERIFIED_URL1);
    ReplicationSite destination = mockSite(DESTINATION_ID, false, URL2, VERIFIED_URL2);
    when(siteManager.objects()).thenReturn(Stream.of(source, destination));
    when(siteManager.get(SOURCE_ID)).thenReturn(source);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destination);

    when(config.getName()).thenReturn("test");
    when(config.getSource()).thenReturn(SOURCE_ID);
    when(config.getDestination()).thenReturn(DESTINATION_ID);
    when(configManager.objects()).thenReturn(Stream.of(config));

    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);

    // when
    runner.scheduleReplication();

    // then
    verify(source, never()).setVerifiedUrl(any());
    verify(destination, never()).setVerifiedUrl(any());
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getConfig().getName(), is("test"));
  }

  @Test
  public void scheduleReplicationWithSuspend() throws Exception {
    when(siteManager.objects()).thenReturn(Stream.empty());
    when(config.getName()).thenReturn("test");
    when(config.isSuspended()).thenReturn(true);
    when(configManager.objects()).thenReturn(Stream.of(config));

    runner.scheduleReplication();

    verify(replicator, never()).submitSyncRequest(any());
  }

  @ClearInterruptions
  @Test
  public void scheduleReplicationInterruptException() throws Exception {
    ReplicationSite source = mockSite(SOURCE_ID, false, URL1, VERIFIED_URL1);
    ReplicationSite destination = mockSite(DESTINATION_ID, false, URL2, VERIFIED_URL2);

    when(siteManager.objects()).thenReturn(Stream.of(source, destination));
    when(siteManager.get(SOURCE_ID)).thenReturn(source);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destination);
    when(config.getName()).thenReturn("test");
    when(config.getSource()).thenReturn(SOURCE_ID);
    when(config.getDestination()).thenReturn(DESTINATION_ID);
    when(configManager.objects()).thenReturn(Stream.of(config));
    doThrow(new InterruptedException()).when(replicator).submitSyncRequest(any(SyncRequest.class));

    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);

    // when
    runner.scheduleReplication();

    // then
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getConfig().getName(), is("test"));
    assertThat(Thread.currentThread().isInterrupted(), is(true));
  }

  @Test
  public void scheduleReplicationWhereConfigNotRunWhenSourceIsNotFound() throws Exception {
    ReplicationSite source = mockSite(SOURCE_ID, false, URL1, VERIFIED_URL1);
    ReplicationSite destination = mockSite(DESTINATION_ID, false, URL2, VERIFIED_URL2);

    when(siteManager.objects()).thenReturn(Stream.of(destination));
    when(config.getName()).thenReturn("test");
    when(config.getSource()).thenReturn(SOURCE_ID);
    when(config.getDestination()).thenReturn(DESTINATION_ID);
    when(configManager.objects()).thenReturn(Stream.of(config));

    // when
    runner.scheduleReplication();

    // then
    verify(replicator, never()).submitSyncRequest(any());
  }

  @Test
  public void scheduleReplicationWhereConfigNotRunWhenDestinationIsNotFound() throws Exception {
    ReplicationSite source = mockSite(SOURCE_ID, false, URL1, VERIFIED_URL1);
    ReplicationSite destination = mockSite(DESTINATION_ID, false, URL2, VERIFIED_URL2);

    when(siteManager.objects()).thenReturn(Stream.of(source));
    when(config.getName()).thenReturn("test");
    when(config.getSource()).thenReturn(SOURCE_ID);
    when(config.getDestination()).thenReturn(DESTINATION_ID);
    when(configManager.objects()).thenReturn(Stream.of(config));

    // when
    runner.scheduleReplication();

    // then
    verify(replicator, never()).submitSyncRequest(any());
  }

  @Test
  public void scheduleReplicationWhereConfigNotRunWhenConfigSourceIsRemoteManaged()
      throws Exception {
    ReplicationSite source = mockSite(SOURCE_ID, true, URL1, VERIFIED_URL1);
    ReplicationSite destination = mockSite(DESTINATION_ID, false, URL2, VERIFIED_URL2);

    when(siteManager.objects()).thenReturn(Stream.of(source, destination));
    when(siteManager.get(SOURCE_ID)).thenReturn(source);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destination);
    when(config.getName()).thenReturn("test");
    when(config.getSource()).thenReturn(SOURCE_ID);
    when(config.getDestination()).thenReturn(DESTINATION_ID);
    when(configManager.objects()).thenReturn(Stream.of(config));

    // when
    runner.scheduleReplication();

    // then
    verify(replicator, never()).submitSyncRequest(any());
  }

  @Test
  public void scheduleReplicationWhereConfigNotRunWhenConfigDestinationIsRemoteManaged()
      throws Exception {
    ReplicationSite source = mockSite(SOURCE_ID, false, URL1, VERIFIED_URL1);
    ReplicationSite destination = mockSite(DESTINATION_ID, true, URL2, VERIFIED_URL2);

    when(siteManager.objects()).thenReturn(Stream.of(source, destination));
    when(siteManager.get(SOURCE_ID)).thenReturn(source);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destination);
    when(config.getName()).thenReturn("test");
    when(config.getSource()).thenReturn(SOURCE_ID);
    when(config.getDestination()).thenReturn(DESTINATION_ID);
    when(configManager.objects()).thenReturn(Stream.of(config));

    // when
    runner.scheduleReplication();

    // then
    verify(replicator, never()).submitSyncRequest(any());
  }

  @Test
  public void scheduleReplicationAsSystemUser() throws Exception {
    final ReplicationSite source = mockSite(SOURCE_ID, false, URL1, VERIFIED_URL1);
    final ReplicationSite destination = mockSite(DESTINATION_ID, false, URL2, VERIFIED_URL2);

    when(siteManager.objects()).thenReturn(Stream.of(source, destination));
    when(siteManager.get(SOURCE_ID)).thenReturn(source);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destination);

    when(config.getName()).thenReturn("test");
    when(config.getSource()).thenReturn(SOURCE_ID);
    when(config.getDestination()).thenReturn(DESTINATION_ID);
    when(configManager.objects()).thenReturn(Stream.of(config));

    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);

    // when
    runner.replicateAsSystemUser();

    // then
    verify(security).runAsAdmin(any(PrivilegedAction.class));
    verify(security).runWithSubjectOrElevate(any(Callable.class));
    verify(source, never()).setVerifiedUrl(any());
    verify(destination, never()).setVerifiedUrl(any());
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getConfig().getName(), is("test"));
  }

  @Test
  public void scheduleHeartbeat() throws Exception {
    final ReplicationSite site1 = mockSite(SOURCE_ID, true, URL1, VERIFIED_URL1);
    final ReplicationSite site2 = mockSite(DESTINATION_ID, true, URL2, VERIFIED_URL2);
    final ReplicationSite site3 =
        mockSite("id3", false, "url3", ReplicatorRunnerTest.VERIFIED_PREFIX + "url3");

    when(siteManager.objects()).thenReturn(Stream.of(site1, site2, site3));

    runner.scheduleHeartbeat();

    verify(heartbeater).heartbeat(site1);
    verify(heartbeater).heartbeat(site2);
    verify(heartbeater, never()).heartbeat(site3);
  }

  @Test
  public void scheduleHeartbeatWhenNotVerified() throws Exception {
    final ReplicationSite site1 = mockSite(SOURCE_ID, true, URL1, VERIFIED_URL1);
    final ReplicationSite site2 = mockSite(DESTINATION_ID, true, URL2, VERIFIED_URL2);
    final ReplicationSite site3 = mockSite("id3", false, "url3", null);

    when(siteManager.objects()).thenReturn(Stream.of(site1, site2, site3));

    runner.scheduleHeartbeat();

    verify(heartbeater).heartbeat(site1);
    verify(heartbeater).heartbeat(site2);
    verify(heartbeater).heartbeat(site3);
  }

  @ClearInterruptions
  @Test
  public void scheduleHeartbeatWhenInterrupted() throws Exception {
    final ReplicationSite site1 = mockSite(SOURCE_ID, true, URL1, VERIFIED_URL1);
    final ReplicationSite site2 = mockSite(DESTINATION_ID, true, URL2, VERIFIED_URL2);

    when(siteManager.objects()).thenReturn(Stream.of(site1, site2));
    doThrow(new InterruptedException("testing")).when(heartbeater).heartbeat(any());

    runner.scheduleHeartbeat();

    verify(heartbeater).heartbeat(site1);
    verify(heartbeater).heartbeat(site2);
    assertThat(Thread.currentThread().isInterrupted(), is(true));
  }

  @Test
  public void scheduleHeartbeatAsSystemUser() throws Exception {
    final ReplicationSite site1 = mockSite(SOURCE_ID, true, URL1, VERIFIED_URL1);
    final ReplicationSite site2 = mockSite(DESTINATION_ID, true, URL2, VERIFIED_URL2);

    when(siteManager.objects()).thenReturn(Stream.of(site1, site2));

    runner.heartbeatAsSystemUser();

    verify(security).runAsAdmin(any(PrivilegedAction.class));
    verify(security).runWithSubjectOrElevate(any(Callable.class));
    verify(heartbeater).heartbeat(site1);
    verify(heartbeater).heartbeat(site2);
  }

  private ReplicationSite mockSite(
      String id, boolean isRemoteManaged, String url, @Nullable String verifiedUrl) {
    ReplicationSite site = mock(ReplicationSite.class);
    when(site.getId()).thenReturn(id);
    when(site.isRemoteManaged()).thenReturn(isRemoteManaged);
    when(site.getUrl()).thenReturn(url);
    when(site.getVerifiedUrl()).thenReturn(verifiedUrl);
    return site;
  }
}
