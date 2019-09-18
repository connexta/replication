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

import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.AdapterInterruptedException;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.ReplicationException;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.QueryRequest;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.TaskInfo;
import com.connexta.replication.api.impl.query.DdfMetadataInfoImpl;
import com.connexta.replication.api.impl.query.ResourceInfoImpl;
import com.connexta.replication.api.impl.query.TaskInfoImpl;
import com.connexta.replication.api.queue.QueueException;
import com.connexta.replication.api.queue.SiteQueue;
import com.connexta.replication.data.QueryRequestImpl;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>QueryService</code> class implements the logic for querying a specific DDF-based site
 * for changes to be replicated or harvested.
 */
public class QueryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

  /** A Facade for the various objects and services used by the query service. */
  private final QueryServiceTools queryServiceTools;

  /** An Executor used to schedule queries to the site. */
  private final ScheduledExecutorService executor;

  /** The current configuration for the site being queried by this query service. */
  private Site site;

  /**
   * The queue for this site which which will be loaded with {@link TaskInfo}s created from queries.
   */
  private final SiteQueue siteQueue;

  /** The adapter to communicate with the given site. */
  private NodeAdapter adapter;

  /** A scheduled future created when the query service is started. */
  private ScheduledFuture future;

  /**
   * Instantiates a new query service.
   *
   * @param site the configuration for the site to be queried by this query service
   * @throws AdapterException if an adapter can't be created for the site
   * @throws IllegalArgumentException if a {@link com.connexta.replication.api.NodeAdapterFactory}
   *     can't be found for the site
   */
  public QueryService(Site site, QueryServiceTools queryServiceTools) {
    this(site, queryServiceTools, Executors.newSingleThreadScheduledExecutor());
  }

  @VisibleForTesting
  QueryService(Site site, QueryServiceTools queryServiceTools, ScheduledExecutorService executor) {
    this.queryServiceTools = queryServiceTools;
    this.siteQueue = queryServiceTools.getQueueFor(site);
    this.executor = executor;
    this.site = site;
    this.adapter = queryServiceTools.getAdapterFor(site);
  }

  /**
   * Updates the config information this query service has about its site.
   *
   * @param site the new site configuration
   * @throws AdapterException if an adapter can't be created for the site
   * @throws IllegalArgumentException if a {@link com.connexta.replication.api.NodeAdapterFactory}
   *     can't be found for the site
   */
  public void update(Site site) {
    LOGGER.trace("Updating QueryService site from {} to {}", this.site, site);
    synchronized (this) {
      this.site = site;
      this.adapter = queryServiceTools.getAdapterFor(site);
    }
  }

  /** Starts this query service. */
  public void start() {
    if (!isRunning()) {
      LOGGER.trace("Starting QueryService for site {}", site.getName());
      long period =
          site.getPollingPeriod()
              .orElse(Duration.of(queryServiceTools.getGlobalPeriod(), ChronoUnit.SECONDS))
              .toMillis();
      this.future = executor.scheduleAtFixedRate(this::query, 0, period, TimeUnit.MILLISECONDS);
      LOGGER.debug(
          "QueryService started for site {} with a polling period of {}ms.",
          site.getName(),
          period);
    }
  }

  /** Stops this query service. */
  public void stop() {
    LOGGER.trace("Shutting down QueryService for site {}", site.getName());
    if (isRunning()) {
      future.cancel(true);
    }
    executor.shutdownNow();
    try {
      adapter.close();
    } catch (IOException e) {
      LOGGER.debug("An error occured while closing the adapter for site {}", site.getName());
    }
  }

  /**
   * Retrieves all the Filters for this query service's site and, for each of them, queries the
   * site, then creates a {@link TaskInfo} for each received piece of metadata and places it in the
   * siteQueue.
   */
  @VisibleForTesting
  void query() {
    try {
      doQuery();
    } catch (ReplicationException e) {
      LOGGER.warn(
          "An exception occurred in the persistence layer while trying to query site {} for metadata. Waiting until the next polling interval to try again.",
          site);
      LOGGER.debug(
          "An exception occurred in the persistence layer while trying to query site {} for metadata. Waiting until the next polling interval to try again.",
          site,
          e);
    }
  }

  private void doQuery() {
    if (!adapter.isAvailable()) {
      LOGGER.debug("System at " + site.getUrl() + " is currently unavailable");
      return;
    }

    final List<Filter> filters =
        queryServiceTools
            .activeFiltersFor(site)
            .sorted(Comparator.comparingInt(Filter::getPriority).reversed())
            .collect(Collectors.toList());
    LOGGER.debug("Filters retrieved for site {}:{}", site.getName(), filters);
    for (Filter filter : filters) {
      final FilterIndex filterIndex = queryServiceTools.getOrCreateFilterIndex(filter);
      LOGGER.debug(
          "Obtained FilterIndex {}, of filter {}, of site {}", filterIndex, filter, site.getName());

      // holds the latest modified time received so far
      AtomicReference<Instant> latestModifiedTime =
          new AtomicReference<>(
              filterIndex.getModifiedSince().orElse(new Date(Long.MIN_VALUE).toInstant()));

      try {
        getNewMetadataForFilter(filter, filterIndex)
            .sorted(Comparator.comparing(Metadata::getMetadataModified))
            .map(metadata -> metadataToTask(metadata, filter.getPriority()))
            .forEach(task -> queueTaskAndUpdateLastModified(task, latestModifiedTime));
      } catch (AdapterInterruptedException e) {
        LOGGER.debug(
            "Thread interrupted. Stopping queries for site {}. This is normal when shutting down.",
            site.getName(),
            e);
        Thread.currentThread().interrupt();
        return;
      } catch (AdapterException e) {
        LOGGER.debug(
            "Couldn't query site {}. Stopping queries for this site until next polling attempt.",
            site.getName(),
            e);
        return;
      } catch (QueueException qe) {
        LOGGER.debug(
            "Failed to put a task in queue for site {}. Stopping queuing of tasks for this site. Will resume at next polling attempt.",
            site.getName(),
            qe);
        return;
      } finally {
        saveLastModifiedInFilterIndex(filterIndex, latestModifiedTime);
      }
    }
  }

  private Stream<Metadata> getNewMetadataForFilter(Filter filter, FilterIndex filterIndex)
      throws AdapterException {
    // get last modified date
    final Date indexDate = filterIndex.getModifiedSince().map(Date::from).orElse(null);
    if (indexDate == null) {
      LOGGER.trace("No index for filter {} found.", filter.getName());
    }

    // not implementing failure retries here
    final QueryRequest queryRequest =
        new QueryRequestImpl(
            filter.getFilter(),
            Collections.emptyList(), // ignoring excluded nodes (origins tag)
            Collections.emptyList(), // ignoring failed items
            indexDate);

    Iterable<Metadata> changeSet;
    LOGGER.trace("Attempting to query site: {}", site.getName());
    changeSet = adapter.query(queryRequest).getMetadata();

    return StreamSupport.stream(changeSet.spliterator(), false);
  }

  private TaskInfo metadataToTask(Metadata metadata, byte filterPriority) {
    LOGGER.trace("Converting metadata with ID: {} to task", metadata.getId());
    ResourceInfoImpl resourceInfo = null;
    if (metadata.getResourceSize() > 0) { // determine if there is a resource
      resourceInfo = new ResourceInfoImpl(metadata);
    }

    // Note: The type here should match what needs to be sent to CDR once the adapter is a
    // pass-through for the task info and once CDR accepts a generic type string
    final MetadataInfo metadataInfo = new DdfMetadataInfoImpl("metacard", metadata);

    return new TaskInfoImpl(
        metadata.getId(),
        filterPriority,
        determineOperationType(),
        metadata.getMetadataModified().toInstant(),
        resourceInfo,
        Set.of(metadataInfo));
  }

  private OperationType determineOperationType() {
    // TODO: determine the operation type based on the site type and kind
    // TODO: potentially ignore deletes and updates depending on the site type and kind
    return OperationType.HARVEST;
  }

  private void queueTaskAndUpdateLastModified(
      TaskInfo task, AtomicReference<Instant> latestModifiedTime) {
    LOGGER.trace("Putting task {} in Queue for site {}", task, site);
    try {
      siteQueue.put(task);
    } catch (InterruptedException e) {
      throw new AdapterInterruptedException(e);
    }

    if (task.getLastModified().isAfter(latestModifiedTime.get())) {
      latestModifiedTime.set(task.getLastModified());
    }
  }

  private void saveLastModifiedInFilterIndex(
      FilterIndex filterIndex, AtomicReference<Instant> latestModifiedTime) {
    filterIndex.setModifiedSince(latestModifiedTime.get());
    queryServiceTools.saveFilterIndex(filterIndex);
    LOGGER.debug("Saved filter index:{}", filterIndex);
  }

  boolean isRunning() {
    return !(future == null || future.isDone());
  }

  @Override
  public String toString() {
    return String.format(
        "QueryService{site=%s[%s], adapter=%s}",
        site.getName(), site.getId(), adapter.getSystemName());
  }
}
