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
import org.codice.ditto.replication.api.data.ReplicationSite;

/**
 * ReplicationSiteImpl represents a replication site and has methods that allow it to easily be
 * converted to, or from, a map.
 */
public class ReplicationSiteImpl extends AbstractPersistable implements ReplicationSite {

  // public so that the persistent store can access it using reflection
  public static final String PERSISTENCE_TYPE = "replication_site";

  private static final String NAME_KEY = "name";

  private static final String URL_KEY = "url";

  private static final String IS_REMOTE_MANAGED_KEY = "is-remote-managed";

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

  private boolean isRemoteManaged = false;

  private String name;

  private String url;

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
  public void setIsRemoteManaged(boolean isRemoteManaged) {
    this.isRemoteManaged = isRemoteManaged;
  }

  @Override
  public boolean isRemoteManaged() {
    return isRemoteManaged;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> result = super.toMap();
    result.put(NAME_KEY, getName());
    result.put(URL_KEY, getUrl());
    result.put(IS_REMOTE_MANAGED_KEY, isRemoteManaged());
    return result;
  }

  @Override
  public void fromMap(Map<String, Object> properties) {
    super.fromMap(properties);
    setName((String) properties.get(NAME_KEY));
    setUrl((String) properties.get(URL_KEY));

    if (super.getVersion() == 1) {
      setIsRemoteManaged(false);
      super.setVersion(CURRENT_VERSION);
    } else {
      setIsRemoteManaged(Boolean.parseBoolean((String) properties.get(IS_REMOTE_MANAGED_KEY)));
    }
  }
}
