/**
 * Copyright (c) Codice Foundation
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
package org.codice.ditto.replication.admin.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.codice.ditto.replication.api.modern.ReplicationSite;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockSitePersistentStore implements ReplicationSitePersistentStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockSitePersistentStore.class);

  private Map<String, ReplicationSite> sites;

  public MockSitePersistentStore() {
    sites = new HashMap<>();
  }

  public void clearSites() {
    sites.clear();
  }

  private ReplicationSite createTestSite(String id, String name, String urlString) {
    URL url;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      LOGGER.error("Malformed URL {} in MockSitePersistentStore. Saving URL as null.", urlString);
      return new ReplicationSiteImpl(id, name, null);
    }
    return new ReplicationSiteImpl(id, name, url);
  }

  @Override
  public Optional<ReplicationSite> getSite(String id) {
    return Optional.of(sites.get(id));
  }

  @Override
  public Set<ReplicationSite> getSites() {
    return new HashSet<>(sites.values());
  }

  @Override
  public ReplicationSite saveSite(ReplicationSite site) {
    sites.put(site.getId(), site);
    return site;
  }

  @Override
  public ReplicationSite saveSite(String name, String urlString) {
    ReplicationSite site = createTestSite(name + "id", name, urlString);
    sites.put(site.getId(), site);
    return site;
  }

  @Override
  public ReplicationSite editSite(String id, String name, String urlString) {
    deleteSite(id);
    return saveSite(createTestSite(id, name, urlString));
  }

  @Override
  public boolean deleteSite(String id) {
    if (sites.containsKey(id)) {
      sites.remove(id);
      return true;
    } else {
      return false;
    }
  }
}
