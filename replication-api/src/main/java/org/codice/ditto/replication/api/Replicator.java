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
package org.codice.ditto.replication.api;

import java.util.Queue;
import java.util.Set;

public interface Replicator {

  /**
   * Submits a {@link SyncRequest} to be executed
   *
   * @param syncRequest The {@link SyncRequest} to run
   */
  void submitSyncRequest(final SyncRequest syncRequest) throws InterruptedException;

  /**
   * Cancel all pending or active {@link SyncRequest}
   *
   * @param syncRequest The syncRequest to cancel
   */
  void cancelSyncRequest(final SyncRequest syncRequest);

  /**
   * Cancel all pending or active {@link SyncRequest} based on the request configuration id
   *
   * @param configId The id of the configuration in the request
   */
  void cancelSyncRequest(final String configId);

  /**
   * Gets the {@link Queue} that have been submitted but have not yet been executed. Does not
   * contain duplicates.
   *
   * @return A unmodifiable {@link Queue} or an empty {@link Queue} if there are none
   */
  Queue<SyncRequest> getPendingSyncRequests();

  /**
   * Gets the {@link Set} that are currently being executed
   *
   * @return A unmodifiable {@link Set} or an empty {@link Set} if there are none
   */
  Set<SyncRequest> getActiveSyncRequests();
}
