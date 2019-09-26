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

import java.util.Objects;

/** Identifies a particular metadata to be transferred. */
public interface MetadataInfo extends DataInfo {
  /**
   * Gets the type of metadata to be transferred (e.g. metacard, DDMS 2.0, DDMS 5.0, ...).
   *
   * @return the type of metadata to be transferred
   */
  public String getType();

  @Override
  public default boolean sameAs(Object obj) {
    if (obj instanceof MetadataInfo) {
      final MetadataInfo info = (MetadataInfo) obj;

      return DataInfo.super.sameAs(info) && Objects.equals(getType(), info.getType());
    }
    return false;
  }
}
