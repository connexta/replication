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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.SyncRequest;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.persistence.SiteManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorImplTest {

  private static final String LOCAL_SITE_ID = "localSiteId";

  private static final String DESTINATION_ID = "destinationId";

  private static final URL LOCAL_SITE_URL;

  private static final URL DESTINATION_URL;

  static {
    try {
      LOCAL_SITE_URL = new URL("https://source:1234");
      DESTINATION_URL = new URL("https://destination:1234");
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

  private ReplicatorImpl replicator;

  @Mock ExecutorService executor;

  @Mock NodeAdapters nodeAdapters;

  @Mock SiteManager siteManager;

  @Mock Syncer syncer;

  @Mock NodeAdapterFactory nodeAdapterFactory;

  @Before
  public void setUp() throws Exception {
    replicator = new ReplicatorImpl(nodeAdapters, siteManager, executor, syncer, LOCAL_SITE_ID);
  }

  @Test
  public void executeSyncRequestForTacticalDdf() throws Exception {
    // setup
    Filter filter = mockFilter();
    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getFilter()).thenReturn(filter);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(LOCAL_SITE_URL);
    when(sourceSite.getType()).thenReturn(SiteType.DDF);
    Site destinationSite = mock(Site.class);
    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
    when(destinationSite.getType()).thenReturn(SiteType.DDF);
    when(destinationSite.getKind()).thenReturn(SiteKind.TACTICAL);

    when(siteManager.get(LOCAL_SITE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);
    NodeAdapter destinationNode = mock(NodeAdapter.class);
    when(destinationNode.isAvailable()).thenReturn(true);

    when(nodeAdapterFactory.create(LOCAL_SITE_URL)).thenReturn(sourceNode);
    when(nodeAdapterFactory.create(DESTINATION_URL)).thenReturn(destinationNode);

    when(nodeAdapters.factoryFor(SiteType.DDF)).thenReturn(nodeAdapterFactory);

    Syncer.Job job = mock(Syncer.Job.class);
    when(syncer.create(sourceNode, destinationNode, filter, Set.of())).thenReturn(job);
    when(syncer.create(destinationNode, sourceNode, filter, Set.of())).thenReturn(job);

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(syncer).create(sourceNode, destinationNode, filter, Set.of());
    verify(syncer).create(destinationNode, sourceNode, filter, Set.of());
    verify(sourceNode, times(1)).close();
    verify(destinationNode, times(1)).close();
  }

  @Test
  public void executeSyncRequestForRegionalDdf() throws Exception {
    // setup
    Filter filter = mockFilter();
    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getFilter()).thenReturn(filter);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(LOCAL_SITE_URL);
    when(sourceSite.getType()).thenReturn(SiteType.DDF);
    Site destinationSite = mock(Site.class);
    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
    when(destinationSite.getType()).thenReturn(SiteType.DDF);
    when(destinationSite.getKind()).thenReturn(SiteKind.REGIONAL);

    when(siteManager.get(LOCAL_SITE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);
    NodeAdapter destinationNode = mock(NodeAdapter.class);
    when(destinationNode.isAvailable()).thenReturn(true);

    when(nodeAdapterFactory.create(LOCAL_SITE_URL)).thenReturn(sourceNode);
    when(nodeAdapterFactory.create(DESTINATION_URL)).thenReturn(destinationNode);

    when(nodeAdapters.factoryFor(SiteType.DDF)).thenReturn(nodeAdapterFactory);

    Syncer.Job job = mock(Syncer.Job.class);
    when(syncer.create(destinationNode, sourceNode, filter, Set.of())).thenReturn(job);

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(syncer).create(destinationNode, sourceNode, filter, Set.of());
    verify(syncer, never()).create(sourceNode, destinationNode, filter, Set.of());
    verify(sourceNode, times(1)).close();
    verify(destinationNode, times(1)).close();
  }

  @Test
  public void executeSyncRequestForIon() throws Exception {
    // setup
    Filter filter = mockFilter();
    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getFilter()).thenReturn(filter);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(LOCAL_SITE_URL);
    when(sourceSite.getType()).thenReturn(SiteType.ION);
    Site destinationSite = mock(Site.class);
    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
    when(destinationSite.getType()).thenReturn(SiteType.ION);

    when(siteManager.get(LOCAL_SITE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);
    NodeAdapter destinationNode = mock(NodeAdapter.class);
    when(destinationNode.isAvailable()).thenReturn(true);

    when(nodeAdapterFactory.create(LOCAL_SITE_URL)).thenReturn(sourceNode);
    when(nodeAdapterFactory.create(DESTINATION_URL)).thenReturn(destinationNode);

    when(nodeAdapters.factoryFor(SiteType.ION)).thenReturn(nodeAdapterFactory);

    Syncer.Job job = mock(Syncer.Job.class);
    when(syncer.create(sourceNode, destinationNode, filter, Set.of())).thenReturn(job);

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(syncer).create(sourceNode, destinationNode, filter, Set.of());
    verify(syncer, never()).create(destinationNode, sourceNode, filter, Set.of());
    verify(sourceNode, times(1)).close();
    verify(destinationNode, times(1)).close();
  }

  @Test
  public void testConnectionUnavailable() throws Exception {
    // setup
    Filter filter = mockFilter();
    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getFilter()).thenReturn(filter);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(LOCAL_SITE_URL);
    when(sourceSite.getType()).thenReturn(SiteType.DDF);
    Site destinationSite = mock(Site.class);
    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
    when(destinationSite.getType()).thenReturn(SiteType.DDF);

    when(siteManager.get(LOCAL_SITE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);
    NodeAdapter destinationNode = mock(NodeAdapter.class);
    when(destinationNode.isAvailable()).thenReturn(false);

    when(nodeAdapterFactory.create(LOCAL_SITE_URL)).thenReturn(sourceNode);
    when(nodeAdapterFactory.create(DESTINATION_URL)).thenReturn(destinationNode);

    when(nodeAdapters.factoryFor(SiteType.DDF)).thenReturn(nodeAdapterFactory);

    Syncer.Job job = mock(Syncer.Job.class);

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(sourceNode, times(1)).close();
    verify(destinationNode, never()).close();
  }

  @Test
  public void testNodeNotFound() throws Exception {
    // setup
    Filter filter = mockFilter();
    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getFilter()).thenReturn(filter);

    when(siteManager.get(DESTINATION_ID)).thenThrow(new NotFoundException("testing"));

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(nodeAdapters, never()).factoryFor(any(SiteType.class));
    verify(syncer, never())
        .create(any(NodeAdapter.class), any(NodeAdapter.class), any(Filter.class), any(Set.class));
  }

  @Test
  public void testExceptionGettingNodeAdapterFactory() throws Exception {
    // setup
    Filter filter = mockFilter();
    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getFilter()).thenReturn(filter);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(LOCAL_SITE_URL);
    when(sourceSite.getType()).thenReturn(SiteType.DDF);

    Site destinationSite = mock(Site.class);
    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
    when(destinationSite.getType()).thenReturn(SiteType.DDF);

    when(siteManager.get(LOCAL_SITE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);

    when(nodeAdapterFactory.create(LOCAL_SITE_URL)).thenReturn(sourceNode);

    when(nodeAdapters.factoryFor(SiteType.DDF))
        .thenReturn(nodeAdapterFactory)
        .thenThrow(new RuntimeException());

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(sourceNode, times(1)).close();
    verify(syncer, never())
        .create(any(NodeAdapter.class), any(NodeAdapter.class), any(Filter.class), any(Set.class));
  }

  private Filter mockFilter() {
    Filter filter = mock(Filter.class);
    when(filter.getSiteId()).thenReturn(DESTINATION_ID);
    return filter;
  }
}
