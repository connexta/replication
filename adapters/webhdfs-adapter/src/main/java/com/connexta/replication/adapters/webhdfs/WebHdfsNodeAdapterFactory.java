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
package com.connexta.replication.adapters.webhdfs;

import java.net.MalformedURLException;
import java.net.URL;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating {@link WebHdfsNodeAdapter} instances for the {@link
 * org.codice.ditto.replication.api.Replicator}
 */
public class WebHdfsNodeAdapterFactory implements NodeAdapterFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsNodeAdapterFactory.class);

  public WebHdfsNodeAdapterFactory() {
    LOGGER.debug("Created a WebHdfsNodeAdapterFactory");
  }

  @Override
  public NodeAdapter create(URL url) {
    String baseUrl = "http://" + url.getHost() + ":" + url.getPort() + "/webhdfs/v1/" + url.getPath();

    try {
      return new WebHdfsNodeAdapter(new URL(baseUrl));
    } catch (MalformedURLException e) {
      throw new AdapterException("Failed to create adapter", e);
    }
  }

  @Override
  public NodeAdapterType getType() {
    return NodeAdapterType.WEBHDFS;
  }
}
