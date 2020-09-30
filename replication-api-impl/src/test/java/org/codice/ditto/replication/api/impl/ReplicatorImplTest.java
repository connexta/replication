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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.security.Subject;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import org.codice.ddf.security.Security;
import org.codice.ditto.replication.api.ReplicationItemManager;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicationStore;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.ReplicatorStoreFactory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.Verifier;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.data.SyncRequestImpl;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorImplTest {

  private static final String URL1 = "https://site1:1234";
  private static final String URL2 = "https://site2:1234";
  private static final String VERIFIED_SUFFIX = "/verified";
  private static final String VERIFIED_URL1 =
      ReplicatorImplTest.URL1 + ReplicatorImplTest.VERIFIED_SUFFIX;
  private static final String VERIFIED_URL2 =
      ReplicatorImplTest.URL2 + ReplicatorImplTest.VERIFIED_SUFFIX;

  ReplicatorImpl replicator;

  @Mock ReplicatorStoreFactory replicatorStoreFactory;
  @Mock ReplicatorHistory history;
  @Mock ReplicationItemManager persistentStore;
  @Mock Verifier verifier;
  @Mock ExecutorService executor;
  @Mock Security security;

  @Mock ReplicationStore store1;
  @Mock ReplicationStore store2;
  @Mock SyncHelper helper;

  ReplicationSite site1;
  ReplicationSite site2;
  ReplicatorConfigImpl config;

  @Before
  public void setUp() throws Exception {
    FilterBuilder builder = new GeotoolsFilterBuilder();
    replicator =
        new ReplicatorImpl(
            replicatorStoreFactory,
            history,
            persistentStore,
            verifier,
            executor,
            builder,
            security) {
          @Override
          SyncHelper createSyncHelper(
              ReplicationStore source,
              ReplicationStore destination,
              ReplicatorConfig config,
              ReplicationStatus status) {
            return helper;
          }
        };
    Subject subject = mock(Subject.class);
    Answer answer =
        i -> {
          i.getArgumentAt(0, Runnable.class).run();
          return null;
        };
    doAnswer(answer).when(subject).execute(any(Runnable.class));
    when(security.runAsAdmin(any(PrivilegedAction.class))).thenReturn(subject);
    site1 = new ReplicationSiteImpl();
    site1.setId("srcId");
    site1.setName("source");
    site1.setUrl(ReplicatorImplTest.URL1);
    site1.setVerifiedUrl(ReplicatorImplTest.VERIFIED_URL1);
    site2 = new ReplicationSiteImpl();
    site2.setId("srcId");
    site2.setName("destination");
    site2.setUrl(ReplicatorImplTest.URL2);
    site2.setVerifiedUrl(ReplicatorImplTest.VERIFIED_URL2);
    config = new ReplicatorConfigImpl();
    config.setName("test");
    config.setSource(site1.getId());
    config.setDestination(site2.getId());
    config.setId("id");
    config.setBidirectional(true);
    config.setFilter("cql");
    config.setVersion(0);
  }

  @Test
  public void executeSyncRequest() throws Exception {
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    when(helper.sync()).thenAnswer(i -> updateStatusAndReturnResponse(status, response));

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, times(2)).sync();
    verify(replicatorStoreFactory, times(2)).createReplicatorStore(any());
    verify(store1).isAvailable();
    verify(store2).isAvailable();
    verify(verifier, never()).verify(any());

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.SUCCESS));
  }

  @Test
  public void executeSyncRequestSourceSiteIsRemoteManaged() throws Exception {
    site1.setRemoteManaged(true);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history, never()).addReplicationEvent(any());
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, never()).createReplicatorStore(any());
    verify(store1, never()).isAvailable();
    verify(store2, never()).isAvailable();
    verify(verifier, never()).verify(any());

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.PENDING));
  }

  @Test
  public void executeSyncRequestDestinationSiteIsRemoteManaged() throws Exception {
    site2.setRemoteManaged(true);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history, never()).addReplicationEvent(any());
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, never()).createReplicatorStore(any());
    verify(store1, never()).isAvailable();
    verify(store2, never()).isAvailable();
    verify(verifier, never()).verify(any());

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.PENDING));
  }

  @Test
  public void executeSyncRequestSourceSiteNotVerifiedAndWontVerify() throws Exception {
    site1.setVerifiedUrl(null);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, never()).createReplicatorStore(any());
    verify(store1, never()).isAvailable();
    verify(store2, never()).isAvailable();
    verify(verifier).verify(site1);
    verify(verifier, never()).verify(site2);

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.CONNECTION_UNAVAILABLE));
  }

  @Test
  public void executeSyncRequestDestinationSiteNotVerifiedAndWontVerify() throws Exception {
    site2.setVerifiedUrl(null);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(store1.isAvailable()).thenReturn(true);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, never()).createReplicatorStore(any());
    verify(store1, never()).isAvailable();
    verify(store2, never()).isAvailable();
    verify(verifier, never()).verify(site1);
    verify(verifier).verify(site2);

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.CONNECTION_UNAVAILABLE));
  }

  @Test
  public void executeSyncRequestSourceSiteNotVerifiedThenVerifiesAsLocal() throws Exception {
    site1.setVerifiedUrl(null);
    doAnswer(this::setVerifiedUrl).when(verifier).verify(site1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    when(helper.sync()).thenAnswer(i -> updateStatusAndReturnResponse(status, response));

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, times(2)).sync();
    verify(replicatorStoreFactory, times(2)).createReplicatorStore(any());
    verify(store1).isAvailable();
    verify(store2).isAvailable();
    verify(verifier).verify(site1);
    verify(verifier, never()).verify(site2);

    Assert.assertThat(site1.getVerifiedUrl(), Matchers.equalTo(ReplicatorImplTest.VERIFIED_URL1));
    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.SUCCESS));
  }

  @Test
  public void executeSyncRequestDestinationSiteNotVerifiedThenVerifiesAsLocal() throws Exception {
    site2.setVerifiedUrl(null);
    doAnswer(this::setVerifiedUrl).when(verifier).verify(site2);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    when(helper.sync()).thenAnswer(i -> updateStatusAndReturnResponse(status, response));

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, times(2)).sync();
    verify(replicatorStoreFactory, times(2)).createReplicatorStore(any());
    verify(store1).isAvailable();
    verify(store2).isAvailable();
    verify(verifier, never()).verify(site1);
    verify(verifier).verify(site2);

    Assert.assertThat(site2.getVerifiedUrl(), Matchers.equalTo(ReplicatorImplTest.VERIFIED_URL2));
    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.SUCCESS));
  }

  @Test
  public void executeSyncRequestSourceSiteNotVerifiedThenVerifiesAsRemote() throws Exception {
    site1.setVerifiedUrl(null);
    doAnswer(this::setVerifiedUrlAndSetAsRemote).when(verifier).verify(site1);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history, never()).addReplicationEvent(any());
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, never()).createReplicatorStore(any());
    verify(verifier).verify(site1);
    verify(verifier, never()).verify(site2);

    Assert.assertThat(site1.getVerifiedUrl(), Matchers.equalTo(ReplicatorImplTest.VERIFIED_URL1));
    Assert.assertThat(site1.isRemoteManaged(), Matchers.equalTo(true));
    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.PENDING));
  }

  @Test
  public void executeSyncRequestDestinationSiteNotVerifiedThenVerifiesAsRemote() throws Exception {
    site2.setVerifiedUrl(null);
    doAnswer(this::setVerifiedUrlAndSetAsRemote).when(verifier).verify(site2);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history, never()).addReplicationEvent(any());
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, never()).createReplicatorStore(any());
    verify(verifier, never()).verify(site1);
    verify(verifier).verify(site2);

    Assert.assertThat(site2.getVerifiedUrl(), Matchers.equalTo(ReplicatorImplTest.VERIFIED_URL2));
    Assert.assertThat(site2.isRemoteManaged(), Matchers.equalTo(true));
    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.PENDING));
  }

  @Test
  public void executeSyncRequestFailToCreateSourceStore() throws Exception {
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenThrow(new RuntimeException("testing"));
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    when(helper.sync()).thenReturn(response);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, times(1)).createReplicatorStore(any());
    verify(store1, never()).isAvailable();
    verify(store2, never()).isAvailable();
    verify(verifier, never()).verify(any());

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.CONNECTION_UNAVAILABLE));
  }

  @Test
  public void executeSyncRequestFailToCreateDestinationStore() throws Exception {
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenThrow(new RuntimeException("testing"));
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    when(helper.sync()).thenReturn(response);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, times(2)).createReplicatorStore(any());
    verify(store1).isAvailable();
    verify(store2, never()).isAvailable();
    verify(verifier, never()).verify(any());

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.CONNECTION_UNAVAILABLE));
  }

  @Test
  public void executeSyncRequestSourceStoreIsNotAvailable() throws Exception {
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(false);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    when(helper.sync()).thenReturn(response);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, times(1)).createReplicatorStore(any());
    verify(store1).isAvailable();
    verify(store2, never()).isAvailable();
    verify(verifier, never()).verify(any());

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.CONNECTION_UNAVAILABLE));
  }

  @Test
  public void executeSyncRequestDestinationStoreIsNotAvailable() throws Exception {
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(false);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    when(helper.sync()).thenReturn(response);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.executeSyncRequest(request);

    verify(history).addReplicationEvent(status);
    verify(helper, never()).sync();
    verify(replicatorStoreFactory, times(2)).createReplicatorStore(any());
    verify(store1).isAvailable();
    verify(store2).isAvailable();
    verify(verifier, never()).verify(any());

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.CONNECTION_UNAVAILABLE));
  }

  @Test
  public void cancelPendingSyncRequest() throws Exception {
    BlockingQueue<SyncRequest> queue = mock(BlockingQueue.class);
    replicator.setPendingSyncRequestsQueue(queue);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);

    replicator.submitSyncRequest(request);
    verify(queue, times(1)).put(request);

    replicator.cancelSyncRequest(request);
    verify(queue, times(1)).remove(request);
  }

  @Test
  public void cancelActiveSyncRequest() throws Exception {
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.CANCELED);

    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);
    when(helper.sync()).thenAnswer(i -> cancelSyncRequest(status, response, request));

    replicator.executeSyncRequest(request);

    verify(helper).cancel();

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.CANCELED));
  }

  @Test
  public void cancelActiveSyncRequestByConfigId() throws Exception {
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL1)))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL(ReplicatorImplTest.VERIFIED_URL2)))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.CANCELED);

    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    SyncRequest request = new SyncRequestImpl(config, site1, site2, status);
    when(helper.sync()).thenAnswer(i -> cancelSyncRequest(status, response, config));

    replicator.submitSyncRequest(request);
    replicator.executeSyncRequest(request);

    verify(helper).cancel();

    Assert.assertThat(status.getStatus(), Matchers.equalTo(Status.CANCELED));
  }

  private Void setVerifiedUrl(InvocationOnMock invocation) {
    final ReplicationSite site = invocation.getArgumentAt(0, ReplicationSite.class);

    site.setVerifiedUrl(site.getUrl() + ReplicatorImplTest.VERIFIED_SUFFIX);
    return null;
  }

  private Void setVerifiedUrlAndSetAsRemote(InvocationOnMock invocation) {
    final ReplicationSite site = invocation.getArgumentAt(0, ReplicationSite.class);

    site.setVerifiedUrl(site.getUrl() + ReplicatorImplTest.VERIFIED_SUFFIX);
    site.setRemoteManaged(true);
    return null;
  }

  private SyncResponse updateStatusAndReturnResponse(
      ReplicationStatusImpl status, SyncResponse response) {
    status.setStatus(response.getStatus());
    return response;
  }

  private SyncResponse cancelSyncRequest(
      ReplicationStatusImpl status, SyncResponse response, SyncRequest request) {
    replicator.cancelSyncRequest(request);
    status.setStatus(response.getStatus());
    return response;
  }

  private SyncResponse cancelSyncRequest(
      ReplicationStatusImpl status, SyncResponse response, ReplicatorConfig config) {
    replicator.cancelSyncRequest(config.getId());
    status.setStatus(response.getStatus());
    return response;
  }
}
