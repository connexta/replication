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
package com.connexta.replication.api.impl.spring;

import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.Replicator;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.impl.ReplicatorImpl;
import com.connexta.replication.api.impl.ReplicatorRunner;
import com.connexta.replication.api.impl.Syncer;
import com.connexta.replication.api.impl.data.FilterIndexManagerImpl;
import com.connexta.replication.api.impl.data.FilterManagerImpl;
import com.connexta.replication.api.impl.data.ReplicationItemManagerImpl;
import com.connexta.replication.api.impl.data.SiteManagerImpl;
import com.connexta.replication.api.impl.persistence.spring.FilterIndexRepository;
import com.connexta.replication.api.impl.persistence.spring.FilterRepository;
import com.connexta.replication.api.impl.persistence.spring.ItemRepository;
import com.connexta.replication.api.impl.persistence.spring.SiteRepository;
import com.connexta.replication.api.impl.queue.memory.MemoryQueueBroker;
import com.connexta.replication.api.impl.worker.WorkerManager;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.ReplicationItemManager;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.QueueBroker;
import com.connexta.replication.spring.ReplicationProperties;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.solr.core.SolrTemplate;

/** Spring auto configuration class for replication implementations. */
@Configuration("replication-api-impl")
public class ServiceConfig {

  @Bean
  public ReplicationItemManager replicationItemManager(
      ItemRepository itemRepository, SolrTemplate solrTemplate) {
    return new ReplicationItemManagerImpl(itemRepository, solrTemplate);
  }

  @Bean
  public SiteManager siteManager(SiteRepository siteRepository) {
    return new SiteManagerImpl(siteRepository);
  }

  /**
   * Used to create a {@link FilterManager} bean which provides an abstraction layer for CRUD
   * operations involving {@link com.connexta.replication.api.data.Filter}s.
   *
   * @param filterRepository a {@link org.springframework.data.repository.CrudRepository} for basic
   *     CRUD operations
   * @param filterIndexManager the {@link FilterIndexManager}
   * @return the {@link FilterManager}
   */
  @Bean
  public FilterManager filterManager(
      FilterRepository filterRepository, FilterIndexManager filterIndexManager) {
    return new FilterManagerImpl(filterRepository, filterIndexManager);
  }

  @Bean
  public FilterIndexManager filterIndexManager(FilterIndexRepository filterIndexRepository) {
    return new FilterIndexManagerImpl(filterIndexRepository);
  }

  @Profile("Classic")
  @Bean
  public Syncer syncer(
      ReplicationItemManager replicationItemManager, FilterIndexManager filterIndexManager) {
    return new Syncer(replicationItemManager, filterIndexManager);
  }

  @Bean
  public NodeAdapters nodeAdapters(List<NodeAdapterFactory> nodeAdapterFactories) {
    NodeAdapters nodeAdapters = new NodeAdapters();
    nodeAdapters.setNodeAdapterFactories(nodeAdapterFactories);
    return nodeAdapters;
  }

  @Profile("Ion")
  @Bean
  public QueueBroker queueBroker(MeterRegistry meterRegistry) {
    return new MemoryQueueBroker(2000, meterRegistry);
  }

  @Profile("Ion")
  @Bean(destroyMethod = "destroy")
  public WorkerManager workerManager(
      QueueBroker queueBroker,
      SiteManager siteManager,
      NodeAdapters nodeAdapters,
      ReplicationProperties properties) {
    WorkerManager workerManager =
        new WorkerManager(queueBroker, siteManager, nodeAdapters, properties);
    workerManager.init();
    return workerManager;
  }

  @Profile("Classic")
  @Bean(destroyMethod = "cleanUp")
  public Replicator replicator(
      NodeAdapters nodeAdapters,
      SiteManager siteManager,
      Syncer syncer,
      ReplicationProperties properties) {
    ReplicatorImpl replicator =
        new ReplicatorImpl(nodeAdapters, siteManager, syncer, properties.getLocalSite());
    replicator.init();
    return replicator;
  }

  @Profile("Classic")
  @Bean(destroyMethod = "destroy")
  public ReplicatorRunner replicatorRunner(
      Replicator replicator,
      FilterManager filterManager,
      SiteManager siteManager,
      ReplicationProperties properties) {
    ReplicatorRunner replicatorRunner =
        new ReplicatorRunner(
            replicator, filterManager, siteManager, properties.sites(), properties.getPeriod());
    replicatorRunner.init();
    return replicatorRunner;
  }
}
