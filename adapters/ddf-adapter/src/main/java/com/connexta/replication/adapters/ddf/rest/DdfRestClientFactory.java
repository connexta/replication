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

import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.shiro.util.ThreadContext;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;

/** Factory for creating a basic client capable of posting resources to the DDF REST endpoint. */
public class DdfRestClientFactory {

  private static final String DEFAULT_REST_ENDPOINT = "/catalog";

  private final ClientFactoryFactory clientFactoryFactory;

  public DdfRestClientFactory(ClientFactoryFactory clientFactoryFactory) {
    this.clientFactoryFactory = clientFactoryFactory;
  }

  /**
   * Creates a limited functionality wrapper around a new {@link WebClient} created from a given
   * host name. The created client can be used for multiple requests.
   *
   * @param host the host for the client to connect to
   * @return a wrapped {@link WebClient}
   */
  public DdfRestClient create(String host) {
    final SecureCxfClientFactory<RESTService> restClientFactory =
        clientFactoryFactory.getSecureCxfClientFactory(
            host + DEFAULT_REST_ENDPOINT, RESTService.class);

    WebClient webClient = restClientFactory.getWebClientForSubject(ThreadContext.getSubject());

    webClient.accept(MediaType.APPLICATION_XML);
    webClient.accept(MediaType.APPLICATION_JSON);

    return new DdfRestClient(webClient);
  }
}
