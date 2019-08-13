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

import java.util.OptionalLong;

/** Identifies a particular piece of data (e.g. metadata, resource) to be transferred. */
public interface DataInfo {
  /**
   * Gets the last modified timestamp from the source site for the piece of data that should be
   * transferred by the task.
   *
   * @return last modified timestamp for the piece data
   */
  public long getLastModified();

  /**
   * Gets the size of the piece of data to be transferred if known.
   *
   * @return the optional size of the piece of data
   */
  public OptionalLong getSize();
}
