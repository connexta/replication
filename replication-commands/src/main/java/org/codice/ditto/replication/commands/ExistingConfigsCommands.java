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
package org.codice.ditto.replication.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.codice.ddf.commands.catalog.SubjectCommands;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;

abstract class ExistingConfigsCommands extends SubjectCommands {

  @Argument(
    name = "configNames",
    description = "Names of the replication configurations",
    multiValued = true,
    required = true
  )
  List<String> configNames;

  @Reference ReplicatorConfigManager replicatorConfigManager;

  @Override
  protected final Object executeWithSubject() throws Exception {
    List<ReplicatorConfig> configs = replicatorConfigManager.objects().collect(Collectors.toList());
    for (final String configName : new HashSet<>(configNames)) {
      final Optional<ReplicatorConfig> config =
          configs.stream().filter(c -> c.getName().equals(configName)).findFirst();

      if (config.isPresent()) {
        executeWithExistingConfig(configName, config.get());
      } else {
        printErrorMessage(
            "There are no replication configurations with the name \""
                + configName
                + "\". Nothing was executed for that name.");
      }
    }

    return null;
  }

  abstract void executeWithExistingConfig(final String configName, final ReplicatorConfig config);
}
