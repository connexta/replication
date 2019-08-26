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
package com.connexta.replication.api.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.Replicator;
import com.connexta.replication.api.SyncRequest;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.SiteManager;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.codice.junit.ClearInterruptions;
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

  private static final String SITE_ID = "site";

  @Rule public final MethodRuleAnnotationProcessor processor = new MethodRuleAnnotationProcessor();

  private ReplicatorRunner runner;

  @Mock Replicator replicator;

  @Mock FilterManager filterManager;

  @Mock SiteManager siteManager;

  @Mock ScheduledExecutorService scheduledExecutor;

  private List<Filter> filters;

  private Set<Site> sites;

  @Mock Filter filter;

  @Mock Site site;

  @Before
  public void setUp() throws Exception {
    runner =
        new ReplicatorRunner(
            scheduledExecutor,
            replicator,
            filterManager,
            siteManager,
            Stream.of(ReplicatorRunnerTest.SITE_ID),
            0);
    filters = List.of(filter);
    sites = Set.of(site);
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
    runner =
        new ReplicatorRunner(
            scheduledExecutor, replicator, filterManager, siteManager, Stream.empty(), 1);
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
    when(siteManager.get(SITE_ID)).thenReturn(site);
    when(site.getKind()).thenReturn(SiteKind.REGIONAL);
    when(site.getType()).thenReturn(SiteType.DDF);
    when(site.getId()).thenReturn(SITE_ID);
    when(filterManager.filtersForSite(SITE_ID)).thenReturn(filters.stream());
    runner.scheduleReplication();
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getFilter(), is(filter));
  }

  @Test
  public void scheduleReplicationWithMissingSite() throws Exception {
    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);
    when(siteManager.get(SITE_ID)).thenReturn(site).thenThrow(new NotFoundException(""));
    when(site.getKind()).thenReturn(SiteKind.REGIONAL);
    when(site.getType()).thenReturn(SiteType.DDF);
    when(site.getId()).thenReturn(SITE_ID);
    when(filterManager.filtersForSite(SITE_ID)).thenReturn(filters.stream());
    runner.scheduleReplication();
    verify(replicator, times(1)).submitSyncRequest(request.capture());
    assertThat(request.getValue().getFilter(), is(filter));
  }

  @Test
  public void scheduleReplicationWithSiteOfUnknownType() throws Exception {
    when(siteManager.get(SITE_ID)).thenReturn(site);
    when(site.getType()).thenReturn(SiteType.UNKNOWN);
    runner.scheduleReplication();
    verify(replicator, never()).submitSyncRequest(any(SyncRequest.class));
  }

  @Test
  public void scheduleReplicationWithSiteOfUnknownKind() throws Exception {
    when(siteManager.get(SITE_ID)).thenReturn(site);
    when(site.getKind()).thenReturn(SiteKind.UNKNOWN);
    when(site.getType()).thenReturn(SiteType.DDF);
    runner.scheduleReplication();
    verify(replicator, never()).submitSyncRequest(any(SyncRequest.class));
  }

  @Test
  public void scheduleReplicationWhenReplicatingAllSites() throws Exception {
    runner =
        new ReplicatorRunner(
            scheduledExecutor, replicator, filterManager, siteManager, Stream.empty(), 1);
    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);
    when(siteManager.objects()).thenReturn(sites.stream());
    when(site.getKind()).thenReturn(SiteKind.REGIONAL);
    when(site.getType()).thenReturn(SiteType.DDF);
    when(site.getId()).thenReturn(SITE_ID);
    when(filterManager.filtersForSite(SITE_ID)).thenReturn(filters.stream());
    runner.scheduleReplication();
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getFilter(), is(filter));
  }

  @Test
  public void scheduleReplicationWithSuspend() throws Exception {
    when(siteManager.get(SITE_ID)).thenReturn(site);
    when(site.getKind()).thenReturn(SiteKind.REGIONAL);
    when(site.getType()).thenReturn(SiteType.DDF);
    when(site.getId()).thenReturn(SITE_ID);
    when(filter.isSuspended()).thenReturn(true);
    when(filterManager.filtersForSite(SITE_ID)).thenReturn(filters.stream());
    runner.scheduleReplication();
    verify(replicator, never()).submitSyncRequest(any());
  }

  @Test
  @ClearInterruptions
  public void scheduleReplicationInterruptException() throws Exception {
    ArgumentCaptor<SyncRequest> request = ArgumentCaptor.forClass(SyncRequest.class);
    when(siteManager.get(SITE_ID)).thenReturn(site);
    when(site.getKind()).thenReturn(SiteKind.REGIONAL);
    when(site.getType()).thenReturn(SiteType.DDF);
    when(site.getId()).thenReturn(SITE_ID);
    when(filterManager.filtersForSite(SITE_ID)).thenReturn(filters.stream());
    doThrow(new InterruptedException()).when(replicator).submitSyncRequest(any(SyncRequest.class));
    runner.scheduleReplication();
    verify(replicator).submitSyncRequest(request.capture());
    assertThat(request.getValue().getFilter(), is(filter));
  }
}
