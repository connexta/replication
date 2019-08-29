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
package com.connexta.replication.query;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.AdapterInterruptedException;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.queue.QueueException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.codice.junit.rules.ClearInterruptions;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryManagerTest {

  @Rule public final ClearInterruptions clearInterruptions = new ClearInterruptions();

  private static final long SERVICE_REFRESH_PERIOD = 30L;

  private static final String SITE_ID = "siteid";

  private static final String SITE_ID2 = "siteid2";

  private static final String SITE_ID3 = "siteid3";

  @Mock QueryServiceTools queryServiceTools;

  @Mock ScheduledExecutorService executor;

  @Mock private Site site;

  @Mock private Site site2;

  @Mock private Site site3;

  @Mock private QueryService queryService;

  private QueryManager queryManager;

  @Before
  public void setUp() throws Exception {
    queryManager =
        new QueryManager(Stream.empty(), queryServiceTools, SERVICE_REFRESH_PERIOD, executor);
    when(queryServiceTools.getGlobalPeriod()).thenReturn(60L);
    when(site.getId()).thenReturn(SITE_ID);
    when(site.getType()).thenReturn(SiteType.DDF);
    when(site.getKind()).thenReturn(SiteKind.REGIONAL);
    when(site.getPollingPeriod()).thenReturn(Optional.empty());
    when(site2.getId()).thenReturn(SITE_ID2);
    when(site2.getType()).thenReturn(SiteType.DDF);
    when(site2.getKind()).thenReturn(SiteKind.REGIONAL);
    when(site2.getPollingPeriod()).thenReturn(Optional.empty());
    when(site3.getId()).thenReturn(SITE_ID3);
    when(site3.getType()).thenReturn(SiteType.ION);
  }

  @Test
  public void init() {
    queryManager.init();
    verify(executor)
        .scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq(SERVICE_REFRESH_PERIOD), eq(TimeUnit.SECONDS));
  }

  @Test
  public void destroy() {
    queryManager.putService(SITE_ID, queryService);
    queryManager.destroy();
    verify(executor).shutdownNow();
    verify(queryService).stop();
  }

  @Test
  public void reloadSiteConfigs() {
    when(queryServiceTools.sites()).thenReturn(Stream.of(site));
    queryManager.reloadSiteConfigs();
    // assert that a query service was started
    assertThat(queryManager.getServices().get(SITE_ID).isRunning(), is(true));
  }

  @Test
  public void reloadSiteConfigsSitesIsNotEmpty() {
    queryManager =
        new QueryManager(Stream.of(SITE_ID), queryServiceTools, SERVICE_REFRESH_PERIOD, executor);
    when(queryServiceTools.sites()).thenReturn(Stream.of(site, site2));
    queryManager.reloadSiteConfigs();
    // assert that a query service was created and started for site1
    assertThat(queryManager.getServices().get(SITE_ID).isRunning(), is(true));
    // assert that a query service was not created for site2
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID2)));
  }

  @Test
  public void reloadSiteConfigsNonPolledSite() {
    when(queryServiceTools.sites()).thenReturn(Stream.of(site3));
    queryManager.reloadSiteConfigs();
    assertThat(queryManager.getServices().get(SITE_ID3), is(Matchers.nullValue()));
  }

  @Test
  public void reloadSiteConfigsHandlesAdapterException() {
    when(queryServiceTools.sites()).thenReturn(Stream.of(site));
    when(queryServiceTools.getAdapterFor(site)).thenThrow(new AdapterException(""));
    queryManager.reloadSiteConfigs();
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
  }

  @Test
  public void reloadSiteConfigsHandlesQueueException() {
    when(queryServiceTools.sites()).thenReturn(Stream.of(site));
    when(queryServiceTools.getQueueFor(site)).thenThrow(new QueueException(""));
    queryManager.reloadSiteConfigs();
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
  }

  @Test
  public void reloadSiteConfigsHandlesAdapterInterruptedException() {
    when(queryServiceTools.sites()).thenReturn(Stream.of(site));
    when(queryServiceTools.getAdapterFor(site))
        .thenThrow(new AdapterInterruptedException(new InterruptedException()));
    queryManager.reloadSiteConfigs();
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
    assertTrue(Thread.interrupted());
  }

  @Test
  public void reloadSiteConfigsHandlesIllegalArgumentException() {
    when(queryServiceTools.sites()).thenReturn(Stream.of(site));
    when(queryServiceTools.getAdapterFor(site)).thenThrow(new IllegalArgumentException(""));
    queryManager.reloadSiteConfigs();
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
  }

  @Test
  public void reloadSiteConfigsAddRemoveAndIgnoreASite() {
    queryManager.putService(SITE_ID, queryService);
    when(queryServiceTools.sites()).thenReturn(Stream.of(site2, site3));
    when(queryServiceTools.getAdapterFor(site2)).thenReturn(mock(NodeAdapter.class));
    queryManager.reloadSiteConfigs();
    // assert that a query service was started
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
    assertThat(queryManager.getServices().get(SITE_ID2).isRunning(), is(true));
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID3)));
  }

  @Test
  public void updateQueryServices() {
    queryManager.putService(SITE_ID, queryService);
    Map<String, Site> newSites = mock(Map.class);
    when(newSites.remove(SITE_ID)).thenReturn(site);
    queryManager.updateQueryServices(newSites);
    verify(queryService).update(site);
  }

  @Test
  public void updateQueryServicesHandlesAdapterException() {
    queryManager.putService(SITE_ID, queryService);
    Map<String, Site> newSites = mock(Map.class);
    when(newSites.remove(SITE_ID)).thenReturn(site);
    doThrow(new AdapterException("")).when(queryService).update(site);
    queryManager.updateQueryServices(newSites);
    verify(queryService).update(site);
    verify(queryService).stop();
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
  }

  @Test(expected = AdapterInterruptedException.class)
  public void updateQueryServicesThrowsAdapterInterruptedException() {
    queryManager.putService(SITE_ID, queryService);
    Map<String, Site> newSites = mock(Map.class);
    when(newSites.remove(SITE_ID)).thenReturn(site);
    doThrow(new AdapterInterruptedException(new InterruptedException()))
        .when(queryService)
        .update(site);
    queryManager.updateQueryServices(newSites);
    verify(queryService).stop();
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
  }

  @Test
  public void updateQueryServicesHandlesIllegalArgumentException() {
    queryManager.putService(SITE_ID, queryService);
    Map<String, Site> newSites = mock(Map.class);
    when(newSites.remove(SITE_ID)).thenReturn(site);
    doThrow(new IllegalArgumentException("")).when(queryService).update(site);
    queryManager.updateQueryServices(newSites);
    verify(queryService).update(site);
    verify(queryService).stop();
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
  }

  @Test
  public void updateQueryServicesRemovedSite() {
    queryManager.putService(SITE_ID, queryService);
    Map<String, Site> newSites = mock(Map.class);
    queryManager.updateQueryServices(newSites);
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
    verify(queryService).stop();
  }

  @Test
  public void updateQueryServicesNoLongerPolledSite() {
    queryManager.putService(SITE_ID, queryService);
    Map<String, Site> newSites = mock(Map.class);
    Site notPolledSite = mock(Site.class);
    when(notPolledSite.getType()).thenReturn(SiteType.ION);
    when(newSites.remove(SITE_ID)).thenReturn(notPolledSite);
    queryManager.updateQueryServices(newSites);
    verify(queryService).stop();
    assertThat(queryManager.getServices().keySet(), Matchers.not(Matchers.contains(SITE_ID)));
  }
}
