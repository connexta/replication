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
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.persistence.NotFoundException;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorConfigManagerImpl implements ReplicatorConfigManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorConfigManagerImpl.class);

  private ReplicationPersistentStore persistentStore;

  private static final int RETRY_DELAY_SEC = 5;

  private static final int RETRY_DURATION_MIN = 5;

  private static final String REPLICATION_CONFIG_FILE = "/etc/replication-configs.json";

  public ReplicatorConfigManagerImpl(ReplicationPersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  public void init() {
    RetryPolicy retryPolicy =
        new RetryPolicy()
            .withDelay(RETRY_DELAY_SEC, TimeUnit.SECONDS)
            .withMaxDuration(RETRY_DURATION_MIN, TimeUnit.MINUTES)
            .retryOn(ReplicationPersistenceException.class);

    Failsafe.with(retryPolicy).run(this::loadConfigsFromFile);
  }

  /** Add replication configurations from file if they don't already exist */
  void loadConfigsFromFile() {
    String basePath = System.getProperty("karaf.home", "./");
    File sitesFile = new File(basePath + REPLICATION_CONFIG_FILE);
    if (!sitesFile.exists()) {
      return;
    }
    Gson gson = new Gson();
    Set<String> existingConfigs =
        this.objects().map(ReplicatorConfig::getId).collect(Collectors.toSet());
    try {
      ReplicatorConfigImpl[] configs =
          gson.fromJson(new FileReader(sitesFile), ReplicatorConfigImpl[].class);
      for (ReplicatorConfigImpl config : configs) {
        if (!existingConfigs.contains(config.getId())) {
          this.save(config);
        }
      }
    } catch (FileNotFoundException e) {
      LOGGER.warn("Could not load replication config: {}", e.getMessage());
      LOGGER.debug("Replication config load error: ", e);
    }
  }

  @Override
  public ReplicatorConfig create() {
    return new ReplicatorConfigImpl();
  }

  @Override
  public ReplicatorConfig get(String id) {
    return persistentStore.get(ReplicatorConfigImpl.class, id);
  }

  @Override
  public Stream<ReplicatorConfig> objects() {
    return persistentStore.objects(ReplicatorConfigImpl.class).map(ReplicatorConfig.class::cast);
  }

  @Override
  public void save(ReplicatorConfig replicatorConfig) {
    if (replicatorConfig instanceof ReplicatorConfigImpl) {
      persistentStore.save((ReplicatorConfigImpl) replicatorConfig);
    } else {
      throw new IllegalArgumentException(
          "Expected a ReplicatorConfigImpl but got a "
              + replicatorConfig.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    persistentStore.delete(ReplicatorConfigImpl.class, id);
  }

  @Override
  public boolean exists(String id) {
    try {
      persistentStore.get(ReplicatorConfigImpl.class, id);
    } catch (NotFoundException e) {
      return false;
    }
    return true;
  }
}
