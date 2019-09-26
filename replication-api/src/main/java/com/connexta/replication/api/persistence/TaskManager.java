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
package com.connexta.replication.api.persistence;

import com.connexta.replication.api.data.Task;

/** A task manager capable of handling persistence operations for tasks. */
public interface TaskManager {
  /**
   * Deserializes JSON content from a given JSON content string into a task.
   *
   * @param clazz the class of task to deserialize to
   * @param content the Json content string to deserialize a task from
   * @return the deserialized task
   * @throws IllegalArgumentException if <code>clazz</code> implementation is not one that can be
   *     loaded
   * @throws com.connexta.replication.api.data.ParsingException if underlying input contains invalid
   *     content or the JSON structure does not match the expected task structure (or has other
   *     mismatch issues)
   * @throws com.connexta.replication.api.data.ProcessingException if a failure occurs while
   *     deserializing the value
   * @throws com.connexta.replication.api.data.ReplicationPersistenceException if any other error
   *     occurs while trying to deserialize the object
   */
  public <T extends Task> T readFrom(Class<T> clazz, String content);

  /**
   * Serializes a task into a JSON content string.
   *
   * @throws IllegalArgumentException if the task implementation is not one that can be saved
   * @throws com.connexta.replication.api.data.ProcessingException if a failure occurs while
   *     serializing the value
   * @throws com.connexta.replication.api.data.ReplicationPersistenceException if any other error
   *     occurs while trying to serialize the object
   */
  public String writeTo(Task task);
}
