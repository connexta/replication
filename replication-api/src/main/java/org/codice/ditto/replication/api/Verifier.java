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
 * This interface is design to provide various implementations capable of verifying if a site is a
 * remote one. The verification process is used to identify if sites should be managed locally or
 * remotely. Remotely managed sites will not be replicated to or from here; the replication
 * responsibility will be left to those sites.
 */
public interface Verifier {
  /**
   * Verifies the specified site.
   *
   * @param site the site to be verified and updated if needed
   */
  void verify(ReplicationSite site);
}
