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
package com.connexta.replication.api.impl.worker;

import com.connexta.replication.api.impl.data.SiteManagerImpl;
import com.connexta.replication.api.impl.persistence.spring.SiteRepository;
import com.connexta.replication.api.persistence.SiteManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO: replace this with the config created for the worker manager when replication-api-impl
// is split up
@Configuration
public class ComponentTestConfig {

  @Bean
  public SiteManager siteManager(SiteRepository siteRepository) {
    return new SiteManagerImpl(siteRepository);
  }
}
