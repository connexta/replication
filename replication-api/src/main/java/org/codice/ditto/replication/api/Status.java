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

/** The states that a {@link SyncRequest} can be in. */
public enum Status {

  /**
   * The {@link org.codice.ditto.replication.api.data.ReplicatorConfig} is waiting to be processed.
   */
  PENDING,

  /**
   * Indicates metadata and/or resources are being pushed from the source to destination {@link
   * NodeAdapter}s
   */
  PUSH_IN_PROGRESS,

  /**
   * Indicates metadata and/or resources are being pushed from the destination to source {@link
   * NodeAdapter}s
   */
  PULL_IN_PROGRESS,

  /** Indicates a {@link SyncRequest} was successfully executed. */
  SUCCESS,

  /** Indicates an unknown error occurred. */
  FAILURE,

  /** Indicates the {@link SyncRequest} was canceled while in the middle of processing. */
  CANCELED,

  /**
   * Indicates connection to the source and/or destination {@link NodeAdapter}s was lost while
   * transferring metadata and/or resources.
   */
  CONNECTION_LOST,

  /**
   * Indicates connection could not be established to the source or destination {@link NodeAdapter}s
   * before trying to transfer metadata and/or resources.
   */
  CONNECTION_UNAVAILABLE
}
