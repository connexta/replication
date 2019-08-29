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
package com.connexta.replication.query.cmp;

import com.connexta.replication.api.impl.data.FilterIndexManagerImpl;
import com.connexta.replication.api.impl.data.FilterManagerImpl;
import com.connexta.replication.api.impl.data.SiteManagerImpl;
import com.connexta.replication.api.impl.persistence.spring.FilterIndexRepository;
import com.connexta.replication.api.impl.persistence.spring.FilterRepository;
import com.connexta.replication.api.impl.persistence.spring.SiteRepository;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.SiteManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO: replace this with the config created for the query service when replication-api-impl
// is split up
@Configuration
public class ComponentTestConfig {

  @Bean
  public SiteManager siteManager(SiteRepository siteRepository) {
    return new SiteManagerImpl(siteRepository);
  }

  /**
   * Used to create a {@link FilterManager} bean which provides an abstraction layer for CRUD
   * operations involving {@link com.connexta.replication.api.data.Filter}s.
   *
   * @param filterRepository a {@link org.springframework.data.repository.CrudRepository} for basic
   *     CRUD operations
   * @param filterIndexManager the {@link FilterIndexManager}
   * @return the {@link FilterManager}
   */
  @Bean
  public FilterManager filterManager(
      FilterRepository filterRepository, FilterIndexManager filterIndexManager) {
    return new FilterManagerImpl(filterRepository, filterIndexManager);
  }

  @Bean
  public FilterIndexManager filterIndexManager(FilterIndexRepository filterIndexRepository) {
    return new FilterIndexManagerImpl(filterIndexRepository);
  }
}
