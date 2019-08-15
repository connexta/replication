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
package com.connexta.replication.api.impl.data;

import com.connexta.ion.replication.api.Action;
import com.connexta.ion.replication.api.Status;
import com.connexta.replication.api.data.ReplicationItem;
import com.connexta.replication.api.impl.persistence.pojo.ReplicationItemPojo;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

public class ReplicationItemImpl extends AbstractPersistable<ReplicationItemPojo>
    implements ReplicationItem {
  private static final String TYPE = "replication item";

  private String metadataId;

  private Date resourceModified;

  private Date metadataModified;

  private Date doneTime;

  private String source;

  private String destination;

  private String configId;

  private long metadataSize;

  private long resourceSize;

  private Date startTime;

  private Status status;

  private Action action;

  /** Instantiates a default replication item. */
  public ReplicationItemImpl() {
    super(ReplicationItemImpl.TYPE);
  }

  protected ReplicationItemImpl(ReplicationItemPojo pojo) {
    super(ReplicationItemImpl.TYPE);
    readFrom(pojo);
  }

  private ReplicationItemImpl(Builder builder) {
    super(ReplicationItemImpl.TYPE, builder.id);
    this.metadataId = builder.metadataId;
    this.resourceModified = builder.resourceModified;
    this.metadataModified = builder.metadataModified;
    this.source = builder.source;
    this.destination = builder.destination;
    this.configId = builder.configId;
    this.metadataSize = builder.metadataSize;
    this.resourceSize = builder.resourceSize;
    this.status = builder.status;
    this.startTime = builder.startTime;
    this.doneTime = builder.doneTime;
    this.action = builder.action;
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
  public Date getDoneTime() {
    return doneTime;
  }

  @Override
  public long getDuration() {
    return doneTime.getTime() - startTime.getTime();
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public Action getAction() {
    return action;
  }

  @Override
  public double getResourceTransferRate() {
    if (getDuration() > 0 && getResourceSize() != 0) {
      double bytesPerMs = (double) getDuration() / getResourceSize();
      return toBytesPerSec(bytesPerMs);
    }
    return 0.0D;
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
        "ReplicationItemImpl{id=%s, metadataId=%s, resourceModified=%s, metadataModified=%s, doneTime=%s, source=%s, destination=%s, configId=%s, metadataSize=%d, resourceSize=%d, startTime=%s, status=%s, action=%s}",
        getId(),
        metadataId,
        resourceModified,
        metadataModified,
        doneTime,
        source,
        destination,
        configId,
        metadataSize,
        resourceSize,
        startTime,
        status,
        action);
  }

  @Override
  protected ReplicationItemPojo writeTo(ReplicationItemPojo pojo) {
    super.writeTo(pojo);
    return pojo.setMetadataId(metadataId)
        .setConfigId(configId)
        .setSource(source)
        .setDestination(destination)
        .setStartTime(startTime)
        .setDoneTime(doneTime)
        .setResourceModified(resourceModified)
        .setResourceSize(resourceSize)
        .setMetadataModified(metadataModified)
        .setMetadataSize(metadataSize)
        .setAction(action.name())
        .setStatus(status.name());
  }

  @Override
  protected void readFrom(ReplicationItemPojo pojo) {
    super.readFrom(pojo);
    setOrFailIfNullOrEmpty("metadataId", pojo::getMetadataId, this::setMetadataId);
    setOrFailIfNullOrEmpty("configId", pojo::getConfigId, this::setConfigId);
    setOrFailIfNullOrEmpty("source", pojo::getSource, this::setSource);
    setOrFailIfNullOrEmpty("destination", pojo::getDestination, this::setDestination);
    setOrFailIfNull("startTime", pojo::getStartTime, this::setStartTime);
    setOrFailIfNull("doneTime", pojo::getDoneTime, this::setDoneTime);
    this.resourceModified = pojo.getResourceModified();
    this.resourceSize = pojo.getResourceSize();
    this.metadataModified = pojo.getMetadataModified();
    this.metadataSize = pojo.getMetadataSize();
    convertAndSetEnumValue(Action.class, Action.UNKNOWN, pojo::getAction, this::setAction);
    convertAndSetEnumValue(Status.class, Status.FAILURE, pojo::getStatus, this::setStatus);
  }

  private void setMetadataId(String metadataId) {
    this.metadataId = metadataId;
  }

  private void setConfigId(String configId) {
    this.configId = configId;
  }

  private void setSource(String source) {
    this.source = source;
  }

  private void setDestination(String destination) {
    this.destination = destination;
  }

  private void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  private void setDoneTime(Date doneTime) {
    this.doneTime = doneTime;
  }

  private void setAction(Action action) {
    this.action = action;
  }

  private void setStatus(Status status) {
    this.status = status;
  }

  private double toBytesPerSec(double bytesPerMs) {
    return bytesPerMs / 1000;
  }

  /** Builder class for creating {@link ReplicationItemImpl}s. */
  public static class Builder {

    private final String id;

    private final String configId;

    private final String source;

    private final String destination;

    private final String metadataId;

    private Date resourceModified = null;

    private Date metadataModified = null;

    private long metadataSize = 0;

    private long resourceSize = 0;

    private Date startTime = null;

    private Date doneTime = null;

    private Status status;

    private Action action;

    /**
     * @param metadataId id of the {@link ReplicationItem}
     * @param configId replicator id the replication item is associated with
     * @param source the source the item comes from
     * @param destination the destination the item was sent to
     */
    public Builder(String metadataId, String configId, String source, String destination) {
      this.id = UUID.randomUUID().toString();
      this.metadataId = Validate.notEmpty(metadataId);
      this.configId = Validate.notEmpty(configId);
      this.source = Validate.notEmpty(source);
      this.destination = Validate.notEmpty(destination);
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

    public Builder markStartTime() {
      this.startTime = new Date();
      return this;
    }

    public Builder markDoneTime() {
      if (startTime == null) {
        throw new IllegalStateException(
            "Argument startTime must be set before doneTime can be set");
      }

      this.doneTime = new Date();
      return this;
    }

    public Builder action(Action action) {
      this.action = action;
      return this;
    }

    public ReplicationItem build() {
      Validate.notNull(metadataModified, "metadataModified cannot be null");
      Validate.notNull(startTime, "startTime cannot be null");
      Validate.notNull(doneTime, "doneTime cannot be null");
      Validate.notNull(action, "action cannot be null");
      Validate.notNull(status, "status cannot be null");

      if (resourceModified != null && resourceSize == 0) {
        throw new IllegalStateException(
            "resourceModified was provided, but the resourceSize was not updated");
      }
      if (resourceSize > 0 && resourceModified == null) {
        throw new IllegalStateException(
            "resourceSize was provided, but resourceModified was not updated");
      }
      return new ReplicationItemImpl(this);
    }
  }
}
