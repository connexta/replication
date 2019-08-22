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
package com.connexta.replication.api.data;

/** Defines all possible reasons why a task would fail to be processed. */
public enum ErrorCode {
  // TODO: add more codes while implementing workers
  INTERNAL_ERROR(true),
  SITE_UNAVAILABLE(true),
  SITE_TIMEOUT(true),
  UPSTREAM_SERVICE_UNAVAILABLE(true),
  UPSTREAM_SERVICE_TIMEOUT(true),
  UNKNOWN_ERROR(false);

  private final boolean shouldBeRetried;

  private ErrorCode(boolean shouldBeRetried) {
    this.shouldBeRetried = shouldBeRetried;
  }

  /**
   * Checks whether or not a task that fails because of this error should be re-processed later in a
   * re-attempt to complete it.
   *
   * @return <code>true</code> if a task that fails because of this error should be retried; <code>
   *     false</code> if not
   */
  public boolean shouldBeRetried() {
    return shouldBeRetried;
  }
}
