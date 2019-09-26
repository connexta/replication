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
package com.connexta.replication.api.data;

import java.time.Instant;
import java.util.Objects;
import java.util.OptionalLong;

/** Identifies a particular piece of data (e.g. metadata, resource) to be transferred. */
public interface DataInfo {
  /**
   * Gets the last modified timestamp from the source site for the piece of data that should be
   * transferred by the task.
   *
   * @return last modified timestamp for the piece data
   */
  public Instant getLastModified();

  /**
   * Gets the size of the piece of data to be transferred if known.
   *
   * @return the optional size of the piece of data
   */
  public OptionalLong getSize();

  /**
   * Checks if this information exposed via this interface (or its sub-interfaces) is the same (or
   * equals) to those exposed by the specified object.
   *
   * <p><i>Note:</i> This method is guaranteed to only compare the data expose via this interface
   * (or its sub-interfaces) and not the data held by implementation of this interface.
   *
   * @param obj the object to compare with
   * @return <code>true</code> if the info exposed is the same to the specified one; <code>false
   *     </code> otherwise
   */
  public default boolean sameAs(Object obj) {
    if (obj instanceof DataInfo) {
      final DataInfo info = (DataInfo) obj;

      return Objects.equals(getLastModified(), info.getLastModified())
          && Objects.equals(getSize(), info.getSize());
    }
    return false;
  }
}
