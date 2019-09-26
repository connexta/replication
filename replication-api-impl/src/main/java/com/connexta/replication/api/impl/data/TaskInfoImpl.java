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
package com.connexta.replication.api.impl.data;

import com.connexta.replication.api.data.InvalidFieldException;
import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.TaskInfo;
import com.connexta.replication.api.data.UnsupportedVersionException;
import com.connexta.replication.api.data.ddf.DdfMetadataInfo;
import com.connexta.replication.api.impl.persistence.pojo.DdfMetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.ResourceInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.TaskInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownPojo;
import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Describes a piece of intel along with information about how to replicate it. This goes into a
 * {@link com.connexta.replication.api.queue.Queue} where an item worker will pick it up later as a
 * {@link com.connexta.replication.api.data.Task}.
 */
public class TaskInfoImpl extends AbstractPersistable<TaskInfoPojo> implements TaskInfo {
  @VisibleForTesting static final byte MIN_PRIORITY = 0;
  @VisibleForTesting static final byte MAX_PRIORITY = 9;

  private static final String PERSISTABLE_TYPE = "task info";

  private byte priority;
  private String intelId;
  private OperationType operation;
  private Instant lastModified;

  @Nullable private ResourceInfo resource;
  private List<MetadataInfo> metadatas;

  private boolean hasUnknowns = false;

  /**
   * Creates a new task info.
   *
   * @param intelId the intel id of the metadata to be replicated
   * @param priority the priority of the task 0 being the lowest and 9 being the highest
   * @param operation the {@link OperationType} for the task
   * @param lastModified the last modified time of the metadata
   * @param metadatas a set of the {@link MetadataInfo} associated with the task
   */
  public TaskInfoImpl(
      String intelId,
      byte priority,
      OperationType operation,
      Instant lastModified,
      Set<MetadataInfo> metadatas) {
    this(intelId, priority, operation, lastModified, null, metadatas);
  }

  /**
   * Creates a new task info with an empty set of metadata.
   *
   * @param intelId the intel id of the metadata to be replicated
   * @param priority the priority of the task 0 being the lowest and 9 being the highest
   * @param operation the {@link OperationType} for the task
   * @param lastModified the last modified time of the metadata
   * @param resource the {@link ResourceInfo} associated with the task
   */
  public TaskInfoImpl(
      String intelId,
      byte priority,
      OperationType operation,
      Instant lastModified,
      @Nullable ResourceInfo resource) {
    this(intelId, priority, operation, lastModified, resource, Set.of());
  }

  /**
   * Creates a new task info.
   *
   * @param intelId the intel id of the metadata to be replicated
   * @param priority the priority of the task 0 being the lowest and 9 being the highest
   * @param operation the {@link OperationType} for the task
   * @param lastModified the last modified time of the metadata
   * @param resource the {@link ResourceInfo} associated with the task
   * @param metadatas a set of the {@link MetadataInfo} associated with the task
   */
  public TaskInfoImpl(
      String intelId,
      byte priority,
      OperationType operation,
      Instant lastModified,
      @Nullable ResourceInfo resource,
      Set<MetadataInfo> metadatas) {
    super(TaskInfoImpl.PERSISTABLE_TYPE);
    this.intelId = intelId;
    this.priority =
        (byte) Math.min(Math.max(priority, TaskInfoImpl.MIN_PRIORITY), TaskInfoImpl.MAX_PRIORITY);
    this.operation = operation;
    this.lastModified = lastModified;
    this.resource = resource;
    this.metadatas = new ArrayList<>(metadatas);
  }

  /**
   * Instantiates a task info based on the provided information.
   *
   * @param info the task info to be cloned
   */
  public TaskInfoImpl(TaskInfo info) {
    super(TaskInfoImpl.PERSISTABLE_TYPE);
    this.intelId = info.getIntelId();
    this.priority =
        (byte)
            Math.min(
                Math.max(info.getPriority(), TaskInfoImpl.MIN_PRIORITY), TaskInfoImpl.MAX_PRIORITY);
    this.operation = info.getOperation();
    this.lastModified = info.getLastModified();
    this.resource = info.getResource().orElse(null);
    this.metadatas = info.metadatas().collect(Collectors.toList());
  }

  /**
   * Instantiates a task info based on the information provided by the specified pojo.
   *
   * @param pojo the pojo to initializes the site with
   */
  public TaskInfoImpl(TaskInfoPojo pojo) {
    super(TaskInfoImpl.PERSISTABLE_TYPE, null);
    readFrom(pojo);
  }

  @Override
  public byte getPriority() {
    return priority;
  }

  @Override
  public String getIntelId() {
    return intelId;
  }

  @Override
  public OperationType getOperation() {
    return operation;
  }

  @Override
  public Instant getLastModified() {
    return lastModified;
  }

  @Override
  public Optional<ResourceInfo> getResource() {
    return Optional.ofNullable(resource);
  }

  @Override
  public Stream<MetadataInfo> metadatas() {
    return metadatas.stream().map(MetadataInfo.class::cast);
  }

  /**
   * Adds the given set of metadata to the set contained in this task info.
   *
   * @param metadatas the set of metadata to combine with the contained set
   */
  public void addMetadata(Set<MetadataInfo> metadatas) {
    addMetadata(metadatas.stream());
  }

  /**
   * Adds the given metadata to the set contained in this task info.
   *
   * @param metadatas the set of metadata to add to the contained set
   */
  public void addMetadata(MetadataInfo... metadatas) {
    addMetadata(Stream.of(metadatas));
  }

  /**
   * Adds the given metadata to the set contained in this task info.
   *
   * @param metadatas the stream of metadata to add to the contained set
   */
  public void addMetadata(Stream<MetadataInfo> metadatas) {
    metadatas.map(TaskInfoImpl::wrap).forEach(this.metadatas::add);
  }

  /**
   * Checks if this task information contains unknown information.
   *
   * @return <code>true</code> if it contains unknown info; <code>false</code> otherwise
   */
  public boolean hasUnknowns() {
    return hasUnknowns;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(), priority, intelId, operation, lastModified, resource, metadatas);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof TaskInfoImpl)) {
      final TaskInfoImpl info = (TaskInfoImpl) obj;

      return (priority == info.priority)
          && (operation == info.operation)
          && Objects.equals(intelId, info.intelId)
          && Objects.equals(lastModified, info.lastModified)
          && Objects.equals(resource, info.resource)
          && Objects.equals(metadatas, info.metadatas);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "TaskInfoImpl[id=%s, priority=%d, intelId=%s, operation=%s, lastModified=%s, resource=%s, metadatas=%s]",
        getId(), priority, intelId, operation, lastModified, resource, metadatas);
  }

  @Override
  protected TaskInfoPojo writeTo(TaskInfoPojo pojo) {
    if (hasUnknowns()) { // cannot serialize if it contains unknowns
      throw new InvalidFieldException("unknown task info");
    }
    super.writeTo(pojo);
    setOrFailIfNullOrEmpty("intelId", this::getIntelId, pojo::setIntelId);
    convertAndSetEnumOrFailIfNullOrUnknown(
        "operation", OperationType.UNKNOWN, this::getOperation, pojo::setOperation);
    setOrFailIfNull("lastModified", this::getLastModified, pojo::setLastModified);
    return pojo.setVersion(TaskInfoPojo.CURRENT_VERSION)
        .setPriority(
            (byte)
                Math.min(Math.max(priority, TaskInfoImpl.MIN_PRIORITY), TaskInfoImpl.MAX_PRIORITY))
        .setResource(TaskInfoImpl.toPojo(resource))
        .setMetadatas(metadatas.stream().map(TaskInfoImpl::toPojo));
  }

  @Override
  protected final void readFrom(TaskInfoPojo pojo) {
    super.readFrom(pojo);
    if (pojo.getVersion() < TaskInfoPojo.MINIMUM_VERSION) {
      throw new UnsupportedVersionException(
          "unsupported "
              + TaskInfoImpl.PERSISTABLE_TYPE
              + " version: "
              + pojo.getVersion()
              + " for object: "
              + getId());
    } // do support pojo.getVersion() > CURRENT_VERSION for forward compatibility
    this.hasUnknowns = pojo instanceof UnknownPojo; // reset the unknown flags
    readFromCurrentOrFutureVersion(pojo);
  }

  private void readFromCurrentOrFutureVersion(TaskInfoPojo pojo) {
    setOrFailIfNullOrEmpty("intelId", pojo::getIntelId, this::setIntelId);
    convertAndSetEnumValueOrFailIfNullOrEmpty(
        "operation",
        OperationType.class,
        OperationType.UNKNOWN,
        pojo::getOperation,
        this::setOperation);
    setOrFailIfNull("lastModified", pojo::getLastModified, this::setLastModified);
    this.priority =
        (byte)
            Math.min(
                Math.max(pojo.getPriority(), TaskInfoImpl.MIN_PRIORITY), TaskInfoImpl.MAX_PRIORITY);
    setResource(fromPojo(pojo.getResource()));
    setMetadatas(pojo.metadatas().map(TaskInfoImpl::fromPojo));
  }

  @VisibleForTesting
  void setIntelId(String intelId) {
    this.intelId = intelId;
  }

  @VisibleForTesting
  void setOperation(OperationType operation) {
    this.operation = operation;
  }

  @VisibleForTesting
  void setLastModified(Instant lastModified) {
    this.lastModified = lastModified;
  }

  @VisibleForTesting
  void setResource(@Nullable ResourceInfoImpl resource) {
    if (resource != null) {
      this.hasUnknowns |= resource.hasUnknowns();
    }
    this.resource = resource;
  }

  @VisibleForTesting
  @SuppressWarnings("squid:S3864" /* peek() designed to 'or' all unknown metadatas found */)
  void setMetadatas(Stream<MetadataInfoImpl> metadatas) {
    this.metadatas = metadatas.peek(this::checkForUnknowns).collect(Collectors.toList());
  }

  private void checkForUnknowns(MetadataInfoImpl metadata) {
    this.hasUnknowns |= metadata.hasUnknowns();
  }

  @Nullable
  private static ResourceInfoImpl wrap(@Nullable ResourceInfo resource) {
    return ((resource instanceof ResourceInfoImpl) || (resource == null))
        ? (ResourceInfoImpl) resource
        : new ResourceInfoImpl(resource);
  }

  private static MetadataInfoImpl wrap(MetadataInfo metadata) {
    if (metadata instanceof MetadataInfoImpl) {
      return (MetadataInfoImpl) metadata;
    } else if (metadata instanceof DdfMetadataInfo) {
      return new DdfMetadataInfoImpl((DdfMetadataInfo<?>) metadata);
    }
    return new MetadataInfoImpl(metadata);
  }

  @Nullable
  private static ResourceInfoImpl fromPojo(@Nullable ResourceInfoPojo pojo) {
    return (pojo != null) ? new ResourceInfoImpl(pojo) : null;
  }

  @Nullable
  private static ResourceInfoPojo toPojo(@Nullable ResourceInfo resource) {
    final ResourceInfoImpl impl = TaskInfoImpl.wrap(resource);

    return (impl != null) ? impl.writeTo(new ResourceInfoPojo()) : null;
  }

  private static MetadataInfoImpl fromPojo(MetadataInfoPojo<?> pojo) {
    return (pojo instanceof DdfMetadataInfoPojo)
        ? new DdfMetadataInfoImpl<>((DdfMetadataInfoPojo) pojo)
        : new MetadataInfoImpl(pojo);
  }

  private static MetadataInfoPojo<?> toPojo(MetadataInfo metadata) {
    if (metadata instanceof DdfMetadataInfoImpl) {
      return ((DdfMetadataInfoImpl<?>) metadata).writeTo(new DdfMetadataInfoPojo());
    } else if (metadata instanceof DdfMetadataInfo) {
      return TaskInfoImpl.wrap((DdfMetadataInfo<?>) metadata).writeTo(new DdfMetadataInfoPojo());
    }
    return TaskInfoImpl.wrap(metadata).writeTo(new MetadataInfoPojo<>());
  }
}
