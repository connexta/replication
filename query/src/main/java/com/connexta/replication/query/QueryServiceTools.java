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

import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.QueueBroker;
import com.connexta.replication.api.queue.SiteQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * A facade for the services/properties that the {@link QueryService} and {@link QueryManager} need
 * to do their jobs. This makes it easy for the QueryManager to share this functionality with each
 * of the QueryServices it creates.
 */
public class QueryServiceTools {

  private final NodeAdapters nodeAdapterFactories;

  private final SiteManager siteManager;

  private final FilterManager filterManager;

  private final FilterIndexManager filterIndexManager;

  private final QueueBroker queueBroker;

  private final long globalPeriod;

  /** Default polling period for configuration changes in seconds. */
  private static final long DEFAULT_REPLICATION_PERIOD = TimeUnit.MINUTES.toSeconds(5);

  /**
   * Creates a new instance of QueryServiceTools.
   *
   * @param nodeAdapterFactories An service which provides a way to retrieve {@link
   *     com.connexta.replication.api.NodeAdapterFactory}s
   * @param siteManager A service used to access site configurations
   * @param filterManager A service used to access filter configurations
   * @param filterIndexManager A service used to access indexes associated with filter
   *     configurations
   * @param queueBroker A service used to retrieve Queues
   * @param globalPeriod The default length between site query intervals in seconds.
   */
  public QueryServiceTools(
      NodeAdapters nodeAdapterFactories,
      SiteManager siteManager,
      FilterManager filterManager,
      FilterIndexManager filterIndexManager,
      QueueBroker queueBroker,
      long globalPeriod) {
    this.nodeAdapterFactories = nodeAdapterFactories;
    this.siteManager = siteManager;
    this.filterManager = filterManager;
    this.filterIndexManager = filterIndexManager;
    this.queueBroker = queueBroker;
    this.globalPeriod = globalPeriod > 0L ? globalPeriod : DEFAULT_REPLICATION_PERIOD;
  }

  /**
   * Gets the default amount of time between polling intervals in seconds. Use this as the polling
   * interval for a site if there is no polling interval specifically for that site.
   *
   * @return the default amount of time between site query intervals in seconds
   */
  public long getGlobalPeriod() {
    return globalPeriod;
  }

  /**
   * Returns all the currently configured sites in a {@link Stream}.
   *
   * @return All the currently configured sites in a {@link Stream}
   */
  public Stream<Site> sites() {
    return siteManager.objects();
  }

  /**
   * Creates a {@link NodeAdapter} for the given site.
   *
   * @param site the site to retrieve a {@link NodeAdapter} for
   * @return the {@link NodeAdapter} for the given site
   * @throws IllegalArgumentException if a {@link NodeAdapterFactory} for the sites type can't be
   *     found
   * @throws com.connexta.replication.api.AdapterException if an adapter can't be created from the
   *     sites URL
   */
  public NodeAdapter getAdapterFor(Site site) {
    return nodeAdapterFactories.factoryFor(site.getType()).create(site.getUrl());
  }

  /**
   * Gets all the filters associated with the given site that aren't suspended.
   *
   * @param site the site to retrieve all the associated filters for
   * @return A {@link Stream} containing all the non-suspended filters for the associated site
   * @throws com.connexta.replication.api.data.ReplicationPersistenceException if there is an error
   *     fetching the filters
   */
  public Stream<Filter> activeFiltersFor(Site site) {
    return filterManager.filtersForSite(site.getId()).filter(f -> !f.isSuspended());
  }

  /**
   * Gets the {@link FilterIndex} associated with the given filter. If there is no associated filter
   * index, then a new one is created.
   *
   * @param filter the filter to get or create a filter index for
   * @return the filter index for the given filter
   * @throws com.connexta.replication.api.data.ReplicationPersistenceException if there is an error
   *     fetching an existing filter index
   */
  public FilterIndex getOrCreateFilterIndex(Filter filter) {
    return filterIndexManager.getOrCreate(filter);
  }

  /**
   * Persists the given filter index.
   *
   * @param filterIndex the filter index to persist
   * @throws com.connexta.replication.api.data.ReplicationPersistenceException if there is an error
   *     saving the filter index
   * @throws IllegalArgumentException if the filterIndex is not one that can be saved
   */
  public void saveFilterIndex(FilterIndex filterIndex) {
    filterIndexManager.save(filterIndex);
  }

  /**
   * Gets the {@link SiteQueue} for the given site, which will receive all tasks created for
   * replicating to/from the site.
   *
   * @param site the site to retrieve the associated queue of
   * @return the site's queue
   * @throws com.connexta.replication.api.queue.QueueException if an error occurs while getting the
   *     queue
   */
  public SiteQueue getQueueFor(Site site) {
    return queueBroker.getQueue(site.getId());
  }
}
