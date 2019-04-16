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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.NotFoundException;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.Replication;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.data.CreateRequest;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.DeleteRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.QueryResponse;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.data.Resource;
import org.codice.ditto.replication.api.data.ResourceRequest;
import org.codice.ditto.replication.api.data.ResourceResponse;
import org.codice.ditto.replication.api.data.UpdateRequest;
import org.codice.ditto.replication.api.data.UpdateStorageRequest;
import org.codice.ditto.replication.api.impl.Syncer.Job;
import org.codice.ditto.replication.api.persistence.ReplicationItemManager;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyncerTest {

  @Mock ReplicationItemManager replicationItemManager;

  @Mock ReplicatorHistoryManager replicatorHistoryManager;

  @Mock NodeAdapter source;

  @Mock NodeAdapter destination;

  @Mock ReplicatorConfig replicatorConfig;

  @Mock ReplicationStatus replicationStatus;

  private Syncer syncer;

  private static final String SOURCE_NAME = "sourceName";

  private static final String DESTINATION_NAME = "destinationName";

  private static final String REPLICATOR_ID = "replicatorId";

  private static final String CQL = "title like '*'";

  @Before
  public void setup() {
    when(source.getSystemName()).thenReturn(SOURCE_NAME);
    when(destination.getSystemName()).thenReturn(DESTINATION_NAME);

    when(replicatorConfig.getId()).thenReturn(REPLICATOR_ID);
    when(replicatorConfig.getFilter()).thenReturn(CQL);

    syncer = new Syncer(replicationItemManager, replicatorHistoryManager);
  }

  @Test
  public void testCanceledJob() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failureRetryCount);

    when(replicationItemManager.getFailureList(failureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    Metadata metadata = mockMetadata(metadataId);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);
    when(source.query(any(QueryRequest.class))).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.CANCELED);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.cancel();
    job.sync();

    // then
    verify(replicationItemManager, never()).getItem(metadataId, SOURCE_NAME, DESTINATION_NAME);
    verify(replicationStatus, never()).setLastMetadataModified(any(Date.class));
    verify(replicationItemManager, never()).saveItem(any(ReplicationItem.class));
    verify(replicationStatus, times(1)).setStatus(Status.CANCELED);
  }

  @Test
  public void testSyncCreate() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    CreateRequest createRequest = createRequestCaptor.getValue();
    assertThat(createRequest.getMetadata(), is(Collections.singletonList(metadata)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);
    verify(replicationItemManager, times(1)).saveItem(replicationItemCaptor.capture());
    verify(replicationStatus, times(1)).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, never()).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(nullValue()));
  }

  @Test
  public void testSyncCreateFailCreateToRemoteAdapter() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    CreateRequest createRequest = createRequestCaptor.getValue();
    assertThat(createRequest.getMetadata(), is(Collections.singletonList(metadata)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);
    verify(replicationItemManager, times(1)).saveItem(replicationItemCaptor.capture());
    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, times(1)).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(nullValue()));
    assertThat(capturedItem.getFailureCount(), is(1));
  }

  @Test
  public void testSyncCreateConnectionLost() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.FAILURE);

    when(source.isAvailable()).thenReturn(false);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture()))
        .thenThrow(AdapterException.class);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    CreateRequest createRequest = createRequestCaptor.getValue();
    assertThat(createRequest.getMetadata(), is(Collections.singletonList(metadata)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);
    verify(replicationItemManager, never()).saveItem(any(ReplicationItem.class));
    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, never()).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, never()).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.CONNECTION_LOST);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());
  }

  @Test
  public void testSyncCreateUnknownFailure() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenThrow(Exception.class);

    when(source.isAvailable()).thenReturn(true);
    when(destination.isAvailable()).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    CreateRequest createRequest = createRequestCaptor.getValue();
    assertThat(createRequest.getMetadata(), is(Collections.singletonList(metadata)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);
    verify(replicationItemManager, times(1)).saveItem(replicationItemCaptor.capture());
    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, times(1)).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(nullValue()));
    assertThat(capturedItem.getFailureCount(), is(1));
  }

  @Test
  public void testSyncCreateResource() throws URISyntaxException {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI resourceUri = new URI("https://onefakestreet:9001");
    final long metadataSize = 5;
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(resourceUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getResourceSize()).thenReturn(metadataSize);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    Resource resource = mock(Resource.class);
    when(resource.getMetadata()).thenReturn(metadata);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    ArgumentCaptor<CreateStorageRequest> createStorageRequestCaptor =
        ArgumentCaptor.forClass(CreateStorageRequest.class);
    when(destination.createResource(createStorageRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    ResourceRequest resourceRequest = resourceRequestCaptor.getValue();
    assertThat(resourceRequest.getMetadata(), is(metadata));

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    CreateStorageRequest createStorageRequest = createStorageRequestCaptor.getValue();
    assertThat(createStorageRequest.getResources(), is(Collections.singletonList(resource)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    verify(replicationStatus, times(1)).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, never()).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, times(1)).incrementBytesTransferred(metadataSize);

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).saveItem(replicationItemCaptor.capture());
    final ReplicationItem capturedItem = replicationItemCaptor.getValue();

    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
  }

  @Test
  public void testSyncCreateResourceFailCreateToDestinationAdapter() throws URISyntaxException {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI resourceUri = new URI("https://onefakestreet:9001");
    final long metadataSize = 5;
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(resourceUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getResourceSize()).thenReturn(metadataSize);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    Resource resource = mock(Resource.class);
    when(resource.getMetadata()).thenReturn(metadata);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    ArgumentCaptor<CreateStorageRequest> createStorageRequestCaptor =
        ArgumentCaptor.forClass(CreateStorageRequest.class);
    when(destination.createResource(createStorageRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    ResourceRequest resourceRequest = resourceRequestCaptor.getValue();
    assertThat(resourceRequest.getMetadata(), is(metadata));

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    CreateStorageRequest createStorageRequest = createStorageRequestCaptor.getValue();
    assertThat(createStorageRequest.getResources(), is(Collections.singletonList(resource)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, times(1)).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(metadataSize);

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).saveItem(replicationItemCaptor.capture());
    final ReplicationItem capturedItem = replicationItemCaptor.getValue();

    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getFailureCount(), is(1));
  }

  @Test
  public void testSyncCreateResourceFailReadResource() throws URISyntaxException {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI resourceUri = new URI("https://onefakestreet:9001");
    final long metadataSize = 5;
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(resourceUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getResourceSize()).thenReturn(metadataSize);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenThrow(AdapterException.class);

    when(source.isAvailable()).thenReturn(true);
    when(destination.isAvailable()).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    ResourceRequest resourceRequest = resourceRequestCaptor.getValue();
    assertThat(resourceRequest.getMetadata(), is(metadata));

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    verify(destination, never()).createResource(any(CreateStorageRequest.class));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, times(1)).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(metadataSize);

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).saveItem(replicationItemCaptor.capture());
    final ReplicationItem capturedItem = replicationItemCaptor.getValue();

    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getFailureCount(), is(1));
  }

  @Test
  public void testSyncCreateResourceConnectionLost() throws URISyntaxException {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI resourceUri = new URI("https://onefakestreet:9001");
    final long metadataSize = 5;
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(resourceUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getResourceSize()).thenReturn(metadataSize);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.CONNECTION_LOST);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenThrow(AdapterException.class);

    when(source.isAvailable()).thenReturn(false);
    when(destination.isAvailable()).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    ResourceRequest resourceRequest = resourceRequestCaptor.getValue();
    assertThat(resourceRequest.getMetadata(), is(metadata));

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    verify(destination, never()).createResource(any(CreateStorageRequest.class));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, never()).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, never()).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.CONNECTION_LOST);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());

    verify(replicationItemManager, never()).saveItem(any(ReplicationItem.class));
  }

  @Test
  public void testSyncUpdate() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(destination.exists(metadata)).thenReturn(true);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.of(replicationItem));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<UpdateRequest> updateRequestCaptor =
        ArgumentCaptor.forClass(UpdateRequest.class);
    when(destination.updateRequest(updateRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);

    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    UpdateRequest updateRequest = updateRequestCaptor.getValue();
    assertThat(updateRequest.getMetadata(), is(Collections.singletonList(metadata)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    verify(replicationItemManager, times(1)).saveItem(replicationItemCaptor.capture());
    verify(replicationStatus, times(1)).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, never()).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(nullValue()));
  }

  @Test
  public void testSyncUpdateFail() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(destination.exists(metadata)).thenReturn(true);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.of(replicationItem));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<UpdateRequest> updateRequestCaptor =
        ArgumentCaptor.forClass(UpdateRequest.class);
    when(destination.updateRequest(updateRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    UpdateRequest updateRequest = updateRequestCaptor.getValue();
    assertThat(updateRequest.getMetadata(), is(Collections.singletonList(metadata)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    verify(replicationItemManager, times(1)).saveItem(replicationItem);
    verify(replicationItem, times(1)).incrementFailureCount();

    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, times(1)).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());
  }

  @Test
  public void testSyncUpdateResource() throws URISyntaxException {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI metadataUri = new URI("https://onefakestreet:1234");
    final long resourceSize = 10;
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(metadataUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getResourceSize()).thenReturn(resourceSize);

    when(destination.exists(metadata)).thenReturn(true);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItem.getResourceModified())
        .thenReturn(new Date(resourceModified.getTime() - 1000));
    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.of(replicationItem));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    Resource resource = mock(Resource.class);
    when(resource.getMetadata()).thenReturn(metadata);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    ArgumentCaptor<UpdateStorageRequest> updateStorageRequestCaptor =
        ArgumentCaptor.forClass(UpdateStorageRequest.class);
    when(destination.updateResource(updateStorageRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    ResourceRequest resourceRequest = resourceRequestCaptor.getValue();
    assertThat(resourceRequest.getMetadata(), is(metadata));

    UpdateStorageRequest updateStorageRequest = updateStorageRequestCaptor.getValue();
    assertThat(updateStorageRequest.getResources(), is(Collections.singletonList(resource)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    ArgumentCaptor<ReplicationItem> repItem = ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).saveItem(repItem.capture());
    ReplicationItem capturedItem = repItem.getValue();
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));

    verify(replicationStatus, times(1)).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, never()).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, times(1)).incrementBytesTransferred(resourceSize);
  }

  @Test
  public void testSyncUpdateStorageFail() throws URISyntaxException {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI metadataUri = new URI("https://onefakestreet:1234");
    final long resourceSize = 10;
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(metadataUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getResourceSize()).thenReturn(resourceSize);

    when(destination.exists(metadata)).thenReturn(true);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItem.getResourceModified())
        .thenReturn(new Date(resourceModified.getTime() - 1000));
    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.of(replicationItem));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    Resource resource = mock(Resource.class);
    when(resource.getMetadata()).thenReturn(metadata);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    ArgumentCaptor<UpdateStorageRequest> updateStorageRequestCaptor =
        ArgumentCaptor.forClass(UpdateStorageRequest.class);
    when(destination.updateResource(updateStorageRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    ResourceRequest resourceRequest = resourceRequestCaptor.getValue();
    assertThat(resourceRequest.getMetadata(), is(metadata));

    UpdateStorageRequest updateStorageRequest = updateStorageRequestCaptor.getValue();
    assertThat(updateStorageRequest.getResources(), is(Collections.singletonList(resource)));

    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    verify(replicationItemManager, times(1)).saveItem(replicationItem);
    verify(replicationItem, times(1)).incrementFailureCount();

    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, times(1)).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());
  }

  @Test
  public void testSyncDelete() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(true);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.of(replicationItem));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<DeleteRequest> deleteRequestCaptor =
        ArgumentCaptor.forClass(DeleteRequest.class);
    when(destination.deleteRequest(deleteRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    DeleteRequest deleteRequest = deleteRequestCaptor.getValue();
    assertThat(deleteRequest.getMetadata(), is(Collections.singletonList(metadata)));

    verify(replicationItemManager, times(1)).deleteItem(metadataId, SOURCE_NAME, DESTINATION_NAME);
    verify(replicationStatus, times(1)).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, never()).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());
  }

  @Test
  public void testSyncDeleteFail() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(true);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.of(replicationItem));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<DeleteRequest> deleteRequestCaptor =
        ArgumentCaptor.forClass(DeleteRequest.class);
    when(destination.deleteRequest(deleteRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    DeleteRequest deleteRequest = deleteRequestCaptor.getValue();
    assertThat(deleteRequest.getMetadata(), is(Collections.singletonList(metadata)));

    verify(replicationItemManager, never()).deleteItem(metadataId, SOURCE_NAME, DESTINATION_NAME);
    verify(replicationItemManager, times(1)).saveItem(replicationItem);
    verify(replicationStatus, never()).incrementCount();
    verify(replicationStatus, times(1)).setLastMetadataModified(modifiedDate);
    verify(replicationStatus, times(1)).incrementFailure();
    verify(replicationStatus, times(1)).setStatus(Status.SUCCESS);
    verify(replicationStatus, never()).incrementBytesTransferred(anyLong());
  }

  @Test
  public void testUpdateOnMetadataDeletedOnDestinationDoesCreate() {
    // setup
    doThrow(NotFoundException.class)
        .when(replicatorHistoryManager)
        .getByReplicatorId(REPLICATOR_ID);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(destination.exists(metadata)).thenReturn(false);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.of(replicationItem));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(true);

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    CreateRequest createRequest = createRequestCaptor.getValue();
    assertThat(createRequest.getMetadata().get(0), is(metadata));

    verify(destination, times(1)).createRequest(any(CreateRequest.class));

    verify(replicationItemManager, times(1)).saveItem(replicationItemCaptor.capture());
    ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getConfigurationId(), is(REPLICATOR_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getFailureCount(), is(0));
  }

  @Test
  public void testFailedItemsAndMofifiedAfterIncludedInQuery() {
    // setup
    ReplicationStatus replicationStatus = mock(ReplicationStatus.class);
    Date lastMetadataModified = new Date();
    when(replicationStatus.getLastMetadataModified()).thenReturn(lastMetadataModified);
    when(replicatorHistoryManager.getByReplicatorId(REPLICATOR_ID)).thenReturn(replicationStatus);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    final String failureId1 = "failureId1";
    final String failureId2 = "failureId2";
    final List<String> failedItemIds = new ArrayList<>();
    failedItemIds.add(failureId1);
    failedItemIds.add(failureId2);
    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(failedItemIds);

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(failedItemIds));
    assertThat(queryRequest.getModifiedAfter(), is(lastMetadataModified));
    verify(replicationStatus).setLastMetadataModified(any(Date.class));
  }

  @Test
  public void testFailedItemsAndMofifiedBeforeLastModifed() {
    // setup
    ReplicationStatus replicationStatus = mock(ReplicationStatus.class);
    Date lastMetadataModified = new Date();
    when(replicationStatus.getLastMetadataModified()).thenReturn(lastMetadataModified);
    when(replicatorHistoryManager.getByReplicatorId(REPLICATOR_ID)).thenReturn(replicationStatus);

    final int failtureRetryCount = 5;
    when(replicatorConfig.getFailureRetryCount()).thenReturn(failtureRetryCount);

    final String failureId1 = "failureId1";
    final String failureId2 = "failureId2";
    final List<String> failedItemIds = new ArrayList<>();
    failedItemIds.add(failureId1);
    failedItemIds.add(failureId2);
    when(replicationItemManager.getFailureList(failtureRetryCount, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(failedItemIds);

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    when(replicationItemManager.getItem(metadataId, SOURCE_NAME, DESTINATION_NAME))
        .thenReturn(Optional.empty());

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    // doesn't matter. do to avoid an NPE when creating sync response
    when(replicationStatus.getStatus()).thenReturn(Status.SUCCESS);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(true);

    when(replicationStatus.getLastMetadataModified())
        .thenReturn(new Date(new Date().getTime() + 100000));

    // when
    Job job = syncer.create(source, destination, replicatorConfig, replicationStatus);
    job.sync();

    // then
    verify(replicationStatus, never()).setLastMetadataModified(any(Date.class));
  }

  private Metadata mockMetadata(String id) {
    Metadata metadata = mock(Metadata.class);
    when(metadata.getId()).thenReturn(id);
    return metadata;
  }
}
