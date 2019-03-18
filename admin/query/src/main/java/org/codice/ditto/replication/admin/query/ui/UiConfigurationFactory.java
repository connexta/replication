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
package org.codice.ditto.replication.admin.query.ui;

import java.io.IOException;
import java.util.Dictionary;
import org.codice.ddf.configuration.DictionaryMap;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiConfigurationFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(UiConfigurationFactory.class);

  private static final String CONFIG_PID = "ddf.platform.ui.config";

  private ConfigurationAdmin configurationAdmin;

  public UiConfigurationFactory(ConfigurationAdmin configurationAdmin) {
    this.configurationAdmin = configurationAdmin;
  }

  /** @return a UiConfigurationField that specifies how the UI should look */
  public UiConfigField getConfig() {
    Dictionary<String, Object> properties;
    try {
      if (configurationAdmin.listConfigurations(String.format("(service.pid=%s)", CONFIG_PID))
          != null) {
        properties = configurationAdmin.getConfiguration(CONFIG_PID, null).getProperties();
      } else {
        properties = new DictionaryMap();
      }
    } catch (IOException | InvalidSyntaxException e) {
      LOGGER.error(
          "Failed to retrieve UI configuration, UI may not load with the proper configuration.");
      properties = new DictionaryMap();
    }

    UiConfigField config = new UiConfigField();
    config.header(getString(properties.get("header")));
    config.footer(getString(properties.get("footer")));
    config.color(getString(properties.get("color")));
    config.background(getString(properties.get("background")));

    return config;
  }

  private String getString(Object object) {
    String val = "";
    if (object instanceof String) {
      val = (String) object;
    }
    return val;
  }
}
