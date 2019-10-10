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
package com.connexta.replication.api.impl.worker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.AdapterInterruptedException;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.ReplicationException;
import com.connexta.replication.api.data.CreateRequest;
import com.connexta.replication.api.data.CreateStorageRequest;
import com.connexta.replication.api.data.DeleteRequest;
import com.connexta.replication.api.data.ErrorCode;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ReplicationPersistenceException;
import com.connexta.replication.api.data.Resource;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.ResourceRequest;
import com.connexta.replication.api.data.ResourceResponse;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.ddf.DdfMetadataInfo;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.Queue;
import com.connexta.replication.api.queue.SiteQueue;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;
import org.codice.junit.rules.ClearInterruptions;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WorkerTest {

  private static final String TASK_ID = "taskId";
  private static final String SITE_ID = "siteId";
  private static final String LOCAL_SITE_NAME = "localSiteName";
  private static final String REMOTE_SITE_NAME = "destinationSiteName";
  private static final URL SITE_URL;
  private static final URI RESOURCE_URI;

  static {
    try {
      SITE_URL = new URL("http://test:1234");
      RESOURCE_URI = new URI("http://test:1234/1234");
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  @Rule public ClearInterruptions clearInterruptions = new ClearInterruptions();

  @Test
  void testUnknownOperationNotSupported() throws Exception {
    // setup
    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.UNKNOWN);

    Queue queue = mock(Queue.class);
    when(queue.take()).thenReturn(task);

    // when
    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testReplicateToOperationNotSupported() throws Exception {
    // setup
    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.REPLICATE_TO);

    Queue queue = mock(Queue.class);
    when(queue.take()).thenReturn(task);

    // when
    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testReplicateFromOperationNotSupported() throws Exception {
    // setup
    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.REPLICATE_FROM);

    Queue queue = mock(Queue.class);
    when(queue.take()).thenReturn(task);

    // when
    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testDeleteOperationNotSupported() throws Exception {
    // setup
    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.DELETE);

    Queue queue = mock(Queue.class);
    when(queue.take()).thenReturn(task);

    // when
    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testMoreThanOneMetadataNotSupported() throws Exception {
    // setup
    MetadataInfo metadata1 = mock(MetadataInfo.class);
    MetadataInfo metadata2 = mock(MetadataInfo.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.REPLICATE_FROM);
    when(task.metadatas()).thenReturn(Stream.of(metadata1, metadata2));

    Queue queue = mock(Queue.class);
    when(queue.take()).thenReturn(task);

    // when
    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testNonDdfMetadataInfoNotSupported() throws Exception {
    // setup
    MetadataInfo metadata = mock(MetadataInfo.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.REPLICATE_FROM);
    when(task.metadatas()).thenReturn(Stream.of(metadata));

    Queue queue = mock(Queue.class);
    when(queue.take()).thenReturn(task);

    // when
    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testDestinationSiteNotFound() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.REPLICATE_FROM);

    MetadataInfo metadata = mock(MetadataInfo.class);
    when(task.metadatas()).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    SiteManager siteManager = mock(SiteManager.class);
    when(siteManager.get(SITE_ID)).thenThrow(NotFoundException.class);

    // when
    Worker worker =
        new Worker(queue, mock(NodeAdapter.class), siteManager, mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testErrorFetchingDestinationSite() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.REPLICATE_FROM);

    MetadataInfo metadata = mock(MetadataInfo.class);
    when(task.metadatas()).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    SiteManager siteManager = mock(SiteManager.class);
    when(siteManager.get(SITE_ID)).thenThrow(ReplicationException.class);

    // when
    Worker worker =
        new Worker(queue, mock(NodeAdapter.class), siteManager, mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @ParameterizedTest
  @MethodSource("unavailableAdapterArgs")
  void testAdaptersNotAvailable(String unavailableSiteName, NodeAdapter local, NodeAdapter remote)
      throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);

    MetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(any(URL.class))).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task)
        .fail(
            ErrorCode.SITE_UNAVAILABLE,
            String.format("Site %s is unavailable", unavailableSiteName));
  }

  @Test
  void testHarvestMetadata() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task).complete();
    verify(local).createRequest(any(CreateRequest.class));
  }

  @Test
  void testHarvestResource() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    ResourceInfo resourceInfo = mock(ResourceInfo.class);
    when(resourceInfo.getUri()).thenReturn(Optional.of(RESOURCE_URI));

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    when(task.getResource()).thenReturn(Optional.of(resourceInfo));

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task).complete();
    verify(local).createResource(any(CreateStorageRequest.class));
    verify(local, never()).createRequest(any(CreateRequest.class));
  }

  @Test
  void testHarvestMetadataWhenDoesNotExistOnSource() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mock(NodeAdapter.class);
    when(remote.isAvailable()).thenReturn(true);
    when(remote.getSystemName()).thenReturn(REMOTE_SITE_NAME);
    when(remote.exists(any(Metadata.class))).thenReturn(false);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task)
        .fail(
            ErrorCode.NO_LONGER_EXISTS,
            "Metadata deleted from site destinationSiteName before able to create");
  }

  @Test
  void testHarvestMetadataCheckingExistenceError() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    when(remote.exists(any(Metadata.class))).thenThrow(AdapterException.class);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task)
        .fail(ErrorCode.OPERATION_FAILURE, "Failed check resource existence on localSiteName");
  }

  @Test
  void testHarvestMetadataUnsuccessful() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);
    when(local.createRequest(any(CreateRequest.class))).thenReturn(false);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task)
        .fail(
            ErrorCode.OPERATION_FAILURE,
            "Failed to HARVEST metadata taskId from destinationSiteName to localSiteName");
  }

  @Test
  void testHarvestMetadataErrorOnCreate() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mock(NodeAdapter.class);
    when(local.getSystemName()).thenReturn(LOCAL_SITE_NAME);
    when(local.isAvailable()).thenReturn(true);
    when(local.createRequest(any(CreateRequest.class))).thenThrow(AdapterException.class);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task)
        .fail(ErrorCode.OPERATION_FAILURE, "Failed to retrieve metadata from localSiteName");
  }

  @Test
  void testHarvestResourceUnsuccessful() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    ResourceInfo resourceInfo = mock(ResourceInfo.class);
    when(resourceInfo.getUri()).thenReturn(Optional.of(RESOURCE_URI));

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    when(task.getResource()).thenReturn(Optional.of(resourceInfo));

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);
    when(local.createResource(any(CreateStorageRequest.class))).thenReturn(false);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task)
        .fail(
            ErrorCode.OPERATION_FAILURE,
            "Failed to HARVEST resource http://test:1234/1234 from destinationSiteName to localSiteName");
  }

  @Test
  void testHarvestResourceErrorReadingResource() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    ResourceInfo resourceInfo = mock(ResourceInfo.class);
    when(resourceInfo.getUri()).thenReturn(Optional.of(RESOURCE_URI));

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    when(task.getResource()).thenReturn(Optional.of(resourceInfo));

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    when(remote.readResource(any(ResourceRequest.class))).thenThrow(AdapterException.class);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task)
        .fail(ErrorCode.OPERATION_FAILURE, "Failed to read resource from destinationSiteName");
  }

  @Test
  void testHarvestResourceErrorCreatingResource() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    ResourceInfo resourceInfo = mock(ResourceInfo.class);
    when(resourceInfo.getUri()).thenReturn(Optional.of(RESOURCE_URI));

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    when(task.getResource()).thenReturn(Optional.of(resourceInfo));

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    when(local.createResource(any(CreateStorageRequest.class))).thenThrow(AdapterException.class);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task)
        .fail(
            ErrorCode.OPERATION_FAILURE,
            "Failed to create resource from destinationSiteName to localSiteName");
  }

  @Test
  void testInterruptedWhileTaking() throws Exception {
    // setup
    Queue queue = mock(Queue.class);
    when(queue.take()).thenThrow(InterruptedException.class);
    NodeAdapter local = mock(NodeAdapter.class);

    // when
    Worker worker = new Worker(queue, local, mock(SiteManager.class), mock(NodeAdapters.class));
    worker.run();

    assertThat(worker.isRunning(), is(false));
    assertThat(Thread.currentThread().isInterrupted(), is(true));
  }

  @Test
  void testRemoteAdapterClosed() throws Exception {
    // setup
    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));

    SiteQueue queue = mock(SiteQueue.class);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);
    when(task.getQueue()).thenReturn(queue);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);
    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);

    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(remote).close();
  }

  @Test
  void testLocalAdapterClosed() throws Exception {
    // setup
    Queue queue = mock(Queue.class);
    NodeAdapter local = mock(NodeAdapter.class);

    Worker worker = new Worker(queue, local, mock(SiteManager.class), mock(NodeAdapters.class));
    worker.setProcessing(false);
    worker.cancelIfNotProcessing();

    // when
    worker.run();

    // then
    verify(local).close();
  }

  @Test
  void testCancelWhileProcessingIsStillRunning() throws Exception {
    // setup
    Queue queue = mock(Queue.class);

    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.setProcessing(true);

    // when
    boolean cancelled = worker.cancelIfNotProcessing();

    // then
    assertThat(cancelled, is(false));
    assertThat(worker.isRunning(), is(true));
  }

  @Test
  void testDestinationAdapterNotFound() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mock(DdfMetadataInfo.class);
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    SiteManager siteManager = mock(SiteManager.class);
    when(siteManager.get(SITE_ID)).thenThrow(NotFoundException.class);

    // when
    Worker worker =
        new Worker(queue, mock(NodeAdapter.class), siteManager, mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testErrorRetrievingDestinationAdapter() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mock(DdfMetadataInfo.class);
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    SiteManager siteManager = mock(SiteManager.class);
    when(siteManager.get(SITE_ID)).thenThrow(ReplicationPersistenceException.class);

    // when
    Worker worker =
        new Worker(queue, mock(NodeAdapter.class), siteManager, mock(NodeAdapters.class));
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testRunAfterCancelledWhileNotProcessing() throws Exception {
    // setup
    Queue queue = mock(Queue.class);

    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.setProcessing(false);
    worker.cancelIfNotProcessing();

    // when
    worker.run();

    // then
    verify(queue, never()).take();
  }

  @Test
  void testIllegalArgumentExceptionWhenGettingDestinationAdapter() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenThrow(IllegalArgumentException.class);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testAdapterExceptionWhenGettingDestinationAdapter() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenThrow(AdapterException.class);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.doRun();

    // then
    verify(task).unlock();
  }

  @Test
  void testUnsupportedTaskUnlockInterrupted() throws Exception {
    // setup
    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.UNKNOWN);
    doThrow(InterruptedException.class).when(task).unlock();

    Queue queue = mock(Queue.class);
    when(queue.take()).thenReturn(task);

    // when
    Worker worker =
        new Worker(
            queue, mock(NodeAdapter.class), mock(SiteManager.class), mock(NodeAdapters.class));
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task, times(2)).unlock();
  }

  @Test
  void testRemoteSiteIsNullTaskUnlockInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.REPLICATE_FROM);
    doThrow(InterruptedException.class).when(task).unlock();

    MetadataInfo metadata = mock(MetadataInfo.class);
    when(task.metadatas()).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    SiteManager siteManager = mock(SiteManager.class);
    when(siteManager.get(SITE_ID)).thenThrow(NotFoundException.class);

    // when
    Worker worker =
        new Worker(queue, mock(NodeAdapter.class), siteManager, mock(NodeAdapters.class));
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task, times(2)).unlock();
  }

  @Test
  void testSourceExistsInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    when(remote.exists(any(Metadata.class))).thenThrow(AdapterInterruptedException.class);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.run();

    // then
    verify(task).unlock();
  }

  @Test
  void testSourceExistsAdapterExceptionTaskFailInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    doThrow(InterruptedException.class).when(task).fail(any(), anyString());

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    when(remote.exists(any(Metadata.class))).thenThrow(AdapterException.class);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  @Test
  void testResourceReadAdapterExceptionTaskFailInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    ResourceInfo resourceInfo = mock(ResourceInfo.class);
    when(resourceInfo.getUri()).thenReturn(Optional.of(RESOURCE_URI));

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    when(task.getResource()).thenReturn(Optional.of(resourceInfo));
    doThrow(InterruptedException.class).when(task).fail(any(), anyString());

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    when(remote.readResource(any(ResourceRequest.class))).thenThrow(AdapterException.class);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  @Test
  void testResourceReadInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    ResourceInfo resourceInfo = mock(ResourceInfo.class);
    when(resourceInfo.getUri()).thenReturn(Optional.of(RESOURCE_URI));

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    when(task.getResource()).thenReturn(Optional.of(resourceInfo));

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    when(remote.readResource(any(ResourceRequest.class)))
        .thenThrow(AdapterInterruptedException.class);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  @Test
  void testResourceCreateAdapterExceptionTaskFailInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    ResourceInfo resourceInfo = mock(ResourceInfo.class);
    when(resourceInfo.getUri()).thenReturn(Optional.of(RESOURCE_URI));

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    when(task.getResource()).thenReturn(Optional.of(resourceInfo));
    doThrow(InterruptedException.class).when(task).fail(any(), anyString());

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);

    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();
    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);
    when(local.createResource(any(CreateStorageRequest.class))).thenThrow(AdapterException.class);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  @Test
  void testMetadataCreateInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);
    when(local.createRequest(any(CreateRequest.class)))
        .thenThrow(AdapterInterruptedException.class);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  @Test
  void testMetadataCreateAdapterExceptionTaskFailInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);
    doThrow(InterruptedException.class).when(task).fail(any(), anyString());

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenReturn(remote);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);
    when(local.createRequest(any(CreateRequest.class))).thenThrow(AdapterException.class);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  @Test
  void testLocalNotAvailableTaskUnlockInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    doThrow(InterruptedException.class).when(task).fail(any(), anyString());

    MetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    SiteManager siteManager = mockSiteManager();

    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    NodeAdapter remote = mockAdapter(REMOTE_SITE_NAME);
    when(factory.create(any(URL.class))).thenReturn(remote);
    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker =
        new Worker(queue, mockUnavailableAdapter(LOCAL_SITE_NAME), siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  @Test
  void testDestinationNotAvailableTaskUnlockInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    doThrow(InterruptedException.class).when(task).fail(any(), anyString());

    MetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata));
    when(task.getQueue()).thenReturn(queue);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    SiteManager siteManager = mockSiteManager();

    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    NodeAdapter remote = mockUnavailableAdapter(REMOTE_SITE_NAME);
    when(factory.create(any(URL.class))).thenReturn(remote);
    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, mockAdapter(LOCAL_SITE_NAME), siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  @Test
  void testCreateDestinationAdapterInterrupted() throws Exception {
    // setup
    SiteQueue queue = mock(SiteQueue.class);

    Task task = mock(Task.class);
    when(task.getOperation()).thenReturn(OperationType.HARVEST);
    when(task.getIntelId()).thenReturn(TASK_ID);

    DdfMetadataInfo metadata = mockDdfMetadata();
    when(task.metadatas()).thenReturn(Stream.of(metadata)).thenReturn(Stream.of(metadata));

    when(task.getQueue()).thenReturn(queue);
    when(queue.take()).thenReturn(task);
    when(queue.getSite()).thenReturn(SITE_ID);

    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(SITE_URL)).thenThrow(AdapterInterruptedException.class);

    SiteManager siteManager = mockSiteManager();

    NodeAdapter local = mockAdapter(LOCAL_SITE_NAME);

    NodeAdapters nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(any(SiteType.class))).thenReturn(factory);

    // when
    Worker worker = new Worker(queue, local, siteManager, nodeAdapters);
    worker.run();

    // then
    assertThat(Thread.currentThread().isInterrupted(), is(true));
    verify(task).unlock();
  }

  private static Stream<Arguments> unavailableAdapterArgs() {
    return Stream.of(
        Arguments.of(
            LOCAL_SITE_NAME,
            mockUnavailableAdapter(LOCAL_SITE_NAME),
            mockAdapter(REMOTE_SITE_NAME)),
        Arguments.of(
            REMOTE_SITE_NAME,
            mockAdapter(LOCAL_SITE_NAME),
            mockUnavailableAdapter(REMOTE_SITE_NAME)));
  }

  private static NodeAdapter mockUnavailableAdapter(String name) {
    NodeAdapter adapter = mock(NodeAdapter.class);
    when(adapter.getSystemName()).thenReturn(name);
    when(adapter.isAvailable()).thenReturn(false);
    return adapter;
  }

  private static NodeAdapter mockAdapter(String name) {
    Resource resource = mock(Resource.class);
    ResourceResponse response = mock(ResourceResponse.class);
    when(response.getResource()).thenReturn(resource);

    NodeAdapter adapter = mock(NodeAdapter.class);
    when(adapter.getSystemName()).thenReturn(name);
    when(adapter.exists(any(Metadata.class))).thenReturn(true);
    when(adapter.createRequest(any(CreateRequest.class))).thenReturn(true);
    when(adapter.deleteRequest(any(DeleteRequest.class))).thenReturn(true);
    when(adapter.readResource(any(ResourceRequest.class))).thenReturn(response);
    when(adapter.createResource(any(CreateStorageRequest.class))).thenReturn(true);
    when(adapter.isAvailable()).thenReturn(true);
    return adapter;
  }

  private static DdfMetadataInfo mockDdfMetadata() {
    DdfMetadataInfo metadata = mock(DdfMetadataInfo.class);
    when(metadata.getData()).thenReturn("data");
    when(metadata.getDataClass()).thenReturn(String.class);
    when(metadata.getLastModified()).thenReturn(Instant.now());
    return metadata;
  }

  private static SiteManager mockSiteManager() {
    Site site = mock(Site.class);
    when(site.getType()).thenReturn(SiteType.DDF);
    when(site.getUrl()).thenReturn(SITE_URL);
    SiteManager siteManager = mock(SiteManager.class);
    when(siteManager.get(SITE_ID)).thenReturn(site);
    return siteManager;
  }
}
