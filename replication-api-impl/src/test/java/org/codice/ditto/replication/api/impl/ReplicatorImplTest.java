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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.security.Subject;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationPersistentStore;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicationStore;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.ReplicatorStoreFactory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.data.SyncRequestImpl;
import org.codice.ditto.replication.api.modern.ReplicationSite;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorImplTest {

  ReplicatorImpl replicator;

  @Mock ReplicatorStoreFactory replicatorStoreFactory;
  @Mock ReplicatorHistory history;
  @Mock ReplicationPersistentStore persistentStore;
  @Mock ReplicationSitePersistentStore siteStore;
  @Mock ExecutorService executor;
  @Mock Security security;

  @Mock ReplicationStore store1;
  @Mock ReplicationStore store2;
  @Mock SyncHelper helper;

  ReplicatorConfigImpl config;

  @Before
  public void setUp() throws Exception {
    FilterBuilder builder = new GeotoolsFilterBuilder();
    replicator =
        new ReplicatorImpl(
            replicatorStoreFactory,
            history,
            persistentStore,
            siteStore,
            executor,
            builder,
            security) {
          SyncHelper getSyncHelper(
              ReplicationStore source,
              ReplicationStore destination,
              ReplicatorConfig config,
              ReplicationPersistentStore persistentStore,
              ReplicatorHistory history,
              FilterBuilder builder) {
            return helper;
          }
        };
    Subject subject = mock(Subject.class);
    Answer answer =
        new Answer() {
          @Override
          public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            ((Runnable) invocationOnMock.getArguments()[0]).run();
            return null;
          }
        };
    doAnswer(answer).when(subject).execute(any(Runnable.class));
    when(security.runAsAdmin(any(PrivilegedAction.class))).thenReturn(subject);
    config = new ReplicatorConfigImpl();
    config.setName("test");
    config.setSource("srcId");
    config.setDestination("destId");
    config.setId("id");
    config.setDirection(Direction.BOTH);
    config.setCql("cql");
    config.setReplicationType(ReplicationType.RESOURCE);
    config.setVersion(0);
  }

  @Test
  public void init() {}

  @Test
  public void executeSyncRequest() throws Exception {
    ReplicationSite site1 = mock(ReplicationSite.class);
    ReplicationSite site2 = mock(ReplicationSite.class);
    when(site1.getUrl()).thenReturn(new URL("https://site1:1234"));
    when(site2.getUrl()).thenReturn(new URL("https://site2:1234"));
    when(siteStore.getSite("srcId")).thenReturn(Optional.of(site1));
    when(siteStore.getSite("destId")).thenReturn(Optional.of(site2));
    when(replicatorStoreFactory.createReplicatorStore(new URL("https://site1:1234")))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL("https://site2:1234")))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    when(helper.sync()).thenReturn(response);
    ReplicationStatus status = new ReplicationStatus("test");
    SyncRequest request = new SyncRequestImpl(config, status);
    replicator.executeSyncRequest(request);
    verify(history).addReplicationEvent(status);
    verify(siteStore).getSite("srcId");
    verify(siteStore).getSite("destId");
    verify(helper, times(2)).sync();
    assertThat(request.getStatus().getPullCount(), is(1L));
    assertThat(request.getStatus().getPullBytes(), is(10L));
    assertThat(request.getStatus().getPushCount(), is(1L));
    assertThat(request.getStatus().getPushBytes(), is(10L));
    assertThat(request.getStatus().getStatus(), is(Status.SUCCESS));
  }

  @Test
  public void cleanUp() {}

  @Test
  public void submitSyncRequest() {}

  @Test
  public void cancelPendingSyncRequest() throws Exception {
    ReplicationStatus status = new ReplicationStatus("test");
    SyncRequest request = new SyncRequestImpl(config, status);
    replicator.submitSyncRequest(request);
    assertThat(replicator.getPendingSyncRequests().size(), is(1));
    replicator.cancelSyncRequest(request);
    assertThat(replicator.getPendingSyncRequests().size(), is(0));
  }

  @Test
  public void cancelActiveSyncRequest() throws Exception {
    ReplicationSite site1 = mock(ReplicationSite.class);
    ReplicationSite site2 = mock(ReplicationSite.class);
    when(site1.getUrl()).thenReturn(new URL("https://site1:1234"));
    when(site2.getUrl()).thenReturn(new URL("https://site2:1234"));
    when(siteStore.getSite("srcId")).thenReturn(Optional.of(site1));
    when(siteStore.getSite("destId")).thenReturn(Optional.of(site2));
    when(replicatorStoreFactory.createReplicatorStore(new URL("https://site1:1234")))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL("https://site2:1234")))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.CANCELED);

    ReplicationStatus status = new ReplicationStatus("test");
    SyncRequest request = new SyncRequestImpl(config, status);
    Answer answer =
        new Answer() {
          @Override
          public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            replicator.cancelSyncRequest(request);
            return response;
          }
        };
    when(helper.sync()).thenAnswer(answer);
    replicator.executeSyncRequest(request);
    verify(helper).cancel();
  }

  @Test
  public void getPendingSyncRequests() {}

  @Test
  public void getActiveSyncRequests() {}
}
