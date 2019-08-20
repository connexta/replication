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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.NodeAdapter;
import com.connexta.ion.replication.api.NodeAdapterFactory;
import com.connexta.ion.replication.api.NodeAdapterType;
import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.SyncRequest;
import com.connexta.replication.api.data.ReplicatorConfig;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.impl.data.SiteImpl;
import com.connexta.replication.api.persistence.SiteManager;
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

  private static final String SOURCE_ID = "sourceId";

  private static final String DESTINATION_ID = "destinationId";

  private static final String SOURCE_URL = "https://source:1234";

  private static final String DESTINATION_URL = "https://destination:1234";

  private static final String REPLICATOR_CONFIG_ID = "replicatorConfigId";

  private ReplicatorImpl replicator;

  @Mock ExecutorService executor;

  @Mock NodeAdapters nodeAdapters;

  @Mock SiteManager siteManager;

  @Mock Syncer syncer;

  @Mock NodeAdapterFactory nodeAdapterFactory;

  @Before
  public void setUp() throws Exception {
    replicator = new ReplicatorImpl(nodeAdapters, siteManager, executor, syncer);
  }

  @Test
  public void executeBiDirectionalSyncRequest() throws Exception {
    // setup
    ReplicatorConfig replicatorConfig = mockConfig();

    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getConfig()).thenReturn(replicatorConfig);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(SOURCE_URL);
    when(sourceSite.getType()).thenReturn(NodeAdapterType.DDF.name());
    Site destinationSite = mock(Site.class);
    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
    when(destinationSite.getType()).thenReturn(NodeAdapterType.DDF.name());

    when(siteManager.get(SOURCE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);
    NodeAdapter destinationNode = mock(NodeAdapter.class);
    when(destinationNode.isAvailable()).thenReturn(true);

    when(nodeAdapterFactory.create(new URL(SOURCE_URL))).thenReturn(sourceNode);
    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);

    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenReturn(nodeAdapterFactory);

    Syncer.Job job = mock(Syncer.Job.class);
    when(syncer.create(sourceNode, destinationNode, replicatorConfig, Set.of())).thenReturn(job);
    when(syncer.create(destinationNode, sourceNode, replicatorConfig, Set.of())).thenReturn(job);

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(syncer).create(sourceNode, destinationNode, replicatorConfig, Set.of());
    verify(syncer).create(destinationNode, sourceNode, replicatorConfig, Set.of());
    verify(sourceNode, times(1)).close();
    verify(destinationNode, times(1)).close();
  }

  @Test
  public void testUnknownSyncError() throws Exception {
    // setup
    ReplicatorConfig replicatorConfig = mockConfig();

    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getConfig()).thenReturn(replicatorConfig);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(SOURCE_URL);
    when(sourceSite.getType()).thenReturn(NodeAdapterType.DDF.name());
    Site destinationSite = mock(Site.class);
    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
    when(destinationSite.getType()).thenReturn(NodeAdapterType.DDF.name());

    when(siteManager.get(SOURCE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);
    NodeAdapter destinationNode = mock(NodeAdapter.class);
    when(destinationNode.isAvailable()).thenReturn(true);

    when(nodeAdapterFactory.create(new URL(SOURCE_URL))).thenReturn(sourceNode);
    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);

    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenReturn(nodeAdapterFactory);

    Syncer.Job job = mock(Syncer.Job.class);
    doThrow(RuntimeException.class).when(job).sync();
    when(syncer.create(destinationNode, sourceNode, replicatorConfig, Set.of())).thenReturn(job);

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(sourceNode, times(1)).close();
    verify(destinationNode, times(1)).close();
  }

  @Test
  public void testConnectionUnavailable() throws Exception {
    // setup
    ReplicatorConfig replicatorConfig = mockConfig();

    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getConfig()).thenReturn(replicatorConfig);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(SOURCE_URL);
    when(sourceSite.getType()).thenReturn(NodeAdapterType.DDF.name());
    Site destinationSite = mock(Site.class);
    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
    when(destinationSite.getType()).thenReturn(NodeAdapterType.DDF.name());

    when(siteManager.get(SOURCE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);
    NodeAdapter destinationNode = mock(NodeAdapter.class);
    when(destinationNode.isAvailable()).thenReturn(false);

    when(nodeAdapterFactory.create(new URL(SOURCE_URL))).thenReturn(sourceNode);
    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);

    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenReturn(nodeAdapterFactory);

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
    ReplicatorConfig replicatorConfig = mockConfig();

    SyncRequest syncRequest = mock(SyncRequest.class);
    when(syncRequest.getConfig()).thenReturn(replicatorConfig);

    Site sourceSite = mock(Site.class);
    when(sourceSite.getUrl()).thenReturn(SOURCE_URL);
    when(sourceSite.getType()).thenReturn(NodeAdapterType.DDF.name());

    when(siteManager.get(SOURCE_ID)).thenReturn(sourceSite);
    when(siteManager.get(DESTINATION_ID)).thenThrow(new NotFoundException());

    NodeAdapter sourceNode = mock(NodeAdapter.class);
    when(sourceNode.isAvailable()).thenReturn(true);

    when(nodeAdapterFactory.create(new URL(SOURCE_URL))).thenReturn(sourceNode);

    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenReturn(nodeAdapterFactory);

    // when
    replicator.executeSyncRequest(syncRequest);

    // then
    verify(sourceNode, times(1)).close();
    verify(syncer, never())
        .create(
            any(NodeAdapter.class),
            any(NodeAdapter.class),
            any(ReplicatorConfig.class),
            any(Set.class));
  }

  @Test
  public void testGetStoreForIdNoType() throws Exception {
    Site destinationSite = new SiteImpl();
    destinationSite.setUrl(DESTINATION_URL);
    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);

    NodeAdapter destinationNode = mock(NodeAdapter.class);
    when(destinationNode.isAvailable()).thenReturn(true);

    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);

    when(nodeAdapters.factoryFor(NodeAdapterType.ION)).thenReturn(nodeAdapterFactory);
    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenThrow(new RuntimeException("error"));

    replicator.getStoreForId(DESTINATION_ID);

    assertThat(destinationSite.getType(), is(NodeAdapterType.ION.name()));
    verify(siteManager).save(destinationSite);
    verify(nodeAdapters, times(3)).factoryFor(any(NodeAdapterType.class));
  }

  private ReplicatorConfig mockConfig() {
    ReplicatorConfig replicatorConfig = mock(ReplicatorConfig.class);
    when(replicatorConfig.getSource()).thenReturn(SOURCE_ID);
    when(replicatorConfig.getDestination()).thenReturn(DESTINATION_ID);
    when(replicatorConfig.isBidirectional()).thenReturn(true);
    return replicatorConfig;
  }
}
