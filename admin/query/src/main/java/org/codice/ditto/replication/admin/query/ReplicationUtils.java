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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.SyncRequest;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.modern.ReplicationSite;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;

/** Utility class that does all the heavy lifting for the graphql operations */
public class ReplicationUtils {

  private static final long BYTES_PER_MB = 1024L * 1024L;

  private final ReplicationSitePersistentStore persistentStore;

  private final ReplicatorConfigLoader configLoader;

  private final ReplicatorHistory history;

  private final Replicator replicator;

  public ReplicationUtils(
      ReplicationSitePersistentStore persistentStore,
      ReplicatorConfigLoader configLoader,
      ReplicatorHistory history,
      Replicator replicator) {
    this.persistentStore = persistentStore;
    this.configLoader = configLoader;
    this.history = history;
    this.replicator = replicator;
  }

  // mutator methods
  public ReplicationSiteField createSite(String name, AddressField address) {
    if (siteExists(name)) {
      throw new ReplicationException(
          "Cannot create site with name " + name + ". Site already exists.");
    }
    String urlString = addressFieldToUrlString(address);
    ReplicationSite newSite = persistentStore.saveSite(name, urlString);

    return getSiteFieldForSite(newSite);
  }

  public boolean siteExists(String name) {
    return persistentStore.getSites().stream().anyMatch(site -> site.getName().equals(name));
  }

  public ReplicationSiteField updateSite(String id, String name, AddressField address) {
    String urlString = addressFieldToUrlString(address);
    ReplicationSite site = persistentStore.editSite(id, name, urlString);
    return getSiteFieldForSite(site);
  }

  private String addressFieldToUrlString(AddressField address) {
    return address.host().hostname() == null
        ? address.url()
        : String.format("https://%s:%s", address.host().hostname(), address.host().port());
  }

  public Boolean deleteSite(String id) {
    return persistentStore.deleteSite(id);
  }

  public boolean replicationConfigExists(String name) {
    return configLoader.getConfig(name).isPresent();
  }

  public ReplicationField createReplication(
      String name, String sourceId, String destinationId, String filter, Boolean biDirectional) {
    if (replicationConfigExists(name)) {
      throw new ReplicationException(
          "Cannot create replication config for "
              + name
              + ". Existing configuration already exists");
    }
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId(UUID.randomUUID().toString().replaceAll("-", ""));
    config.setName(name);
    config.setSource(sourceId);
    config.setDestination(destinationId);
    config.setCql(filter);
    config.setReplicationType(ReplicationType.RESOURCE);
    config.setDirection(biDirectional ? Direction.BOTH : Direction.PUSH);
    configLoader.saveConfig(config);

    return getReplicationFieldForConfig(config);
  }

  public ReplicationField updateReplication(
      String id,
      String name,
      String sourceId,
      String destinationId,
      String filter,
      Boolean biDirectional) {
    ReplicatorConfig existingConfig =
        configLoader
            .getConfig(id)
            .orElseThrow(
                () ->
                    new ReplicationException(
                        "Could not update replication config for "
                            + name
                            + ". No configuration found."));

    ReplicatorConfigImpl config = new ReplicatorConfigImpl(existingConfig);
    config.setName(name);
    config.setSource(sourceId);
    config.setDestination(destinationId);
    config.setCql(filter);
    config.setDirection(biDirectional ? Direction.BOTH : Direction.PUSH);
    configLoader.saveConfig(config);
    return getReplicationFieldForConfig(config);
  }

  private ReplicationField getReplicationFieldForConfig(ReplicatorConfig config) {
    ReplicationField field = new ReplicationField();
    field.id(config.getId());
    field.name(config.getName());
    field.source(
        getSiteFieldForSite(
            persistentStore
                .getSite(config.getSource())
                .orElseThrow(
                    () ->
                        new ReplicationException(
                            "Could not find site for " + config.getSource()))));
    field.destination(
        getSiteFieldForSite(
            persistentStore
                .getSite(config.getDestination())
                .orElseThrow(
                    () ->
                        new ReplicationException(
                            "Could not find site for " + config.getDestination()))));
    field.filter(config.getCql());
    field.biDirectional(Direction.BOTH.equals(config.getDirection()));
    List<ReplicationStatus> statusList = history.getReplicationEvents(config.getName());
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
    return field;
  }

  private @Nullable ReplicationSiteField getSiteFieldForSite(ReplicationSite site) {
    if (site == null) {
      return null;
    }
    ReplicationSiteField field = new ReplicationSiteField();
    field.id(site.getId());
    field.name(site.getName());
    AddressField address = new AddressField();
    address.url(site.getUrl().toString());
    address.hostname(site.getUrl().getHost());
    address.port(site.getUrl().getPort());
    field.address(address);
    return field;
  }

  public boolean deleteConfig(String id) {
    ReplicatorConfig config =
        configLoader
            .getAllConfigs()
            .stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    if (config == null) {
      return false;
    }
    configLoader.removeConfig(config);
    return true;
  }

  public ListField<ReplicationSiteField> getSites() {
    ListField<ReplicationSiteField> siteFields = new ReplicationSiteField.ListImpl();
    Set<ReplicationSite> sites = persistentStore.getSites();

    for (ReplicationSite site : sites) {
      siteFields.add(getSiteFieldForSite(site));
    }

    return siteFields;
  }

  public ListField<ReplicationField> getReplications() {
    ListField<ReplicationField> fields = new ReplicationField.ListImpl();
    configLoader
        .getAllConfigs()
        .stream()
        .map(this::getReplicationFieldForConfig)
        .forEach(fields::add);
    return fields;
  }
}
