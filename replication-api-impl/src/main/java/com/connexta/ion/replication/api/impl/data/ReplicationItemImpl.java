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
package com.connexta.ion.replication.api.impl.data;

import com.connexta.ion.replication.api.ReplicationItem;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(collection = "replication_item")
public class ReplicationItemImpl implements ReplicationItem {

  @Id
  @Indexed(name = "id_txt")
  private String id;

  @Indexed(name = "resource-modified_tdt")
  private Date resourceModified;

  @Indexed(name = "metacard-modified_tdt")
  private Date metadataModified;

  @Indexed(name = "source_txt")
  private String source;

  @Indexed(name = "destination_txt")
  private String destination;

  @Indexed(name = "config-id_txt")
  private String configId;

  @Indexed(name = "failure-count_int")
  private int failureCount;

  public ReplicationItemImpl() {}

  public ReplicationItemImpl(
      String metadataId,
      Date resourceModified,
      Date metadataModified,
      String source,
      String destination,
      String configId) {
    this(metadataId, resourceModified, metadataModified, source, destination, configId, 0);
  }

  public ReplicationItemImpl(
      String metadataId,
      Date resourceModified,
      Date metadataModified,
      String source,
      String destination,
      String configId,
      int failureCount) {
    this.id = notBlank(metadataId);
    this.resourceModified = resourceModified;
    this.metadataModified = metadataModified;
    this.source = notBlank(source);
    this.destination = notBlank(destination);
    this.configId = configId;
    this.failureCount = failureCount;
  }

  private static String notBlank(String s) {
    if (!StringUtils.isBlank(s)) {
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
  public String getConfigId() {
    return configId;
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
        "ReplicationItemImpl{id=%s, resourceModified=%s, metadataModified=%s, sourceName=%s, destinationName=%s, replicatorConfigId=%s}",
        id, resourceModified, metadataModified, source, destination, configId);
  }
}
