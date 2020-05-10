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
package org.codice.ditto.replication.api;

/** Exception used to wrap exceptions thrown by querying a ddf. */
public class QueryException extends RuntimeException {

  /**
   * Creates an Exception with a message and cause
   *
   * @param message A message describing why the exception was thrown
   * @param cause An exception that will be wrapped by this one
   */
  public QueryException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates an Exception with a cause
   *
   * @param cause An exception that will be wrapped by this one
   */
  public QueryException(Throwable cause) {
    super(cause);
  }
}
