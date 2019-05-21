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

import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * ReplicatorConfigImpl represents a replication config and has methods that allow it to easily be
 * converted to, or from, a map.
 */
@SolrDocument(collection = "replication_config")
public class ReplicatorConfigImpl extends AbstractPersistable implements ReplicatorConfig {

  /**
   * 0/No Version - initial version of configs which were saved in the catalog framework. 1 - the
   * first version of configs to be saved in the replication persistent store.
   */
  public static final int CURRENT_VERSION = 1;

  @Indexed(name = "name_txt")
  private String name;

  @Indexed(name = "bidirectional_b")
  private boolean bidirectional;

  @Indexed(name = "source_txt")
  private String source;

  @Indexed(name = "destination_txt")
  private String destination;

  @Indexed(name = "filter_txt")
  private String filter;

  @Indexed(name = "retry_count_int")
  private int failureRetryCount;

  @Indexed(name = "description_txt")
  private String description;

  @Indexed(name = "suspended_b")
  private boolean suspended;

  public ReplicatorConfigImpl() {
    super.setVersion(CURRENT_VERSION);
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
