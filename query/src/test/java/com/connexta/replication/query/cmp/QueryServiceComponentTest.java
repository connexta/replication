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
package com.connexta.replication.query.cmp;

import static com.connexta.replication.query.cmp.TestDataGenerationUtils.generateFilterPojo;
import static com.connexta.replication.query.cmp.TestDataGenerationUtils.generateMetadataWithResource;
import static com.connexta.replication.query.cmp.TestDataGenerationUtils.generateSitePojo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.QueryRequest;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.ddf.DdfMetadataInfo;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.impl.data.FilterImpl;
import com.connexta.replication.api.impl.data.SiteImpl;
import com.connexta.replication.api.impl.persistence.spring.FilterIndexRepository;
import com.connexta.replication.api.impl.queue.memory.MemoryQueueBroker;
import com.connexta.replication.api.impl.queue.memory.MemorySiteQueue;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.QueueBroker;
import com.connexta.replication.data.QueryResponseImpl;
import com.connexta.replication.junit.rules.MultiThreadedErrorCollector;
import com.connexta.replication.micrometer.MeterRegistryMock;
import com.connexta.replication.query.QueryManager;
import com.connexta.replication.query.QueryServiceTools;
import com.connexta.replication.solr.EmbeddedSolrConfig;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.awaitility.Awaitility;
import org.codice.junit.rules.ClearInterruptions;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.test.context.junit4.SpringRunner;

@ComponentScan(
    basePackageClasses = {
      ComponentTestConfig.class,
      FilterIndexRepository.class,
      EmbeddedSolrConfig.class
    })
@EnableSolrRepositories(basePackages = "com.connexta.replication")
@RunWith(SpringRunner.class)
public class QueryServiceComponentTest {
  @Rule public final MultiThreadedErrorCollector collector = new MultiThreadedErrorCollector();
  @Rule public final ClearInterruptions clearInterruptions = new ClearInterruptions();

  private static final int QUEUE_CAPACITY = 3;
  private static final long THIRTY_MINUTES = TimeUnit.MINUTES.toSeconds(30L);
  private static final MeterRegistry REGISTRY = new MeterRegistryMock();

  private final QueueBroker broker =
      new MemoryQueueBroker(
          QueryServiceComponentTest.QUEUE_CAPACITY, QueryServiceComponentTest.REGISTRY);

  @Autowired private FilterIndexManager indexManager;
  @Autowired private FilterManager filterManager;
  @Autowired private SiteManager siteManager;

  private final NodeAdapters nodeAdapters = mock(NodeAdapters.class);
  private final NodeAdapterFactory adapterFactory = mock(NodeAdapterFactory.class);
  private final Set<Task> tasks = new HashSet<>();
  private final NodeAdapter adapter = mock(NodeAdapter.class);
  private QueryManager queryManager;

  private void startQueryManager(long period) {
    queryManager =
        new QueryManager(
            Stream.empty(),
            new QueryServiceTools(
                nodeAdapters, siteManager, filterManager, indexManager, broker, period),
            period,
            "localSite");
    queryManager.init();
  }

  @Before
  public void setup() {
    when(adapterFactory.create(any(URL.class))).thenReturn(adapter);
    when(nodeAdapters.factoryFor(SiteType.DDF)).thenReturn(adapterFactory);
    when(nodeAdapters.factoryFor(SiteType.ION)).thenAnswer(collectAdapterException());
    when(adapter.isAvailable()).thenReturn(true);
  }

  private Answer<?> collectAdapterException() {
    return (Answer<Object>)
        invocationOnMock -> {
          Throwable t = new AdapterException("no adapters for ION sites should be retrieved");
          collector.addError(t);
          throw t;
        };
  }

  @After
  public void cleanup() throws Exception {
    if (queryManager != null) {
      queryManager.destroy();
    }
    tasks.clear();
    filterManager.objects().map(Filter::getId).forEach(filterManager::remove);
    siteManager.objects().map(Site::getId).forEach(siteManager::remove);
  }

  /*
   Verifies that multiple metacards can be handled and that the proper date is saved in the filterIndex
  */
  @Test
  public void testQueryMultipleMetacards() throws Exception {
    final Site site = new SiteImpl(generateSitePojo(1));
    siteManager.save(site);
    final Filter filter = new FilterImpl(generateFilterPojo("1", 11));
    filterManager.save(filter);
    List<Metadata> metadataList =
        List.of(
            generateMetadataWithResource(111),
            generateMetadataWithResource(112),
            generateMetadataWithResource(113, new Date(1)));
    when(adapter.query(Mockito.argThat(isQueryRequestWithFilter(filter.getFilter()))))
        .thenReturn(
            new QueryResponseImpl(metadataList, this::collectError),
            new QueryResponseImpl(
                List.of(),
                RuntimeException
                    ::new)); // we don't care about polls after the first one so we pass a response
    // without the error collector
    startQueryManager(THIRTY_MINUTES);

    final MemorySiteQueue queue = (MemorySiteQueue) broker.getQueue(site.getId());

    assertThat(
        "timed out waiting for expected published task",
        queue.waitForPendingSizeToReach(3, 5L, TimeUnit.SECONDS),
        Matchers.equalTo(true));

    for (Metadata metadata : metadataList) {
      assertTask(queue.take(), metadata, (byte) 0, OperationType.HARVEST);
    }

    assertIndexSaved(filter, metadataList.get(2).getMetadataModified());
  }

  @Test
  public void testQueryThrough2Polls() throws Exception {
    final Site site = new SiteImpl(generateSitePojo(1));
    siteManager.save(site);
    final Filter filter = new FilterImpl(generateFilterPojo("1", 11));
    filterManager.save(filter);
    List<Metadata> metadataList =
        List.of(
            generateMetadataWithResource(111),
            generateMetadataWithResource(112),
            generateMetadataWithResource(113, new Date(1)));
    Metadata secondPollMetadata = generateMetadataWithResource(114, new Date(2));
    when(adapter.query(Mockito.argThat(isQueryRequestWithFilter(filter.getFilter()))))
        .thenReturn(
            new QueryResponseImpl(metadataList, this::collectError),
            new QueryResponseImpl(List.of(secondPollMetadata), this::collectError),
            new QueryResponseImpl(List.of(), RuntimeException::new));
    startQueryManager(THIRTY_MINUTES);

    final MemorySiteQueue queue = (MemorySiteQueue) broker.getQueue(site.getId());

    assertThat(
        "timed out waiting for expected published task",
        queue.waitForPendingSizeToReach(3, 5L, TimeUnit.SECONDS),
        Matchers.equalTo(true));
    for (Metadata metadata : metadataList) {
      assertTask(queue.take(), metadata, (byte) 0, OperationType.HARVEST);
    }

    assertIndexSaved(filter, metadataList.get(2).getMetadataModified());

    try {
      queue.suspendSignals();
      for (final Task task : tasks) {
        task.complete();
      }
    } finally {
      queue.resumeSignals();
    }
    assertThat(
        "timed out waiting for expected published task",
        queue.waitForPendingSizeToReach(1, 5L, TimeUnit.SECONDS),
        Matchers.equalTo(true));
    verify(adapter)
        .query(
            Mockito.argThat(
                isQueryRequestWithFilterAndModifiedDate(
                    filter.getFilter(), metadataList.get(2).getMetadataModified())));
    assertTask(queue.take(), secondPollMetadata, (byte) 0, OperationType.HARVEST);

    assertIndexSaved(filter, secondPollMetadata.getMetadataModified());
  }

  /*
  Verifies that multiple filters can be handled, suspended filters are ignored, and higher priority
  filters are handled first.
   */
  @Test
  public void testQueryMultipleFilters() throws Exception {
    final Site site = new SiteImpl(generateSitePojo(1));
    siteManager.save(site);
    List<Filter> filterList =
        List.of(
            new FilterImpl(generateFilterPojo("1", 11)),
            new FilterImpl(generateFilterPojo("1", 12).setSuspended(true).setPriority((byte) 1)),
            new FilterImpl(generateFilterPojo("1", 13).setPriority((byte) 9)));
    List<Metadata> metadataList =
        List.of(
            generateMetadataWithResource(111),
            generateMetadataWithResource(121),
            generateMetadataWithResource(131));

    // for each filter save it in the filter manager and when performing a query on that filter
    // return a metacard
    // unique metacards must be used so the queue isn't given several tasks with the same id
    for (int i = 0; i < filterList.size(); i++) {
      filterManager.save(filterList.get(i));
      when(adapter.query(Mockito.argThat(isQueryRequestWithFilter(filterList.get(i).getFilter()))))
          .thenReturn(
              new QueryResponseImpl(List.of(metadataList.get(i)), this::collectError),
              new QueryResponseImpl(List.of(), RuntimeException::new));
    }

    startQueryManager(THIRTY_MINUTES);

    final MemorySiteQueue queue = (MemorySiteQueue) broker.getQueue(site.getId());

    assertThat(
        "timed out waiting for expected published task",
        queue.waitForPendingSizeToReach(2, 5L, TimeUnit.SECONDS),
        Matchers.equalTo(true));

    assertTask(queue.take(), metadataList.get(2), (byte) 9, OperationType.HARVEST);
    assertTask(queue.take(), metadataList.get(0), (byte) 0, OperationType.HARVEST);

    assertIndexSaved(filterList.get(0), metadataList.get(0).getMetadataModified());
    assertIndexSaved(filterList.get(2), metadataList.get(2).getMetadataModified());
  }

  /*
  Verifies that ION and Tactical DDF sites are handled correctly
   */
  @Test
  public void testQueryMultipleSites() throws Exception {
    List<Site> siteList =
        List.of(
            new SiteImpl(generateSitePojo(1).setKind(SiteKind.TACTICAL)),
            new SiteImpl(generateSitePojo(2).setType(SiteType.ION)),
            new SiteImpl(generateSitePojo(3)));
    List<Filter> filterList =
        List.of(
            new FilterImpl(generateFilterPojo("1", 11)),
            new FilterImpl(generateFilterPojo("2", 21)),
            new FilterImpl(generateFilterPojo("3", 31)));
    List<Metadata> metadataList =
        List.of(
            generateMetadataWithResource(111),
            generateMetadataWithResource(211),
            generateMetadataWithResource(311));

    // for each site, save it in the siteManager and save the associated filter in the
    // filterManager.
    // when a query is made with a filter, return the associated metacard.
    for (int i = 0; i < siteList.size(); i++) {
      siteManager.save(siteList.get(i));
      filterManager.save(filterList.get(i));
      when(adapter.query(Mockito.argThat(isQueryRequestWithFilter(filterList.get(i).getFilter()))))
          .thenReturn(
              new QueryResponseImpl(List.of(metadataList.get(i)), this::collectError),
              new QueryResponseImpl(List.of(), RuntimeException::new));
    }

    startQueryManager(THIRTY_MINUTES);

    // grab the queue for each site and verify only the queues for valid sites get tasks put in
    // them.
    MemorySiteQueue tacticalQueue = (MemorySiteQueue) broker.getQueue(siteList.get(0).getId());
    MemorySiteQueue ionQueue = (MemorySiteQueue) broker.getQueue(siteList.get(1).getId());
    MemorySiteQueue regionalQueue = (MemorySiteQueue) broker.getQueue(siteList.get(2).getId());

    assertThat(
        "timed out waiting for expected published task",
        regionalQueue.waitForPendingSizeToReach(1, 5L, TimeUnit.SECONDS),
        Matchers.equalTo(true));
    assertTask(regionalQueue.take(), metadataList.get(2), (byte) 0, OperationType.HARVEST);
    assertThat(tacticalQueue.pendingSize(), Matchers.equalTo(0));
    assertThat(ionQueue.pendingSize(), Matchers.equalTo(0));

    assertIndexSaved(filterList.get(2), metadataList.get(2).getMetadataModified());
  }

  private void assertIndexSaved(Filter filter, Date lastModified) {
    // Awaitility has a default polling period of 100ms and timeout of 10s
    Awaitility.await()
        .catchUncaughtExceptions()
        .until(
            () ->
                indexManager
                    .getOrCreate(filter)
                    .getModifiedSince()
                    .get()
                    .equals(lastModified.toInstant()));
  }

  private RuntimeException collectError(Throwable e) {
    collector.addError(new AssertionError(e));
    return new RuntimeException(e);
  }

  private ArgumentMatcher<QueryRequest> isQueryRequestWithFilter(String filter) {
    return new ArgumentMatcher<>() {
      @Override
      public boolean matches(QueryRequest request) {
        return (request != null) && request.getCql().equals(filter);
      }

      @Override
      public String toString() {
        return "request for '" + filter + "'";
      }
    };
  }

  private ArgumentMatcher<QueryRequest> isQueryRequestWithFilterAndModifiedDate(
      String filter, Date modified) {
    return new ArgumentMatcher<>() {
      @Override
      public boolean matches(QueryRequest request) {
        return request != null
            && request.getModifiedAfter() != null
            && request.getCql().equals(filter)
            && request.getModifiedAfter().equals(modified);
      }

      @Override
      public String toString() {
        return String.format("request for '%s' after '%s'", filter, modified);
      }
    };
  }

  private void assertTask(Task task, Metadata metacard, byte priority, OperationType operation)
      throws Exception {
    tasks.add(task);
    assertThat(task.getId(), Matchers.equalTo(metacard.getId()));
    assertThat(task.getPriority(), Matchers.equalTo(priority));
    assertThat(task.getOperation(), Matchers.equalTo(operation));
    assertThat(Date.from(task.getLastModified()), Matchers.equalTo(metacard.getMetadataModified()));
    final List<MetadataInfo> metadataInfos = task.metadatas().collect(Collectors.toList());

    assertThat(metadataInfos.size(), Matchers.equalTo(1));
    final MetadataInfo metadataInfo = metadataInfos.get(0);

    assertThat(
        Date.from(metadataInfo.getLastModified()),
        Matchers.equalTo(metacard.getMetadataModified()));
    assertThat(
        metadataInfo.getSize().stream().boxed().findFirst(),
        OptionalMatchers.isPresentAndIs(metacard.getMetadataSize()));
    assertThat(metadataInfo.getType(), Matchers.equalTo("metacard"));
    assertThat(metadataInfo, Matchers.isA(DdfMetadataInfo.class));
    final DdfMetadataInfo<?> ddfMetadataInfo = (DdfMetadataInfo<?>) metadataInfo;

    assertThat(ddfMetadataInfo.getDataClass(), Matchers.equalTo(metacard.getType()));
    assertThat(ddfMetadataInfo.getData(), Matchers.equalTo(metacard.getRawMetadata()));
    if (metacard.getResourceSize() > 0) {
      assertThat(task.getResource(), OptionalMatchers.isPresent());
      final ResourceInfo resource = task.getResource().get();

      assertThat(
          resource.getResourceUri(), OptionalMatchers.isPresentAndIs(metacard.getResourceUri()));
      assertThat(
          Date.from(resource.getLastModified()), Matchers.equalTo(metacard.getResourceModified()));
      assertThat(
          resource.getSize().stream().boxed().findFirst(),
          OptionalMatchers.isPresentAndIs(metacard.getResourceSize()));
    } else {
      assertThat(task.getResource(), OptionalMatchers.isEmpty());
    }
  }
}
