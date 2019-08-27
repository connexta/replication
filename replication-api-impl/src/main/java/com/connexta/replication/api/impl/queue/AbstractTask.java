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
package com.connexta.replication.api.impl.queue;

import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.TaskInfo;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an abstract implementation of a task which delegates all information-related methods to
 * an associated {@link TaskInfo} object.
 */
public abstract class AbstractTask implements Task {
  protected final TaskInfo info;

  /**
   * Creates an abstract task which wrapps around the specified task information.
   *
   * @param info the task info to wrap around
   */
  public AbstractTask(TaskInfo info) {
    this.info = info;
  }

  @Override
  public byte getPriority() {
    return info.getPriority();
  }

  @Override
  public String getId() {
    return info.getId();
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
}
