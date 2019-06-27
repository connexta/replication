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
package com.connexta.ion.replication.adapters.ddf.spring;

import com.connexta.ion.replication.adapters.ddf.DdfNodeAdapter;
import com.connexta.ion.replication.adapters.ddf.DdfNodeAdapterFactory;
import com.connexta.ion.replication.spring.ReplicationProperties;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.ddf.cxf.client.impl.ClientFactoryFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** A class for instantiating beans in this module */
@Configuration("ddf-adapter")
public class ServiceConfig {

  @Bean
  public ClientFactoryFactory clientFactoryFactory() {
    return new ClientFactoryFactoryImpl();
  }

  /**
   * Instantiates a {@link DdfNodeAdapterFactory} bean.
   *
   * @param clientFactoryFactory a factory for {@link SecureCxfClientFactory}s
   * @param replicationProperties application properties used to obtain the timeouts for any clients
   *     created by this bean
   * @return A factory for creating {@link DdfNodeAdapter}s
   */
  @Bean
  public DdfNodeAdapterFactory ddfNodeAdapterFactory(
      ClientFactoryFactory clientFactoryFactory, ReplicationProperties replicationProperties) {
    return new DdfNodeAdapterFactory(
        clientFactoryFactory,
        replicationProperties.getConnectionTimeout(),
        replicationProperties.getReceiveTimeout());
  }
}
