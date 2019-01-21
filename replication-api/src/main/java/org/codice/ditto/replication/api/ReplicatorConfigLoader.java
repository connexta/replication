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

import java.util.List;
import java.util.Optional;

public interface ReplicatorConfigLoader {

  /**
   * Get the replicator configuration associated with the given id
   *
   * @param id The replicator configuration id
   * @return The {@link Optional<ReplicatorConfig>} or {@link Optional#empty()} if there are no
   *     configurations with that id
   */
  Optional<ReplicatorConfig> getConfig(String id);

  /**
   * Gets all the replicator configurations
   *
   * @return List of all replicator configurations
   */
  List<ReplicatorConfig> getAllConfigs();

  /**
   * Updates an existing ReplicatorConfig with the same {@link ReplicatorConfig#getId()} if there is
   * one. Else, saves the {@link ReplicatorConfig}.
   *
   * @param replicationConfig The configuration to save or update
   */
  void saveConfig(ReplicatorConfig replicationConfig);

  /**
   * Deletes a Replicator configuration
   *
   * @param replicationConfig The configuration to be removed
   */
  void removeConfig(ReplicatorConfig replicationConfig);
}
