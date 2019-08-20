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
import com.connexta.ion.replication.api.data.QueryRequest;
import java.time.Instant;
import javax.annotation.Nullable;

/**
 * A ReplicatorConfig holds information about how replication should be performed such as the sites
 * to replicate between and the filter specifying what to replicate.
 */
public interface ReplicatorConfig extends Persistable {

  /**
   * Get the human readable name for this configuration
   *
   * @return configuration name
   */
  String getName();

  /**
   * Get the source site id associated with this configuration
   *
   * @return source id
   */
  String getSource();

  /**
   * Get the destination site id associated with this configuration
   *
   * @return destination id
   */
  String getDestination();

  /**
   * Get the filter used for determining the data set to replicate
   *
   * @return replication cql filter
   */
  String getFilter();

  /**
   * Tells whether this replication is configured to be bidirectional or not.
   *
   * @return True if this replication is bidirectional, otherwise, False.
   */
  boolean isBidirectional();

  /**
   * Get a short description for this configuration
   *
   * @return configuration description
   */
  @Nullable
  String getDescription();

  /**
   * Gets the suspended state of this config. Suspended configs will not be run.
   *
   * @return boolean indicating if this config is suspended.
   */
  boolean isSuspended();

  /**
   * A {@link Instant} which represents the modified instant of the last {@link Metadata} that was
   * attempted to be replicated. If available, this should be used by the {@link
   * QueryRequest#getModifiedAfter()}.
   *
   * @return the {@link Instant}, or {@code null} if no metadata has attempted to be replicated
   */
  @Nullable
  Instant getLastMetadataModified();

  /**
   * See {@link #getLastMetadataModified()}.
   *
   * @param lastMetadataModified the {@link Metadata}'s modified instant
   */
  // still internally used by the syncer
  void setLastMetadataModified(@Nullable Instant lastMetadataModified);
}
