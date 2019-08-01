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

import com.connexta.ion.replication.api.data.ReplicatorConfig;
import java.util.Date;
import javax.annotation.Nullable;
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

  @Indexed(name = "name")
  private String name;

  @Indexed(name = "bidirectional")
  private boolean bidirectional;

  @Indexed(name = "source")
  private String source;

  @Indexed(name = "destination")
  private String destination;

  @Indexed(name = "filter")
  private String filter;

  @Indexed(name = "description")
  private String description;

  @Indexed(name = "suspended")
  private boolean suspended;

  @Indexed(name = "last_metadata_modified")
  @Nullable
  private Date lastMetadataModified;

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
  public boolean isSuspended() {
    return suspended;
  }

  @Override
  public void setSuspended(boolean suspended) {
    this.suspended = suspended;
  }

  @Override
  public void setLastMetadataModified(Date lastMetadataModified) {
    this.lastMetadataModified = lastMetadataModified;
  }

  @Nullable
  @Override
  public Date getLastMetadataModified() {
    return lastMetadataModified;
  }
}
