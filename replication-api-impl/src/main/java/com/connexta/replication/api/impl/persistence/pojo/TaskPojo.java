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

import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownTaskPojo;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * This class provides a pojo implementation for a task capable of reloading all supported fields
 * for all supported versions from a Json string. It also provides the capability of persisting back
 * the fields based on the latest version format.
 */
@JsonPropertyOrder({"clazz", "id", "version", "info"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "clazz",
    defaultImpl = UnknownTaskPojo.class)
@JsonSubTypes(@Type(TaskPojo.class))
@JsonTypeName("task")
public class TaskPojo extends Pojo<TaskPojo> {
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

  @JsonProperty("info")
  @Nullable
  private TaskInfoPojo info;

  @JsonProperty("attempts")
  private int totalAttempts;

  @JsonProperty("state")
  @Nullable
  private String state;

  @JsonProperty("original_queued_time")
  @Nullable
  private Instant originalQueuedTime;

  @JsonProperty("queued_time")
  @Nullable
  private Instant queuedTime;

  @JsonProperty("frozen_time")
  private long frozenTime;

  @JsonProperty("duration")
  @Nullable
  private Duration duration;

  @JsonProperty("pending_duration")
  @Nullable
  private Duration pendingDuration;

  @JsonProperty("active_duration")
  @Nullable
  private Duration activeDuration;

  /**
   * Gets the information for this task.
   *
   * @return the info for this task
   */
  @Nullable
  public TaskInfoPojo getInfo() {
    return info;
  }

  /**
   * Sets the information for this task.
   *
   * @param info the info for this task
   * @return this for chaining
   */
  public TaskPojo setInfo(@Nullable TaskInfoPojo info) {
    this.info = info;
    return this;
  }

  /**
   * Gets the number of times this particular task has been attempted. When a task fails for a
   * reason that doesn't preclude it from being re-tried and it gets re-queued by the worker, its
   * total attempts counter will automatically be incremented as it is re-added to the associated
   * queue for later reprocessing.
   *
   * @return the total number of times this task has been attempted so far
   */
  public int getTotalAttempts() {
    return totalAttempts;
  }

  /**
   * Sets the number of times this particular task has been attempted. When a task fails for a
   * reason that doesn't preclude it from being re-tried and it gets re-queued by the worker, its
   * total attempts counter will automatically be incremented as it is re-added to the associated
   * queue for later reprocessing.
   *
   * @param totalAttempts the total number of times this task has been attempted so far
   * @return this for chaining
   */
  public TaskPojo setTotalAttempts(int totalAttempts) {
    this.totalAttempts = totalAttempts;
    return this;
  }

  /**
   * Gets the current state of the task.
   *
   * @return the current task state
   */
  @Nullable
  public String getState() {
    return state;
  }

  /**
   * Sets the current state of the task.
   *
   * @param state the current task state
   * @return this for chaining
   */
  public TaskPojo setState(@Nullable String state) {
    this.state = state;
    return this;
  }

  /**
   * Sets the current state of the task.
   *
   * @param state the current task state
   * @return this for chaining
   */
  public TaskPojo setState(@Nullable Task.State state) {
    this.state = (state != null) ? state.name() : null;
    return this;
  }

  /**
   * Gets the time when the first attempted task was queued.
   *
   * @return the time when the first attempted task was queued from the epoch
   */
  @Nullable
  public Instant getOriginalQueuedTime() {
    return originalQueuedTime;
  }

  /**
   * Sets the time when the first attempted task was queued.
   *
   * @param originalQueuedTime the time when the first attempted task was queued from the epoch
   * @return this for chaining
   */
  public TaskPojo setOriginalQueuedTime(@Nullable Instant originalQueuedTime) {
    this.originalQueuedTime = originalQueuedTime;
    return this;
  }

  /**
   * Gets the time when the task was queued. This will be the same time as reported by {@link
   * #getOriginalQueuedTime()} if the task has never been requeued after a failed attempt.
   *
   * @return the time when this task was queued from the epoch
   */
  @Nullable
  public Instant getQueuedTime() {
    return queuedTime;
  }

  /**
   * Sets the time when the task was queued. This will be the same time as reported by {@link
   * #getOriginalQueuedTime()} if the task has never been requeued after a failed attempt.
   *
   * @param queuedTime the time when this task was queued from the epoch
   * @return this for chaining
   */
  public TaskPojo setQueuedTime(@Nullable Instant queuedTime) {
    this.queuedTime = queuedTime;
    return this;
  }

  /**
   * Gets the time when the pojo was created. At that time it is expected that durations would have
   * been recomputed to accumulate time up to that moment.
   *
   * @return the time in milliseconds when this pojo was created and durations were re-computed
   */
  public long getFrozenTime() {
    return frozenTime;
  }

  /**
   * Sets the time when the pojo was created. At that time it is expected that durations would have
   * been recomputed to accumulate time up to that moment.
   *
   * @param frozenTime the time in milliseconds when this pojo was created and durations were
   *     re-computed
   * @return this for chaining
   */
  public TaskPojo setFrozenTime(long frozenTime) {
    this.frozenTime = frozenTime;
    return this;
  }

  /**
   * Gets the total amount of time since this task has been created until it has completed
   * successfully or not.
   *
   * @return the total duration for this task
   */
  @Nullable
  public Duration getDuration() {
    return duration;
  }

  /**
   * Sets the total amount of time since this task has been created until it has completed
   * successfully or not.
   *
   * @param duration the total duration for this task
   * @return this for chaining
   */
  public TaskPojo setDuration(@Nullable Duration duration) {
    this.duration = duration;
    return this;
  }

  /**
   * Gets the total amount of time this task has been waiting in queue (not counting the time it was
   * actively being processed).
   *
   * @return the total duration this task was in queue waiting
   */
  @Nullable
  public Duration getPendingDuration() {
    return pendingDuration;
  }

  /**
   * Sets the total amount of time this task has been waiting in queue (not counting the time it was
   * actively being processed).
   *
   * @param pendingDuration the total duration this task was in queue waiting
   * @return this for chaining
   */
  public TaskPojo setPendingDuration(@Nullable Duration pendingDuration) {
    this.pendingDuration = pendingDuration;
    return this;
  }

  /**
   * Gets the total amount of time this task has been actively being processed.
   *
   * @return the total duration this task was actively being processed
   */
  @Nullable
  public Duration getActiveDuration() {
    return activeDuration;
  }

  /**
   * Sets the total amount of time this task has been actively being processed.
   *
   * @param activeDuration the total duration this task was actively being processed
   * @return this for chaining
   */
  public TaskPojo setActiveDuration(@Nullable Duration activeDuration) {
    this.activeDuration = activeDuration;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        info,
        totalAttempts,
        state,
        originalQueuedTime,
        queuedTime,
        frozenTime,
        duration,
        pendingDuration,
        activeDuration);
  }

  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj) && (obj instanceof TaskPojo)) {
      final TaskPojo pojo = (TaskPojo) obj;

      return (totalAttempts == pojo.totalAttempts)
          && (frozenTime == pojo.frozenTime)
          && Objects.equals(info, pojo.info)
          && Objects.equals(state, pojo.state)
          && Objects.equals(originalQueuedTime, pojo.originalQueuedTime)
          && Objects.equals(queuedTime, pojo.queuedTime)
          && Objects.equals(duration, pojo.duration)
          && Objects.equals(pendingDuration, pojo.pendingDuration)
          && Objects.equals(activeDuration, pojo.activeDuration);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format(
        "TaskPojo[id=%s, version=%d, info=%s, totalAttempts=%d, state=%s, originalQueuedTime=%s, queuedTime=%s, frozenTime=%d, duration=%s, pendingDuration=%s, activeDuration=%s]",
        getId(),
        getVersion(),
        info,
        totalAttempts,
        state,
        originalQueuedTime,
        queuedTime,
        frozenTime,
        duration,
        pendingDuration,
        activeDuration);
  }
}
