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

import java.io.InterruptedIOException;
import java.util.OptionalInt;

/** This exception is thrown when an adapter is interrupted while performing an operation. */
public class AdapterInterruptedException extends AdapterException {
  /** How many bytes had been transferred as part of the I/O operation before it was interrupted. */
  private final int bytesTransferred;

  /**
   * Instantiates a new exception.
   *
   * @param cause the cause for this exception
   */
  public AdapterInterruptedException(InterruptedException cause) {
    super(cause);
    this.bytesTransferred = -1;
  }

  /**
   * Instantiates a new exception.
   *
   * @param cause the cause for this exception
   */
  public AdapterInterruptedException(InterruptedIOException cause) {
    this(cause, cause);
  }

  /**
   * Instantiates a new exception.
   *
   * @param cause the cause for this exception
   * @param nested the nested cause for this exception
   */
  public AdapterInterruptedException(Throwable cause, InterruptedIOException nested) {
    super(cause);
    this.bytesTransferred = nested.bytesTransferred;
  }

  /**
   * Gets the number of bytes that were transferred before the operation was interrupted.
   *
   * @return the number of bytes that were transferred before the operation was interrupted or empty
   *     if unknown
   */
  public OptionalInt getBytesTransferred() {
    return (bytesTransferred >= 0) ? OptionalInt.of(bytesTransferred) : OptionalInt.empty();
  }
}
