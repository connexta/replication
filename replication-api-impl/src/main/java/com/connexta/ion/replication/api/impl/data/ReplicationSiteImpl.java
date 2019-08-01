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

import com.connexta.ion.replication.api.data.ReplicationSite;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * ReplicationSiteImpl represents a replication site and has methods that allow it to easily be
 * converted to, or from, a map.
 */
@SolrDocument(collection = "replication_site")
public class ReplicationSiteImpl extends AbstractPersistable implements ReplicationSite {

  /**
   * List of possible versions:
   *
   * <ul>
   *   <li>1 - initial version.
   *   <li>2 - Adds
   *       <ul>
   *         <li>is-remote-managed field of type boolean, defaults to false
   *       </ul>
   * </ul>
   */
  public static final int CURRENT_VERSION = 2;

  @Indexed(name = "remote_managed")
  private boolean isRemoteManaged = false;

  @Indexed(name = "name")
  private String name;

  @Indexed(name = "url")
  private String url;

  @Indexed(name = "type")
  private String type;

  public ReplicationSiteImpl() {
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
  public String getUrl() {
    return url;
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public void setRemoteManaged(boolean remoteManaged) {
    this.isRemoteManaged = remoteManaged;
  }

  @Override
  public boolean isRemoteManaged() {
    return isRemoteManaged;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }
}
