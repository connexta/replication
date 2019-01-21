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
package org.codice.ditto.replication.api.impl;

import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.codice.ditto.replication.api.ReplicationItem;

public class ReplicationItemImpl implements ReplicationItem {

  private final String metacardId;

  private final Date resourceModified;

  private final Date metacardModified;

  private final String source;

  private final String destination;

  private final String configurationId;

  private int failureCount;

  public ReplicationItemImpl(
      String metacardId,
      Date resourceModified,
      Date metacardModified,
      String source,
      String destination,
      String configId) {
    this(metacardId, resourceModified, metacardModified, source, destination, configId, 0);
  }

  public ReplicationItemImpl(
      String metacardId,
      Date resourceModified,
      Date metacardModified,
      String source,
      String destination,
      String configId,
      int failureCount) {
    this.metacardId = notBlank(metacardId);
    // TODO these dates don't matter for delete requests that fail. Need to make a way to
    // instantiate a failed ReplicationItem for failed deletes.
    //    this.resourceModified = notNull(resourceModified);
    //    this.metacardModified = notNull(metacardModified);
    this.resourceModified = resourceModified;
    this.metacardModified = metacardModified;
    this.source = notBlank(source);
    this.destination = notBlank(destination);
    this.configurationId = configId;
    this.failureCount = failureCount;
  }

  private static String notBlank(String s) {
    if (StringUtils.isNotBlank(s)) {
      return s;
    } else {
      throw new IllegalArgumentException("String argument may not be empty");
    }
  }

  @Override
  public String getMetacardId() {
    return metacardId;
  }

  @Override
  public Date getResourceModified() {
    return resourceModified;
  }

  @Override
  public Date getMetacardModified() {
    return metacardModified;
  }

  @Override
  public String getSource() {
    return source;
  }

  @Override
  public String getDestination() {
    return destination;
  }

  @Override
  public String getConfigurationId() {
    return configurationId;
  }

  @Override
  public int getFailureCount() {
    return failureCount;
  }

  @Override
  public void incrementFailureCount() {
    failureCount++;
  }
}
