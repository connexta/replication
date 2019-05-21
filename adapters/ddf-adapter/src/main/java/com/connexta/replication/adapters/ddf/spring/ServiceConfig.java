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
package com.connexta.replication.adapters.ddf.spring;

import com.connexta.replication.adapters.ddf.DdfNodeAdapterFactory;
import com.connexta.replication.adapters.ddf.rest.DdfRestClientFactory;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.impl.ClientFactoryFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("ddf-adapter")
public class ServiceConfig {

  @Bean
  public ClientFactoryFactory clientFactoryFactory() {
    return new ClientFactoryFactoryImpl();
  }

  @Bean
  public DdfRestClientFactory ddfRestClientFactory(ClientFactoryFactory clientFactoryFactory) {
    return new DdfRestClientFactory(clientFactoryFactory);
  }

  @Bean
  public DdfNodeAdapterFactory ddfNodeAdapterFactory(
      DdfRestClientFactory ddfRestClientFactory, ClientFactoryFactory clientFactoryFactory) {
    return new DdfNodeAdapterFactory(ddfRestClientFactory, clientFactoryFactory);
  }
}
