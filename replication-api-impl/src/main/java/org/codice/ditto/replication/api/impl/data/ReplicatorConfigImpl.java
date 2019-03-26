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

  /**
   * 0/No Version - initial version of configs which were saved in the catalog framework. 1 - the
   * first version of configs to be saved in the replication persistent store.
   */
  public static final int CURRENT_VERSION = 1;

  private String name;

  private boolean bidirectional;

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
    result.put(BIDIRECTIONAL_KEY, Boolean.toString(isBidirectional()));
    result.put(RETRY_COUNT_KEY, getFailureRetryCount());
    result.put(DESCRIPTION_KEY, getDescription());
    result.put(SUSPENDED_KEY, Boolean.toString(isSuspended()));
    return result;
  }

  @Override
  public void fromMap(Map<String, Object> properties) {
    super.fromMap(properties);
    setName((String) properties.get(NAME_KEY));
    setSource((String) properties.get(SOURCE_KEY));
    setDestination((String) properties.get(DESTINATION_KEY));
    setFilter((String) properties.get(FILTER_KEY));
    setBidirectional(Boolean.valueOf((String) properties.get(BIDIRECTIONAL_KEY)));
    setFailureRetryCount((int) properties.get(RETRY_COUNT_KEY));
    setDescription((String) properties.get(DESCRIPTION_KEY));
    setSuspended(Boolean.valueOf((String) properties.get(SUSPENDED_KEY)));
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
  public boolean isBidirectional() {
    return bidirectional;
  }

  @Override
  public void setBidirectional(boolean bidirectional) {
    this.bidirectional = bidirectional;
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
}
