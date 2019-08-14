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

/** Defines the various priority levels supported for queued tasks. */
public enum PriorityLevel {
  HIGH((byte) 9),
  MEDIUM((byte) 5),
  LOW((byte) 0),

  /**
   * The unknown value is used for forward compatibility where a worker might not be able to
   * understand this new priority and would mapped it automatically to priority level 1.
   */
  UNKNOWN((byte) 1);

  private final byte level;

  private PriorityLevel(byte level) {
    this.level = level;
  }

  /**
   * Gets the level for this priority ranging from 9 being the highest level to 0 being the lowest.
   *
   * @return the level for this priority
   */
  public byte getLevel() {
    return level;
  }
}
