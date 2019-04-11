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
package org.codice.ditto.replication.api.impl;

import static org.apache.commons.lang3.Validate.notNull;

import org.codice.ditto.replication.api.Status;

public class SyncResponse {
  private final long bytesTransferred;
  private final long itemsReplicated;
  private final long itemsFailed;
  private final Status status;

  public SyncResponse(long itemsReplicated, long itemsFailed, long bytesTransferred, Status status) {
    this.itemsReplicated = notNull(itemsReplicated);
    this.itemsFailed = notNull(itemsFailed);
    this.bytesTransferred = notNull(bytesTransferred);
    this.status = notNull(status);
  }

  public long getBytesTransferred() {
    return bytesTransferred;
  }

  public long getItemsReplicated() {
    return itemsReplicated;
  }

  public long getItemsFailed() {
    return itemsFailed;
  }

  public Status getStatus() {
    return status;
  }
}
