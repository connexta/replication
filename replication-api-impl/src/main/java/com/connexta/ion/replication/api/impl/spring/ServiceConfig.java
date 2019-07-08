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
import com.connexta.ion.replication.api.impl.persistence.ReplicatorHistoryManagerImpl;
import com.connexta.ion.replication.api.impl.persistence.SiteManagerImpl;
import com.connexta.ion.replication.api.persistence.ReplicationItemManager;
import com.connexta.ion.replication.api.persistence.ReplicatorConfigManager;
import com.connexta.ion.replication.api.persistence.ReplicatorHistoryManager;
import com.connexta.ion.replication.api.persistence.SiteManager;
import com.connexta.ion.replication.spring.ReplicationProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** A class for instantiating beans in this module */
@Configuration("replication-api-impl")
public class ServiceConfig {

  @Bean
  public ReplicationItemManager replicationItemManager(ItemRepository itemRepository) {
    return new ReplicationItemManagerImpl(itemRepository);
  }

  @Bean
  public ReplicatorConfigManager replicatorConfigManager(ConfigRepository configRepository) {
    return new ReplicatorConfigManagerImpl(configRepository);
  }

  @Bean
  public ReplicatorHistoryManager replicatorHistoryManager(HistoryRepository historyRepository) {
    return new ReplicatorHistoryManagerImpl(historyRepository);
  }

  @Bean
  public SiteManager siteManager(SiteRepository siteRepository) {
    return new SiteManagerImpl(siteRepository);
  }

  @Bean
  public Syncer syncer(
      ReplicationItemManager replicationItemManager,
      ReplicatorHistoryManager replicatorHistoryManager) {
    return new Syncer(replicationItemManager, replicatorHistoryManager);
  }

  @Bean
  public NodeAdapters nodeAdapters(List<NodeAdapterFactory> nodeAdapterFactories) {
    NodeAdapters nodeAdapters = new NodeAdapters();
    nodeAdapters.setNodeAdapterFactories(nodeAdapterFactories);
    return nodeAdapters;
  }

  @Bean(destroyMethod = "cleanUp")
  public Replicator replicator(
      NodeAdapters nodeAdapters,
      ReplicatorHistoryManager history,
      SiteManager siteManager,
      Syncer syncer) {
    ReplicatorImpl replicator = new ReplicatorImpl(nodeAdapters, history, siteManager, syncer);
    replicator.init();
    return replicator;
  }

  @Bean(destroyMethod = "destroy")
  public ReplicatorRunner replicatorRunner(
      Replicator replicator,
      ReplicatorConfigManager replicatorConfigManager,
      ReplicationProperties properties) {
    ReplicatorRunner replicatorRunner =
        new ReplicatorRunner(replicator, replicatorConfigManager, properties.getPeriod());
    replicatorRunner.init();
    return replicatorRunner;
  }
}
