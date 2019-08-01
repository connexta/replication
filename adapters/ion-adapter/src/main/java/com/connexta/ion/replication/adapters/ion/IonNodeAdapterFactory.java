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
package com.connexta.ion.replication.adapters.ion;

import com.connexta.ion.replication.api.AdapterException;
import com.connexta.ion.replication.api.NodeAdapter;
import com.connexta.ion.replication.api.NodeAdapterFactory;
import com.connexta.ion.replication.api.NodeAdapterType;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Factory for creating {@link IonNodeAdapter}s for the {@link
 * com.connexta.ion.replication.api.Replicator}.
 */
public class IonNodeAdapterFactory implements NodeAdapterFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(IonNodeAdapterFactory.class);

  private final int connectionTimeout;

  private final int receiveTimeout;

  private final SimpleClientHttpRequestFactory requestFactory;

  /**
   * Creates an IonNodeAdapter.
   *
   * @param requestFactory a factory used to create HTTP request objects
   * @param connectionTimeout the connection timeout for any clients created by this factory
   * @param receiveTimeout the receive timeout for any clients created by this factory
   */
  public IonNodeAdapterFactory(
      SimpleClientHttpRequestFactory requestFactory, int connectionTimeout, int receiveTimeout) {
    this.requestFactory = requestFactory;
    this.connectionTimeout = connectionTimeout;
    this.receiveTimeout = receiveTimeout;
    LOGGER.debug(
        "Created a IonNodeAdapterFactory with a connection timeout of {} seconds and a receive timeout of {} seconds",
        connectionTimeout,
        receiveTimeout);
  }

  @Override
  public NodeAdapter create(URL url) {
    // TODO change this when ingest enpoint supports https
    String baseUrl = "http://" + url.getHost() + ":" + url.getPort();
    try {
      RestTemplate template = new RestTemplate();
      requestFactory.setBufferRequestBody(false);
      requestFactory.setConnectTimeout(connectionTimeout);
      requestFactory.setReadTimeout(receiveTimeout);
      template.setRequestFactory(requestFactory);
      return new IonNodeAdapter(new URL(baseUrl), template);
    } catch (MalformedURLException e) {
      throw new AdapterException("Failed to create adapter", e);
    }
  }

  @Override
  public NodeAdapterType getType() {
    return NodeAdapterType.ION;
  }
}
