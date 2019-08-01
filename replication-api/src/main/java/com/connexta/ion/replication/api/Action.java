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

/** Describes an action taken on a {@link ReplicationItem} */
public enum Action {

  /**
   * Action for a replication item that was created from a source {@link NodeAdapter} to a
   * destination {@link NodeAdapter}
   */
  CREATE,

  /**
   * Action for a replication item that was updated from a source {@link NodeAdapter} to a
   * destination {@link NodeAdapter}
   */
  UPDATE,

  /**
   * Action for a replication item that was deleted between a source {@link NodeAdapter} to a
   * destination {@link NodeAdapter}
   */
  DELETE
}
