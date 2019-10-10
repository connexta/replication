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

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/** Identifies the need for the resource associated with an intel to be transferred. */
public interface ResourceInfo extends DataInfo {
  /**
   * Gets the URI for the resource or empty if there is none or if it is not known.
   *
   * @return the optional URI for the resource
   */
  public Optional<URI> getUri();

  @Override
  public default boolean sameAs(Object obj) {
    if (obj instanceof ResourceInfo) {
      final ResourceInfo info = (ResourceInfo) obj;

      return DataInfo.super.sameAs(info) && Objects.equals(getUri(), info.getUri());
    }
    return false;
  }
}
