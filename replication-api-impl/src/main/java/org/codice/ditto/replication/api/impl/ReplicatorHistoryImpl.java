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
package org.codice.ditto.replication.api.impl;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.impl.CreateRequestImpl;
import ddf.catalog.operation.impl.DeleteRequestImpl;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.source.CatalogProvider;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.service.SecurityServiceException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.codice.ditto.replication.api.mcard.ReplicationHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A metacard/catalog based persistence implementation of the ReplicatorHistory interface */
public class ReplicatorHistoryImpl implements ReplicatorHistory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorHistoryImpl.class);

  private final Security security;

  private final CatalogFramework framework;

  private final CatalogProvider provider;

  private final FilterBuilder filterBuilder;

  private final MetacardHelper helper;

  private final MetacardType metacardType;

  public ReplicatorHistoryImpl(
      CatalogFramework framework,
      CatalogProvider provider,
      FilterBuilder filterBuilder,
      MetacardHelper helper,
      MetacardType metacardType) {
    this(framework, provider, filterBuilder, helper, metacardType, Security.getInstance());
  }

  /*Visible for testing only*/
  ReplicatorHistoryImpl(
      CatalogFramework framework,
      CatalogProvider provider,
      FilterBuilder filterBuilder,
      MetacardHelper helper,
      MetacardType metacardType,
      Security security) {
    this.framework = framework;
    this.provider = provider;
    this.filterBuilder = filterBuilder;
    this.helper = helper;
    this.metacardType = metacardType;
    this.security = security;
  }

  /** Initialize the history records. This is used to update outdated formats */
  public void init() {
    // Using failsafe retry here because when this is deployed
    // as a kar file there were several times when the catalog
    // was not available so re-running was required
    RetryPolicy retryPolicy =
        new RetryPolicy()
            .withDelay(5, TimeUnit.SECONDS)
            .withMaxDuration(2, TimeUnit.MINUTES)
            .retryOn(Exception.class);

    Failsafe.with(retryPolicy).run(this::runResultsAsSystemUser);
  }

  @Override
  public List<ReplicationStatus> getReplicationEvents() {
    return helper.getTypeForFilter(
        filterBuilder
            .attribute(Core.METACARD_TAGS)
            .is()
            .equalTo()
            .text(ReplicationHistory.METACARD_TAG),
        this::getStatusFromMetacard);
  }

  @Override
  public List<ReplicationStatus> getReplicationEvents(String replicationConfigId) {
    return helper.getTypeForFilter(
        filterBuilder.allOf(
            filterBuilder
                .attribute(Core.METACARD_TAGS)
                .is()
                .equalTo()
                .text(ReplicationHistory.METACARD_TAG),
            filterBuilder
                .attribute(ReplicationConfig.NAME)
                .is()
                .equalTo()
                .text(replicationConfigId)),
        this::getStatusFromMetacard);
  }

  @Override
  public void addReplicationEvent(ReplicationStatus replicationStatus) {
    try {
      ReplicationStatus previous =
          getReplicationEvents(replicationStatus.getReplicatorName())
              .stream()
              .findFirst()
              .orElse(null);
      if (previous != null) {
        addStats(previous, replicationStatus);
        provider.update(new UpdateRequestImpl(previous.getId(), getMetacardFromStatus(previous)));
        return;
      }
      replicationStatus.setLastRun(replicationStatus.getStartTime());
      if (replicationStatus.getStatus().equals(Status.SUCCESS)) {
        replicationStatus.setLastSuccess(replicationStatus.getStartTime());
      }
      framework.create(new CreateRequestImpl(getMetacardFromStatus(replicationStatus)));

    } catch (IngestException | SourceUnavailableException e) {
      throw new ReplicationPersistenceException(
          "Error creating replication history item for " + replicationStatus.getReplicatorName(),
          e);
    }
  }

  @Override
  public void removeReplicationEvent(ReplicationStatus replicationStatus) {
    try {
      provider.delete(new DeleteRequestImpl(replicationStatus.getId()));
    } catch (IngestException e) {
      throw new ReplicationPersistenceException(
          "Error deleting replication history item " + replicationStatus.getReplicatorName(), e);
    }
  }

  @Override
  public void removeReplicationEvents(Set<String> ids) {
    try {
      provider.delete(new DeleteRequestImpl(ids.toArray(new String[0])));
    } catch (IngestException e) {
      throw new ReplicationPersistenceException(
          "Error deleting replication history items " + ids, e);
    }
  }

  private ReplicationStatus getStatusFromMetacard(Metacard metacard) {
    ReplicationStatus status =
        new ReplicationStatusImpl(
            metacard.getId(),
            helper.getAttributeValueOrDefault(metacard, ReplicationConfig.NAME, null));
    status.setDuration(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.DURATION, -1L));
    status.setPushCount(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.PUSH_COUNT, 0L));
    status.setPushFailCount(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.PUSH_FAIL_COUNT, 0L));
    status.setPullCount(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.PULL_COUNT, 0L));
    status.setPullFailCount(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.PULL_FAIL_COUNT, 0L));
    status.setPushBytes(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.PUSH_BYTES, 0L));
    status.setPullBytes(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.PULL_BYTES, 0L));
    status.setStatus(
        Status.valueOf(
            helper.getAttributeValueOrDefault(
                metacard, ReplicationHistory.STATUS, Status.PENDING.name())));
    status.setStartTime(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.START_TIME, null));
    status.setLastSuccess(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.LAST_SUCCESS, null));
    status.setLastRun(
        helper.getAttributeValueOrDefault(metacard, ReplicationHistory.LAST_RUN, null));

    return status;
  }

  private Metacard getMetacardFromStatus(ReplicationStatus replicationStatus) {
    MetacardImpl mcard = new MetacardImpl(metacardType);
    helper.setIfPresent(mcard, ReplicationConfig.NAME, replicationStatus.getReplicatorName());
    helper.setIfPresent(mcard, ReplicationHistory.DURATION, replicationStatus.getDuration());
    helper.setIfPresent(mcard, ReplicationHistory.START_TIME, replicationStatus.getStartTime());
    helper.setIfPresent(mcard, ReplicationHistory.PULL_COUNT, replicationStatus.getPullCount());
    helper.setIfPresent(
        mcard, ReplicationHistory.PULL_FAIL_COUNT, replicationStatus.getPullFailCount());
    helper.setIfPresent(mcard, ReplicationHistory.PUSH_COUNT, replicationStatus.getPushCount());
    helper.setIfPresent(
        mcard, ReplicationHistory.PUSH_FAIL_COUNT, replicationStatus.getPushFailCount());
    helper.setIfPresent(mcard, ReplicationHistory.PULL_BYTES, replicationStatus.getPullBytes());
    helper.setIfPresent(mcard, ReplicationHistory.PUSH_BYTES, replicationStatus.getPushBytes());
    helper.setIfPresent(mcard, ReplicationHistory.STATUS, replicationStatus.getStatus().name());
    helper.setIfPresent(mcard, ReplicationHistory.LAST_SUCCESS, replicationStatus.getLastSuccess());
    helper.setIfPresent(mcard, ReplicationHistory.LAST_RUN, replicationStatus.getLastRun());
    mcard.setId(replicationStatus.getId());
    mcard.setTags(Collections.singleton(ReplicationHistory.METACARD_TAG));
    return mcard;
  }

  private void addStats(ReplicationStatus base, ReplicationStatus status) {
    base.setPushCount(base.getPushCount() + status.getPushCount());
    base.setPushBytes(base.getPushBytes() + status.getPushBytes());
    base.setPushFailCount(base.getPushFailCount() + status.getPushFailCount());
    base.setPullCount(base.getPullCount() + status.getPullCount());
    base.setPullBytes(base.getPullBytes() + status.getPullBytes());
    base.setPullFailCount(base.getPullFailCount() + status.getPullFailCount());
    if (base.getLastRun() == null || status.getStartTime().after(base.getLastRun())) {
      base.setLastRun(status.getStartTime());
      base.setStatus(status.getStatus());
    }

    if (base.getStartTime().after(status.getStartTime())) {
      base.setStartTime(status.getStartTime());
    }

    base.setDuration(base.getDuration() + status.getDuration());

    if (status.getStatus().equals(Status.SUCCESS)
        && (base.getLastSuccess() == null || status.getStartTime().after(base.getLastSuccess()))) {
      base.setLastSuccess(status.getStartTime());
    }
  }

  /** */
  void runResultsAsSystemUser() {
    security.runAsAdmin(
        () -> {
          try {
            security.runWithSubjectOrElevate(
                () -> {
                  getReplicationEvents()
                      .stream()
                      .map(ReplicationStatus::getReplicatorName)
                      .distinct()
                      .map(this::getReplicationEvents)
                      .forEach(this::condenseResults);
                  return null;
                });
          } catch (SecurityServiceException | InvocationTargetException e) {
            LOGGER.debug("Error condensing replication history.", e);
          }
          return null;
        });
  }

  /**
   * Condenses old mvp status events into a single event
   *
   * @param events events to condense for a single configuration events should be in order of newest
   *     to oldest
   */
  void condenseResults(List<ReplicationStatus> events) {
    if (events.size() == 1 && events.get(0).getLastRun() != null) {
      return;
    }
    ReplicationStatus condensedStatus = null;
    Set<String> idsToRemove = new HashSet<>();
    for (ReplicationStatus status : events) {
      idsToRemove.add(status.getId());
      if (condensedStatus == null) {
        condensedStatus = new ReplicationStatusImpl(status.getReplicatorName());
        condensedStatus.setStartTime(status.getStartTime());
        condensedStatus.setStatus(status.getStatus());
        condensedStatus.setDuration(0L);
      }
      addStats(condensedStatus, status);
    }

    if (condensedStatus == null) {
      return;
    }

    try {
      framework.create(new CreateRequestImpl(getMetacardFromStatus(condensedStatus)));
      removeReplicationEvents(idsToRemove);
    } catch (IngestException | SourceUnavailableException e) {
      LOGGER.debug(
          "Error creating replication history item for {}", condensedStatus.getReplicatorName(), e);
    }
  }
}
