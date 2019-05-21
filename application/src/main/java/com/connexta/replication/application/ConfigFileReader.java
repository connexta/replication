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
package com.connexta.replication.application;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfigFileReader will attempt to read sites.json and configs.json in the etc folder and save the
 * objects in storage. Adding a config to configs.json might look something like this:
 *
 * <pre>
 * [{
 *     "name": "file6",
 *     "bidirectional": false,
 *     "source": "ditto-1",
 *     "destination": "ditto-2",
 *     "filter": "title like 'file6'"
 * }]
 * </pre>
 *
 * Note that source and destination take the NAME of the site NOT the ID. The source and destination
 * sites need to exist in storage or sites.json before the config can be created. Here's what adding
 * some sites to sites.json would look like:
 *
 * <pre>
 * [{
 *    "name": "ditto-1",
 *    "url": "https://ditto-1.phx.connexta.com:8993/services"
 * },
 * {
 *    "name": "ditto-2",
 *    "url": "https://ditto-2.phx.connexta.com:8993/services"
 * }]
 * </pre>
 */
public class ConfigFileReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileReader.class);

  ReplicatorConfigManager configManager;

  SiteManager siteManager;

  ReplicatorHistoryManager historyManager;

  public ConfigFileReader(
      ReplicatorConfigManager configManager,
      SiteManager siteManager,
      ReplicatorHistoryManager historyManager) {
    this.configManager = configManager;
    this.siteManager = siteManager;
    this.historyManager = historyManager;
  }

  public void run() {
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(this::readConfigFiles, 5, 60, TimeUnit.SECONDS);
  }

  private void readConfigFiles() {
    Path resourcePath = Paths.get(System.getProperty("user.dir"), "config");
    readSites(Paths.get(resourcePath.toString(), "sites.json"));
    readConfigs(Paths.get(resourcePath.toString(), "configs.json"));
  }

  @VisibleForTesting
  void readSites(Path sitesPath) {

    LOGGER.trace("Attempting to read sites from: {}", sitesPath.toString());
    try {
      JsonReader jsonReader = new JsonReader(new FileReader(sitesPath.toString()));
      Gson gson = new Gson();
      ReplicationSiteImpl[] sites = gson.fromJson(jsonReader, ReplicationSiteImpl[].class);
      List<ReplicationSite> existingSites = siteManager.objects().collect(Collectors.toList());
      for (ReplicationSite site : sites) {
        String name = site.getName();
        if (existingSites.stream().map(ReplicationSite::getName).noneMatch(name::equals)) {
          siteManager.save(site);
          LOGGER.info("New site \"{}\" saved", name);
        }
        // remove a site from the list if it exists in sites.json
        existingSites.removeIf(s -> s.getName().equals(name));
      }
      // any remaining sites don't exist in sites.json so delete them
      for (ReplicationSite site : existingSites) {
        siteManager.remove(site.getId());
        LOGGER.info("Site \"{}\" deleted", site.getName());
      }

      LOGGER.trace("Successfully read sites");
    } catch (FileNotFoundException e) {
      LOGGER.warn("Sites file not found");
    } catch (JsonSyntaxException jse) {
      LOGGER.warn("Sites file contained syntax errors", jse);
    }
  }

  @VisibleForTesting
  void readConfigs(Path configsPath) {
    LOGGER.trace("Attempting to read configs from: {}", configsPath.toString());
    try {
      JsonReader jsonReader = new JsonReader(new FileReader(configsPath.toString()));
      Gson gson = new Gson();
      ReplicatorConfigImpl[] configs = gson.fromJson(jsonReader, ReplicatorConfigImpl[].class);
      List<ReplicatorConfig> existingConfigs = configManager.objects().collect(Collectors.toList());
      List<ReplicationSite> existingSites = siteManager.objects().collect(Collectors.toList());
      for (ReplicatorConfig config : configs) {
        String name = config.getName();
        if (existingConfigs.stream().map(ReplicatorConfig::getName).noneMatch(name::equals)) {
          // easier to specify names so the names of the sites will be in source and destination at
          // first
          // we need to swap them out for the ids
          ReplicationSite source =
              existingSites
                  .stream()
                  .filter(site -> site.getName().equals(config.getSource()))
                  .findFirst()
                  .orElse(null);
          ReplicationSite destination =
              existingSites
                  .stream()
                  .filter(site -> site.getName().equals(config.getDestination()))
                  .findFirst()
                  .orElse(null);

          if (source == null) {
            LOGGER.warn("site {} does not exist", config.getSource());
            continue;
          } else if (destination == null) {
            LOGGER.warn("site {} does not exist", config.getDestination());
            continue;
          }
          config.setSource(source.getId());
          config.setDestination(destination.getId());
          config.setFailureRetryCount(5);

          configManager.save(config);
          LOGGER.info("New config \"{}\" saved", name);
        }
        // remove a config from the list if it exists in configs.json
        existingConfigs.removeIf(c -> c.getName().equals(name));
      }
      // any remaining configs don't exist in configs.json so delete them
      for (ReplicatorConfig config : existingConfigs) {
        configManager.remove(config.getId());
        ReplicationStatus history = historyManager.getByReplicatorId(config.getId());
        historyManager.remove(history.getId());
        LOGGER.info("Config \"{}\" deleted", config.getName());
      }

      LOGGER.trace("Successfully read configs");
    } catch (FileNotFoundException e) {
      LOGGER.warn("Configs file not found");
    } catch (JsonSyntaxException jse) {
      LOGGER.warn("Configs file contained syntax errors", jse);
    }
  }
}
