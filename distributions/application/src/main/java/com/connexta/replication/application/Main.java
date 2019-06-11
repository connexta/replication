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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@SpringBootApplication(
  scanBasePackages = {"org.codice.ditto.replication", "com.connexta.replication"}
)
@EnableSolrRepositories(basePackages = "org.codice.ditto.replication")
@EnableConfigurationProperties
public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    loadBootSystemProperties();
    SpringApplication application = new SpringApplication(Main.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);
    while (true) {
      Thread.sleep(1000);
    }
  }

  @Bean
  public ConfigFileReader configFileReader(
      ReplicatorConfigManager configManager,
      SiteManager siteManager,
      ReplicatorHistoryManager historyManager) {
    ConfigFileReader reader = new ConfigFileReader(configManager, siteManager, historyManager);
    reader.run();
    return reader;
  }

  public static void loadBootSystemProperties() {
    String sslPropertiesPath = System.getProperty("ssl.system.properties");
    if (sslPropertiesPath == null) {
      sslPropertiesPath = "ssl.properties";
    }
    File sslPropertiesFile = new File(sslPropertiesPath);
    if (!sslPropertiesFile.exists()) {
      LOGGER.info("No ssl properties found at {}", sslPropertiesPath);
      return;
    }
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(sslPropertiesFile));
      properties.putAll(System.getProperties());
      System.setProperties(properties);
      LOGGER.trace("System properties {}", System.getProperties());
    } catch (IOException e) {
      LOGGER.error("Error loading ssl system properties.", e);
    }
  }
}
