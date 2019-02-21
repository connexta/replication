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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.codice.ddf.commands.catalog.SubjectCommands;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;

@Service
@Command(
  scope = "replication",
  name = "config-list",
  description = "List replication configurations"
)
public class ConfigListCommand extends SubjectCommands {

  @Reference ReplicatorConfigLoader replicatorConfigLoader;

  @Reference ReplicationSitePersistentStore siteStore;

  @Override
  protected Object executeWithSubject() {
    final ShellTable shellTable = new ShellTable();
    shellTable.column("Name");
    shellTable.column("Direction");
    shellTable.column("Type");
    shellTable.column("Failure Retry Count");
    shellTable.column("Source");
    shellTable.column("Destination");
    shellTable.column("CQL");
    shellTable.column("Description");
    shellTable.emptyTableText("There are no current replication configurations.");

    for (ReplicatorConfig replicatorConfig : replicatorConfigLoader.getAllConfigs()) {
      shellTable
          .addRow()
          .addContent(
              replicatorConfig.getName(),
              replicatorConfig.getDirection(),
              replicatorConfig.getReplicationType(),
              replicatorConfig.getFailureRetryCount(),
              siteStore.getSite(replicatorConfig.getSource()).get().getUrl(),
              siteStore.getSite(replicatorConfig.getDestination()).get().getUrl(),
              replicatorConfig.getCql(),
              replicatorConfig.getDescription());
    }

    shellTable.print(console);

    return null;
  }
}
