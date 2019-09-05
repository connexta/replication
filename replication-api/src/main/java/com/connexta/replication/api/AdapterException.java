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
package com.connexta.replication.api;

/** General exception for {@link NodeAdapter} errors. */
public class AdapterException extends RuntimeException {
  /**
   * Instantiates a new exception.
   *
   * @param msg the message for this exception
   */
  public AdapterException(String msg) {
    super(msg);
  }

  /**
   * Instantiates a new exception.
   *
   * @param msg the message for this exception
   * @param cause the cause for this exception
   */
  public AdapterException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Instantiates a new exception.
   *
   * @param cause the cause for this exception
   */
  public AdapterException(Throwable cause) {
    super(cause);
  }
}
