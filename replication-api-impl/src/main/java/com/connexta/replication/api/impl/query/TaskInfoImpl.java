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
package com.connexta.replication.api.impl.query;

import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.TaskInfo;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Describes a piece of intel along with information about how to replicate it. This goes into a
 * {@link com.connexta.replication.api.queue.Queue} where an item worker will pick it up later as a
 * {@link com.connexta.replication.api.data.Task}.
 */
// todo Move back to query module when possible
public class TaskInfoImpl implements TaskInfo {

  private String id;

  private byte priority;

  private OperationType operation;

  private Instant lastModified;

  @Nullable private ResourceInfo resourceInfo;

  private Set<MetadataInfo> metadatas;

  /**
   * Creates a new TaskInfoImpl with a null resourceInfo.
   *
   * @param id The ID of the metadata to be replicated
   * @param priority The priority of the task 0 being the lowest and 9 being the highest
   * @param operation The {@link OperationType} for the task
   * @param lastModified The last modified time of the metadata
   * @param metadatas A set of the {@link MetadataInfo} associated with the task
   */
  public TaskInfoImpl(
      String id,
      byte priority,
      OperationType operation,
      Instant lastModified,
      Set<MetadataInfo> metadatas) {
    this(id, priority, operation, lastModified, null, metadatas);
  }

  /**
   * Creates a new TaskInfoImpl with an empty set of metadata.
   *
   * @param id The ID of the metadata to be replicated
   * @param priority The priority of the task 0 being the lowest and 9 being the highest
   * @param operation The {@link OperationType} for the task
   * @param lastModified The last modified time of the metadata
   * @param resourceInfo The {@link ResourceInfo} associated with the task
   */
  public TaskInfoImpl(
      String id,
      byte priority,
      OperationType operation,
      Instant lastModified,
      @Nullable ResourceInfo resourceInfo) {
    this(id, priority, operation, lastModified, resourceInfo, Set.of());
  }

  /**
   * Creates a new TaskInfoImpl.
   *
   * @param id The ID of the metadata to be replicated
   * @param priority The priority of the task 0 being the lowest and 9 being the highest
   * @param operation The {@link OperationType} for the task
   * @param lastModified The last modified time of the metadata
   * @param resourceInfo The {@link ResourceInfo} associated with the task
   * @param metadatas A set of the {@link MetadataInfo} associated with the task
   */
  public TaskInfoImpl(
      String id,
      byte priority,
      OperationType operation,
      Instant lastModified,
      @Nullable ResourceInfo resourceInfo,
      Set<MetadataInfo> metadatas) {
    this.id = id;
    this.priority = priority;
    this.operation = operation;
    this.lastModified = lastModified;
    this.resourceInfo = resourceInfo;
    this.metadatas = new HashSet<>(metadatas);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public byte getPriority() {
    return priority;
  }

  @Override
  public OperationType getOperation() {
    return operation;
  }

  @Override
  public Instant getLastModified() {
    return lastModified;
  }

  /**
   * Returns an optional containing the {@link ResourceInfo}.
   *
   * @return An optional containing the {@link ResourceInfo} if there is any
   */
  @Override
  public Optional<ResourceInfo> getResource() {
    return Optional.ofNullable(resourceInfo);
  }

  /**
   * Returns a {@link Stream} containing {@link MetadataInfo}s describing the metadata that should
   * be replicated for this task.
   *
   * @return A stream containing {@link MetadataInfo}s
   */
  @Override
  public Stream<MetadataInfo> metadatas() {
    return metadatas.stream();
  }

  /**
   * Adds the given set of metadata to the set contained in this TaskInfo.
   *
   * @param metadata The set of metadata to combine with the contained set
   */
  public void addMetadata(Set<MetadataInfo> metadata) {
    this.metadatas.addAll(metadata);
  }

  /**
   * Adds the given metadata to the set contained in this TaskInfo.
   *
   * @param metadata The metadata to add to the contained set
   */
  public void addMetadata(MetadataInfo metadata) {
    this.metadatas.add(metadata);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskInfoImpl taskInfo = (TaskInfoImpl) o;
    return priority == taskInfo.priority
        && operation == taskInfo.operation
        && Objects.equals(id, taskInfo.id)
        && Objects.equals(lastModified, taskInfo.lastModified)
        && Objects.equals(resourceInfo, taskInfo.resourceInfo)
        && Objects.equals(metadatas, taskInfo.metadatas);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, priority, operation, lastModified, resourceInfo, metadatas);
  }

  @Override
  public String toString() {
    return "TaskInfoImpl{"
        + "id='"
        + id
        + '\''
        + ", priority="
        + priority
        + ", operation="
        + operation
        + ", lastModified="
        + lastModified
        + ", resourceInfo="
        + resourceInfo
        + '}';
  }
}
