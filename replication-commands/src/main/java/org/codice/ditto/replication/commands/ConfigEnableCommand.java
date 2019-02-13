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
import java.util.stream.Collectors;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.codice.ddf.commands.catalog.SubjectCommands;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;

@Service
@Command(
  scope = "replication",
  name = "enable",
  description = "Enables or suspends replication configurations"
)
public class ConfigEnableCommand extends SubjectCommands {

  @Option(name = "--suspend", description = "Suspend the given enabled configurations")
  boolean suspend = false;

  @Argument(
    name = "configNames",
    description = "Names of the replication configurations to enable/suspend.",
    multiValued = true
  )
  List<String> configNames;

  @Reference ReplicatorConfigManager replicatorConfigManager;

  @Reference Replicator replicator;

  @Override
  protected Object executeWithSubject() throws Exception {
    List<ReplicatorConfig> configs = replicatorConfigManager.objects().collect(Collectors.toList());
    for (String configName : configNames) {
      ReplicatorConfig replicatorConfig =
          configs.stream().filter(c -> c.getName().equals(configName)).findFirst().orElse(null);
      if (replicatorConfig == null) {
        printErrorMessage("No config found for name " + configName);
        continue;
      }
      replicatorConfig.setSuspended(suspend);
      replicatorConfigManager.save(replicatorConfig);
      if (suspend) {
        replicator.cancelSyncRequest(replicatorConfig.getId());
      }
    }
    return null;
  }
}
