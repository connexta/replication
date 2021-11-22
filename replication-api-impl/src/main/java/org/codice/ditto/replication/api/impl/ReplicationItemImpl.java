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
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.codice.ditto.replication.api.ReplicationItem;

public class ReplicationItemImpl implements ReplicationItem {

  private final String id;

  private final String metadataId;

  private final Date resourceModified;

  private final Date metadataModified;

  private final String source;

  private final String destination;

  private final String configurationId;

  private int failureCount;

  public ReplicationItemImpl(
      String metadataId,
      Date resourceModified,
      Date metadataModified,
      String source,
      String destination,
      String configId) {
    this(null, metadataId, resourceModified, metadataModified, source, destination, configId, 0);
  }

  public ReplicationItemImpl(
      String id,
      String metadataId,
      Date resourceModified,
      Date metadataModified,
      String source,
      String destination,
      String configId) {
    this(id, metadataId, resourceModified, metadataModified, source, destination, configId, 0);
  }

  public ReplicationItemImpl(
      String metadataId,
      Date resourceModified,
      Date metadataModified,
      String source,
      String destination,
      String configId,
      int failureCount) {
    this(
        null,
        metadataId,
        resourceModified,
        metadataModified,
        source,
        destination,
        configId,
        failureCount);
  }

  public ReplicationItemImpl(
      String id,
      String metadataId,
      Date resourceModified,
      Date metadataModified,
      String source,
      String destination,
      String configId,
      int failureCount) {
    this.metadataId = notBlank(metadataId);
    // TODO these dates don't matter for delete requests that fail. Need to make a way to
    // instantiate a failed ReplicationItem for failed deletes.
    //    this.resourceModified = notNull(resourceModified);
    //    this.metadataModified = notNull(metadataModified);
    this.resourceModified = resourceModified;
    this.metadataModified = metadataModified;
    this.source = notBlank(source);
    this.destination = notBlank(destination);
    this.configurationId = configId;
    this.failureCount = failureCount;
    if (id == null) {
      this.id = UUID.randomUUID().toString();
    } else {
      this.id = id;
    }
  }

  private static String notBlank(String s) {
    if (StringUtils.isNotBlank(s)) {
      return s;
    } else {
      throw new IllegalArgumentException("String argument may not be empty");
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getMetadataId() {
    return metadataId;
  }

  @Override
  public Date getResourceModified() {
    return resourceModified;
  }

  @Override
  public Date getMetadataModified() {
    return metadataModified;
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

  @Override
  public String toString() {
    return String.format(
        "ReplicationItemImpl{id=%s, metacardId=%s, resourceModified=%s, metadataModified=%s, sourceName=%s, destinationName=%s, replicatorConfigId=%s}",
        id, metadataId, resourceModified, metadataModified, source, destination, configurationId);
  }
}
