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
package org.codice.ditto.replication.api.modern;

import java.net.URL;

/** A ReplicationSite holds information about a system to be replicated to/from */
public interface ReplicationSite {

  /**
   * Get the unique ID for this site
   *
   * @return site ID
   */
  String getId();

  /**
   * Get the human readable name of this site
   *
   * @return site name
   */
  String getName();

  /**
   * Get the URL of this site
   *
   * @return site URL
   */
  URL getUrl();
}
