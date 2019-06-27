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
package com.connexta.ion.replication.api;

import com.connexta.ion.replication.api.data.ReplicationStatus;
import com.connexta.ion.replication.api.data.ReplicatorConfig;

public interface SyncRequest {

  /**
   * Get the configuration for this request
   *
   * @return The configuration of this request
   */
  ReplicatorConfig getConfig();

  /**
   * Get the status of this request. Status will be updated during processing.
   *
   * @return The status of this request
   */
  ReplicationStatus getStatus();
}
