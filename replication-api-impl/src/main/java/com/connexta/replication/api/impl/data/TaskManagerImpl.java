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

import com.connexta.replication.api.ReplicationException;
import com.connexta.replication.api.data.ParsingException;
import com.connexta.replication.api.data.ProcessingException;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.impl.jackson.JsonUtils;
import com.connexta.replication.api.impl.persistence.pojo.TaskPojo;
import com.connexta.replication.api.persistence.TaskManager;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.micrometer.core.instrument.Clock;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/** Provides useful methods for persisting tasks. */
public class TaskManagerImpl implements TaskManager {
  private final Clock clock;

  /**
   * Instantiates a new task manager.
   *
   * @param clock the clock to use for retrieving wall and monotonic times
   */
  public TaskManagerImpl(Clock clock) {
    this.clock = clock;
  }

  @Override
  public <T extends Task> T readFrom(Class<T> clazz, String content) {
    if (!AbstractTaskImpl.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException(
          "expected an AbstractTaskImpl but got a " + clazz.getSimpleName());
    }
    final Constructor<T> ctor;

    try {
      ctor = clazz.getConstructor(TaskPojo.class, Clock.class);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("missing expected constructor", e);
    }
    final TaskPojo pojo;

    try {
      pojo = JsonUtils.read(TaskPojo.class, content);
    } catch (JsonParseException | JsonMappingException e) {
      throw new ParsingException(e);
    } catch (JsonProcessingException e) {
      throw new ProcessingException(e);
    }
    try {
      return ctor.newInstance(pojo, clock);
    } catch (InstantiationException e) {
      throw new IllegalArgumentException("invalid abstract class: " + clazz.getName(), e);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(
          "constructor is not accessible for class: " + clazz.getName(), e);
    } catch (InvocationTargetException e) {
      final Throwable t = e.getTargetException();

      if (t instanceof ReplicationException) {
        throw (ReplicationException) t;
      }
      throw new ProcessingException("failed to instantiate class: " + clazz.getName(), t);
    }
  }

  @Override
  public String writeTo(Task task) {
    if (!(task instanceof AbstractTaskImpl)) {
      throw new IllegalArgumentException(
          "expected an AbstractTaskImpl but got a " + task.getClass().getSimpleName());
    }
    try {
      return JsonUtils.write(((AbstractTaskImpl) task).writeTo(new TaskPojo()));
    } catch (JsonProcessingException e) {
      throw new ProcessingException(e);
    }
  }
}
