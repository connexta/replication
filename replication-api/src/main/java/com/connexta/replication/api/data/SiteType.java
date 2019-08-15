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

/** Type of sites. */
public enum SiteType {
  DDF(true),
  ION(false),

  /**
   * The unknown value is used for forward compatibility where the current code might not be able to
   * understand a new type of site and would mapped this new site to <code>UNKNOWN</code> and most
   * likely ignore it.
   */
  UNKNOWN(false);

  /**
   * Flag indicating if this type of site must be polled to discover changes or if polling is not
   * required as sites of this type supports pushing their changes.
   */
  private final boolean mustBePolled;

  /**
   * Instantiates a type of site.
   *
   * @param mustBePolled <code>true</code> if sites of this type must be polled for changes; <code>
   *     false</code> if they support pushing their changes
   */
  private SiteType(boolean mustBePolled) {
    this.mustBePolled = mustBePolled;
  }

  /**
   * Checks if sites of this type must be polled to discover changes or if polling is not * required
   * as sites of this type supports pushing their changes.
   *
   * @return <code>true</code> if sites of this type must be polled for changes; <code>false</code>
   *     if they support pushing their changes
   */
  public boolean mustBePolled() {
    return mustBePolled;
  }
}
