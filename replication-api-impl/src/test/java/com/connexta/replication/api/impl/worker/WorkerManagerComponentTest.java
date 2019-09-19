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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.data.CreateRequest;
import com.connexta.replication.api.data.CreateStorageRequest;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.Resource;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.ResourceRequest;
import com.connexta.replication.api.data.ResourceResponse;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.TaskInfo;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.impl.data.SiteImpl;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.connexta.replication.api.impl.queue.memory.MemoryQueueBroker;
import com.connexta.replication.api.impl.queue.memory.MemorySiteQueue;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.data.MetadataImpl;
import com.connexta.replication.junit.rules.MultiThreadedErrorCollector;
import com.connexta.replication.micrometer.MeterRegistryMock;
import com.connexta.replication.solr.EmbeddedSolrConfig;
import com.connexta.replication.spring.ReplicationProperties;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.test.context.junit4.SpringRunner;

@ComponentScan(
    basePackageClasses = {
      WorkerManager.class,
      ComponentTestConfig.class,
      EmbeddedSolrConfig.class,
    })
@EnableSolrRepositories(basePackages = "com.connexta.replication")
@RunWith(SpringRunner.class)
public class WorkerManagerComponentTest {

  private static final String METADATA_ID = "metadataId";
  private static final String LOCAL_SITE_ID = "localSiteId";
  private static final String REMOTE_SITE_ID = "remoteSiteId";
  private static final URL LOCAL_SITE_URL;
  private static final URL REMOTE_SITE_URL;
  private static final int QUEUE_CAPACITY = 20;

  static {
    try {
      LOCAL_SITE_URL = new URL("http://local.test:1234");
      REMOTE_SITE_URL = new URL("http://remote.test:1234");
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

  private static final MeterRegistry REGISTRY = new MeterRegistryMock();

  private WorkerManager workerManager;

  @Autowired private SiteManager siteManager;

  @Rule public final MultiThreadedErrorCollector collector = new MultiThreadedErrorCollector();

  private NodeAdapters nodeAdapters = mock(NodeAdapters.class);

  private NodeAdapterFactory adapterFactory = mock(NodeAdapterFactory.class);

  private MemoryQueueBroker broker;

  private NodeAdapter remoteAdapter;

  private NodeAdapter localAdapter;

  @Before
  public void setup() {
    when(nodeAdapters.factoryFor(SiteType.DDF)).thenReturn(adapterFactory);
    when(nodeAdapters.factoryFor(SiteType.ION)).thenReturn(adapterFactory);

    remoteAdapter = mock(NodeAdapter.class);
    when(remoteAdapter.isAvailable()).thenReturn(true);
    when(remoteAdapter.exists(any(Metadata.class))).thenReturn(true);

    localAdapter = mock(NodeAdapter.class);
    when(localAdapter.isAvailable()).thenReturn(true);
    when(adapterFactory.create(any(URL.class))).thenReturn(localAdapter).thenReturn(remoteAdapter);
    when(localAdapter.createRequest(any(CreateRequest.class))).thenReturn(true);

    broker =
        new MemoryQueueBroker(
            WorkerManagerComponentTest.QUEUE_CAPACITY, WorkerManagerComponentTest.REGISTRY);

    ReplicationProperties props = new ReplicationProperties();
    props.setLocalSite(LOCAL_SITE_ID);

    final Site localSite =
        new SiteImpl(createSitePojo(LOCAL_SITE_ID, LOCAL_SITE_URL, SiteType.ION, 1));
    siteManager.save(localSite);

    final Site remoteSite =
        new SiteImpl(createSitePojo(REMOTE_SITE_ID, REMOTE_SITE_URL, SiteType.DDF, 2));
    siteManager.save(remoteSite);

    assertTimeoutPreemptively(
        Duration.ofSeconds(10),
        () -> {
          while (true) {
            try {
              siteManager.get(LOCAL_SITE_ID);
              siteManager.get(REMOTE_SITE_ID);
              Thread.onSpinWait();
              break;
            } catch (NotFoundException ignore) {
            }
          }
        });

    workerManager = new WorkerManager(broker, siteManager, nodeAdapters, props);
  }

  @After
  public void cleanup() throws Exception {
    workerManager.destroy();
    siteManager.objects().map(Site::getId).forEach(siteManager::remove);
  }

  @Test
  public void testWorkerHarvestDdfResource() throws Exception {
    // setup
    final URI resourceUri = new URI("http://remote.test:1234/testId");
    final Date modifiedDate = new Date();
    final int resourceSize = 10;
    final Metadata metadata = createMetadata(METADATA_ID, resourceUri, modifiedDate, resourceSize);
    final ResourceInfo resourceInfo = createResourceInfo(metadata);
    final MetadataInfo metadataInfo = createMetadataInfo(metadata);
    final MemorySiteQueue queue = broker.getSiteQueue(REMOTE_SITE_ID);
    TaskInfo taskInfo = createTaskInfo(METADATA_ID, metadataInfo, resourceInfo);
    Task task = queue.putAndPeek(taskInfo);

    ArgumentCaptor<CreateStorageRequest> createStorageRequestCaptor =
        ArgumentCaptor.forClass(CreateStorageRequest.class);
    when(localAdapter.createResource(createStorageRequestCaptor.capture())).thenReturn(true);

    Resource resource = mock(Resource.class);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceResponse.getResource()).thenReturn(resource);

    ArgumentCaptor<ResourceRequest> resourceRequestCaptor =
        ArgumentCaptor.forClass(ResourceRequest.class);
    when(remoteAdapter.readResource(resourceRequestCaptor.capture())).thenReturn(resourceResponse);

    // when
    workerManager.init();

    // then
    queue.waitForSizeToReach(0, 10, TimeUnit.SECONDS);

    assertTrue(task.wasSuccessful());
    ResourceRequest sentResourceRequest = resourceRequestCaptor.getValue();

    final Metadata sentMetadata = sentResourceRequest.getMetadata();
    assertEquals(metadata.getType(), sentMetadata.getType());
    assertEquals(metadata.getMetadataModified(), sentMetadata.getMetadataModified());
    assertEquals(metadata.getRawMetadata(), sentMetadata.getRawMetadata());
    assertEquals(metadata.getId(), sentMetadata.getId());
  }

  @Test
  public void testWorkerHarvestDdfMetadata() throws Exception {
    final MemorySiteQueue queue = broker.getSiteQueue(REMOTE_SITE_ID);
    // for DDF metadata, it is assumed Task id == metadata ID
    final Metadata metadata = createMetadata(METADATA_ID);
    final TaskInfo taskInfo = createTaskInfo(METADATA_ID, createMetadataInfo(metadata));
    final Task task = queue.putAndPeek(taskInfo);

    ArgumentCaptor<CreateRequest> createRequestCaptor =
        ArgumentCaptor.forClass(CreateRequest.class);
    when(localAdapter.createRequest(createRequestCaptor.capture())).thenReturn(true);

    // when
    workerManager.init();

    // then
    queue.waitForSizeToReach(0, 10, TimeUnit.SECONDS);
    assertTrue(task.wasSuccessful());
    assertFalse(task.isLocked());

    CreateRequest createRequest = createRequestCaptor.getValue();
    Metadata sentMetadata = createRequest.getMetadata().get(0);
    assertEquals(metadata.getType(), sentMetadata.getType());
    assertEquals(metadata.getMetadataModified(), sentMetadata.getMetadataModified());
    assertEquals(metadata.getRawMetadata(), sentMetadata.getRawMetadata());
    assertEquals(metadata.getId(), sentMetadata.getId());
  }

  @Test
  public void testMultipleTasksTaken() throws Exception {
    final String metadataId1 = METADATA_ID + "1";
    final String metadataId2 = METADATA_ID + "2";
    final String metadataId3 = METADATA_ID + "3";

    final MemorySiteQueue queue = broker.getSiteQueue(REMOTE_SITE_ID);
    final TaskInfo taskInfo1 =
        createTaskInfo(metadataId1, createMetadataInfo(createMetadata(metadataId1)));
    final TaskInfo taskInfo2 =
        createTaskInfo(metadataId2, createMetadataInfo(createMetadata(metadataId2)));
    final TaskInfo taskInfo3 =
        createTaskInfo(metadataId3, createMetadataInfo(createMetadata(metadataId3)));
    final Task task1 = queue.putAndPeek(taskInfo1);
    final Task task2 = queue.putAndPeek(taskInfo2);
    final Task task3 = queue.putAndPeek(taskInfo3);

    workerManager.init();

    queue.waitForSizeToReach(0, 10, TimeUnit.SECONDS);

    assertTrue(task1.wasSuccessful());
    assertTrue(task2.wasSuccessful());
    assertTrue(task3.wasSuccessful());
  }

  @Test
  public void testIgnoredTask() throws Exception {
    final MemorySiteQueue queue = broker.getSiteQueue(REMOTE_SITE_ID);
    // for DDF metadata, it is assumed Task id == metadata ID
    final Metadata metadata = createMetadata(METADATA_ID);
    final TaskInfo taskInfo =
        createTaskInfo(METADATA_ID, OperationType.UNKNOWN, createMetadataInfo(metadata));
    final Task task = queue.putAndPeek(taskInfo);

    // when
    workerManager.init();

    // then
    queue.waitForPendingSizeToReach(0, 10, TimeUnit.SECONDS);

    queue.waitForSizeToReach(1, 10, TimeUnit.SECONDS);
    assertFalse(task.isLocked());
    assertEquals(1, task.getTotalAttempts());
  }

  @Test
  public void testRetryableFailedTask() throws Exception {
    final MemorySiteQueue queue = broker.getSiteQueue(REMOTE_SITE_ID);
    // for DDF metadata, it is assumed Task id == metadata ID
    final Metadata metadata = createMetadata(METADATA_ID);
    final TaskInfo taskInfo = createTaskInfo(METADATA_ID, createMetadataInfo(metadata));
    final Task task = queue.putAndPeek(taskInfo);

    when(localAdapter.createRequest(any(CreateRequest.class))).thenReturn(false);

    // when
    workerManager.init();

    // then
    queue.waitForPendingSizeToReach(0, 10, TimeUnit.SECONDS);

    queue.waitForSizeToReach(1, 10, TimeUnit.SECONDS);
    assertFalse(task.isLocked());
    assertEquals(1, task.getTotalAttempts());
    queue.waitForSizeToReach(1, 10, TimeUnit.SECONDS);
  }

  @Test
  public void testUnretryableFailedTask() throws Exception {
    final MemorySiteQueue queue = broker.getSiteQueue(REMOTE_SITE_ID);
    // for DDF metadata, it is assumed Task id == metadata ID
    final Metadata metadata = createMetadata(METADATA_ID);
    final TaskInfo taskInfo = createTaskInfo(METADATA_ID, createMetadataInfo(metadata));
    final Task task = queue.putAndPeek(taskInfo);

    when(remoteAdapter.exists(any(Metadata.class))).thenReturn(false);

    // when
    workerManager.init();

    // then
    queue.waitForSizeToReach(0, 10, TimeUnit.SECONDS);

    assertFalse(task.isLocked());
    assertTrue(task.hasFailed());
    assertEquals(1, task.getTotalAttempts());
  }

  private SitePojo createSitePojo(String id, URL url, SiteType type, int seed) {
    return new SitePojo()
        .setId(id)
        .setName("name" + seed)
        .setDescription("description" + seed)
        .setUrl(url)
        .setType(type)
        .setKind(SiteKind.REGIONAL)
        .setPollingPeriod(100)
        .setParallelismFactor(1);
  }

  private TaskInfo createTaskInfo(String id, MetadataInfo metadataInfo) {
    return new TaskInfoImpl(
        id, (byte) 1, OperationType.HARVEST, Instant.now(), Set.of(metadataInfo));
  }

  private TaskInfo createTaskInfo(
      String id, OperationType operationType, MetadataInfo metadataInfo) {
    return new TaskInfoImpl(id, (byte) 1, operationType, Instant.now(), Set.of(metadataInfo));
  }

  private TaskInfo createTaskInfo(String id, MetadataInfo metadataInfo, ResourceInfo resourceInfo) {
    return new TaskInfoImpl(
        id, (byte) 1, OperationType.HARVEST, Instant.now(), resourceInfo, Set.of(metadataInfo));
  }

  private MetadataInfo createMetadataInfo(Metadata metadata) {
    return new DdfMetadataInfoImpl<>("metacard", metadata);
  }

  private Metadata createMetadata(String id) {
    return new MetadataImpl(new Object(), Object.class, id, new Date());
  }

  private Metadata createMetadata(String id, URI resourceUri, Date resourceModified, int size) {
    Metadata metadata = new MetadataImpl(new Object(), Object.class, id, new Date());
    metadata.setResourceUri(resourceUri);
    metadata.setResourceModified(resourceModified);
    metadata.setResourceSize(size);
    return metadata;
  }

  private ResourceInfo createResourceInfo(Metadata metadata) {
    return new ResourceInfoImpl(metadata);
  }
}
