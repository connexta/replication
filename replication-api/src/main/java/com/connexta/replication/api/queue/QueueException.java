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
package com.connexta.replication.api.queue;

import com.connexta.replication.api.ReplicationException;

/** Base class for all queuing related exceptions. */
public class QueueException extends ReplicationException {
  /**
   * Create a new {@link QueueException}.
   *
   * @param message the exception message
   */
  public QueueException(String message) {
    super(message);
  }

  /**
   * Create a new {@link QueueException}.
   *
   * @param message the exception message
   * @param cause the exception cause
   */
  public QueueException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create a new {@link QueueException}.
   *
   * @param cause the exception cause
   */
  public QueueException(Throwable cause) {
    super(cause);
  }
}
