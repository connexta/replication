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
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;

@Service
@Command(
  scope = "replication",
  name = "config-list",
  description = "List replication configurations"
)
public class ConfigListCommand extends SubjectCommands {

  @Reference ReplicatorConfigManager replicatorConfigManager;

  @Reference SiteManager siteManager;

  @Override
  protected Object executeWithSubject() {
    final ShellTable shellTable = new ShellTable();
    shellTable.column("Name");
    shellTable.column("Enabled");
    shellTable.column("Direction");
    shellTable.column("Type");
    shellTable.column("Failure Retry Count");
    shellTable.column("Source");
    shellTable.column("Destination");
    shellTable.column("CQL");
    shellTable.column("Description");
    shellTable.column("Version");

    shellTable.emptyTableText("There are no current replication configurations.");

    replicatorConfigManager.objects().forEach(config -> addConfigRow(config, shellTable));

    shellTable.print(console);

    return null;
  }

  private void addConfigRow(ReplicatorConfig replicatorConfig, ShellTable shellTable) {
    shellTable
        .addRow()
        .addContent(
            replicatorConfig.getName(),
            !replicatorConfig.isSuspended(),
            replicatorConfig.isBidirectional() ? Direction.BOTH : Direction.PUSH,
            ReplicationType.RESOURCE,
            replicatorConfig.getFailureRetryCount(),
            getUrlFromSite(replicatorConfig.getSource()),
            getUrlFromSite(replicatorConfig.getDestination()),
            replicatorConfig.getFilter(),
            replicatorConfig.getDescription(),
            replicatorConfig.getVersion());
  }

  private String getUrlFromSite(String id) {
    ReplicationSite site = siteManager.get(id);
    if (site != null) {
      return site.getUrl();
    }
    return null;
  }
}
