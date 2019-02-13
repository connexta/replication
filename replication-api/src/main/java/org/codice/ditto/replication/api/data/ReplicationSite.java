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
package org.codice.ditto.replication.api.data;

/** A ReplicationSite holds information about a system to be replicated to/from */
public interface ReplicationSite extends Persistable {

  /**
   * Get the human readable name of this site
   *
   * @return site name
   */
  String getName();

  /**
   * Set the name of this site
   *
   * @param name the name to give this site
   */
  void setName(String name);

  /**
   * Get the URL of this site
   *
   * @return site URL
   */
  String getUrl();

  /**
   * Set the URL of this site
   *
   * @param url the URL to give this site
   */
  void setUrl(String url);
}
