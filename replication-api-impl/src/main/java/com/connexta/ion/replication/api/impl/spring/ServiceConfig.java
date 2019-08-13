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
package com.connexta.ion.replication.api.impl.spring;

import com.connexta.ion.replication.api.NodeAdapterFactory;
import com.connexta.ion.replication.api.Replicator;
import com.connexta.ion.replication.api.impl.NodeAdapters;
import com.connexta.ion.replication.api.impl.ReplicatorImpl;
import com.connexta.ion.replication.api.impl.ReplicatorRunner;
import com.connexta.ion.replication.api.impl.Syncer;
import com.connexta.ion.replication.api.impl.persistence.ReplicationItemManagerImpl;
import com.connexta.ion.replication.api.impl.persistence.ReplicatorConfigManagerImpl;
import com.connexta.ion.replication.api.impl.persistence.SiteManagerImpl;
import com.connexta.ion.replication.api.persistence.ReplicationItemManager;
import com.connexta.ion.replication.api.persistence.ReplicatorConfigManager;
import com.connexta.ion.replication.api.persistence.SiteManager;
import com.connexta.ion.replication.spring.ReplicationProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;

/** A class for instantiating beans in this module */
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
