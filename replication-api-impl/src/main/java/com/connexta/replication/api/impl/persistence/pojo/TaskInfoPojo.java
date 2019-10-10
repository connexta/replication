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
package com.connexta.replication.api.impl.persistence.pojo;

import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownTaskInfoPojo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * This class provides a pojo implementation for a task info object capable of reloading all
 * supported fields for all supported versions from a Json string. It also provides the capability
 * of persisting back the fields based on the latest version format.
 */
@JsonPropertyOrder({"clazz", "id", "version", "intel_id", "operation"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "clazz",
    defaultImpl = UnknownTaskInfoPojo.class)
@JsonSubTypes(@Type(TaskInfoPojo.class))
@JsonTypeName("task")
public class TaskInfoPojo extends Pojo<TaskInfoPojo> {
  /**
   * Current version format.
   *
   * <p>Version history:
   *
   * <ul>
   *   <li>1 - initial version.
   * </ul>
   */
  public static final int CURRENT_VERSION = 1;

  /** The oldest version supported by the current code (anything before that will fail). */
  public static final int MINIMUM_VERSION = 1;

  @JsonProperty("priority")
  private byte priority;

  @JsonProperty("intel_id")
  @Nullable
  private String intelId;

  @JsonProperty("operation")
  @Nullable
  private String operation;

  @JsonProperty("last_modified")
  @Nullable
  private Instant lastModified;

  @JsonProperty("resource")
  @Nullable
  private ResourceInfoPojo resource;

  @JsonProperty("metadata")
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
  private List<MetadataInfoPojo<?>> metadatas = new ArrayList<>(5);

  /**
   * Gets the priority for this task, represented as a number from 9 being the highest level to 0
   * being the lowest.
   *
   * @return the priority for the task
   */
  public byte getPriority() {
    return priority;
  }

  /**
   * Sets the priority for this task, represented as a number from 9 being the highest level to 0
   * being the lowest.
   *
   * @param priority the priority for the task
   * @return this for chaining
   */
  public TaskInfoPojo setPriority(byte priority) {
    this.priority = priority;
    return this;
  }

  /**
   * Gets the identifier of the intel document this task is for.
   *
   * @return the intel document identifier
   */
  public String getIntelId() {
    return intelId;
  }

  /**
   * Sets the identifier of the intel document this task is for.
   *
   * @param intelId the intel document identifier
   * @return this for chaining
   */
  public TaskInfoPojo setIntelId(@Nullable String intelId) {
    this.intelId = intelId;
    return this;
  }

  /**
   * Gets the operation to be performed.
   *
   * @return the task operation
   */
  public String getOperation() {
    return operation;
  }

  /**
   * Sets the operation to be performed.
   *
   * @param operation the task operation
   * @return this for chaining
   */
  public TaskInfoPojo setOperation(@Nullable String operation) {
    this.operation = operation;
    return this;
  }

  /**
   * Sets the operation to be performed.
   *
   * @param operation the task operation
   * @return this for chaining
   */
  public TaskInfoPojo setOperation(@Nullable OperationType operation) {
    this.operation = (operation != null) ? operation.name() : null;
    return this;
  }

  /**
   * Gets the last modified timestamp from the source site for the intel document referenced by this
   * task.
   *
   * @return the last modified timestamp for the intel document
   */
  public Instant getLastModified() {
    return lastModified;
  }

  /**
   * Sets the last modified timestamp from the source site for the intel document referenced by this
   * task.
   *
   * @param lastModified the last modified timestamp for the intel document
   * @return this for chaining
   */
  public TaskInfoPojo setLastModified(@Nullable Instant lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  /**
   * Gets the information for the resource that should be transferred or empty if the resource
   * should not be transferred.
   *
   * @return the info for the resource that should be transferred or empty if none should be
   *     transferred
   */
  @Nullable
  public ResourceInfoPojo getResource() {
    return resource;
  }

  /**
   * Sets the information for the resource that should be transferred or empty if the resource
   * should not be transferred.
   *
   * @param resource the info for the resource that should be transferred or empty if none should be
   *     transferred
   * @return this for chaining
   */
  public TaskInfoPojo setResource(@Nullable ResourceInfoPojo resource) {
    this.resource = resource;
    return this;
  }

  /**
   * Gets the information for the different types of metadata that should be transferred.
   *
   * @return all the different types of metadata that should be transferred or empty if none should
   *     be transferred
   */
  @Nullable
  public List<MetadataInfoPojo<?>> getMetadatas() {
    return metadatas;
  }

  /**
   * Gets the information for the different types of metadata that should be transferred.
   *
   * @return all the different types of metadata that should be transferred or empty if none should
   *     be transferred
   */
  public Stream<MetadataInfoPojo<?>> metadatas() {
    return (metadatas != null) ? metadatas.stream() : Stream.empty();
  }

  /**
   * Sets the information for the different types of metadata that should be transferred.
   *
   * @param metadatas the different types of metadata that should be transferred; <code>null</code>
   *     or empty if none should be transferred
   * @return this for chaining
   */
  public TaskInfoPojo setMetadatas(@Nullable List<MetadataInfoPojo<?>> metadatas) {
    this.metadatas = (metadatas != null) ? metadatas : new ArrayList<>(5);
    return this;
  }

  /**
   * Sets the information for the different types of metadata that should be transferred.
   *
   * @param metadatas the different types of metadata that should be transferred or empty if none
   *     should be transferred
   * @return this for chaining
   */
  @JsonIgnore
  public TaskInfoPojo setMetadatas(Stream<MetadataInfoPojo<?>> metadatas) {
    this.metadatas = metadatas.collect(Collectors.toList());
    return this;
  }

  /**
   * Adds the information for a type of metadata that should be transferred.
   *
   * @param metadata the type of metadata that should be transferred
   * @return this for chaining
   */
  public TaskInfoPojo addMetadata(MetadataInfoPojo<?> metadata) {
    metadatas.add(metadata);
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(), priority, intelId, operation, lastModified, resource, metadatas);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof TaskInfoPojo)) {
      final TaskInfoPojo pojo = (TaskInfoPojo) obj;

      return (priority == pojo.priority)
          && Objects.equals(intelId, pojo.intelId)
          && Objects.equals(operation, pojo.operation)
          && Objects.equals(lastModified, pojo.lastModified)
          && Objects.equals(resource, pojo.resource)
          && Objects.equals(metadatas, pojo.metadatas);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "TaskInfoPojo[id=%s, version=%d, priority=%d, intelId=%s, operation=%s, lastModified=%s, resource=%s, metadatas=%s]",
        getId(), getVersion(), priority, intelId, operation, lastModified, resource, metadatas);
  }
}
