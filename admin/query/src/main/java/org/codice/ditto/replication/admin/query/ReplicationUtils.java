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
package org.codice.ditto.replication.admin.query;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.codice.ditto.replication.admin.query.status.fields.ReplicationStats;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class for operating on replication configurations and sites. */
public class ReplicationUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationUtils.class);

  private static final String DEFAULT_CONTEXT = "services";

  private static final String NOT_RUN = "NOT_RUN";

  private final SiteManager siteManager;

  private final ReplicatorConfigManager configManager;

  private final ReplicatorHistory history;

  private final Replicator replicator;

  public ReplicationUtils(
      SiteManager siteManager,
      ReplicatorConfigManager configManager,
      ReplicatorHistory history,
      Replicator replicator) {
    this.siteManager = siteManager;
    this.configManager = configManager;
    this.history = history;
    this.replicator = replicator;
  }

  // mutator methods
  public ReplicationSiteField createSite(
      String name, AddressField address, @Nullable String rootContext, boolean isRemoteManaged) {
    String urlString = addressFieldToUrlString(address, rootContext);
    ReplicationSite newSite = siteManager.createSite(name, urlString);
    newSite.setRemoteManaged(isRemoteManaged);
    siteManager.save(newSite);

    return getSiteFieldForSite(newSite);
  }

  public boolean isDuplicateSiteName(String name) {
    return siteManager.objects().map(ReplicationSite::getName).anyMatch(name::equalsIgnoreCase);
  }

  @Nullable
  public ReplicationSite getSite(String id) {
    try {
      return siteManager.get(id);
    } catch (NotFoundException e) {
      return null;
    }
  }

  public boolean isUpdatedSitesName(String id, String name) {
    final ReplicationSite site = this.getSite(id);
    if (site != null) {
      return site.getName().equalsIgnoreCase(name);
    }

    return false;
  }

  public boolean siteIdExists(String id) {
    return siteManager.exists(id);
  }

  public boolean updateSite(
      String id, @Nullable String name, AddressField address, @Nullable String rootContext) {
    ReplicationSite site = siteManager.get(id);

    String updatedUrl = updateUrl(site.getUrl(), address, rootContext);
    setIfPresent(site::setName, name);
    setIfPresent(site::setUrl, updatedUrl);

    try {
      siteManager.save(site);
    } catch (ReplicationPersistenceException e) {
      LOGGER.debug("Unable to save updated site {} with id {}", site, id, e);
      return false;
    }

    return true;
  }

  private String updateUrl(String prevUrl, AddressField addressField, @Nullable String context) {
    URL url;
    try {
      url = new URL(prevUrl);
    } catch (MalformedURLException e) {
      // sites previous saved url should always be valid
      return "";
    }

    final String oldHostname = url.getHost();
    final int oldPort = url.getPort();
    final String oldContext = stripStartingSlashes(url.getPath());

    final String newHostname;
    final int newPort;
    final String newContext;

    if (addressField.host().hostname() != null) {
      newHostname = addressField.host().hostname();
    } else {
      newHostname = oldHostname;
    }

    if (addressField.host().port() != null) {
      newPort = addressField.host().port();
    } else {
      newPort = oldPort;
    }

    if (context != null) {
      newContext = stripStartingSlashes(context);
    } else {
      newContext = oldContext;
    }

    try {
      return new URL(
              String.format("%s://%s:%d/%s", url.getProtocol(), newHostname, newPort, newContext))
          .toString();
    } catch (MalformedURLException e) {
      // all fields are validated by graphql before we make it here so this cannot be hit.
      return "";
    }
  }

  private String stripStartingSlashes(String str) {
    return str.trim().replaceFirst("^[/\\s]*", "");
  }

  private String addressFieldToUrlString(AddressField address, @Nullable String rootContext) {
    final String context =
        (rootContext != null) ? stripStartingSlashes(rootContext) : DEFAULT_CONTEXT;

    return address.host().hostname() == null
        ? address.url()
        : String.format(
            "https://%s:%s/%s", address.host().hostname(), address.host().port(), context);
  }

  public boolean deleteSite(String id) {
    try {
      siteManager.remove(id);
      return true;
    } catch (NotFoundException e) {
      return false;
    }
  }

  public boolean replicationConfigExists(String name) {
    return configManager.objects().map(ReplicatorConfig::getName).anyMatch(name::equalsIgnoreCase);
  }

  public boolean configExists(String id) {
    return configManager.exists(id);
  }

  public ReplicationField createReplication(
      String name, String sourceId, String destinationId, String filter, Boolean biDirectional) {
    ReplicatorConfig config = configManager.create();
    config.setName(name);
    config.setSource(sourceId);
    config.setDestination(destinationId);
    config.setFilter(filter);
    config.setBidirectional(biDirectional);
    config.setFailureRetryCount(5);
    configManager.save(config);

    return getReplicationFieldForConfig(config);
  }

  public ReplicationField updateReplication(
      String id,
      @Nullable String name,
      @Nullable String sourceId,
      @Nullable String destinationId,
      @Nullable String filter,
      @Nullable Boolean biDirectional) {
    ReplicatorConfig config = configManager.get(id);

    setIfPresent(config::setName, name);
    setIfPresent(config::setSource, sourceId);
    setIfPresent(config::setDestination, destinationId);
    setIfPresent(config::setFilter, filter);
    setIfPresent(config::setBidirectional, biDirectional);

    configManager.save(config);
    return getReplicationFieldForConfig(config);
  }

  private <T> void setIfPresent(Consumer<T> setter, @Nullable T o) {
    if (o != null) {
      setter.accept(o);
    }
  }

  private ReplicationField getReplicationFieldForConfig(ReplicatorConfig config) {
    ReplicationField field = new ReplicationField();
    field.id(config.getId());
    field.name(config.getName());
    field.source(getSiteFieldForSite(siteManager.get(config.getSource())));
    field.destination(getSiteFieldForSite(siteManager.get(config.getDestination())));
    field.filter(config.getFilter());
    field.biDirectional(config.isBidirectional());
    field.modified(config.getModified());
    field.version(config.getVersion());
    field.suspended(config.isSuspended());

    ReplicationStats stats = new ReplicationStats();
    List<ReplicationStatus> statusList = history.getReplicationEvents(config.getName());
    if (!statusList.isEmpty()) {
      ReplicationStatus lastStatus = statusList.get(0);
      stats.setLastRun(lastStatus.getLastRun());
      stats.setStartTime(lastStatus.getStartTime());
      stats.setLastSuccess(lastStatus.getLastSuccess());
    }

    replicator
        .getActiveSyncRequests()
        .stream()
        .filter(sync -> sync.getConfig().getId().equals(config.getId()))
        .map(SyncRequest::getStatus)
        .forEach(status -> statusList.add(0, status));

    if (statusList.isEmpty()) {
      stats.setStatus(NOT_RUN);
      stats.setPullBytes(0L);
      stats.setPushBytes(0L);
      stats.setPullCount(0L);
      stats.setPushCount(0L);
      stats.setPullFailCount(0L);
      stats.setPushFailCount(0L);
      stats.setDuration(0L);
    } else {
      ReplicationStatus status = statusList.get(0);
      stats.setPid(status.getId());
      stats.setStatus(status.getStatus().name());
      stats.setPullBytes(status.getPullBytes());
      stats.setPushBytes(status.getPushBytes());
      stats.setPullCount(status.getPullCount());
      stats.setPushCount(status.getPushCount());
      stats.setPullFailCount(status.getPullFailCount());
      stats.setPushFailCount(status.getPushFailCount());
      stats.setDuration(status.getDuration());
    }

    field.stats(stats);
    return field;
  }

  /**
   * Saves stats for a replication identified by the replication's name. If there is an existing
   * {@link ReplicationStatus} with the id {@link ReplicationStats#getPid()}, then it will first be
   * deleted then re-saved with the new information.
   *
   * @param stats stats object to convert to {@link ReplicationStatus} to be saved
   * @return {@code true} if the stats were successfully saved, otherwise false.
   */
  public boolean updateReplicationStats(StringField replicationName, ReplicationStats stats) {
    final String repName = replicationName.getValue();
    ReplicationStatus status = new ReplicationStatusImpl(stats.getPid(), repName);
    status.setDuration(stats.getDuration());
    status.setStartTime(getDate(stats.getStartTime()));
    status.setLastSuccess(getDate(stats.getLastSuccess()));
    status.setLastRun(getDate(stats.getLastRun()));
    status.setStatus(Status.valueOf(stats.getStatus()));
    status.setPullBytes(stats.getPullBytes());
    status.setPullCount(stats.getPullCount());
    status.setPullFailCount(stats.getPullFailCount());
    status.setPushBytes(stats.getPushBytes());
    status.setPushCount(stats.getPushCount());
    status.setPushFailCount(stats.getPushFailCount());

    try {
      Set<String> statusIds =
          history
              .getReplicationEvents(repName)
              .stream()
              .map(ReplicationStatus::getId)
              .collect(Collectors.toSet());
      history.removeReplicationEvents(statusIds);

      history.addReplicationEvent(status);
    } catch (ReplicationPersistenceException e) {
      LOGGER.debug(
          "Failed to save replication stats for replication {}", replicationName.getValue(), e);
      return false;
    }

    return true;
  }

  @Nullable
  private Date getDate(String iso8601) {
    if (iso8601 != null) {
      return Date.from(Instant.parse(iso8601));
    }
    return null;
  }

  public @Nullable ReplicationSiteField getSiteFieldForSite(ReplicationSite site) {
    if (site == null) {
      return null;
    }
    ReplicationSiteField field = new ReplicationSiteField();
    field.id(site.getId());
    field.name(site.getName());
    field.modified(site.getModified());
    field.version(site.getVersion());
    AddressField address = new AddressField();
    URL url;

    try {
      url = new URL(site.getUrl());
    } catch (MalformedURLException e) {
      throw new ReplicationException("Malformed URL: " + site.getUrl(), e);
    }

    address.url(site.getUrl());
    address.hostname(url.getHost());
    address.port(url.getPort());
    field.address(address);
    field.rootContext(StringUtils.isEmpty(url.getPath()) ? DEFAULT_CONTEXT : url.getPath());
    field.isDisableLocal(site.isRemoteManaged());
    return field;
  }

  public boolean markConfigDeleted(String id, boolean deleteData) {
    try {
      ReplicatorConfig config = configManager.get(id);
      config.setDeleted(true);
      config.setDeleteData(deleteData);
      config.setSuspended(true);
      configManager.save(config);
      replicator.cancelSyncRequest(id);
      return true;
    } catch (ReplicationPersistenceException e) {
      LOGGER.debug("Unable to delete replicator configuration with id {}", id, e);
      return false;
    } catch (NotFoundException e) {
      LOGGER.debug("Config with id {}", id, e);
      return true;
    }
  }

  public boolean cancelConfig(String id) {
    replicator.cancelSyncRequest(id);
    return true;
  }

  public boolean setConfigSuspended(String id, boolean suspended) {
    ReplicatorConfig config = getConfigForId(id);
    if (config.isSuspended() == suspended) {
      return false;
    }

    config.setSuspended(suspended);
    configManager.save(config);
    if (suspended) {
      replicator.cancelSyncRequest(id);
    }
    return true;
  }

  public ReplicatorConfig getConfigForId(String id) {
    return configManager.get(id);
  }

  public ListField<ReplicationSiteField> getSites() {
    ListField<ReplicationSiteField> siteFields = new ReplicationSiteField.ListImpl();
    siteManager.objects().map(this::getSiteFieldForSite).forEach(siteFields::add);
    return siteFields;
  }

  public ListField<ReplicationField> getReplications(boolean filterDeleted) {
    ListField<ReplicationField> fields = new ReplicationField.ListImpl();

    if (filterDeleted) {
      configManager
          .objects()
          .filter(c -> !c.isDeleted())
          .map(this::getReplicationFieldForConfig)
          .forEach(fields::add);
    } else {
      configManager.objects().map(this::getReplicationFieldForConfig).forEach(fields::add);
    }
    return fields;
  }
}
