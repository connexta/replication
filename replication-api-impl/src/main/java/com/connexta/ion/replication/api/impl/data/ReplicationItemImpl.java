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

import static org.apache.commons.lang3.Validate.notEmpty;

import com.connexta.ion.replication.api.ReplicationItem;
import com.connexta.ion.replication.api.Status;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(collection = "replication_item")
public class ReplicationItemImpl implements ReplicationItem {

  private static final String LONG = "_lng";

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

  @Indexed(name = "metadata-size" + LONG)
  private long metadataSize;

  @Indexed(name = "resource-size" + LONG)
  private long resourceSize;

  @Indexed(name = "start-time_tdt")
  private Date startTime;

  @Indexed(name = "duration" + LONG)
  private long duration = -1;

  @Indexed(name = "status_txt", type = "string")
  private Status status;

  /** This default ctor is needed for spring-solr to instantiate an item when querying the solr */
  public ReplicationItemImpl() {}

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
  public long getResourceSize() {
    return resourceSize;
  }

  @Override
  public long getMetadataSize() {
    return metadataSize;
  }

  @Override
  public Date getStartTime() {
    return startTime;
  }

  @Override
  public void markStartTime() {
    startTime = new Date();
  }

  @Override
  public void markDoneTime() {
    duration = new Date().getTime() - startTime.getTime();
  }

  @Override
  public long getDuration() {
    return duration;
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public double getTransferRate() {
    if (duration > 0 && resourceSize != -1.0D) {
      return (double) resourceSize / TimeUnit.MILLISECONDS.toSeconds(duration);
    }
    return -1.0D;
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
  public String toString() {
    return String.format(
        "ReplicationItemImpl{id=%s, resourceModified=%s, metadataModified=%s, sourceName=%s, destinationName=%s, configId=%s, metadataSize=%d, resourceSize=%d, startTime=%s, durationMs=%d, status=%s}",
        id,
        resourceModified,
        metadataModified,
        source,
        destination,
        configId,
        metadataSize,
        resourceSize,
        startTime,
        duration,
        status);
  }

  /**
   * Copies fields from a {@link ReplicationItem} to a new item.
   *
   * @param item item to copy
   * @return a builder for the new copied item
   */
  public static Builder from(ReplicationItem item) {
    return new Builder(item.getId(), item.getConfigId(), item.getSource(), item.getDestination())
        .resourceModified(item.getResourceModified())
        .metadataModified(item.getMetadataModified())
        .metadataSize(item.getMetadataSize())
        .resourceSize(item.getResourceSize())
        .status(item.getStatus())
        .duration(item.getDuration())
        .startTime(item.getStartTime());
  }

  private ReplicationItemImpl(Builder builder) {
    this.id = builder.id;
    this.resourceModified = builder.resourceModified;
    this.metadataModified = builder.metadataModified;
    this.source = builder.source;
    this.destination = builder.destination;
    this.configId = builder.configId;
    this.metadataSize = builder.metadataSize;
    this.resourceSize = builder.resourceSize;
    this.status = builder.status;
    this.duration = builder.duration;
    this.startTime = builder.startTime;
  }

  /** Builder class for creating {@link ReplicationItemImpl}s. */
  public static class Builder {

    private final String id;

    private final String configId;

    private final String source;

    private final String destination;

    private Date resourceModified = null;

    private Date metadataModified = null;

    private long metadataSize = -1;

    private long resourceSize = -1;

    private Date startTime;

    private long duration = -1;

    private Status status;

    /**
     * @param id id of the {@link ReplicationItem}
     * @param configId replicator id the replication item is associated with
     * @param source the source the item comes from
     * @param destination the destination the item was sent to
     */
    public Builder(String id, String configId, String source, String destination) {
      this.id = notEmpty(id);
      this.configId = notEmpty(configId);
      this.source = notEmpty(source);
      this.destination = notEmpty(destination);
    }

    public Builder resourceModified(Date date) {
      this.resourceModified = date;
      return this;
    }

    public Builder metadataModified(Date date) {
      this.metadataModified = date;
      return this;
    }

    public Builder metadataSize(long size) {
      this.metadataSize = size;
      return this;
    }

    public Builder resourceSize(long size) {
      this.resourceSize = size;
      return this;
    }

    public Builder status(Status status) {
      this.status = status;
      return this;
    }

    public ReplicationItem build() {
      return new ReplicationItemImpl(this);
    }

    Builder startTime(Date startTime) {
      this.startTime = startTime;
      return this;
    }

    Builder duration(long duration) {
      this.duration = duration;
      return this;
    }
  }
}
