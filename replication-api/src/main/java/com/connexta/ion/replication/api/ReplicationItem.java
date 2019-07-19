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

import com.connexta.ion.replication.api.data.Metadata;
import com.connexta.ion.replication.api.data.ReplicatorConfig;
import java.util.Date;

/** Represents a resource that replication has replicated or attempted to replicate. */
public interface ReplicationItem {

  /** @return id for which {@link Metadata} this item represents */
  String getId();

  /** @return the id of the {@link ReplicatorConfig} this item belongs to */
  String getConfigId();

  /** @return this last time the resource associated with this item modified */
  Date getResourceModified();

  /** @return the last time the {@link Metadata} associated with this item was modified */
  Date getMetadataModified();

  /** @return the size in bytes of this item's resource */
  long getResourceSize();

  /** @return the size in bytes of this item's metadata */
  long getMetadataSize();

  /**
   * @return the time at which this item began transferring, or {@code null} if it has not begun
   *     transferring
   */
  Date getStartTime();

  /** Called when the item begins transfer, setting the {@link #getStartTime()} to now. */
  void markStartTime();

  /** Called when the item ends transfer, setting the {@link #getDuration()}. */
  void markDoneTime();

  /**
   * @return the time in milliseconds it took transfer this item, or -1 if it has not transferred
   */
  long getDuration();

  /** @return the status of this replication item */
  Status getStatus();

  /** @return the speed of transfer in bytes per second, or -1 if the item has not transferred */
  double getTransferRate();

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
