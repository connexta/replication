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
package org.codice.ditto.replication.api.impl.modern;

import java.net.URL;
import org.codice.ditto.replication.api.modern.ReplicationSite;

public class ReplicationSiteImpl implements ReplicationSite {

  private String id;

  private String name;

  private URL url;

  public ReplicationSiteImpl(String id, String name, URL url) {
    this.id = id;
    this.name = name;
    this.url = url;
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
  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }
}
