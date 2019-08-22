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
import java.util.Optional;

/** Keeps track of where a filter is at when querying a Site. */
public interface FilterIndex extends Persistable {

  /**
   * A time which represents the modified date of the last metadata that was put to the Queue.
   *
   * @return the {@link Instant}, or empty optional if no metadata has attempted to be replicated
   */
  Optional<Instant> getModifiedSince();

  /**
   * @see #getModifiedSince()
   * @param modifiedSince the modified time of the last metadata put into the queue
   */
  void setModifiedSince(Instant modifiedSince);
}
