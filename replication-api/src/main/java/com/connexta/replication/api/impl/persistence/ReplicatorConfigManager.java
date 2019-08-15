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
package com.connexta.replication.api.impl.persistence;

import com.connexta.replication.api.data.ReplicatorConfig;

/** Performs CRUD operations for {@link ReplicatorConfig}. */
public interface ReplicatorConfigManager extends DataManager<ReplicatorConfig> {

  /**
   * @param configId unique id of {@link ReplicatorConfig}
   * @return {@code true} if the config exists, otherwise {@code false}.
   */
  boolean configExists(String configId);
}
