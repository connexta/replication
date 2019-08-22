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

/** The different kind of sites that are currently supported. */
public enum SiteKind {
  /**
   * A tactical site typically smaller in size (e.g. portable laptop, deployment on a ship, ...).
   */
  TACTICAL,

  /** A regional site (e.g. east coast, west coast, ...). */
  REGIONAL,

  /**
   * The unknown value is used for forward compatibility where the current code might not be able to
   * understand a new kind of site and would map this new site to <code>UNKNOWN</code> and most
   * likely ignore it.
   */
  UNKNOWN
}
