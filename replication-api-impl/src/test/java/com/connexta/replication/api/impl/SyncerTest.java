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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.Replication;
import com.connexta.replication.api.Status;
import com.connexta.replication.api.data.CreateRequest;
import com.connexta.replication.api.data.CreateStorageRequest;
import com.connexta.replication.api.data.DeleteRequest;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.QueryRequest;
import com.connexta.replication.api.data.QueryResponse;
import com.connexta.replication.api.data.ReplicationItem;
import com.connexta.replication.api.data.Resource;
import com.connexta.replication.api.data.ResourceRequest;
import com.connexta.replication.api.data.ResourceResponse;
import com.connexta.replication.api.data.UpdateRequest;
import com.connexta.replication.api.data.UpdateStorageRequest;
import com.connexta.replication.api.impl.Syncer.Job;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.ReplicationItemManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyncerTest {
  @Mock ReplicationItemManager replicationItemManager;

  @Mock NodeAdapter source;

  @Mock NodeAdapter destination;

  @Mock Filter filter;

  @Mock FilterIndex filterIndex;

  @Mock FilterIndexManager filterIndexManager;

  private Syncer syncer;

  private static final String SOURCE_NAME = "sourceName";

  private static final String DESTINATION_NAME = "destinationName";

  private static final String FILTER_ID = "replicatorId";

  private static final String CQL = "title like '*'";

  private static final long METADATA_SIZE = 1L;

  private static final long RESOURCE_SIZE = 2L;

  private static final Set<Consumer<ReplicationItem>> callbacks = new HashSet<>();

  @Mock Consumer<ReplicationItem> callback;

  @Before
  public void setup() {
    when(source.getSystemName()).thenReturn(SOURCE_NAME);
    when(destination.getSystemName()).thenReturn(DESTINATION_NAME);

    when(filter.getId()).thenReturn(FILTER_ID);
    when(filter.getFilter()).thenReturn(CQL);
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    when(filterIndexManager.getOrCreate(any(Filter.class))).thenReturn(filterIndex);

    callbacks.add(callback);

    syncer = new Syncer(replicationItemManager, filterIndexManager);
  }

  @Test
  public void testSyncCreate() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(0L);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(nullValue()));
    assertThat(capturedItem.getStatus(), is(Status.SUCCESS));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(0L));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncCreateFailCreateToRemoteAdapter() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(new Date());
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(notNullValue()));
    assertThat(capturedItem.getStatus(), is(Status.FAILURE));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncCreateConnectionLost() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    when(source.isAvailable()).thenReturn(false);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture()))
        .thenThrow(AdapterException.class);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(nullValue()));
    assertThat(capturedItem.getStatus(), is(Status.CONNECTION_LOST));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(-1.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(0L));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncCreateUnknownFailure() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(new Date());
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture()))
        .thenThrow(RuntimeException.class);

    when(source.isAvailable()).thenReturn(true);
    when(destination.isAvailable()).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(notNullValue()));
    assertThat(capturedItem.getStatus(), is(Status.FAILURE));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncCreateResource() throws URISyntaxException {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI resourceUri = new URI("https://onefakestreet:9001");
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(resourceUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    Resource resource = mock(Resource.class);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    ArgumentCaptor<CreateStorageRequest> createStorageRequestCaptor =
        ArgumentCaptor.forClass(CreateStorageRequest.class);
    when(destination.createResource(createStorageRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getStatus(), is(Status.SUCCESS));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncCreateResourceFailCreateToDestinationAdapter() throws URISyntaxException {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI resourceUri = new URI("https://onefakestreet:9001");
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(resourceUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    Resource resource = mock(Resource.class);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    ArgumentCaptor<CreateStorageRequest> createStorageRequestCaptor =
        ArgumentCaptor.forClass(CreateStorageRequest.class);
    when(destination.createResource(createStorageRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());
    final ReplicationItem capturedItem = replicationItemCaptor.getValue();

    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getStatus(), is(Status.FAILURE));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncCreateResourceFailReadResource() throws URISyntaxException {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI resourceUri = new URI("https://onefakestreet:9001");
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(resourceUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);

    when(source.readResource(resourceRequestCaptor.capture())).thenThrow(AdapterException.class);

    when(source.isAvailable()).thenReturn(true);
    when(destination.isAvailable()).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getStatus(), is(Status.FAILURE));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncCreateResourceConnectionLost() throws URISyntaxException {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI resourceUri = new URI("https://onefakestreet:9001");
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(resourceUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);

    when(source.readResource(resourceRequestCaptor.capture())).thenThrow(AdapterException.class);

    when(source.isAvailable()).thenReturn(false);
    when(destination.isAvailable()).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());

    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getStatus(), is(Status.CONNECTION_LOST));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncUpdate() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);

    when(destination.exists(metadata)).thenReturn(true);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItemManager.getLatest(FILTER_ID, metadataId))
        .thenReturn(Optional.of(replicationItem));

    ArgumentCaptor<UpdateRequest> updateRequestCaptor =
        ArgumentCaptor.forClass(UpdateRequest.class);
    when(destination.updateRequest(updateRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());
    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(nullValue()));
    assertThat(capturedItem.getStatus(), is(Status.SUCCESS));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(0L));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncUpdateFail() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);

    when(destination.exists(metadata)).thenReturn(true);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItemManager.getLatest(FILTER_ID, metadataId))
        .thenReturn(Optional.of(replicationItem));

    ArgumentCaptor<UpdateRequest> updateRequestCaptor =
        ArgumentCaptor.forClass(UpdateRequest.class);
    when(destination.updateRequest(updateRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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

    ArgumentCaptor<ReplicationItem> replicationItemCaptor =
        ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());
    final ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(nullValue()));
    assertThat(capturedItem.getStatus(), is(Status.FAILURE));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(0L));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncUpdateResource() throws URISyntaxException {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI metadataUri = new URI("https://onefakestreet:1234");
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(metadataUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    when(destination.exists(metadata)).thenReturn(true);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItem.getResourceModified())
        .thenReturn(new Date(resourceModified.getTime() - 1000));
    when(replicationItemManager.getLatest(FILTER_ID, metadataId))
        .thenReturn(Optional.of(replicationItem));

    Resource resource = mock(Resource.class);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    ArgumentCaptor<UpdateStorageRequest> updateStorageRequestCaptor =
        ArgumentCaptor.forClass(UpdateStorageRequest.class);
    when(destination.updateResource(updateStorageRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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
    verify(replicationItemManager, times(1)).save(repItem.capture());
    ReplicationItem capturedItem = repItem.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getStatus(), is(Status.SUCCESS));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncUpdateStorageFail() throws URISyntaxException {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    final Date resourceModified = new Date();
    final URI metadataUri = new URI("https://onefakestreet:1234");
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(metadataUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    when(destination.exists(metadata)).thenReturn(true);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    // set as some time in the past
    when(replicationItem.getMetadataModified()).thenReturn(new Date(modifiedDate.getTime() - 1000));
    when(replicationItem.getResourceModified())
        .thenReturn(new Date(resourceModified.getTime() - 1000));
    when(replicationItemManager.getLatest(FILTER_ID, metadataId))
        .thenReturn(Optional.of(replicationItem));

    Resource resource = mock(Resource.class);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(source.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    ArgumentCaptor<UpdateStorageRequest> updateStorageRequestCaptor =
        ArgumentCaptor.forClass(UpdateStorageRequest.class);
    when(destination.updateResource(updateStorageRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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
    verify(replicationItemManager, times(1)).save(repItem.capture());
    ReplicationItem capturedItem = repItem.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getStatus(), is(Status.FAILURE));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testSyncDelete() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(true);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(new Date());
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    when(replicationItemManager.getLatest(FILTER_ID, metadataId))
        .thenReturn(Optional.of(replicationItem));

    ArgumentCaptor<DeleteRequest> deleteRequestCaptor =
        ArgumentCaptor.forClass(DeleteRequest.class);
    when(destination.deleteRequest(deleteRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    DeleteRequest deleteRequest = deleteRequestCaptor.getValue();
    assertThat(deleteRequest.getMetadata(), is(Collections.singletonList(metadata)));
  }

  @Test
  public void testSyncDeleteFail() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(true);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    when(replicationItemManager.getLatest(FILTER_ID, metadataId))
        .thenReturn(Optional.of(replicationItem));

    ArgumentCaptor<DeleteRequest> deleteRequestCaptor =
        ArgumentCaptor.forClass(DeleteRequest.class);
    when(destination.deleteRequest(deleteRequestCaptor.capture())).thenReturn(false);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(Collections.emptyList()));
    assertThat(queryRequest.getModifiedAfter(), is(nullValue()));

    DeleteRequest deleteRequest = deleteRequestCaptor.getValue();
    assertThat(deleteRequest.getMetadata(), is(Collections.singletonList(metadata)));

    ArgumentCaptor<ReplicationItem> repItem = ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(repItem.capture());
    ReplicationItem capturedItem = repItem.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getStatus(), is(Status.FAILURE));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(0L));
    assertThat(capturedItem.getResourceSize(), is(0L));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testUpdateOnMetadataDeletedOnDestinationDoesCreate() {
    // setup
    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(new Date());
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    when(destination.exists(metadata)).thenReturn(false);

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

    ReplicationItem replicationItem = mock(ReplicationItem.class);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
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

    verify(replicationItemManager, times(1)).save(replicationItemCaptor.capture());
    ReplicationItem capturedItem = replicationItemCaptor.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getStatus(), is(Status.SUCCESS));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
  }

  @Test
  public void testFailedItemsAndModifiedAfterIncludedInQuery() {
    // setup
    Filter filter = mock(Filter.class);
    Instant lastMetadataModified = Instant.now();
    when(filterIndex.getModifiedSince()).thenReturn(Optional.of(lastMetadataModified));
    when(filter.getId()).thenReturn(FILTER_ID);
    when(filter.getFilter()).thenReturn(CQL);

    final String failureId1 = "failureId1";
    final String failureId2 = "failureId2";
    final List<String> failedItemIds = new ArrayList<>();
    failedItemIds.add(failureId1);
    failedItemIds.add(failureId2);
    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(failedItemIds);

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date(System.currentTimeMillis() + 10000);
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);
    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
    job.sync();

    // then
    QueryRequest queryRequest = queryRequestCaptor.getValue();
    assertThat(queryRequest.getCql(), is(CQL));
    assertThat(queryRequest.getExcludedNodes(), is(Collections.singletonList(DESTINATION_NAME)));
    assertThat(queryRequest.getFailedItemIds(), is(failedItemIds));
    assertThat(queryRequest.getModifiedAfter(), is(Date.from(lastMetadataModified)));
    verify(filterIndex).setModifiedSince(any(Instant.class));
  }

  @Test
  public void testFailedUpdatesAreRetriedWhenNotAfterModifiedDate() throws Exception {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date(new Date().getTime() - 1000);
    final Date resourceModified = new Date(new Date().getTime() - 1000);
    final URI metadataUri = new URI("https://onefakestreet:1234");
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(metadataUri);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(resourceModified);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Instant lastMetadataModified = Instant.now();
    when(filterIndex.getModifiedSince()).thenReturn(Optional.of(lastMetadataModified));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    when(source.query(any(QueryRequest.class))).thenReturn(queryResponse);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    when(replicationItem.getMetadataModified()).thenReturn(modifiedDate);
    when(replicationItem.getResourceModified()).thenReturn(resourceModified);
    when(replicationItem.getStatus()).thenReturn(Status.FAILURE);
    when(replicationItemManager.getLatest(FILTER_ID, metadataId))
        .thenReturn(Optional.of(replicationItem));

    when(destination.exists(metadata)).thenReturn(true);

    ResourceResponse resourceResponse = mock(ResourceResponse.class);

    when(source.readResource(any(ResourceRequest.class))).thenReturn(resourceResponse);
    when(destination.updateResource(any(UpdateStorageRequest.class))).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
    job.sync();

    // then
    verify(metadata, times(1)).addLineage(SOURCE_NAME);
    verify(metadata, times(1)).addTag(Replication.REPLICATED_TAG);

    ArgumentCaptor<ReplicationItem> repItem = ArgumentCaptor.forClass(ReplicationItem.class);
    verify(replicationItemManager, times(1)).save(repItem.capture());
    ReplicationItem capturedItem = repItem.getValue();
    assertThat(capturedItem.getId(), is(notNullValue()));
    assertThat(capturedItem.getMetadataId(), is(metadataId));
    assertThat(capturedItem.getFilterId(), is(FILTER_ID));
    assertThat(capturedItem.getSource(), is(SOURCE_NAME));
    assertThat(capturedItem.getDestination(), is(DESTINATION_NAME));
    assertThat(capturedItem.getMetadataModified(), is(modifiedDate));
    assertThat(capturedItem.getResourceModified(), is(resourceModified));
    assertThat(capturedItem.getStatus(), is(Status.SUCCESS));
    assertThat(capturedItem.getStartTime(), is(notNullValue()));
    assertThat(capturedItem.getDuration(), greaterThanOrEqualTo(0L));
    assertThat(capturedItem.getResourceTransferRate(), greaterThanOrEqualTo(0.0D));
    assertThat(capturedItem.getMetadataSize(), is(METADATA_SIZE));
    assertThat(capturedItem.getResourceSize(), is(RESOURCE_SIZE));

    verify(callback).accept(capturedItem);
    verify(filterIndex, never()).setModifiedSince(any(Instant.class));
  }

  @Test
  public void testSkippingUpdate() {
    // setup

    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(Collections.emptyList());

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date(new Date().getTime() - 1000);
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getResourceSize()).thenReturn(RESOURCE_SIZE);

    Instant lastMetadataModified = Instant.now();
    when(filterIndex.getModifiedSince()).thenReturn(Optional.of(lastMetadataModified));

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    when(source.query(any(QueryRequest.class))).thenReturn(queryResponse);

    ReplicationItem replicationItem = mock(ReplicationItem.class);
    when(replicationItem.getMetadataModified()).thenReturn(modifiedDate);
    when(replicationItem.getStatus()).thenReturn(Status.SUCCESS);
    when(replicationItemManager.getLatest(FILTER_ID, metadataId))
        .thenReturn(Optional.of(replicationItem));

    when(destination.exists(metadata)).thenReturn(true);

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
    job.sync();

    // then
    verify(replicationItemManager, never()).save(any(ReplicationItem.class));
    verify(callback, never()).accept(any(ReplicationItem.class));
    verify(filterIndex, never()).setModifiedSince(any(Instant.class));
  }

  @Test
  public void testFailedItemsAndModifiedBeforeLastModified() {
    // setup
    Filter filter = mock(Filter.class);
    Instant lastMetadataModified = Instant.now();
    when(filterIndex.getModifiedSince()).thenReturn(Optional.of(lastMetadataModified));
    when(filter.getId()).thenReturn(FILTER_ID);

    final String failureId1 = "failureId1";
    final String failureId2 = "failureId2";
    final List<String> failedItemIds = new ArrayList<>();
    failedItemIds.add(failureId1);
    failedItemIds.add(failureId2);
    when(replicationItemManager.getFailureList(FILTER_ID)).thenReturn(failedItemIds);

    final String metadataId = "metadataId";
    final Date modifiedDate = new Date();
    Metadata metadata = mockMetadata(metadataId);
    when(metadata.isDeleted()).thenReturn(false);
    when(metadata.getResourceUri()).thenReturn(null);
    when(metadata.getMetadataModified()).thenReturn(modifiedDate);
    when(metadata.getResourceModified()).thenReturn(null);

    Iterable<Metadata> iterable = mock(Iterable.class);
    Iterator<Metadata> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true).thenReturn(false);
    when(iterator.next()).thenReturn(metadata);
    when(iterable.iterator()).thenReturn(iterator);

    QueryResponse queryResponse = mock(QueryResponse.class);
    when(queryResponse.getMetadata()).thenReturn(iterable);

    ArgumentCaptor<QueryRequest> queryRequestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    when(source.query(queryRequestCaptor.capture())).thenReturn(queryResponse);

    when(replicationItemManager.getLatest(FILTER_ID, metadataId)).thenReturn(Optional.empty());

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(destination.createRequest(createRequestCaptor.capture())).thenReturn(true);

    // this is a time after the failed items modified date, so the last metadata modified value
    // should not be updated
    when(filterIndex.getModifiedSince())
        .thenReturn(Optional.of(Instant.ofEpochMilli(System.currentTimeMillis() + 10000)));

    // when
    Job job = syncer.create(source, destination, filter, callbacks);
    job.sync();

    // then
    verify(filterIndex, never()).setModifiedSince(any(Instant.class));
  }

  private Metadata mockMetadata(String id) {
    Metadata metadata = mock(Metadata.class);
    when(metadata.getId()).thenReturn(id);
    return metadata;
  }
}
