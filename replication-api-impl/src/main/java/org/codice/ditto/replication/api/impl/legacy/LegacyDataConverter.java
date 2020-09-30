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
package org.codice.ditto.replication.api.impl.legacy;

import com.google.common.annotations.VisibleForTesting;
import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.operation.impl.DeleteRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.util.impl.CatalogQueryException;
import ddf.security.service.SecurityServiceException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.NotFoundException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.security.Security;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.Metacards;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.persistence.ReplicationPersistentStore;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will attempt to convert data (such as ReplicatorConfigs and ReplicationSites) stored
 * in an older format into the current format.
 */
public class LegacyDataConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDataConverter.class);

  private static final int DEFAULT_FAILURE_RETRY_COUNT = 5;

  private final CatalogFramework framework;

  private final FilterBuilder filterBuilder;

  private final Metacards metacards;

  private final SiteManager siteManager;

  private final ReplicatorConfigManager newConfigManager;

  private final ReplicationPersistentStore persistentStore;

  private final Security security;

  public LegacyDataConverter(
      CatalogFramework framework,
      FilterBuilder filterBuilder,
      Metacards metacards,
      SiteManager siteManager,
      ReplicatorConfigManager newConfigManager,
      ReplicationPersistentStore persistentStore,
      Security security) {
    this.framework = framework;
    this.filterBuilder = filterBuilder;
    this.metacards = metacards;
    this.siteManager = siteManager;
    this.newConfigManager = newConfigManager;
    this.persistentStore = persistentStore;
    this.security = security;
  }

  public void init() {
    // retry on any errors caused by the storage system being unavailable
    List<Class<? extends Throwable>> errorsToRetryOn = new ArrayList<>();
    errorsToRetryOn.add(ReplicationException.class);
    errorsToRetryOn.add(ReplicationPersistenceException.class);
    errorsToRetryOn.add(CatalogQueryException.class);

    Failsafe.with(new RetryPolicy().retryOn(errorsToRetryOn).withBackoff(3, 60, TimeUnit.SECONDS))
        .onFailedAttempt(
            throwable -> LOGGER.debug("failed to upgrade legacy configs, will retry", throwable))
        .onFailure(
            throwable ->
                LOGGER.debug("failed to upgrade legacy configs, restart to try again.", throwable))
        .run(this::convert);
  }

  private void convert() {
    convertSites();

    security.runAsAdmin(
        () -> {
          try {
            security.runWithSubjectOrElevate(
                () -> {
                  convertConfigs();
                  return null;
                });
          } catch (SecurityServiceException e) {
            LOGGER.warn(
                "Security error occured while trying to transform catalog saved metacards to persistent store saved configs. Legacy configs will not be accessible.",
                e);
          } catch (InvocationTargetException ite) {
            throw new ReplicationException(ite.getTargetException());
          }
          return null;
        });
  }

  private void convertConfigs() {
    // configs that already exist in the new version
    Set<String> existingConfigIds =
        newConfigManager.objects().map(ReplicatorConfig::getId).collect(Collectors.toSet());

    getAllConfigs()
        .stream()
        .filter(Objects::nonNull)
        .forEach(
            config -> {
              // don't overwrite any configs that have already
              // been upgraded but didn't have their legacy
              // versions deleted for whatever reason.
              if (!existingConfigIds.contains(config.getId())) {
                newConfigManager.save(config);
              }
              removeConfig(config);
            });
  }

  List<ReplicatorConfig> getAllConfigs() {
    return metacards.getTypeForFilter(
        filterBuilder.attribute(Metacard.TAGS).is().like().text(ReplicationConfig.METACARD_TAG),
        this::getConfigFromMetacard);
  }

  void removeConfig(ReplicatorConfig replicationConfig) {
    try {
      framework.delete(new DeleteRequestImpl(replicationConfig.getId()));
    } catch (IngestException e) {
      LOGGER.debug("An error occured while trying to delete a legacy replication config.", e);
    } catch (SourceUnavailableException e) {
      throw new ReplicationException(e);
    }
  }

  @VisibleForTesting
  @Nullable
  ReplicatorConfig getConfigFromMetacard(Metacard mcard) {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    Direction direction;
    config.setId(mcard.getId());
    config.setName(metacards.getAttributeValueOrDefault(mcard, ReplicationConfig.NAME, null));
    config.setDescription(
        metacards.getAttributeValueOrDefault(mcard, ReplicationConfig.DESCRIPTION, null));
    direction =
        Direction.valueOf(
            metacards.getAttributeValueOrDefault(
                mcard, ReplicationConfig.DIRECTION, Direction.PUSH.name()));
    config.setFailureRetryCount(
        metacards.getAttributeValueOrDefault(
            mcard, ReplicationConfig.FAILURE_RETRY_COUNT, DEFAULT_FAILURE_RETRY_COUNT));
    String oldUrl = metacards.getAttributeValueOrDefault(mcard, ReplicationConfig.URL, null);

    if (oldUrl != null) {
      config = setSourceAndDestinationWithOldUrl(config, direction, oldUrl);
      if (config == null) {
        return null;
      }
    } else {
      config.setDestination(
          metacards.getAttributeValueOrDefault(mcard, ReplicationConfig.DESTINATION, null));
      config.setSource(metacards.getAttributeValueOrDefault(mcard, ReplicationConfig.SOURCE, null));
    }

    config.setFilter(metacards.getAttributeValueOrDefault(mcard, ReplicationConfig.CQL, null));
    config.setBidirectional(direction == Direction.BOTH);
    config.setMetadataOnly(
        metacards.getAttributeValueOrDefault(
                mcard, ReplicationConfig.TYPE, ReplicationType.RESOURCE)
            == ReplicationType.METACARD);

    if (config.getDestination() == null
        || config.getSource() == null
        || config.getFilter() == null
        || config.getName() == null) {
      LOGGER.warn(
          "Invalid replication configuration found. {} is missing one or more required fields. {}, {}, {}, {}",
          mcard.getId(),
          ReplicationConfig.NAME,
          ReplicationConfig.SOURCE,
          ReplicationConfig.DESTINATION,
          ReplicationConfig.CQL);
      return null;
    }
    config.setVersion(metacards.getAttributeValueOrDefault(mcard, ReplicationConfig.VERSION, 1));
    config.setSuspended(
        metacards.getAttributeValueOrDefault(mcard, ReplicationConfig.SUSPEND, false));

    return config;
  }

  @Nullable
  private ReplicatorConfigImpl setSourceAndDestinationWithOldUrl(
      ReplicatorConfigImpl config, Direction direction, String oldUrl) {
    try {
      if (direction.equals(Direction.PULL)) {
        config.setSource(getOrCreateSite(oldUrl).getId());
        config.setDestination(getOrCreateSite(SystemBaseUrl.INTERNAL.getBaseUrl()).getId());
      } else {
        config.setDestination(getOrCreateSite(oldUrl).getId());
        config.setSource(getOrCreateSite(SystemBaseUrl.INTERNAL.getBaseUrl()).getId());
      }
    } catch (MalformedURLException e) {
      LOGGER.warn(
          "Could not create URL from {}. Unable to create ReplicatorConfig from metacard.", oldUrl);
      return null;
    }
    return config;
  }

  @VisibleForTesting
  ReplicationSite getOrCreateSite(String siteUrl) throws MalformedURLException {
    ReplicationSite replicationSite =
        siteManager.objects().filter(s -> s.getUrl().equals(siteUrl)).findFirst().orElse(null);
    if (replicationSite != null) {
      return replicationSite;
    } else {
      URL url = new URL(siteUrl);
      ReplicationSite site = siteManager.createSite(url.getHost(), siteUrl);
      siteManager.save(site);
      return site;
    }
  }

  private void convertSites() {
    // sites that already exist in the new version
    Set<String> existingSiteIds =
        siteManager.objects().map(ReplicationSite::getId).collect(Collectors.toSet());

    persistentStore
        .objects(OldSite.class)
        .forEach(
            oldSite -> {
              if (!existingSiteIds.contains(oldSite.getId())) {
                ReplicationSite newSite =
                    siteManager.createSite(oldSite.getName(), oldSite.getUrl());
                newSite.setId(oldSite.getId());
                siteManager.save(newSite);
              }
              try {
                persistentStore.delete(OldSite.class, oldSite.getId());
              } catch (NotFoundException e) {
                // no op
              }
            });
  }
}
