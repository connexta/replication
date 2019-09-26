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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.AdapterInterruptedException;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.ReplicationException;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.QueryRequest;
import com.connexta.replication.api.data.QueryResponse;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.ddf.DdfMetadataInfo;
import com.connexta.replication.api.impl.data.TaskInfoImpl;
import com.connexta.replication.api.queue.QueueException;
import com.connexta.replication.api.queue.SiteQueue;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.codice.junit.rules.ClearInterruptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryServiceTest {

  @Rule public final ClearInterruptions clearInterruptions = new ClearInterruptions();

  private static final long PERIOD = 60L;

  private static final Date MIN_DATE = new Date(Long.MIN_VALUE);

  public static final String METADATA_ID = "metadataId";

  public static final String METADATA_RESOURCE_URI = "http://test:123";

  public static final Date METADATA_RESOURCE_MODIFIED = new Date();

  public static final long METADATA_RESOURCE_SIZE = 2;

  public static final Date METADATA_MODIFIED = new Date();

  public static final long METADATA_SIZE = 1;

  public static final Class METADATA_TYPE = Map.class;

  public static final Map<String, Object> RAW_METADATA = new HashMap<>();

  @Mock private Site site;

  @Mock private QueryServiceTools queryServiceTools;

  @Mock private ScheduledExecutorService executor;

  @Mock private SiteQueue siteQueue;

  @Mock private NodeAdapter adapter;

  private QueryService queryService;

  @Before
  public void setUp() throws Exception {
    when(queryServiceTools.getQueueFor(site)).thenReturn(siteQueue);
    when(queryServiceTools.getAdapterFor(site)).thenReturn(adapter);
    when(queryServiceTools.getGlobalPeriod()).thenReturn(PERIOD);
    when(site.getPollingPeriod()).thenReturn(Optional.empty());

    queryService = new QueryService(site, queryServiceTools, executor);
  }

  @Test
  public void update() {
    Site site2 = mock(Site.class);
    queryService.update(site2);
    verify(queryServiceTools).getAdapterFor(site2);
  }

  @Test
  public void start() {
    ScheduledFuture future = mock(ScheduledFuture.class);
    when(executor.scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq(PERIOD * 1000), eq(TimeUnit.MILLISECONDS)))
        .thenReturn(future);
    queryService.start();
    verify(executor)
        .scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq(PERIOD * 1000), eq(TimeUnit.MILLISECONDS));
    assertThat(queryService.isRunning(), is(true));
  }

  @Test
  public void startSiteHasPollingPeriod() {
    ScheduledFuture future = mock(ScheduledFuture.class);
    when(site.getPollingPeriod()).thenReturn(Optional.of(Duration.of(30, ChronoUnit.SECONDS)));
    when(executor.scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq(30000L), eq(TimeUnit.MILLISECONDS)))
        .thenReturn(future);
    queryService.start();
    verify(executor)
        .scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(30000L), eq(TimeUnit.MILLISECONDS));
    assertThat(queryService.isRunning(), is(true));
  }

  @Test
  public void startWhenAlreadyRunning() {
    ScheduledFuture future = mock(ScheduledFuture.class);
    when(executor.scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq(PERIOD * 1000), eq(TimeUnit.MILLISECONDS)))
        .thenReturn(future);
    queryService.start();
    assertThat(queryService.isRunning(), is(true));
    queryService.start();
    verify(executor, times(1))
        .scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq(PERIOD * 1000), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  public void query() throws Exception {
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    Metadata metadata = mockBasicMetadata();
    QueryResponse queryResponse = mock(QueryResponse.class);
    when(adapter.query(any(QueryRequest.class))).thenReturn(queryResponse);
    when(queryResponse.getMetadata()).thenReturn(List.of(metadata));

    // #metadataToTask
    when(filter.getPriority()).thenReturn((byte) 0);
    when(metadata.getResourceSize()).thenReturn(0L);
    Date lastModified = new Date();
    when(metadata.getMetadataModified()).thenReturn(lastModified);
    when(metadata.getType()).thenReturn(HashMap.class);
    when(metadata.getRawMetadata()).thenReturn(new HashMap<>());

    queryService.query();

    ArgumentCaptor<QueryRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(QueryRequest.class);
    verify(adapter).query(requestArgumentCaptor.capture());
    assertThat(requestArgumentCaptor.getValue().getModifiedAfter(), is(nullValue()));

    ArgumentCaptor<TaskInfoImpl> taskCaptor = ArgumentCaptor.forClass(TaskInfoImpl.class);
    verify(siteQueue).put(taskCaptor.capture());
    TaskInfoImpl createdTask = taskCaptor.getValue();
    assertThat(createdTask.getIntelId(), is(metadata.getId()));
    assertThat(createdTask.getPriority(), is(filter.getPriority()));
    assertThat(createdTask.getOperation(), is(OperationType.HARVEST));
    assertThat(createdTask.getLastModified(), is(metadata.getMetadataModified().toInstant()));
    assertThat(createdTask.getResource(), is(Optional.empty()));
    DdfMetadataInfo metadataInfo =
        (DdfMetadataInfo) createdTask.metadatas().findFirst().orElse(null);
    assertThat(metadataInfo.getType(), is("metacard"));
    assertThat(metadataInfo.getLastModified(), is(metadata.getMetadataModified().toInstant()));
    assertThat(metadataInfo.getSize().getAsLong(), is(metadata.getMetadataSize()));
    assertThat(metadataInfo.getData(), is(metadata.getRawMetadata()));
    verify(siteQueue).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(lastModified.toInstant());
    verify(queryServiceTools).saveFilterIndex(filterIndex);
  }

  @Test
  public void queryHandlesReplicationException() throws Exception {
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenThrow(new ReplicationException(""));

    queryService.query();

    verify(siteQueue, never()).put(any(TaskInfoImpl.class));
    verify(queryServiceTools, never()).saveFilterIndex(any());
  }

  @Test
  public void queryWithMultipleMetadata() throws Exception {
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    Metadata metadata1 = mockBasicMetadata();
    Metadata metadata2 = mockBasicMetadata();
    QueryResponse queryResponse = mock(QueryResponse.class);
    when(adapter.query(any(QueryRequest.class))).thenReturn(queryResponse);

    // return metadata with the later modified date so we can verify they are sorted correctly
    // and we can verify the later modified date is saved in the filter index
    when(queryResponse.getMetadata()).thenReturn(List.of(metadata2, metadata1));

    // #metadataToTask
    when(filter.getPriority()).thenReturn((byte) 0);
    Date lastModified1 = new Date(0);
    when(metadata1.getMetadataModified()).thenReturn(lastModified1);
    Date lastModified2 = new Date();
    when(metadata2.getMetadataModified()).thenReturn(lastModified2);

    queryService.query();

    ArgumentCaptor<QueryRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(QueryRequest.class);
    verify(adapter).query(requestArgumentCaptor.capture());
    assertThat(requestArgumentCaptor.getValue().getModifiedAfter(), is(nullValue()));

    // verify metadata is processed in order of modified date
    InOrder orderVerifier = Mockito.inOrder(metadata1, metadata2);
    orderVerifier.verify(metadata1).getId();
    orderVerifier.verify(metadata2).getId();

    verify(siteQueue, times(2)).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(lastModified2.toInstant());
    verify(queryServiceTools).saveFilterIndex(filterIndex);
  }

  @Test
  public void queryWithMultipleFilters() throws Exception {
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter1 = mock(Filter.class);
    when(filter1.getPriority()).thenReturn((byte) 1);
    Filter filter2 = mock(Filter.class);
    when(filter2.getPriority()).thenReturn((byte) 0);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter2, filter1));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(any(Filter.class))).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    Metadata metadata = mockBasicMetadata();
    QueryResponse queryResponse = mock(QueryResponse.class);
    when(adapter.query(any(QueryRequest.class))).thenReturn(queryResponse);
    when(queryResponse.getMetadata()).thenReturn(List.of(metadata));

    // #metadataToTask
    when(metadata.getResourceSize()).thenReturn(0L);
    Date lastModified = new Date();
    when(metadata.getMetadataModified()).thenReturn(lastModified);
    when(metadata.getType()).thenReturn(HashMap.class);
    when(metadata.getRawMetadata()).thenReturn(new HashMap<>());

    queryService.query();

    ArgumentCaptor<QueryRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(QueryRequest.class);
    verify(adapter, times(2)).query(requestArgumentCaptor.capture());
    assertThat(requestArgumentCaptor.getValue().getModifiedAfter(), is(nullValue()));

    // verify filters are processed in order of priority
    InOrder orderVerifier = Mockito.inOrder(filter1, filter2);
    orderVerifier.verify(filter1).getFilter();
    orderVerifier.verify(filter2).getFilter();

    verify(siteQueue, times(2)).put(any(TaskInfoImpl.class));
    verify(queryServiceTools, times(2)).saveFilterIndex(filterIndex);
  }

  @Test
  public void queryAdapterIsUnavailable() throws Exception {
    when(adapter.isAvailable()).thenReturn(false);

    queryService.query();

    verify(queryServiceTools, never()).activeFiltersFor(any());
  }

  @Test
  public void queryWithPreviousFilterIndex() throws Exception {
    ArgumentCaptor<QueryRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(QueryRequest.class);
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    Instant index = Instant.EPOCH;
    when(filterIndex.getModifiedSince()).thenReturn(Optional.of(index));
    Metadata metadata = mockBasicMetadata();
    QueryResponse queryResponse = mock(QueryResponse.class);
    when(adapter.query(any(QueryRequest.class))).thenReturn(queryResponse);
    when(queryResponse.getMetadata()).thenReturn(List.of(metadata));

    // #metadataToTask
    when(filter.getPriority()).thenReturn((byte) 0);
    when(metadata.getResourceSize()).thenReturn(0L);
    Instant lastModified = Instant.now();
    when(metadata.getMetadataModified()).thenReturn(Date.from(lastModified));
    ArgumentCaptor<Instant> lastModifiedCaptor = ArgumentCaptor.forClass(Instant.class);
    when(metadata.getType()).thenReturn(HashMap.class);
    when(metadata.getRawMetadata()).thenReturn(new HashMap<>());

    queryService.query();

    verify(adapter).query(requestArgumentCaptor.capture());
    assertThat(requestArgumentCaptor.getValue().getModifiedAfter(), is(Date.from(index)));
    verify(siteQueue).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(lastModifiedCaptor.capture());
    assertThat(lastModifiedCaptor.getValue().isAfter(index), is(true));
    verify(queryServiceTools).saveFilterIndex(filterIndex);
  }

  @Test
  public void queryHandlesAdapterException() throws Exception {
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    when(adapter.query(any(QueryRequest.class))).thenThrow(new AdapterException(""));

    queryService.query();

    verify(siteQueue, never()).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(MIN_DATE.toInstant());
    verify(queryServiceTools).saveFilterIndex(filterIndex);
  }

  @Test
  public void queryHandlesAdapterInterruptedException() throws Exception {
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    when(adapter.query(any(QueryRequest.class)))
        .thenThrow(new AdapterInterruptedException(new InterruptedException()));

    queryService.query();

    verify(siteQueue, never()).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(MIN_DATE.toInstant());
    verify(queryServiceTools).saveFilterIndex(filterIndex);
    Assert.assertTrue(Thread.currentThread().isInterrupted());
  }

  @Test
  public void queryHandlesAdapterInterruptedExceptionWhileIteratingMetadata() throws Exception {
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    Spliterator spliterator = mock(Spliterator.class);
    Iterable metadata = mock(Iterable.class);
    QueryResponse response = mock(QueryResponse.class);
    when(response.getMetadata()).thenReturn(metadata);
    when(metadata.spliterator()).thenReturn(spliterator);
    doThrow(new AdapterInterruptedException(new InterruptedException()))
        .when(spliterator)
        .forEachRemaining(any());
    when(adapter.query(any(QueryRequest.class))).thenReturn(response);

    queryService.query();

    verify(siteQueue, never()).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(MIN_DATE.toInstant());
    verify(queryServiceTools).saveFilterIndex(filterIndex);
    Assert.assertTrue(Thread.currentThread().isInterrupted());
  }

  @Test
  public void queryHandlesInterruptedExceptionWhilePuttingTasksInQueue() throws Exception {
    ArgumentCaptor<QueryRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(QueryRequest.class);
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    Metadata metadata = mockBasicMetadata();
    QueryResponse queryResponse = mock(QueryResponse.class);
    when(adapter.query(any(QueryRequest.class))).thenReturn(queryResponse);
    when(queryResponse.getMetadata()).thenReturn(List.of(metadata));

    // #metadataToTask
    when(filter.getPriority()).thenReturn((byte) 0);
    when(metadata.getResourceSize()).thenReturn(0L);
    Date lastModified = new Date();
    when(metadata.getMetadataModified()).thenReturn(lastModified);
    when(metadata.getType()).thenReturn(HashMap.class);
    when(metadata.getRawMetadata()).thenReturn(new HashMap<>());

    // #queueTaskAndPutLastModifiedInSet
    doThrow(new InterruptedException()).when(siteQueue).put(any(TaskInfoImpl.class));

    queryService.query();

    verify(adapter).query(requestArgumentCaptor.capture());
    assertThat(requestArgumentCaptor.getValue().getModifiedAfter(), is(nullValue()));
    verify(siteQueue).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(MIN_DATE.toInstant());
    verify(queryServiceTools).saveFilterIndex(filterIndex);
  }

  @Test
  public void queryHandlesQueueException() throws Exception {
    ArgumentCaptor<QueryRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(QueryRequest.class);
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    Metadata metadata = mockBasicMetadata();
    QueryResponse queryResponse = mock(QueryResponse.class);
    when(adapter.query(any(QueryRequest.class))).thenReturn(queryResponse);
    when(queryResponse.getMetadata()).thenReturn(List.of(metadata));

    // #metadataToTask
    when(filter.getPriority()).thenReturn((byte) 0);
    when(metadata.getResourceSize()).thenReturn(0L);
    Date lastModified = new Date();
    when(metadata.getMetadataModified()).thenReturn(lastModified);
    when(metadata.getType()).thenReturn(HashMap.class);
    when(metadata.getRawMetadata()).thenReturn(new HashMap<>());

    // #queueTaskAndPutLastModifiedInSet
    doThrow(new QueueException("")).when(siteQueue).put(any(TaskInfoImpl.class));

    queryService.query();

    verify(adapter).query(requestArgumentCaptor.capture());
    assertThat(requestArgumentCaptor.getValue().getModifiedAfter(), is(nullValue()));
    verify(siteQueue).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(MIN_DATE.toInstant());
    verify(queryServiceTools).saveFilterIndex(filterIndex);
  }

  @Test
  public void queryUpdatesIndexWithLastModifiedIf2ndPutFails() throws Exception {
    ArgumentCaptor<QueryRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(QueryRequest.class);
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    Metadata metadata1 = mockBasicMetadata();
    Metadata metadata2 = mockBasicMetadata();
    QueryResponse queryResponse = mock(QueryResponse.class);
    when(adapter.query(any(QueryRequest.class))).thenReturn(queryResponse);
    when(queryResponse.getMetadata()).thenReturn(List.of(metadata1, metadata2));

    // #metadataToTask
    when(filter.getPriority()).thenReturn((byte) 0);
    Date lastModified1 = new Date(1);
    when(metadata1.getMetadataModified()).thenReturn(lastModified1);
    Date lastModified2 = new Date(2);
    when(metadata2.getMetadataModified()).thenReturn(lastModified2);

    // succeed, then fail
    doNothing().doThrow(new QueueException("")).when(siteQueue).put(any(TaskInfoImpl.class));

    queryService.query();

    verify(adapter).query(requestArgumentCaptor.capture());
    assertThat(requestArgumentCaptor.getValue().getModifiedAfter(), is(nullValue()));
    verify(siteQueue, times(2)).put(any(TaskInfoImpl.class));
    verify(filterIndex).setModifiedSince(lastModified1.toInstant());
    verify(queryServiceTools).saveFilterIndex(filterIndex);
  }

  @Test
  public void queryLoadsResource() throws Exception {
    // #query
    when(adapter.isAvailable()).thenReturn(true);
    Filter filter = mock(Filter.class);
    when(queryServiceTools.activeFiltersFor(site)).thenReturn(Stream.of(filter));
    FilterIndex filterIndex = mock(FilterIndex.class);
    when(queryServiceTools.getOrCreateFilterIndex(filter)).thenReturn(filterIndex);

    // #getNewMetadataForFilter
    when(filterIndex.getModifiedSince()).thenReturn(Optional.empty());
    Metadata metadata = mockBasicMetadata();
    QueryResponse queryResponse = mock(QueryResponse.class);
    when(adapter.query(any(QueryRequest.class))).thenReturn(queryResponse);
    when(queryResponse.getMetadata()).thenReturn(List.of(metadata));

    // #metadataToTask
    when(filter.getPriority()).thenReturn((byte) 0);

    queryService.query();

    ArgumentCaptor<QueryRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(QueryRequest.class);
    verify(adapter).query(requestArgumentCaptor.capture());
    assertThat(requestArgumentCaptor.getValue().getModifiedAfter(), is(nullValue()));

    ArgumentCaptor<TaskInfoImpl> taskCaptor = ArgumentCaptor.forClass(TaskInfoImpl.class);
    verify(siteQueue).put(taskCaptor.capture());
    TaskInfoImpl createdTask = taskCaptor.getValue();
    assertThat(createdTask.getIntelId(), is(metadata.getId()));
    assertThat(createdTask.getPriority(), is(filter.getPriority()));
    assertThat(createdTask.getOperation(), is(OperationType.HARVEST));
    assertThat(createdTask.getLastModified(), is(metadata.getMetadataModified().toInstant()));
    ResourceInfo resourceInfo = createdTask.getResource().orElse(null);
    assertThat(resourceInfo.getUri().get(), is(metadata.getResourceUri()));
    assertThat(resourceInfo.getLastModified(), is(metadata.getResourceModified().toInstant()));
    assertThat(resourceInfo.getSize().getAsLong(), is(metadata.getResourceSize()));
  }

  private static Metadata mockBasicMetadata() throws Exception {
    Metadata metadata = mock(Metadata.class);
    when(metadata.getId()).thenReturn(METADATA_ID);
    when(metadata.getResourceUri()).thenReturn(new URI(METADATA_RESOURCE_URI));
    when(metadata.getResourceSize()).thenReturn(METADATA_RESOURCE_SIZE);
    when(metadata.getResourceModified()).thenReturn(METADATA_RESOURCE_MODIFIED);
    when(metadata.getMetadataModified()).thenReturn(METADATA_MODIFIED);
    when(metadata.getMetadataSize()).thenReturn(METADATA_SIZE);
    when(metadata.getType()).thenReturn(METADATA_TYPE);
    when(metadata.getRawMetadata()).thenReturn(RAW_METADATA);
    return metadata;
  }

  @Test
  public void stop() throws Exception {
    queryService.stop();
    verify(executor).shutdownNow();
    verify(adapter).close();
  }

  @Test
  public void stopWhileServiceIsRunning() throws Exception {
    ScheduledFuture future = mock(ScheduledFuture.class);
    when(executor.scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq(PERIOD * 1000), eq(TimeUnit.MILLISECONDS)))
        .thenReturn(future);
    queryService.start();
    assertThat(queryService.isRunning(), is(true));
    queryService.stop();
    verify(future).cancel(true);
    verify(executor).shutdownNow();
    verify(adapter).close();
  }

  @Test
  public void stopHandlesAdapterException() throws Exception {
    doThrow(new IOException()).when(adapter).close();
    queryService.stop();
    verify(executor).shutdownNow();
    verify(adapter).close();
  }
}
