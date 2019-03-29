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
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.ReplicationPersistentStore;
import org.codice.ditto.replication.api.ReplicationStore;
import org.codice.ditto.replication.api.ReplicatorStoreFactory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.data.SyncRequestImpl;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
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
  @Mock ReplicatorHistoryManager history;
  @Mock ReplicationPersistentStore persistentStore;
  @Mock SiteManager siteManager;
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
            siteManager,
            executor,
            builder,
            security) {
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
    config.setBidirectional(true);
    config.setFilter("cql");
    config.setVersion(0);
  }

  @Test
  public void executeSyncRequest() throws Exception {
    ReplicationSite site1 = mock(ReplicationSite.class);
    ReplicationSite site2 = mock(ReplicationSite.class);
    when(site1.getUrl()).thenReturn("https://site1:1234");
    when(site2.getUrl()).thenReturn("https://site2:1234");
    when(siteManager.get("srcId")).thenReturn(site1);
    when(siteManager.get("destId")).thenReturn(site2);
    when(replicatorStoreFactory.createReplicatorStore(new URL("https://site1:1234")))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL("https://site2:1234")))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.SUCCESS);
    when(helper.sync()).thenReturn(response);
    ReplicationStatusImpl status = new ReplicationStatusImpl();
    status.setReplicatorId("test");
    SyncRequest request = new SyncRequestImpl(config, status);
    replicator.executeSyncRequest(request);
    verify(history).save(status);
    verify(siteManager).get("srcId");
    verify(siteManager).get("destId");
    verify(helper, times(2)).sync();
  }

  @Test
  public void cancelPendingSyncRequest() throws Exception {
    BlockingQueue<SyncRequest> queue = mock(BlockingQueue.class);
    replicator.setPendingSyncRequestsQueue(queue);
    ReplicationStatusImpl status = new ReplicationStatusImpl();
    status.setReplicatorId("test");
    SyncRequest request = new SyncRequestImpl(config, status);
    replicator.submitSyncRequest(request);
    verify(queue, times(1)).put(request);
    replicator.cancelSyncRequest(request);
    verify(queue, times(1)).remove(request);
  }

  @Test
  public void cancelActiveSyncRequest() throws Exception {
    ReplicationSite site1 = mock(ReplicationSite.class);
    ReplicationSite site2 = mock(ReplicationSite.class);
    when(site1.getUrl()).thenReturn("https://site1:1234");
    when(site2.getUrl()).thenReturn("https://site2:1234");
    when(siteManager.get("srcId")).thenReturn(site1);
    when(siteManager.get("destId")).thenReturn(site2);
    when(replicatorStoreFactory.createReplicatorStore(new URL("https://site1:1234")))
        .thenReturn(store1);
    when(replicatorStoreFactory.createReplicatorStore(new URL("https://site2:1234")))
        .thenReturn(store2);
    when(store1.isAvailable()).thenReturn(true);
    when(store2.isAvailable()).thenReturn(true);
    SyncResponse response = new SyncResponse(1L, 0L, 10L, Status.CANCELED);

    ReplicationStatusImpl status = new ReplicationStatusImpl();
    status.setReplicatorId("test");
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
}
