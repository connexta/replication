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
package org.codice.ditto.replication.api.impl.modern;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ddf.persistence.PersistenceException;
import org.codice.ddf.persistence.PersistentItem;
import org.codice.ddf.persistence.PersistentStore;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.modern.ReplicationSite;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationSitePersistentStoreImpl implements ReplicationSitePersistentStore {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ReplicationSitePersistentStoreImpl.class);

  private static final String ID = "id";

  private static final String NAME = "name";

  private static final String URL = "url";

  private static final String PERSISTENCE_TYPE = "replication";

  private static final String LOCAL_SITE_ID = "local-site-id-1234567890";

  private static final int DEFAULT_PAGE_SIZE = 100;

  private static final int DEFAULT_START_INDEX = 0;

  private PersistentStore persistentStore;

  public ReplicationSitePersistentStoreImpl(PersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  public void init() {
    RetryPolicy retryPolicy =
        new RetryPolicy()
            .withDelay(5, TimeUnit.SECONDS)
            .withMaxDuration(5, TimeUnit.MINUTES)
            .retryOn(ReplicationPersistenceException.class);

    Failsafe.with(retryPolicy).run(this::createLocalSite);
  }

  private void createLocalSite() throws MalformedURLException {
    ReplicationSite site = getSite(LOCAL_SITE_ID).orElse(null);
    if (site != null
        && site.getName().equals(SystemInfo.getSiteName())
        && site.getUrl().toString().equals(SystemBaseUrl.EXTERNAL.getBaseUrl())) {
      return;
    }
    saveSite(
        new ReplicationSiteImpl(
            LOCAL_SITE_ID, SystemInfo.getSiteName(), new URL(SystemBaseUrl.EXTERNAL.getBaseUrl())));
  }

  @Override
  public Optional<ReplicationSite> getSite(String id) {
    String ecql = String.format("'id' = '%s'", id);
    List<Map<String, Object>> matchingPersistedSites;

    try {
      matchingPersistedSites = persistentStore.get(PERSISTENCE_TYPE, ecql);
    } catch (PersistenceException e) {
      LOGGER.debug("failed to retrieve site with id: {}", id, e);
      throw new ReplicationPersistenceException("Failed to retrieve site", e);
    }

    if (matchingPersistedSites == null || matchingPersistedSites.isEmpty()) {
      LOGGER.debug("couldn't find persisted site with id: {}", id);
      return Optional.empty();
    } else if (matchingPersistedSites.size() > 1) {
      throw new IllegalStateException("Found multiple persisted sites with id: " + id);
    }

    return Optional.of(mapToSite(matchingPersistedSites.get(0)));
  }

  @Override
  public Set<ReplicationSite> getSites() {
    List<Map<String, Object>> persistedSites = new ArrayList<>();

    try {
      persistedSites =
          persistentStore.get(PERSISTENCE_TYPE, "", DEFAULT_START_INDEX, DEFAULT_PAGE_SIZE);
    } catch (PersistenceException e) {
      LOGGER.debug("Failed to retrieve sites", e);
      throw new ReplicationPersistenceException("Failed to retrieve sites", e);
    }

    return persistedSites.stream().map(this::mapToSite).collect(Collectors.toSet());
  }

  private ReplicationSite mapToSite(Map<String, Object> persistedMap) {
    Map<String, Object> attributes = PersistentItem.stripSuffixes(persistedMap);

    String id = (String) attributes.get(ID);
    String name = (String) attributes.get(NAME);
    String urlString = (String) attributes.get(URL);
    URL url = null;

    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      LOGGER.debug("A persisted site contained an invalid URL: {}", urlString);
    }

    return new ReplicationSiteImpl(id, name, url);
  }

  @Override
  public ReplicationSite saveSite(ReplicationSite site) {
    PersistentItem item = siteToPersistentItem(site);
    try {
      persistentStore.add(PERSISTENCE_TYPE, item);
    } catch (PersistenceException e) {
      LOGGER.debug("error persisting site with id: {}", site.getId(), e);
      throw new ReplicationPersistenceException("Failed to save site.", e);
    }
    return site;
  }

  @Override
  public ReplicationSite saveSite(String name, String urlString) {
    URL url;

    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      LOGGER.debug(
          "Attempted to save site with invalid URL: {} Site will not be saved.", urlString);
      return new ReplicationSiteImpl("ERROR", name, null);
    }

    ReplicationSite site = new ReplicationSiteImpl(UUID.randomUUID().toString(), name, url);
    return saveSite(site);
  }

  @Override
  public ReplicationSite editSite(String id, String name, String urlString) {
    Optional<ReplicationSite> siteOptional = getSite(id);
    if (siteOptional.isPresent()) {
      ReplicationSite oldSite = siteOptional.get();
      URL url;
      if (name == null) {
        name = oldSite.getName();
      }
      if (urlString == null) {
        url = oldSite.getUrl();
      } else {
        try {
          url = new URL(urlString);
        } catch (MalformedURLException e) {
          LOGGER.debug(
              "Attempted to update site with invalid URL: {} Site will not be updated.", urlString);
          return new ReplicationSiteImpl("ERROR", name, null);
        }
      }
      if (deleteSite(id)) {
        return saveSite(new ReplicationSiteImpl(id, name, url));
      } else {
        return new ReplicationSiteImpl("ERROR", name, null);
      }
    }
    LOGGER.debug("Attempted to edit site with id: {} but no such site was found.", id);
    return new ReplicationSiteImpl("ERROR", name, null);
  }

  private PersistentItem siteToPersistentItem(ReplicationSite site) {
    PersistentItem item = new PersistentItem();
    item.addIdProperty(site.getId());
    item.addProperty(NAME, site.getName());
    item.addProperty(URL, site.getUrl());

    return item;
  }

  @Override
  public boolean deleteSite(String id) {
    String ecql = String.format("'id' = '%s'", id);
    try {
      return persistentStore.delete(PERSISTENCE_TYPE, ecql) > 0;
    } catch (PersistenceException e) {
      LOGGER.debug("error deleting site with id: {}", id, e);
      return false;
    }
  }
}
