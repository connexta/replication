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
import java.util.stream.Stream;

/** Represents a task that can be queued for later processing by a worker. */
public interface TaskInfo {
  /**
   * Gets the priority for this task, represented as a number from 9 being the highest level to 0
   * being the lowest.
   *
   * @return the priority for the task
   */
  public byte getPriority();

  /**
   * Gets the identifier of the intel document this task is for.
   *
   * @return the intel document identifier
   */
  public String getIntelId();

  /**
   * Gets the operation to be performed.
   *
   * @return the task operation
   */
  public OperationType getOperation();

  /**
   * Gets the last modified timestamp from the source site for the intel document referenced by this
   * task.
   *
   * @return the last modified timestamp for the intel document
   */
  public Instant getLastModified();

  /**
   * Gets the information for the resource that should be transferred or empty if the resource
   * should not be transferred.
   *
   * @return the info for the resource that should be transferred or empty if none should be
   *     transferred
   */
  public Optional<ResourceInfo> getResource();

  /**
   * Gets the information for the different types of metadata that should be transferred.
   *
   * @return all the different types of metadata that should be transferred or empty if none should
   *     be transferred
   */
  public Stream<MetadataInfo> metadatas();
}
