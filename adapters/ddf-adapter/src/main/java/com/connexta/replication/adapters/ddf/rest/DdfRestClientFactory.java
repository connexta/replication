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
package com.connexta.replication.adapters.ddf.rest;

import java.net.URL;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;

/** Factory for creating a basic client capable of posting resources to the DDF REST endpoint. */
public class DdfRestClientFactory {

  private static final String DEFAULT_REST_ENDPOINT = "/catalog";

  private final ClientFactoryFactory clientFactoryFactory;

  private final int connectionTimeout;

  private final int receiveTimeout;

  /**
   * creates a DdfRestClientFactory
   *
   * @param clientFactoryFactory a factory for the {@link SecureCxfClientFactory}s which will create
   *     the rest clients
   * @param connectionTimeout the connection timeout for clients created by this factory
   * @param receiveTimeout the receive timeout for clients created by this factory
   */
  public DdfRestClientFactory(
      ClientFactoryFactory clientFactoryFactory, int connectionTimeout, int receiveTimeout) {
    this.clientFactoryFactory = clientFactoryFactory;
    this.connectionTimeout = connectionTimeout;
    this.receiveTimeout = receiveTimeout;
  }

  /**
   * Creates a limited functionality wrapper around a new {@link WebClient} created from a given
   * url. The created client can be used for multiple requests.
   *
   * @param url the url for the client to connect to
   * @return a wrapped {@link WebClient}
   */
  public DdfRestClient create(URL url) {
    return new DdfRestClient(initializeClient(url));
  }

  public DdfRestClient createWithSubject(URL url) {
    String pathlessUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();

    WebClient whoamiClient =
        clientFactoryFactory
            .getSecureCxfClientFactory(
                pathlessUrl,
                RESTService.class,
                null,
                null,
                false,
                false,
                connectionTimeout,
                receiveTimeout)
            .getWebClient();
    whoamiClient.path("/whoami");
    whoamiClient.accept(MediaType.APPLICATION_XML);
    whoamiClient.accept(MediaType.APPLICATION_JSON);
    Response sessionInfo = whoamiClient.get();

    WebClient restClient = initializeClient(url);
    restClient.cookie(sessionInfo.getCookies().get("JSESSIONID"));
    return new DdfRestClient(restClient);
  }

  private WebClient initializeClient(URL url) {
    final SecureCxfClientFactory<RESTService> restClientFactory =
        clientFactoryFactory.getSecureCxfClientFactory(
            url.toString() + DEFAULT_REST_ENDPOINT,
            RESTService.class,
            null,
            null,
            false,
            false,
            connectionTimeout,
            receiveTimeout);

    WebClient webClient = restClientFactory.getWebClient();
    webClient.accept(MediaType.APPLICATION_XML);
    webClient.accept(MediaType.APPLICATION_JSON);
    return webClient;
  }
}
