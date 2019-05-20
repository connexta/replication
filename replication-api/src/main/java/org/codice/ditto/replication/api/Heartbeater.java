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
package org.codice.ditto.replication.api;

import org.codice.ditto.replication.api.data.ReplicationSite;

/**
 * This interface is design to provide various implementations capable of generating a heartbeat to
 * sites.
 */
public interface Heartbeater {
  /**
   * Submit a request to generate a heartbeat to the specified site.
   *
   * @param site the site to send a heartbeat to and be updated if needed
   * @throws InterruptedException if interrupted while submitting the request
   */
  void heartbeat(ReplicationSite site) throws InterruptedException;
}
