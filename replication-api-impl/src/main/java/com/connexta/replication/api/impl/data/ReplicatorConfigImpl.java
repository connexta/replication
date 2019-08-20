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

import com.connexta.replication.api.data.ReplicatorConfig;
import com.connexta.replication.api.impl.persistence.pojo.ConfigPojo;
import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import javax.annotation.Nullable;

/**
 * ReplicatorConfigImpl represents a replication config and has methods that allow it to easily be
 * converted to, or from, a map.
 */
public class ReplicatorConfigImpl extends AbstractPersistable<ConfigPojo>
    implements ReplicatorConfig {
  private static final String TYPE = "replicator config";

  private String name;

  private boolean bidirectional;

  private String source;

  private String destination;

  private String filter;

  @Nullable private String description;

  private boolean suspended;

  @Nullable private Instant lastMetadataModified;

  public ReplicatorConfigImpl() {
    super(ReplicatorConfigImpl.TYPE);
  }

  protected ReplicatorConfigImpl(ConfigPojo pojo) {
    super(ReplicatorConfigImpl.TYPE, null);
    readFrom(pojo);
  }

  @Override
  public String getName() {
    return name;
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
  public String getFilter() {
    return filter;
  }

  @Override
  public boolean isBidirectional() {
    return bidirectional;
  }

  @Nullable
  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean isSuspended() {
    return suspended;
  }

  @Override
  public void setLastMetadataModified(Instant lastMetadataModified) {
    this.lastMetadataModified = lastMetadataModified;
  }

  @Nullable
  @Override
  public Instant getLastMetadataModified() {
    return lastMetadataModified;
  }

  @Override
  protected ConfigPojo writeTo(ConfigPojo pojo) {
    super.writeTo(pojo);
    return pojo.setVersion(ConfigPojo.CURRENT_VERSION)
        .setName(name)
        .setSource(source)
        .setDestination(destination)
        .setFilter(filter)
        .setDescription(description)
        .setSuspended(suspended)
        .setBidirectional(bidirectional)
        .setLastMetadataModified(lastMetadataModified);
  }

  @Override
  protected void readFrom(ConfigPojo pojo) {
    super.readFrom(pojo);
    setOrFailIfNullOrEmpty("name", pojo::getName, this::setName);
    setOrFailIfNullOrEmpty("source", pojo::getSource, this::setSource);
    setOrFailIfNullOrEmpty("destination", pojo::getDestination, this::setDestination);
    setOrFailIfNullOrEmpty("filter", pojo::getFilter, this::setFilter);
    this.description = pojo.getDescription();
    this.suspended = pojo.isSuspended();
    this.bidirectional = pojo.isBidirectional();
    this.lastMetadataModified = pojo.getLastMetadataModified();
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setSource(String source) {
    this.source = source;
  }

  @VisibleForTesting
  void setDestination(String destination) {
    this.destination = destination;
  }

  @VisibleForTesting
  void setDescription(String description) {
    this.description = description;
  }

  @VisibleForTesting
  void setFilter(String filter) {
    this.filter = filter;
  }

  @VisibleForTesting
  void setBidirectional(boolean bidirectional) {
    this.bidirectional = bidirectional;
  }

  @VisibleForTesting
  void setSuspended(boolean suspended) {
    this.suspended = suspended;
  }
}
