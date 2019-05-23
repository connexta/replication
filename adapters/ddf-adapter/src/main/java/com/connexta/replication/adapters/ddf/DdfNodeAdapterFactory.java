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
package com.connexta.replication.adapters.ddf;

import com.connexta.replication.adapters.ddf.rest.DdfRestClientFactory;
import java.net.URL;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;

/**
 * Factory for creating {@link DdfNodeAdapter}s for the {@link
 * org.codice.ditto.replication.api.Replicator}.
 */
public class DdfNodeAdapterFactory implements NodeAdapterFactory {

  private final DdfRestClientFactory ddfRestClientFactory;

  private final ClientFactoryFactory clientFactory;

  public DdfNodeAdapterFactory(
      DdfRestClientFactory ddfRestClientFactory, ClientFactoryFactory clientFactory) {
    this.ddfRestClientFactory = ddfRestClientFactory;
    this.clientFactory = clientFactory;
  }

  @Override
  public NodeAdapter create(URL url) {
    return new DdfNodeAdapter(ddfRestClientFactory, clientFactory, url);
  }

  @Override
  public NodeAdapterType getType() {
    return NodeAdapterType.DDF;
  }
}
