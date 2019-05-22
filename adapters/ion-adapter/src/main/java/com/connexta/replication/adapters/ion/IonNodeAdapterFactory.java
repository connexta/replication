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
package com.connexta.replication.adapters.ion;

import java.net.MalformedURLException;
import java.net.URL;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class IonNodeAdapterFactory implements NodeAdapterFactory {

  @Override
  public NodeAdapter create(URL url) {
    // TODO change this when ingest enpoint supports https
    String baseUrl = "http://" + url.getHost() + ":" + url.getPort();
    try {
      RestTemplate template = new RestTemplate();
      SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
      requestFactory.setBufferRequestBody(false);
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
