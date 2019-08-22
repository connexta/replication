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
package com.connexta.replication.spring;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This class is a bean which reads from the application properties and populates the fields it
 * contains based on the prefix and the name of the field. For example, if the application is given
 * an application.yml containing the following values:
 *
 * <pre>
 *  replication:
 *    period: 60
 *    connectionTimeout: 30
 *    receiveTimeout: 60
 * </pre>
 *
 * Then the fields below with the corresponding names will be given their respective value.
 */
@Component
@ConfigurationProperties(prefix = "replication")
public class ReplicationProperties {

  /** Sets of destination sites to be handled by this replicator or none to handle all. */
  private final Set<String> sites = new HashSet<>();

  /** The interval, in seconds, on which to execute replication jobs */
  private long period;

  /** The connection timeout, in seconds, for any requests made by replication */
  private int connectionTimeout;

  /** The receive timeout, in seconds, for any requests made by replication */
  private int receiveTimeout;

  public long getPeriod() {
    return period;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  // We let the user set the timeouts in seconds for readability but most clients take
  // timeouts in milliseconds so we'll go ahead and do the conversion for them here.
  public int getConnectionTimeout() {
    if (connectionTimeout > 0) {
      return Math.toIntExact(TimeUnit.SECONDS.toMillis(connectionTimeout));
    } else {
      return 30000;
    }
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public int getReceiveTimeout() {
    if (receiveTimeout > 0) {
      return Math.toIntExact(TimeUnit.SECONDS.toMillis(receiveTimeout));
    } else {
      return 60000;
    }
  }

  public void setReceiveTimeout(int receiveTimeout) {
    this.receiveTimeout = receiveTimeout;
  }

  /**
   * Gets the internal set of all destination site identifiers handled by this replicator or empty
   * to have all sites handled.
   *
   * @return the internal set of all destination sites or empty to have all sites handled
   */
  public Set<String> getSites() {
    return sites;
  }

  /**
   * Gets the destination site identifiers handled by this replicator or empty to have all sites
   * handled.
   *
   * @return the destination sites or empty to have all sites handled
   */
  public Stream<String> sites() {
    return sites.stream();
  }
}
