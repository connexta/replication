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
package org.codice.ditto.replication.api.impl.data;

import java.net.URL;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.ReplicatorConfig;

public class ReplicatorConfigImpl implements ReplicatorConfig {

  private String id;

  private String name;

  private Direction direction;

  private ReplicationType type;

  private URL url;

  private String cql;

  private String description;

  private int failureRetryCount;

  public ReplicatorConfigImpl() {}

  public ReplicatorConfigImpl(ReplicatorConfig config) {
    this.id = config.getId();
    this.name = config.getName();
    this.direction = config.getDirection();
    this.type = config.getReplicationType();
    this.url = config.getUrl();
    this.cql = config.getCql();
    this.description = config.getDescription();
    this.failureRetryCount = config.getFailureRetryCount();
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  @Override
  public ReplicationType getReplicationType() {
    return type;
  }

  public void setReplicationType(ReplicationType type) {
    this.type = type;
  }

  @Override
  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  @Override
  public String getCql() {
    return cql;
  }

  public void setCql(String cql) {
    this.cql = cql;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public int getFailureRetryCount() {
    return failureRetryCount;
  }

  public void setFailureRetryCount(int count) {
    failureRetryCount = count;
  }
}
