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

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorImplTest {

  //  private static final String SOURCE_ID = "sourceId";
  //
  //  private static final String DESTINATION_ID = "destinationId";
  //
  //  private static final String SOURCE_URL = "https://source:1234";
  //
  //  private static final String DESTINATION_URL = "https://destination:1234";
  //
  //  private static final String REPLICATOR_CONFIG_ID = "replicatorConfigId";
  //
  //  private ReplicatorImpl replicator;
  //
  //  @Mock ExecutorService executor;
  //
  //  @Mock NodeAdapters nodeAdapters;
  //
  //  @Mock ReplicatorHistoryManager replicatorHistoryManager;
  //
  //  @Mock SiteManager siteManager;
  //
  //  @Mock Syncer syncer;
  //
  //  @Mock NodeAdapterFactory nodeAdapterFactory;
  //
  //  @Before
  //  public void setUp() throws Exception {
  //    replicator =
  //        new ReplicatorImpl(nodeAdapters, replicatorHistoryManager, siteManager, executor,
  // syncer);
  //  }
  //
  //  @Test
  //  public void executeBiDirectionalSyncRequest() throws Exception {
  //    // setup
  //    ReplicationStatus replicationStatus = mock(ReplicationStatus.class);
  //    ReplicatorConfig replicatorConfig = mockConfig();
  //
  //    SyncRequest syncRequest = mock(SyncRequest.class);
  //    when(syncRequest.getStatus()).thenReturn(replicationStatus);
  //    when(syncRequest.getConfig()).thenReturn(replicatorConfig);
  //
  //    ReplicationSite sourceSite = mock(ReplicationSite.class);
  //    when(sourceSite.getUrl()).thenReturn(SOURCE_URL);
  //    when(sourceSite.getType()).thenReturn(NodeAdapterType.DDF.name());
  //    ReplicationSite destinationSite = mock(ReplicationSite.class);
  //    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
  //    when(destinationSite.getType()).thenReturn(NodeAdapterType.DDF.name());
  //
  //    when(siteManager.get(SOURCE_ID)).thenReturn(sourceSite);
  //    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);
  //
  //    NodeAdapter sourceNode = mock(NodeAdapter.class);
  //    when(sourceNode.isAvailable()).thenReturn(true);
  //    NodeAdapter destinationNode = mock(NodeAdapter.class);
  //    when(destinationNode.isAvailable()).thenReturn(true);
  //
  //    when(nodeAdapterFactory.create(new URL(SOURCE_URL))).thenReturn(sourceNode);
  //    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);
  //
  //    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenReturn(nodeAdapterFactory);
  //
  //    SyncResponse syncResponse = mock(SyncResponse.class);
  //    when(syncResponse.getStatus()).thenReturn(Status.SUCCESS);
  //    Syncer.Job job = mock(Syncer.Job.class);
  //    when(job.sync()).thenReturn(syncResponse);
  //    when(syncer.create(sourceNode, destinationNode, replicatorConfig, replicationStatus))
  //        .thenReturn(job);
  //
  //    when(syncer.create(destinationNode, sourceNode, replicatorConfig, replicationStatus))
  //        .thenReturn(job);
  //
  //    // when
  //    replicator.executeSyncRequest(syncRequest);
  //
  //    // then
  //    verify(replicationStatus, times(1)).setStatus(Status.PULL_IN_PROGRESS);
  //    verify(replicationStatus, times(1)).setStatus(Status.PUSH_IN_PROGRESS);
  //    verify(replicatorHistoryManager, times(1)).save(replicationStatus);
  //    verify(sourceNode, times(1)).close();
  //    verify(destinationNode, times(1)).close();
  //  }
  //
  //  @Test
  //  public void testUnknownSyncError() throws Exception {
  //    // setup
  //    ReplicationStatus replicationStatus = mock(ReplicationStatus.class);
  //    ReplicatorConfig replicatorConfig = mockConfig();
  //
  //    SyncRequest syncRequest = mock(SyncRequest.class);
  //    when(syncRequest.getStatus()).thenReturn(replicationStatus);
  //    when(syncRequest.getConfig()).thenReturn(replicatorConfig);
  //
  //    ReplicationSite sourceSite = mock(ReplicationSite.class);
  //    when(sourceSite.getUrl()).thenReturn(SOURCE_URL);
  //    when(sourceSite.getType()).thenReturn(NodeAdapterType.DDF.name());
  //    ReplicationSite destinationSite = mock(ReplicationSite.class);
  //    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
  //    when(destinationSite.getType()).thenReturn(NodeAdapterType.DDF.name());
  //
  //    when(siteManager.get(SOURCE_ID)).thenReturn(sourceSite);
  //    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);
  //
  //    NodeAdapter sourceNode = mock(NodeAdapter.class);
  //    when(sourceNode.isAvailable()).thenReturn(true);
  //    NodeAdapter destinationNode = mock(NodeAdapter.class);
  //    when(destinationNode.isAvailable()).thenReturn(true);
  //
  //    when(nodeAdapterFactory.create(new URL(SOURCE_URL))).thenReturn(sourceNode);
  //    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);
  //
  //    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenReturn(nodeAdapterFactory);
  //
  //    SyncResponse syncResponse = mock(SyncResponse.class);
  //    when(syncResponse.getStatus()).thenReturn(Status.SUCCESS);
  //    Syncer.Job job = mock(Syncer.Job.class);
  //    when(job.sync()).thenThrow(Exception.class);
  //    when(syncer.create(destinationNode, sourceNode, replicatorConfig, replicationStatus))
  //        .thenReturn(job);
  //
  //    // when
  //    replicator.executeSyncRequest(syncRequest);
  //
  //    // then
  //    verify(replicationStatus, times(1)).setStatus(Status.FAILURE);
  //    verify(replicatorHistoryManager, times(1)).save(replicationStatus);
  //    verify(sourceNode, times(1)).close();
  //    verify(destinationNode, times(1)).close();
  //  }
  //
  //  @Test
  //  public void testConnectionUnavailable() throws Exception {
  //    // setup
  //    ReplicationStatus replicationStatus = mock(ReplicationStatus.class);
  //    ReplicatorConfig replicatorConfig = mockConfig();
  //
  //    SyncRequest syncRequest = mock(SyncRequest.class);
  //    when(syncRequest.getStatus()).thenReturn(replicationStatus);
  //    when(syncRequest.getConfig()).thenReturn(replicatorConfig);
  //
  //    ReplicationSite sourceSite = mock(ReplicationSite.class);
  //    when(sourceSite.getUrl()).thenReturn(SOURCE_URL);
  //    when(sourceSite.getType()).thenReturn(NodeAdapterType.DDF.name());
  //    ReplicationSite destinationSite = mock(ReplicationSite.class);
  //    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
  //    when(destinationSite.getType()).thenReturn(NodeAdapterType.DDF.name());
  //
  //    when(siteManager.get(SOURCE_ID)).thenReturn(sourceSite);
  //    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);
  //
  //    NodeAdapter sourceNode = mock(NodeAdapter.class);
  //    when(sourceNode.isAvailable()).thenReturn(true);
  //    NodeAdapter destinationNode = mock(NodeAdapter.class);
  //    when(destinationNode.isAvailable()).thenReturn(false);
  //
  //    when(nodeAdapterFactory.create(new URL(SOURCE_URL))).thenReturn(sourceNode);
  //    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);
  //
  //    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenReturn(nodeAdapterFactory);
  //
  //    SyncResponse syncResponse = mock(SyncResponse.class);
  //    when(syncResponse.getStatus()).thenReturn(Status.SUCCESS);
  //    Syncer.Job job = mock(Syncer.Job.class);
  //    when(job.sync()).thenReturn(syncResponse);
  //    when(syncer.create(sourceNode, destinationNode, replicatorConfig, replicationStatus))
  //        .thenReturn(job);
  //    when(syncer.create(destinationNode, sourceNode, replicatorConfig, replicationStatus))
  //        .thenReturn(job);
  //
  //    // when
  //    replicator.executeSyncRequest(syncRequest);
  //
  //    // then
  //    verify(replicationStatus, times(1)).setStatus(Status.CONNECTION_UNAVAILABLE);
  //    verify(replicatorHistoryManager, times(1)).save(replicationStatus);
  //    verify(sourceNode, times(1)).close();
  //    verify(destinationNode, never()).close();
  //  }
  //
  //  @Test
  //  public void cancelPendingSyncRequest() throws Exception {
  //    ReplicatorConfig replicatorConfig = mockConfig();
  //    BlockingQueue<SyncRequest> queue = mock(BlockingQueue.class);
  //    replicator.setPendingSyncRequestsQueue(queue);
  //    ReplicationStatus replicationStatus = mock(ReplicationStatus.class);
  //    when(replicationStatus.getReplicatorId()).thenReturn(REPLICATOR_CONFIG_ID);
  //    SyncRequest syncRequest = mock(SyncRequest.class);
  //    when(syncRequest.getStatus()).thenReturn(replicationStatus);
  //    when(syncRequest.getConfig()).thenReturn(replicatorConfig);
  //    replicator.submitSyncRequest(syncRequest);
  //    verify(queue, times(1)).put(syncRequest);
  //    replicator.cancelSyncRequest(syncRequest);
  //    verify(queue, times(1)).remove(syncRequest);
  //  }
  //
  //  @Test
  //  public void cancelActiveSyncRequest() throws Exception {
  //    // setup
  //    ReplicationStatus replicationStatus = mock(ReplicationStatus.class);
  //    ReplicatorConfig replicatorConfig = mockConfig();
  //
  //    SyncRequest syncRequest = mock(SyncRequest.class);
  //    when(syncRequest.getStatus()).thenReturn(replicationStatus);
  //    when(syncRequest.getConfig()).thenReturn(replicatorConfig);
  //
  //    ReplicationSite sourceSite = mock(ReplicationSite.class);
  //    when(sourceSite.getUrl()).thenReturn(SOURCE_URL);
  //    when(sourceSite.getType()).thenReturn(NodeAdapterType.DDF.name());
  //    ReplicationSite destinationSite = mock(ReplicationSite.class);
  //    when(destinationSite.getUrl()).thenReturn(DESTINATION_URL);
  //    when(destinationSite.getType()).thenReturn(NodeAdapterType.DDF.name());
  //
  //    when(siteManager.get(SOURCE_ID)).thenReturn(sourceSite);
  //    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);
  //
  //    NodeAdapter sourceNode = mock(NodeAdapter.class);
  //    when(sourceNode.isAvailable()).thenReturn(true);
  //    NodeAdapter destinationNode = mock(NodeAdapter.class);
  //    when(destinationNode.isAvailable()).thenReturn(true);
  //
  //    when(nodeAdapterFactory.create(new URL(SOURCE_URL))).thenReturn(sourceNode);
  //    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);
  //
  //    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenReturn(nodeAdapterFactory);
  //
  //    SyncResponse syncResponse = mock(SyncResponse.class);
  //    when(syncResponse.getStatus()).thenReturn(Status.CANCELED);
  //    Syncer.Job job = mock(Syncer.Job.class);
  //
  //    Answer answer =
  //        invocationOnMock -> {
  //          replicator.cancelSyncRequest(syncRequest);
  //          return syncResponse;
  //        };
  //
  //    when(job.sync()).thenAnswer(answer);
  //    when(syncer.create(sourceNode, destinationNode, replicatorConfig, replicationStatus))
  //        .thenReturn(job);
  //    when(syncer.create(destinationNode, sourceNode, replicatorConfig, replicationStatus))
  //        .thenReturn(job);
  //
  //    // when
  //    replicator.executeSyncRequest(syncRequest);
  //
  //    // then
  //    verify(job).cancel();
  //  }
  //
  //  @Test
  //  public void testGetStoreForIdNoType() throws Exception {
  //    ReplicationSite destinationSite = new ReplicationSiteImpl();
  //    destinationSite.setUrl(DESTINATION_URL);
  //    when(siteManager.get(DESTINATION_ID)).thenReturn(destinationSite);
  //
  //    NodeAdapter destinationNode = mock(NodeAdapter.class);
  //    when(destinationNode.isAvailable()).thenReturn(true);
  //
  //    when(nodeAdapterFactory.create(new URL(DESTINATION_URL))).thenReturn(destinationNode);
  //
  //    when(nodeAdapters.factoryFor(NodeAdapterType.ION)).thenReturn(nodeAdapterFactory);
  //    when(nodeAdapters.factoryFor(NodeAdapterType.DDF)).thenThrow(new RuntimeException("error"));
  //
  //    replicator.getStoreForId(DESTINATION_ID);
  //
  //    assertThat(destinationSite.getType(), is(NodeAdapterType.ION.name()));
  //    verify(siteManager).save(destinationSite);
  //    verify(nodeAdapters, times(3)).factoryFor(any(NodeAdapterType.class));
  //  }
  //
  //  private ReplicatorConfig mockConfig() {
  //    ReplicatorConfig replicatorConfig = mock(ReplicatorConfig.class);
  //    when(replicatorConfig.getSource()).thenReturn(SOURCE_ID);
  //    when(replicatorConfig.getDestination()).thenReturn(DESTINATION_ID);
  //    when(replicatorConfig.isBidirectional()).thenReturn(true);
  //    when(replicatorConfig.getId()).thenReturn(REPLICATOR_CONFIG_ID);
  //    return replicatorConfig;
  //  }
}
