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
package com.connexta.replication.query.spring;

import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.TaskInfo;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.QueueBroker;
import com.connexta.replication.api.queue.SiteQueue;
import com.connexta.replication.query.QueryManager;
import com.connexta.replication.query.QueryService;
import com.connexta.replication.query.QueryServiceTools;
import com.connexta.replication.spring.ReplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration("query")
@Profile("Ion")
public class ServiceConfig {

  /**
   * Creates a {@link QueryManager} which will create, update, and delete {@link QueryService}s as
   * needed.
   *
   * @param nodeAdapters A service holding a list of factories used to create NodeAdapters
   * @param siteManager A service used to perform CRUD operations on {@link Site}s
   * @param filterManager A service used to perform CRUD operations on {@link Filter}s
   * @param filterIndexManager A service used to perform CRUD operations on {@link FilterIndex}s
   * @param queueBroker A service used to retrieve {@link SiteQueue}s where {@link TaskInfo}s will
   *     be placed
   * @param replicationProperties Application properties used to retrieve the default site polling
   *     period
   * @param queryProperties Application properties used to retrieve the query service refresh period
   * @return The QueryManager service
   */
  @Bean(destroyMethod = "destroy")
  public QueryManager queryManager(
      NodeAdapters nodeAdapters,
      SiteManager siteManager,
      FilterManager filterManager,
      FilterIndexManager filterIndexManager,
      QueueBroker queueBroker,
      ReplicationProperties replicationProperties,
      QueryProperties queryProperties) {
    QueryServiceTools queryServiceTools =
        new QueryServiceTools(
            nodeAdapters,
            siteManager,
            filterManager,
            filterIndexManager,
            queueBroker,
            replicationProperties.getPeriod());
    QueryManager queryManager =
        new QueryManager(
            replicationProperties.getSites().stream(),
            queryServiceTools,
            queryProperties.getServiceRefreshPeriod(),
            replicationProperties.getLocalSite());

    queryManager.init();
    return queryManager;
  }
}
