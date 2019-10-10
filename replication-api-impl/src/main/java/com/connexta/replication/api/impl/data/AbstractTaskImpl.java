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
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.TaskInfo;
import com.connexta.replication.api.data.UnsupportedVersionException;
import com.connexta.replication.api.impl.persistence.pojo.TaskInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.TaskPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownPojo;
import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Describes a piece of intel along with information about how to replicate it. This is picked up by
 * an item worker.
 */
public abstract class AbstractTaskImpl extends AbstractPersistable<TaskPojo> implements Task {
  private static final String PERSISTABLE_TYPE = "task";

  protected final Clock clock;

  private TaskInfo info;

  private byte priority;

  protected int attempts = 1;
  protected State state = State.PENDING;

  private Instant originalQueuedTime;
  protected volatile long queueTime;

  /**
   * Start time in nanoseconds to calculate additional duration time from for the current state.
   *
   * <p><i>Note:</i> The delta between now and this time gets added to the duration that corresponds
   * to the current state.
   */
  protected volatile long startTime;

  // durations are all in nanoseconds
  protected volatile Duration duration = Duration.ZERO;
  protected volatile Duration pendingDuration = Duration.ZERO;
  protected volatile Duration activeDuration = Duration.ZERO;

  private boolean hasUnknowns = false;

  /**
   * Creates a new task for the corresponding task information as it is being queued to a given
   * queue.
   *
   * @param info the corresponding task info
   * @param clock the clock to use for retrieving wall and monotonic times
   */
  public AbstractTaskImpl(TaskInfo info, Clock clock) {
    super(AbstractTaskImpl.PERSISTABLE_TYPE);
    this.clock = clock;
    this.info = info;
    this.priority =
        (byte)
            Math.min(
                Math.max(info.getPriority(), TaskInfoImpl.MIN_PRIORITY), TaskInfoImpl.MAX_PRIORITY);
    final long now = clock.wallTime();

    this.startTime = clock.monotonicTime();
    this.queueTime = now;
    this.originalQueuedTime = Instant.ofEpochMilli(now);
  }

  /**
   * Instantiates a task based on the information provided by the specified pojo.
   *
   * @param pojo the pojo to initializes the site with
   * @param clock the clock to use for retrieving wall and monotonic times
   */
  public AbstractTaskImpl(TaskPojo pojo, Clock clock) {
    super(AbstractTaskImpl.PERSISTABLE_TYPE, null);
    this.clock = clock;
    readFrom(pojo);
  }

  @Override
  public byte getPriority() {
    return priority;
  }

  @Override
  public String getIntelId() {
    return info.getIntelId();
  }

  @Override
  public OperationType getOperation() {
    return info.getOperation();
  }

  @Override
  public Instant getLastModified() {
    return info.getLastModified();
  }

  @Override
  public Optional<ResourceInfo> getResource() {
    return info.getResource();
  }

  @Override
  public Stream<MetadataInfo> metadatas() {
    return info.metadatas();
  }

  @Override
  public int getTotalAttempts() {
    return attempts;
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public Instant getOriginalQueuedTime() {
    return originalQueuedTime;
  }

  @Override
  public Instant getQueuedTime() {
    return Instant.ofEpochMilli(queueTime);
  }

  @Override
  public Duration getDuration() {
    return getDuration(clock::monotonicTime);
  }

  @Override
  public Duration getPendingDuration() {
    return getPendingDuration(clock::monotonicTime);
  }

  @Override
  public Duration getActiveDuration() {
    return getActiveDuration(clock::monotonicTime);
  }

  /**
   * Checks if this task contains unknown information.
   *
   * @return <code>true</code> if it contains unknown info; <code>false</code> otherwise
   */
  public boolean hasUnknowns() {
    return hasUnknowns;
  }

  /**
   * Gets the information for this task.
   *
   * @return the corresponding task info
   */
  public TaskInfo getInfo() {
    return info;
  }

  @Override
  public int hashCode() {
    return hashCode0();
  }

  @Override
  public boolean equals(Object obj) {
    return equals0(obj);
  }

  @Override
  public String toString() {
    return String.format(
        "TaskImpl[id=%s, priority=%d, info=%s, totalAttempts=%d, state=%s, originalQueuedTime=%s, queuedTime=%s, startTime=%d, duration=%s, pendingDuration=%s, activeDuration=%s]",
        getId(),
        priority,
        info,
        attempts,
        state,
        originalQueuedTime,
        queueTime,
        startTime,
        duration,
        pendingDuration,
        activeDuration);
  }

  @Override
  protected TaskPojo writeTo(TaskPojo pojo) {
    if (hasUnknowns()) { // cannot serialize if it contains unknowns
      throw new InvalidFieldException("unknown task");
    }
    super.writeTo(pojo);
    convertAndSetOrFailIfNull("info", this::getInfo, AbstractTaskImpl::toPojo, pojo::setInfo);
    convertAndSetOrFailIfNull("state", this::getState, State::name, pojo::setState);
    setOrFailIfNull("originalQueuedTime", this::getOriginalQueuedTime, pojo::setOriginalQueuedTime);
    setOrFailIfNull("queuedTime", this::getQueuedTime, pojo::setQueuedTime);
    // freeze time so we can serialize this task with durations calculated up to that time
    final long now = clock.wallTime();
    final long monotonicTime = clock.monotonicTime();

    setOrFailIfNull("duration", () -> getDuration(() -> monotonicTime), pojo::setDuration);
    setOrFailIfNull(
        "pendingDuration", () -> getPendingDuration(() -> monotonicTime), pojo::setPendingDuration);
    setOrFailIfNull(
        "activeDuration", () -> getActiveDuration(() -> monotonicTime), pojo::setActiveDuration);
    return pojo.setVersion(TaskPojo.CURRENT_VERSION).setTotalAttempts(attempts).setFrozenTime(now);
  }

  @Override
  protected final void readFrom(TaskPojo pojo) {
    super.readFrom(pojo);
    if (pojo.getVersion() < TaskPojo.MINIMUM_VERSION) {
      throw new UnsupportedVersionException(
          "unsupported "
              + AbstractTaskImpl.PERSISTABLE_TYPE
              + " version: "
              + pojo.getVersion()
              + " for object: "
              + getId());
    } // do support pojo.getVersion() > CURRENT_VERSION for forward compatibility
    this.hasUnknowns = pojo instanceof UnknownPojo; // reset the unknown flag
    readFromCurrentOrFutureVersion(pojo);
    this.priority =
        (byte)
            Math.min(
                Math.max(info.getPriority(), TaskInfoImpl.MIN_PRIORITY), TaskInfoImpl.MAX_PRIORITY);
  }

  @VisibleForTesting
  void setInfo(TaskInfoImpl info) {
    this.info = info;
    this.hasUnknowns |= info.hasUnknowns();
  }

  @VisibleForTesting
  void setTotalAttempts(int attempts) {
    this.attempts = attempts;
  }

  @VisibleForTesting
  void setState(State state) {
    this.state = state;
  }

  @VisibleForTesting
  void setOriginalQueuedTime(Instant originalQueuedTime) {
    this.originalQueuedTime = originalQueuedTime;
  }

  @VisibleForTesting
  void setQueuedTime(Instant queueTime) {
    this.queueTime = queueTime.toEpochMilli();
  }

  @VisibleForTesting
  void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  @VisibleForTesting
  void setDuration(Duration duration) {
    this.duration = duration;
  }

  @VisibleForTesting
  void setPendingDuration(Duration pendingDuration) {
    this.pendingDuration = pendingDuration;
  }

  @VisibleForTesting
  void setActiveDuration(Duration activeDuration) {
    this.activeDuration = activeDuration;
  }

  private void readFromCurrentOrFutureVersion(TaskPojo pojo) {
    convertAndSetOrFailIfNull("info", pojo::getInfo, AbstractTaskImpl::fromPojo, this::setInfo);
    convertAndSetEnumValueOrFailIfNullOrEmpty(
        "state", State.class, State.UNKNOWN, pojo::getState, this::setState);
    setOrFailIfNull("originalQueuedTime", pojo::getOriginalQueuedTime, this::setOriginalQueuedTime);
    setOrFailIfNull("queuedTime", pojo::getQueuedTime, this::setQueuedTime);
    final long now = clock.wallTime();
    final long monotonicTime = clock.monotonicTime();
    // get the frozen time and re-calculate durations to include the time spent frozen
    final long frozenMillis = Math.min(now - pojo.getFrozenTime(), 0L); // don't go back in time

    setOrFailIfNull("duration", pojo::getDuration, d -> setDuration(d, frozenMillis));
    setOrFailIfNull(
        "pendingDuration", pojo::getPendingDuration, d -> setPendingDuration(d, frozenMillis));
    setOrFailIfNull(
        "activeDuration", pojo::getActiveDuration, d -> setActiveDuration(d, frozenMillis));
    this.startTime = monotonicTime; // restart our internal time
    this.attempts = pojo.getTotalAttempts();
  }

  private Duration getDuration(LongSupplier monotonicTime) {
    final long s = this.startTime;
    final Duration d = this.duration;

    switch (state) {
      case PENDING:
      case ACTIVE:
        return d.plusNanos(monotonicTime.getAsLong() - s);
      case FAILED:
      case SUCCESSFUL:
      default:
        return d;
    }
  }

  private Duration getPendingDuration(LongSupplier monotonicTime) {
    final long s = this.startTime;
    final Duration d = this.pendingDuration;

    switch (state) {
      case PENDING:
        return d.plusNanos(monotonicTime.getAsLong() - s);
      case ACTIVE:
      case FAILED:
      case SUCCESSFUL:
      default:
        return d;
    }
  }

  private Duration getActiveDuration(LongSupplier monotonicTime) {
    final long s = this.startTime;
    final Duration d = this.activeDuration;

    switch (state) {
      case ACTIVE:
        return d.plusNanos(monotonicTime.getAsLong() - s);
      case PENDING:
      case FAILED:
      case SUCCESSFUL:
      default:
        return d;
    }
  }

  private void setDuration(Duration duration, long millis) {
    switch (state) {
      case PENDING:
      case ACTIVE:
        this.duration = duration.plusMillis(millis);
        break;
      case FAILED:
      case SUCCESSFUL:
      default:
        this.duration = duration;
    }
  }

  private void setPendingDuration(Duration pendingDuration, long millis) {
    switch (state) {
      case PENDING:
        this.pendingDuration = pendingDuration.plusMillis(millis);
        break;
      case ACTIVE:
      case FAILED:
      case SUCCESSFUL:
      default:
        this.pendingDuration = pendingDuration;
    }
  }

  private void setActiveDuration(Duration activeDuration, long millis) {
    switch (state) {
      case ACTIVE:
        this.activeDuration = activeDuration.plusMillis(millis);
        break;
      case PENDING:
      case FAILED:
      case SUCCESSFUL:
      default:
        this.activeDuration = activeDuration;
    }
  }

  @VisibleForTesting
  int hashCode0() {
    return Objects.hash(
        super.hashCode(),
        info,
        priority,
        attempts,
        state,
        queueTime,
        originalQueuedTime,
        startTime,
        duration,
        pendingDuration,
        activeDuration);
  }

  @VisibleForTesting
  boolean equals0(Object obj) {
    if (super.equals(obj) && (obj instanceof AbstractTaskImpl)) {
      final AbstractTaskImpl task = (AbstractTaskImpl) obj;

      return (attempts == task.attempts)
          && (priority == task.priority)
          && (state == task.state)
          && (queueTime == task.queueTime)
          && (startTime == task.startTime)
          && Objects.equals(info, task.info)
          && Objects.equals(originalQueuedTime, task.originalQueuedTime)
          && Objects.equals(duration, task.duration)
          && Objects.equals(pendingDuration, task.pendingDuration)
          && Objects.equals(activeDuration, task.activeDuration);
    }
    return false;
  }

  private static TaskInfoImpl fromPojo(@Nullable TaskInfoPojo pojo) {
    return new TaskInfoImpl(pojo);
  }

  private static TaskInfoPojo toPojo(TaskInfo info) {
    return AbstractTaskImpl.wrap(info).writeTo(new TaskInfoPojo());
  }

  private static TaskInfoImpl wrap(TaskInfo info) {
    return (info instanceof TaskInfoImpl) ? (TaskInfoImpl) info : new TaskInfoImpl(info);
  }
}
