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

/** The result of a {@link ReplicationItem} after it has been processed. */
public enum Status {

  /** Indicates a {@link ReplicationItem} was successfully transferred. */
  SUCCESS,

  /** Indicates the {@link ReplicationItem} failed to be transferred. */
  FAILURE,

  /**
   * Indicates connection to the source and/or destination {@link NodeAdapter}s was lost while
   * transferring metadata and/or resources.
   */
  CONNECTION_LOST
}
