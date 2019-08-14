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
package com.connexta.replication.queue;

/**
 * Defines the various type of operations a worker can perform with an intel document for the site
 * associated with the queue where a task has been added.
 */
public enum OperationType {
  /** Harvests a given intel document from the site. */
  HARVEST,

  /** Replicates data for a given intel document to the site. */
  REPLICATE_TO,

  /** Replicates data for a given intel document from the site. */
  REPLICATE_FROM,

  /** Deletes a given intel document from the site. */
  DELETE,

  /**
   * The unknown value is used for forward compatibility where a worker might not be able to
   * understand a new type of operation and would mapped this new operation to <code>UNKNOWN</code>
   * and most likely ignore it without removing it from the queue.
   */
  UNKNOWN
}
