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
import com.connexta.replication.api.impl.data.ReplicatorConfigManagerImpl;
import com.connexta.replication.api.impl.data.SiteManagerImpl;
import com.connexta.replication.api.impl.persistence.spring.ConfigRepository;
import com.connexta.replication.api.impl.persistence.spring.FilterIndexRepository;
import com.connexta.replication.api.impl.persistence.spring.FilterRepository;
import com.connexta.replication.api.impl.persistence.spring.ItemRepository;
import com.connexta.replication.api.impl.persistence.spring.SiteRepository;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.ReplicationItemManager;
import com.connexta.replication.api.persistence.ReplicatorConfigManager;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.spring.ReplicationProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
  public ReplicatorConfigManager replicatorConfigManager(ConfigRepository configRepository) {
    return new ReplicatorConfigManagerImpl(configRepository);
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
   * @return the {@link FilterManager}
   */
  @Bean
  public FilterManager filterManager(FilterRepository filterRepository) {
    return new FilterManagerImpl(filterRepository);
  }

  @Bean
  public FilterIndexManager siteIndexManager(FilterIndexRepository filterIndexRepository) {
    return new FilterIndexManagerImpl(filterIndexRepository);
  }

  @Bean
  public Syncer syncer(
      ReplicationItemManager replicationItemManager,
      ReplicatorConfigManager replicatorConfigManager) {
    return new Syncer(replicationItemManager, replicatorConfigManager);
  }

  @Bean
  public NodeAdapters nodeAdapters(List<NodeAdapterFactory> nodeAdapterFactories) {
    NodeAdapters nodeAdapters = new NodeAdapters();
    nodeAdapters.setNodeAdapterFactories(nodeAdapterFactories);
    return nodeAdapters;
  }

  @Bean(destroyMethod = "cleanUp")
  public Replicator replicator(NodeAdapters nodeAdapters, SiteManager siteManager, Syncer syncer) {
    ReplicatorImpl replicator = new ReplicatorImpl(nodeAdapters, siteManager, syncer);
    replicator.init();
    return replicator;
  }

  @Bean(destroyMethod = "destroy")
  public ReplicatorRunner replicatorRunner(
      Replicator replicator,
      ReplicatorConfigManager replicatorConfigManager,
      ReplicationProperties properties) {
    ReplicatorRunner replicatorRunner =
        new ReplicatorRunner(
            replicator, replicatorConfigManager, properties.sites(), properties.getPeriod());
    replicatorRunner.init();
    return replicatorRunner;
  }
}
