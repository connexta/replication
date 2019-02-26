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
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.codice.ddf.commands.catalog.SubjectCommands;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;

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

  @Reference ReplicatorConfigLoader replicatorConfigLoader;

  @Reference Replicator replicator;

  @Override
  protected Object executeWithSubject() throws Exception {
    for (String config : configNames) {
      ReplicatorConfig replicatorConfig = replicatorConfigLoader.getConfig(config).orElse(null);
      if (replicatorConfig == null) {
        printErrorMessage("No config found for name " + config);
        continue;
      }
      ReplicatorConfigImpl newConfig = new ReplicatorConfigImpl(replicatorConfig);
      newConfig.setSuspended(suspend);
      replicatorConfigLoader.saveConfig(newConfig);
      if (suspend) {
        replicator.cancelSyncRequest(newConfig.getId());
      }
    }
    return null;
  }
}
