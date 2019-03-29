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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.UpdateStorageRequest;
import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.operation.impl.ProcessingDetailsImpl;
import ddf.catalog.resource.Resource;
import ddf.security.impl.SubjectImpl;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.NotFoundException;
import org.apache.shiro.util.ThreadContext;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.ReplicationPersistentStore;
import org.codice.ditto.replication.api.ReplicationStore;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyncHelperTest {

  SyncHelper helper;

  @Mock ReplicationStore source;
  @Mock ReplicationStore destination;
  @Mock ReplicatorConfig config;
  @Mock ReplicationPersistentStore persistentStore;
  @Mock ReplicatorHistoryManager history;

  ReplicationStatus status;

  FilterBuilder builder;

  @Before
  public void setUp() throws Exception {
    builder = new GeotoolsFilterBuilder();
    status = new ReplicationStatusImpl();
    status.setReplicatorId("test");
    when(source.getRemoteName()).thenReturn("local");
    when(destination.getRemoteName()).thenReturn("remote");
    helper = new SyncHelper(source, destination, config, status, persistentStore, history, builder);
  }

  @Test
  public void cancel() throws Exception {
    helper.cancel();
    when(config.getId()).thenReturn("test");
    when(config.getFilter()).thenReturn("title like '*'");
    when(config.getFailureRetryCount()).thenReturn(5);
    SourceResponse response = mock(SourceResponse.class);
    MetacardImpl mcard = new MetacardImpl();
    mcard.setId("id");
    when(response.getResults()).thenReturn(Collections.singletonList(new ResultImpl(mcard)));
    when(source.query(any(QueryRequest.class))).thenReturn(response);
    when(persistentStore.getFailureList(anyInt(), anyString(), anyString()))
        .thenReturn(Collections.emptyList());
    when(history.getByReplicatorId("test")).thenThrow(new NotFoundException());
    helper.sync();
    verify(destination, never()).create(any(CreateRequest.class));
    verify(destination, never()).create(any(CreateStorageRequest.class));
    verify(destination, never()).update(any(UpdateRequest.class));
    verify(destination, never()).update(any(UpdateStorageRequest.class));
    verify(destination, never()).delete(any(DeleteRequest.class));
  }

  @Test
  public void syncCreate() throws Exception {
    SubjectImpl subject = mock(SubjectImpl.class);
    ThreadContext.bind(subject);
    CreateResponse createResponse = mock(CreateResponse.class);
    when(createResponse.getProcessingErrors()).thenReturn(Collections.emptySet());
    when(config.getId()).thenReturn("test");
    when(config.getFilter()).thenReturn("title like '*'");
    when(config.getFailureRetryCount()).thenReturn(5);
    when(destination.create(any(CreateRequest.class))).thenReturn(createResponse);
    SourceResponse response = mock(SourceResponse.class);
    MetacardImpl mcard = new MetacardImpl();
    mcard.setId("id");
    mcard.setModifiedDate(new Date());
    mcard.setAttribute(Core.METACARD_MODIFIED, new Date());
    when(response.getResults()).thenReturn(Collections.singletonList(new ResultImpl(mcard)));
    when(source.query(any(QueryRequest.class))).thenReturn(response);
    when(persistentStore.getFailureList(anyInt(), anyString(), anyString()))
        .thenReturn(Collections.emptyList());
    when(persistentStore.getItem(anyString(), anyString(), anyString()))
        .thenReturn(Optional.empty());
    when(history.getByReplicatorId("test")).thenThrow(new NotFoundException());
    helper.sync();
    assertThat(status.getPushCount(), is(1L));
    assertThat(status.getStatus(), is(Status.SUCCESS));
  }

  @Test
  public void syncCreateResource() throws Exception {
    SubjectImpl subject = mock(SubjectImpl.class);
    ThreadContext.bind(subject);
    CreateResponse createResponse = mock(CreateResponse.class);
    when(createResponse.getProcessingErrors()).thenReturn(Collections.emptySet());
    when(config.getId()).thenReturn("test");
    when(config.getFilter()).thenReturn("title like '*'");
    when(config.getFailureRetryCount()).thenReturn(5);
    when(destination.create(any(CreateStorageRequest.class))).thenReturn(createResponse);
    SourceResponse response = mock(SourceResponse.class);
    MetacardImpl mcard = new MetacardImpl();
    mcard.setId("id");
    mcard.setModifiedDate(new Date());
    mcard.setAttribute(Core.METACARD_MODIFIED, new Date());
    mcard.setResourceURI(new URI("https://connext.com/resource.exe"));
    mcard.setResourceSize("100");
    when(response.getResults()).thenReturn(Collections.singletonList(new ResultImpl(mcard)));
    when(source.query(any(QueryRequest.class))).thenReturn(response);
    when(persistentStore.getFailureList(anyInt(), anyString(), anyString()))
        .thenReturn(Collections.emptyList());
    when(persistentStore.getItem(anyString(), anyString(), anyString()))
        .thenReturn(Optional.empty());
    when(history.getByReplicatorId("test")).thenThrow(new NotFoundException());
    // resource specific stuff
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    Resource resource = mock(Resource.class);
    when(source.retrieveResource(any(URI.class), any(Map.class))).thenReturn(resourceResponse);
    when(resourceResponse.getResource()).thenReturn(resource);
    when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    helper.sync();
    assertThat(status.getPushCount(), is(1L));
    assertThat(status.getPushBytes(), is(100L));
    assertThat(status.getStatus(), is(Status.SUCCESS));
  }

  @Test
  public void syncDelete() throws Exception {
    ArgumentCaptor<DeleteRequest> requestCaptor = ArgumentCaptor.forClass(DeleteRequest.class);
    helper = new SyncHelper(source, destination, config, status, persistentStore, history, builder);
    SubjectImpl subject = mock(SubjectImpl.class);
    ThreadContext.bind(subject);
    DeleteResponse deleteResponse = mock(DeleteResponse.class);
    when(deleteResponse.getProcessingErrors()).thenReturn(Collections.emptySet());
    when(config.getId()).thenReturn("test");
    when(config.getFilter()).thenReturn("title like '*'");
    when(config.getFailureRetryCount()).thenReturn(5);
    when(destination.delete(any(DeleteRequest.class))).thenReturn(deleteResponse);
    SourceResponse response = mock(SourceResponse.class);
    MetacardImpl mcard = new MetacardImpl();
    mcard.setId("id");
    mcard.setModifiedDate(new Date());
    mcard.setAttribute(MetacardVersion.VERSION_OF_ID, "test");
    mcard.setAttribute(Core.METACARD_MODIFIED, new Date());
    mcard.setAttribute(MetacardVersion.ACTION, "Deleted");
    when(response.getResults()).thenReturn(Collections.singletonList(new ResultImpl(mcard)));
    when(source.query(any(QueryRequest.class))).thenReturn(response);
    when(persistentStore.getFailureList(anyInt(), anyString(), anyString()))
        .thenReturn(Collections.emptyList());
    ReplicationItem item =
        new ReplicationItemImpl("test", new Date(), new Date(), "local", "remote", "test");
    when(persistentStore.getItem(anyString(), anyString(), anyString()))
        .thenReturn(Optional.of(item));
    ReplicationStatus prevStatus = new ReplicationStatusImpl();
    prevStatus.setReplicatorId("test");
    prevStatus.setLastSuccess(new Date());
    when(history.getByReplicatorId("test")).thenReturn(prevStatus);
    helper.sync();
    verify(destination).delete(requestCaptor.capture());
    verify(persistentStore).deleteItem("test", "local", "remote");
    assertThat(requestCaptor.getValue().getAttributeValues().contains("test"), is(true));
    assertThat(status.getPushCount(), is(1L));
    assertThat(status.getStatus(), is(Status.SUCCESS));
  }

  @Test
  public void syncUpdate() throws Exception {
    SubjectImpl subject = mock(SubjectImpl.class);
    ThreadContext.bind(subject);
    UpdateResponse updateResponse = mock(UpdateResponse.class);
    when(updateResponse.getProcessingErrors()).thenReturn(Collections.emptySet());
    when(config.getId()).thenReturn("test");
    when(config.getFilter()).thenReturn("title like '*'");
    when(config.getFailureRetryCount()).thenReturn(5);
    when(destination.update(any(UpdateRequest.class))).thenReturn(updateResponse);
    SourceResponse response = mock(SourceResponse.class);
    MetacardImpl mcard = new MetacardImpl();
    mcard.setId("id");
    mcard.setModifiedDate(new Date());
    mcard.setAttribute(Core.METACARD_MODIFIED, new Date());
    when(response.getResults()).thenReturn(Collections.singletonList(new ResultImpl(mcard)));
    when(source.query(any(QueryRequest.class))).thenReturn(response);
    ReplicationStatus prevStatus = new ReplicationStatusImpl();
    prevStatus.setReplicatorId("test");
    prevStatus.setLastSuccess(new Date());
    when(history.getByReplicatorId("test")).thenReturn(prevStatus);
    SourceResponse sourceResponse = mock(SourceResponse.class);
    when(sourceResponse.getHits()).thenReturn(1L);
    when(destination.query(any(QueryRequest.class))).thenReturn(sourceResponse);
    ReplicationItem item =
        new ReplicationItemImpl("id", new Date(), new Date(), "local", "remote", "oldConfigId");
    item.incrementFailureCount();
    when(persistentStore.getItem(anyString(), anyString(), anyString()))
        .thenReturn(Optional.of(item));
    when(persistentStore.getFailureList(anyInt(), anyString(), anyString()))
        .thenReturn(Collections.singletonList(item.getMetacardId()));
    helper.sync();
    ArgumentCaptor<ReplicationItem> itemCaptor = ArgumentCaptor.forClass(ReplicationItem.class);
    verify(persistentStore).saveItem(itemCaptor.capture());
    assertThat(itemCaptor.getValue().getConfigurationId(), is("oldConfigId"));
    assertThat(status.getPushCount(), is(1L));
    assertThat(status.getStatus(), is(Status.SUCCESS));
  }

  @Test
  public void syncItemFailure() throws Exception {
    SubjectImpl subject = mock(SubjectImpl.class);
    ThreadContext.bind(subject);
    CreateResponse createResponse = mock(CreateResponse.class);
    when(createResponse.getProcessingErrors())
        .thenReturn(Collections.singleton(new ProcessingDetailsImpl()));
    when(config.getId()).thenReturn("test");
    when(config.getFilter()).thenReturn("title like '*'");
    when(config.getFailureRetryCount()).thenReturn(5);
    when(destination.create(any(CreateRequest.class))).thenReturn(createResponse);
    when(destination.isAvailable()).thenReturn(true);
    SourceResponse response = mock(SourceResponse.class);
    MetacardImpl mcard = new MetacardImpl();
    mcard.setId("id");
    mcard.setModifiedDate(new Date());
    mcard.setAttribute(Core.METACARD_MODIFIED, new Date());
    when(response.getResults()).thenReturn(Collections.singletonList(new ResultImpl(mcard)));
    when(source.query(any(QueryRequest.class))).thenReturn(response);
    when(source.isAvailable()).thenReturn(true);
    when(persistentStore.getFailureList(anyInt(), anyString(), anyString()))
        .thenReturn(Collections.emptyList());
    when(persistentStore.getItem(anyString(), anyString(), anyString()))
        .thenReturn(Optional.empty());
    when(history.getByReplicatorId("test")).thenThrow(new NotFoundException());
    helper.sync();
    assertThat(status.getPushFailCount(), is(1L));
    assertThat(status.getStatus(), is(Status.SUCCESS));
  }

  @Test
  public void isCanceled() {
    assertThat(helper.isCanceled(), is(false));
    helper.cancel();
    assertThat(helper.isCanceled(), is(true));
  }

  @Test
  public void getTimeOfLastSuccessfulRunLastSuccessIsNull() {
    ReplicationStatus prevStatus = new ReplicationStatusImpl();
    when(config.getId()).thenReturn("id");
    when(history.getByReplicatorId(anyString())).thenReturn(prevStatus);
    assertThat(helper.getTimeOfLastSuccessfulRun(), is(0L));
  }
}
