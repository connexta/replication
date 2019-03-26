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

import java.util.Map;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.data.ReplicatorConfig;

/**
 * ReplicatorConfigImpl represents a replication config and has methods that allow it to easily be
 * converted to, or from, a map.
 */
public class ReplicatorConfigImpl extends AbstractPersistable implements ReplicatorConfig {

  // public so that the persistent store can access it using reflection
  public static final String PERSISTENCE_TYPE = "replication_config";

  private static final String NAME_KEY = "name";

  private static final String SOURCE_KEY = "source";

  private static final String DESTINATION_KEY = "destination";

  private static final String FILTER_KEY = "filter";

  private static final String RETRY_COUNT_KEY = "retry_count";

  private static final String BIDIRECTIONAL_KEY = "bidirectional";

  private static final String DESCRIPTION_KEY = "description";

  private static final String SUSPENDED_KEY = "suspended";

  private static final String DELETED_KEY = "deleted";

  private static final String DELETE_DATA_KEY = "deleteData";

  /**
   * Field specifying the version of the configuration. Possible versions include:
   *
   * <ol>
   *   <li>0 (No version) - initial version of configs which were saved in the catalog framework
   *   <li>1 - The first version of configs to be saved in the replication persistent store
   *       <ul>
   *         <li>Add <b>suspended</b> field of type boolean
   *         <li>Add <b>deleted</b> field of type boolean
   *       </ul>
   * </ol>
   */
  public static final int CURRENT_VERSION = 1;

  private boolean deleted = false;

  private boolean deleteData = false;

  private String name;

  private Direction direction;

  private String source;

  private String destination;

  private String filter;

  private int failureRetryCount;

  private String description;

  private boolean suspended;

  public ReplicatorConfigImpl() {
    super.setVersion(CURRENT_VERSION);
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> result = super.toMap();
    result.put(NAME_KEY, getName());
    result.put(SOURCE_KEY, getSource());
    result.put(DESTINATION_KEY, getDestination());
    result.put(FILTER_KEY, getFilter());
    result.put(BIDIRECTIONAL_KEY, Boolean.toString(isBiDirectional()));
    result.put(RETRY_COUNT_KEY, getFailureRetryCount());
    result.put(DESCRIPTION_KEY, getDescription());
    result.put(SUSPENDED_KEY, Boolean.toString(isSuspended()));
    result.put(DELETED_KEY, Boolean.toString(isDeleted()));
    result.put(DELETE_DATA_KEY, Boolean.toString(deleteData()));
    return result;
  }

  @Override
  public void fromMap(Map<String, Object> properties) {
    super.fromMap(properties);
    setName((String) properties.get(NAME_KEY));
    setSource((String) properties.get(SOURCE_KEY));
    setDestination((String) properties.get(DESTINATION_KEY));
    setFilter((String) properties.get(FILTER_KEY));
    setBiDirectional(Boolean.valueOf((String) properties.get(BIDIRECTIONAL_KEY)));
    setFailureRetryCount((int) properties.get(RETRY_COUNT_KEY));
    setDescription((String) properties.get(DESCRIPTION_KEY));
    setSuspended(Boolean.valueOf((String) properties.get(SUSPENDED_KEY)));
    setDeleted(Boolean.valueOf((String) properties.get(DELETED_KEY)));
    setDeleteData(Boolean.valueOf((String) properties.get(DELETE_DATA_KEY)));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Direction getDirection() {
    return direction;
  }

  @Override
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  @Override
  public ReplicationType getReplicationType() {
    return ReplicationType.RESOURCE;
  }

  @Override
  public void setReplicationType(ReplicationType type) {
    // no op
  }

  @Override
  public String getSource() {
    return source;
  }

  @Override
  public void setSource(String source) {
    this.source = source;
  }

  @Override
  public String getDestination() {
    return destination;
  }

  @Override
  public void setDestination(String destination) {
    this.destination = destination;
  }

  @Override
  public String getFilter() {
    return filter;
  }

  @Override
  public void setFilter(String filter) {
    this.filter = filter;
  }

  @Override
  public boolean isBiDirectional() {
    return direction == Direction.BOTH;
  }

  @Override
  public void setBiDirectional(boolean biDirectional) {
    direction = biDirectional ? Direction.BOTH : Direction.PUSH;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public int getFailureRetryCount() {
    return failureRetryCount;
  }

  @Override
  public void setFailureRetryCount(int count) {
    failureRetryCount = count;
  }

  @Override
  public boolean isSuspended() {
    return suspended;
  }

  @Override
  public void setSuspended(boolean suspended) {
    this.suspended = suspended;
  }

  @Override
  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public boolean deleteData() {
    return deleteData;
  }

  @Override
  public void setDeleteData(boolean deleteData) {
    this.deleteData = deleteData;
  }
}
