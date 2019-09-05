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
package com.connexta.replication.api;

import com.connexta.replication.api.data.CreateRequest;
import com.connexta.replication.api.data.CreateStorageRequest;
import com.connexta.replication.api.data.DeleteRequest;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.QueryRequest;
import com.connexta.replication.api.data.QueryResponse;
import com.connexta.replication.api.data.Resource;
import com.connexta.replication.api.data.ResourceRequest;
import com.connexta.replication.api.data.ResourceResponse;
import com.connexta.replication.api.data.UpdateRequest;
import com.connexta.replication.api.data.UpdateStorageRequest;
import java.io.Closeable;

/** Used by the {@link Replicator} to replicate metadata/resource between. */
public interface NodeAdapter extends Closeable {

  /** @return {@code true} if available for replicating, otherwise {@code false}. */
  boolean isAvailable();

  /**
   * @return a human-readable name representing the name of this {@code NodeAdapter}
   * @throws AdapterException if there is an error communicating with the remote server
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  String getSystemName();

  /**
   * Gets the Metadata that should be created, updated, or deleted to a remote {@code NodeAdapter}.
   *
   * @param queryRequest query criteria to determine creates, updates, and/or deletes
   * @return a {@link QueryResponse} containing the {@link Metadata} matching the {@link
   *     QueryRequest} criteria
   * @throws AdapterException if there is an error communicating with the remote server
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  QueryResponse query(QueryRequest queryRequest);

  /**
   * @param metadata {@link Metadata} to check existence for
   * @return {@code true} if the {@link Metadata} exists on the {@code NodeAdapter}, otherwise
   *     {@code false}.
   * @throws AdapterException if there is an error checking if the metadata exists
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  boolean exists(Metadata metadata);

  /**
   * Creates {@link Metadata} on this {@code NodeAdapter}.
   *
   * @param createRequest request containing {@link Metadata} to create.
   * @return {@code true} if the creation was successful, {@code false} otherwise
   * @throws AdapterException if there is an error creating the metadata
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  boolean createRequest(CreateRequest createRequest);

  /**
   * Updates {@link Metadata} on this {@code NodeAdapter}.
   *
   * @param updateRequest request containing {@link Metadata} to update.
   * @return {@code true} if the update was successful, {@code false} otherwise
   * @throws AdapterException if there is an error updating the metadata
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  boolean updateRequest(UpdateRequest updateRequest);

  /**
   * Deletes {@link Metadata} on this {@code NodeAdapter}.
   *
   * @param deleteRequest request containing {@link Metadata} to delete.
   * @return {@code true} if the delete was successful, {@code false} otherwise
   * @throws AdapterException if there is an error deleting the metadata
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  boolean deleteRequest(DeleteRequest deleteRequest);

  /**
   * Reads a resource from this {@code NodeAdapter}.
   *
   * @param resourceRequest request containing the {@link Metadata} for the {@link Resource} to
   *     fetch
   * @return the {@link Resource}
   * @throws AdapterException if there was an error retrieving the {@link Resource}
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  ResourceResponse readResource(ResourceRequest resourceRequest);

  /**
   * Creates a {@link Resource} on this {@code NodeAdapter}.
   *
   * @param createStorageRequest request containing the {@link Resource} to create
   * @return {@code true} if the creation was successful, {@code false} otherwise
   * @throws AdapterException if there was an error creating the {@link Resource}
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  boolean createResource(CreateStorageRequest createStorageRequest);

  /**
   * Updates a {@link Resource} on this {@code NodeAdapter}.
   *
   * @param updateStorageRequest request containing the {@link Resource} to update
   * @return {@code true} if the update was successful, {@code false} otherwise
   * @throws AdapterException if there was an error updating the {@link Resource}
   * @throws AdapterInterruptedException if the operation was interrupted and could not complete
   */
  boolean updateResource(UpdateStorageRequest updateStorageRequest);
}
