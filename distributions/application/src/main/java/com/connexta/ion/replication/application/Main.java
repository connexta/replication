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
package com.connexta.ion.replication.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@SpringBootApplication(
    scanBasePackages = {"com.connexta.replication", "com.connexta.ion.replication"})
@EnableSolrRepositories(basePackages = "com.connexta.replication")
@EnableConfigurationProperties
public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  @SuppressWarnings("squid:S2189")
  public static void main(String[] args) throws Exception {
    // Need to load some system properties before anything else is done
    loadSslSystemProperties();
    new SpringApplication(Main.class).run(args);
  }

  /**
   * Loads the ssl system properties using the following precedence 1. Java system properties 2.
   * External ssl.properties 3. Internal(Default) ssl.properties
   */
  public static void loadSslSystemProperties() {
    Properties properties = getDefaultSslProperties();
    properties.putAll(getExternalSslProperties());
    LOGGER.trace("ssl.properties: {}", properties);
    properties.forEach(Main::putPropertyIfMissing);
    LOGGER.trace("Final system properties {}", System.getProperties());
  }

  private static void putPropertyIfMissing(Object name, Object value) {
    // properties passed in on the command line take precedence
    if (System.getProperty((String) name) == null) {
      System.setProperty((String) name, (String) value);
    }
  }

  public static Properties getDefaultSslProperties() {
    Properties properties = new Properties();
    try (InputStream is = Main.class.getClassLoader().getResource("ssl.properties").openStream()) {
      properties.load(is);
    } catch (IOException e) {
      LOGGER.error("Error loading default ssl system properties.", e);
    }
    return properties;
  }

  public static Properties getExternalSslProperties() {
    String sslPropertiesPath = System.getProperty("ssl.system.properties", "config/ssl.properties");
    File sslPropertiesFile = new File(sslPropertiesPath);
    Properties properties = new Properties();
    if (!sslPropertiesFile.exists()) {
      LOGGER.debug("No external ssl.properties found");
      return new Properties();
    }
    try (FileInputStream fis = new FileInputStream(sslPropertiesFile)) {
      properties.load(fis);
    } catch (IOException e) {
      LOGGER.error("Error loading external ssl system properties.", e);
    }
    return properties;
  }
}
