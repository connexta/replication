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
package org.codice.ditto.replication.api.impl.persistence;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteManagerImpl implements SiteManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SiteManagerImpl.class);

  private ReplicationPersistentStore persistentStore;

  private static final int RETRY_DELAY_SEC = 5;

  private static final int RETRY_DURATION_MIN = 5;

  private static final String LOCAL_SITE_ID = "local-site-id-1234567890";

  private static final String SITE_CONFIG_FILE = "/etc/replication-sites.json";

  public SiteManagerImpl(ReplicationPersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  public void init() {

    RetryPolicy retryPolicy =
        new RetryPolicy()
            .withDelay(RETRY_DELAY_SEC, TimeUnit.SECONDS)
            .withMaxDuration(RETRY_DURATION_MIN, TimeUnit.MINUTES)
            .retryOn(ReplicationPersistenceException.class);

    Failsafe.with(retryPolicy).run(this::createLocalSite);
    Failsafe.with(retryPolicy).run(this::loadSitesFromFile);
  }

  void loadSitesFromFile() {
    String basePath = System.getProperty("karaf.home", "./");
    File sitesFile = new File(basePath + SITE_CONFIG_FILE);
    if (!sitesFile.exists()) {
      return;
    }
    Gson gson = new Gson();
    Set<String> existingSites =
        this.objects().map(ReplicationSite::getId).collect(Collectors.toSet());
    try {
      ReplicationSiteImpl[] sites =
          gson.fromJson(new FileReader(sitesFile), ReplicationSiteImpl[].class);
      for (ReplicationSiteImpl site : sites) {
        if (!existingSites.contains(site.getId())) {
          this.save(site);
        }
      }
    } catch (FileNotFoundException e) {
      LOGGER.warn("Could not load replication site configuration: {}", e.getMessage());
      LOGGER.debug("Replication site config load error: ", e);
    }
  }

  private void createLocalSite() {
    if (!localSiteExistsAndIsCorrect()) {
      ReplicationSite site = new ReplicationSiteImpl();
      site.setId(LOCAL_SITE_ID);
      site.setName(SystemInfo.getSiteName());
      site.setUrl(SystemBaseUrl.EXTERNAL.constructUrl(null, true));
      save(site);
    }
  }

  private boolean localSiteExistsAndIsCorrect() {
    ReplicationSite site;

    try {
      site = get(LOCAL_SITE_ID);
    } catch (NotFoundException e) {
      return false;
    }

    return site.getName().equals(SystemInfo.getSiteName())
        && site.getUrl().equals(SystemBaseUrl.EXTERNAL.constructUrl(null, true));
  }

  @Override
  public ReplicationSite create() {
    return new ReplicationSiteImpl();
  }

  @Override
  public ReplicationSite createSite(String name, String url) {
    ReplicationSite site = new ReplicationSiteImpl();
    site.setName(name);
    site.setUrl(url);
    return site;
  }

  @Override
  public ReplicationSite get(String id) {
    return persistentStore.get(ReplicationSiteImpl.class, id);
  }

  @Override
  public Stream<ReplicationSite> objects() {
    return persistentStore.objects(ReplicationSiteImpl.class).map(ReplicationSite.class::cast);
  }

  @Override
  public void save(ReplicationSite site) {
    if (site instanceof ReplicationSiteImpl) {
      persistentStore.save((ReplicationSiteImpl) site);
    } else {
      throw new IllegalArgumentException(
          "Expected a ReplicationSiteImpl but got a " + site.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    persistentStore.delete(ReplicationSiteImpl.class, id);
  }

  @Override
  public boolean exists(String id) {
    try {
      persistentStore.get(ReplicationSiteImpl.class, id);
    } catch (NotFoundException e) {
      return false;
    }
    return true;
  }
}
