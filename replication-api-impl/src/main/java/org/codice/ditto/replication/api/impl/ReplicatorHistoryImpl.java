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
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import java.util.Collections;
import java.util.List;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.codice.ditto.replication.api.mcard.ReplicationHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorHistoryImpl implements ReplicatorHistory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorHistoryImpl.class);

  private final CatalogFramework framework;

  private final FilterBuilder filterBuilder;

  private final MetacardHelper helper;

  private final MetacardType metacardType;

  public ReplicatorHistoryImpl(
      CatalogFramework framework,
      FilterBuilder filterBuilder,
      MetacardHelper helper,
      MetacardType metacardType) {
    this.framework = framework;
    this.filterBuilder = filterBuilder;
    this.helper = helper;
    this.metacardType = metacardType;
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
  public List<ReplicationStatus> getReplicationEvents(String replicatorid) {
    return helper.getTypeForFilter(
        filterBuilder.allOf(
            filterBuilder
                .attribute(Core.METACARD_TAGS)
                .is()
                .equalTo()
                .text(ReplicationHistory.METACARD_TAG),
            filterBuilder.attribute(ReplicationConfig.NAME).is().equalTo().text(replicatorid)),
        this::getStatusFromMetacard);
  }

  @Override
  public void addReplicationEvent(ReplicationStatus replicationStatus) {
    try {
      framework.create(new CreateRequestImpl(getMetacardFromStatus(replicationStatus)));
    } catch (IngestException | SourceUnavailableException e) {
      LOGGER.warn(
          "Error creating replication history item for {}",
          replicationStatus.getReplicatorName(),
          e);
    }
  }

  private ReplicationStatus getStatusFromMetacard(Metacard metacard) {
    ReplicationStatus status =
        new ReplicationStatus(
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
    mcard.setId(replicationStatus.getId());
    mcard.setTags(Collections.singleton(ReplicationHistory.METACARD_TAG));
    return mcard;
  }
}
