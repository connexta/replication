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
package com.connexta.replication.api.data.ddf;

import com.connexta.replication.api.data.MetadataInfo;

/**
 * Extension to the {@link MetadataInfo} class which provides additional collected information that
 * can be useful for a worker processing an associated task.
 *
 * @param <T> the type of raw data
 */
public interface DdfMetadataInfo<T> extends MetadataInfo {
  /**
   * The class format for the raw data defining the metadata.
   *
   * @return the class for the raw data
   */
  public Class<T> getDataClass();

  /**
   * The raw data defining the metadata.
   *
   * @return the raw data
   */
  public T getData();
}
