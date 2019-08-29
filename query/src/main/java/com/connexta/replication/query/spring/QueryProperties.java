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
package com.connexta.replication.query.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Reads fields specific to the query service from the application configuration. */
@Component
@ConfigurationProperties("query")
public class QueryProperties {

  // TODO: move the default site polling period here

  /**
   * The number of seconds to wait before the {@link com.connexta.replication.query.QueryManager}
   * will reload the site configs and update the {@link
   * com.connexta.replication.query.QueryService}s accordingly.
   */
  private long serviceRefreshPeriod;

  public long getServiceRefreshPeriod() {
    return serviceRefreshPeriod;
  }

  public void setServiceRefreshPeriod(long serviceRefreshPeriod) {
    this.serviceRefreshPeriod = serviceRefreshPeriod;
  }
}
