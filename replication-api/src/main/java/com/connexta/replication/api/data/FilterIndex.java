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

import com.connexta.ion.replication.api.data.Metadata;
import java.time.Instant;

/** Keeps track of what to query from a {@link com.connexta.replication.api.data.Site}. */
public interface FilterIndex extends Persistable {

  /**
   * A time which represents the modified date of the last {@link Metadata} that was put to the
   * Queue.
   *
   * @return the {@link Instant}, or {@code null} if no metadata has attempted to be replicated
   */
  Instant getModifiedSince();

  /** @return the site id this index is for */
  String getFilterId();
}
