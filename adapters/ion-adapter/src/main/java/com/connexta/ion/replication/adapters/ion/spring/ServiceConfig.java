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
package com.connexta.ion.replication.adapters.ion.spring;

import com.connexta.ion.replication.adapters.ion.IonNodeAdapter;
import com.connexta.ion.replication.adapters.ion.IonNodeAdapterFactory;
import com.connexta.ion.replication.spring.ReplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/** A class for instantiating beans in this module */
@Configuration("ion-adapter")
public class ServiceConfig {

  /**
   * Instantiates an {@link IonNodeAdapterFactory} bean.
   *
   * @param replicationProperties application properties containing the timeouts for any client this
   *     factory creates
   * @return A factory for creating {@link IonNodeAdapter}s
   */
  @Bean
  public IonNodeAdapterFactory ionNodeAdapterFactory(ReplicationProperties replicationProperties) {
    return new IonNodeAdapterFactory(
        new SimpleClientHttpRequestFactory(),
        replicationProperties.getConnectionTimeout(),
        replicationProperties.getReceiveTimeout());
  }
}
