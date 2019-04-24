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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.ws.rs.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class that does all the heavy lifting for the graphql operations */
public class ReplicationUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationUtils.class);

  private static final long BYTES_PER_MB = 1024L * 1024L;

  private static final String DEFAULT_CONTEXT = "services";

  private final SiteManager siteManager;

  private final ReplicatorConfigManager configManager;

  private final ReplicatorHistoryManager history;

  private final Replicator replicator;

  public ReplicationUtils(
      SiteManager siteManager,
      ReplicatorConfigManager configManager,
      ReplicatorHistoryManager history,
      Replicator replicator) {
    this.siteManager = siteManager;
    this.configManager = configManager;
    this.history = history;
    this.replicator = replicator;
  }

  // mutator methods
  public ReplicationSiteField createSite(
      String name, AddressField address, @Nullable String rootContext) {
    String urlString = addressFieldToUrlString(address, rootContext);
    ReplicationSite newSite = siteManager.createSite(name, urlString);
    siteManager.save(newSite);

    return getSiteFieldForSite(newSite);
  }

  public boolean siteExists(String name) {
    return siteManager.objects().map(ReplicationSite::getName).anyMatch(name::equalsIgnoreCase);
  }

  public boolean siteIdExists(String id) {
    try {
      siteManager.get(id);
      return true;
    } catch (NotFoundException e) {
      return false;
    }
  }

  public ReplicationSiteField updateSite(
      String id, String name, AddressField address, @Nullable String rootContext) {
    String urlString = addressFieldToUrlString(address, rootContext);
    ReplicationSite site = siteManager.get(id);

    setIfPresent(site::setName, name);
    setIfPresent(site::setUrl, urlString);

    return getSiteFieldForSite(site);
  }

  private String addressFieldToUrlString(AddressField address, @Nullable String rootContext) {
    final String context =
        (rootContext != null) ? rootContext.trim().replaceFirst("^[/\\s]*", "") : DEFAULT_CONTEXT;

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
    return configManager.configExists(id);
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
    field.suspended(config.isSuspended());
    setStatusRelatedFields(field, config);

    return field;
  }

  private void setStatusRelatedFields(ReplicationField field, ReplicatorConfig config) {
    List<ReplicationStatus> statusList = new ArrayList<>();
    try {
      statusList.add(history.getByReplicatorId(config.getId()));
    } catch (NotFoundException e) {
      LOGGER.trace(
          "No history for replication config {} found. This config may not have completed a run yet.",
          config.getId());
    }

    if (!statusList.isEmpty()) {
      ReplicationStatus lastStatus = statusList.get(0);
      field.lastRun(lastStatus.getLastRun());
      field.firstRun(lastStatus.getStartTime());
      field.lastSuccess(lastStatus.getLastSuccess());
    }

    replicator
        .getActiveSyncRequests()
        .stream()
        .filter(sync -> sync.getConfig().getId().equals(config.getId()))
        .map(SyncRequest::getStatus)
        .forEach(status -> statusList.add(0, status));

    long bytesTransferred = 0;
    long itemsTransferred = 0;
    for (ReplicationStatus status : statusList) {
      bytesTransferred += status.getPullBytes() + status.getPushBytes();
      itemsTransferred += status.getPullCount() + status.getPushCount();
    }
    if (statusList.isEmpty()) {
      field.status("NOT_RUN");
    } else {
      field.status(statusList.get(0).getStatus().name());
    }

    field.dataTransferred(String.format("%d MB", bytesTransferred / BYTES_PER_MB));
    field.itemsTransferred((int) itemsTransferred);
  }

  private @Nullable ReplicationSiteField getSiteFieldForSite(ReplicationSite site) {
    if (site == null) {
      return null;
    }
    ReplicationSiteField field = new ReplicationSiteField();
    field.id(site.getId());
    field.name(site.getName());
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
    return field;
  }

  public boolean deleteConfig(String id) {
    try {
      configManager.remove(id);
      return true;
    } catch (NotFoundException e) {
      return false;
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

  public ListField<ReplicationField> getReplications() {
    ListField<ReplicationField> fields = new ReplicationField.ListImpl();
    configManager.objects().map(this::getReplicationFieldForConfig).forEach(fields::add);
    return fields;
  }
}
