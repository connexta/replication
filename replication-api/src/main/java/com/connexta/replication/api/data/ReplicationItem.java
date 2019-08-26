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

import com.connexta.replication.api.Action;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.Status;
import java.util.Date;

/** Represents a resource that replication has replicated or attempted to replicate. */
public interface ReplicationItem extends Persistable {
  /** @return id of the metadata retrieved from a remote system */
  String getMetadataId();

  /** @return the id of the {@link Filter} this item belongs to */
  String getFilterId();

  /**
   * @return this last time the resource associated with this item modified, or {@code null} if
   *     there is no resource associated with this metadata
   */
  Date getResourceModified();

  /** @return the last time the {@link Metadata} associated with this item was modified */
  Date getMetadataModified();

  /**
   * @return the size in bytes of this item's resource, or 0 if there is no resource associated with
   *     this metadata
   */
  long getResourceSize();

  /** @return the size in bytes of this item's metadata, cannot be negative */
  long getMetadataSize();

  /** @return the time at which this item began processing */
  Date getStartTime();

  /** @return the time at which this item finished processing */
  Date getDoneTime();

  /** @return the time in milliseconds it took process this item, cannot be negative. */
  long getDuration();

  /**
   * See {@link Status}.
   *
   * @return the status of this replication item
   */
  Status getStatus();

  /**
   * See {@link Action}.
   *
   * @return the action performed on the metadata/resource
   */
  Action getAction();

  /** @return the resource's speed of transfer in bytes per second, cannot be negative */
  double getResourceTransferRate();

  /**
   * @return the name for the source {@link NodeAdapter} the metadata/resource was being replicated
   *     from.
   */
  String getSource();

  /**
   * @return the name for the destination {@link NodeAdapter} the metadata/resource was being
   *     replicated to.
   */
  String getDestination();
}
