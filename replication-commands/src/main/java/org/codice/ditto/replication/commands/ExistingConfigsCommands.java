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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.codice.ddf.commands.catalog.CommandSupport;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;

abstract class ExistingConfigsCommands extends CommandSupport {

  @Argument(
      name = "configNames",
      description = "Names of the replication configurations",
      multiValued = true,
      required = true)
  List<String> configNames;

  @Reference ReplicatorConfigManager replicatorConfigManager;

  @Override
  public Object execute() throws Exception {
    final Map<String, ReplicatorConfig> configs =
        replicatorConfigManager
            .objects()
            .collect(Collectors.toMap(ReplicatorConfig::getName, Function.identity()));

    configNames.stream()
        .distinct()
        .forEach(config -> executeWithExistingConfig(configs.get(config)));
    return null;
  }

  private void checkAndExecute(String configName, ReplicatorConfig config) {
    if (config == null) {
      printErrorMessage(
          "There are no replication configurations with the name \""
              + configName
              + "\". Nothing was executed for that name.");
      return;
    }
    executeWithExistingConfig(config);
  }

  /**
   * Executes the command for the specified configuration and sites.
   *
   * @param config the replication configuration for which to execute the command
   */
  protected abstract void executeWithExistingConfig(ReplicatorConfig config);
}
