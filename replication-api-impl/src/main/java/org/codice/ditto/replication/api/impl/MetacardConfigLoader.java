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
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.impl.CreateRequestImpl;
import ddf.catalog.operation.impl.DeleteRequestImpl;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetacardConfigLoader implements ReplicatorConfigLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetacardConfigLoader.class);

  private static final int DEFAULT_FAILURE_RETRY_COUNT = 5;

  private final CatalogFramework framework;

  private final FilterBuilder filterBuilder;

  private final MetacardHelper helper;

  private final MetacardType configMetacardType;

  public MetacardConfigLoader(
      CatalogFramework framework,
      FilterBuilder filterBuilder,
      MetacardHelper helper,
      MetacardType configMetacardType) {
    this.framework = framework;
    this.filterBuilder = filterBuilder;
    this.helper = helper;
    this.configMetacardType = configMetacardType;
  }

  @Override
  public Optional<ReplicatorConfig> getConfig(final String id) {
    List<ReplicatorConfig> configs =
        helper.getTypeForFilter(
            filterBuilder.allOf(
                filterBuilder.attribute(ReplicationConfig.NAME).is().equalTo().text(id),
                filterBuilder
                    .attribute(Metacard.TAGS)
                    .is()
                    .like()
                    .text(ReplicationConfig.METACARD_TAG)),
            this::getConfigFromMetacard);

    final int count = configs.size();
    if (count == 1) {
      return Optional.of(configs.get(0));
    } else if (count > 1) {
      throw new IllegalStateException(
          "Found more than one replication config for id {}. This should never happen");
    } else {
      return Optional.empty();
    }
  }

  @Override
  public List<ReplicatorConfig> getAllConfigs() {
    return helper.getTypeForFilter(
        filterBuilder.attribute(Metacard.TAGS).is().like().text(ReplicationConfig.METACARD_TAG),
        this::getConfigFromMetacard);
  }

  @Override
  public void saveConfig(ReplicatorConfig replicationConfig) {
    Metacard mcard = null;
    if (replicationConfig.getId() != null) {
      mcard = helper.getMetacardById(replicationConfig.getId());
    }
    boolean create = mcard == null;
    mcard = getMetacardFromConfig(replicationConfig, mcard);

    if (mcard == null) {
      LOGGER.warn(
          "Error creating/updating metacard for replication configuration {}",
          replicationConfig.getName());
      throw new ReplicationException(
          "Error creating/updating metacard for replication configuration "
              + replicationConfig.getName());
    }

    if (create) { // create
      try {
        framework.create(new CreateRequestImpl(mcard));
      } catch (IngestException | SourceUnavailableException e) {
        LOGGER.warn(
            "Error creating replication configuration for {}", replicationConfig.getName(), e);
        throw new ReplicationException(
            "Error creating replication configuration for " + replicationConfig.getName());
      }
    } else { // update
      try {
        framework.update(new UpdateRequestImpl(mcard.getId(), mcard));
      } catch (IngestException | SourceUnavailableException e) {
        LOGGER.warn(
            "Error updating replication configuration for {}", replicationConfig.getName(), e);
        throw new ReplicationException(
            "Error updating replication configuration for " + replicationConfig.getName());
      }
    }
  }

  @Override
  public void removeConfig(ReplicatorConfig replicationConfig) {
    try {
      framework.delete(new DeleteRequestImpl(replicationConfig.getId()));
    } catch (IngestException | SourceUnavailableException e) {
      LOGGER.warn("Could not delete Replicator configuration {}", replicationConfig.getName(), e);
      throw new ReplicationException(
          "Could not delete Replicator configuration for " + replicationConfig.getName());
    }
  }

  private Metacard getMetacardFromConfig(ReplicatorConfig config, Metacard existingMetacard) {
    if (config.getUrl() == null || config.getCql() == null || config.getName() == null) {
      LOGGER.warn(
          "Invalid replication configuration found. {} is missing one or more required fields. {}, {}, {}",
          config.getName(),
          ReplicationConfig.NAME,
          ReplicationConfig.URL,
          ReplicationConfig.CQL);
      return null;
    }

    Metacard mcard = existingMetacard;
    if (mcard == null) {
      mcard = new MetacardImpl(configMetacardType);
      ((MetacardImpl) mcard).setTags(Collections.singleton(ReplicationConfig.METACARD_TAG));
    }

    helper.setIfPresent(mcard, ReplicationConfig.NAME, config.getName());
    helper.setIfPresent(mcard, ReplicationConfig.DESCRIPTION, config.getDescription());
    helper.setIfPresent(mcard, ReplicationConfig.URL, config.getUrl().toString());
    helper.setIfPresent(mcard, ReplicationConfig.CQL, config.getCql());
    helper.setIfPresentOrDefault(
        mcard,
        ReplicationConfig.TYPE,
        config.getReplicationType(),
        ReplicationType.METACARD.toString());
    helper.setIfPresentOrDefault(
        mcard, ReplicationConfig.DIRECTION, config.getDirection(), Direction.PULL.toString());
    helper.setIfPresentOrDefault(
        mcard,
        ReplicationConfig.FAILURE_RETRY_COUNT,
        config.getFailureRetryCount(),
        DEFAULT_FAILURE_RETRY_COUNT);

    return mcard;
  }

  private ReplicatorConfig getConfigFromMetacard(Metacard mcard) {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId(mcard.getId());
    config.setName(helper.getAttributeValueOrDefault(mcard, ReplicationConfig.NAME, null));
    config.setDescription(
        helper.getAttributeValueOrDefault(mcard, ReplicationConfig.DESCRIPTION, null));
    config.setDirection(
        Direction.valueOf(
            helper.getAttributeValueOrDefault(
                mcard, ReplicationConfig.DIRECTION, Direction.PULL.name())));
    config.setReplicationType(
        ReplicationType.valueOf(
            helper.getAttributeValueOrDefault(
                mcard, ReplicationConfig.TYPE, ReplicationType.METACARD.name())));
    config.setFailureRetryCount(
        helper.getAttributeValueOrDefault(
            mcard, ReplicationConfig.FAILURE_RETRY_COUNT, DEFAULT_FAILURE_RETRY_COUNT));

    try {
      config.setUrl(new URL(helper.getAttributeValueOrDefault(mcard, ReplicationConfig.URL, null)));
    } catch (MalformedURLException e) {
      LOGGER.warn(
          "Could not create URL from {}. Unable to create ReplicatorConfig from metacard.",
          helper.getAttributeValueOrDefault(mcard, ReplicationConfig.URL, null),
          e);
    }
    config.setCql(helper.getAttributeValueOrDefault(mcard, ReplicationConfig.CQL, null));

    if (config.getUrl() == null || config.getCql() == null || config.getName() == null) {
      LOGGER.warn(
          "Invalid replication configuration found. {} is missing one or more required fields. {}, {}, {}",
          mcard.getId(),
          ReplicationConfig.NAME,
          ReplicationConfig.URL,
          ReplicationConfig.CQL);
      return null;
    }
    return config;
  }
}
