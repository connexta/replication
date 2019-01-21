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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.codice.ddf.commands.catalog.SubjectCommands;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.SyncRequest;

@Service
@Command(
  scope = "replication",
  name = "status",
  description = "Display the statuses of replication runs"
)
public class StatusCommand extends SubjectCommands {

  private static final long MB_PER_BYTE = 1024L * 1024L;

  @Option(
    name = "--historyCount",
    aliases = {"-h"},
    description =
        "Include statuses of the most-recent completed replication runs up to the specified count (default = 10)"
  )
  int historyCount = 10;

  @Reference Replicator replicator;

  @Reference ReplicatorHistory replicatorHistory;

  @Override
  protected Object executeWithSubject() {
    final ShellTable shellTable = new ShellTable();
    shellTable.column("#");
    shellTable.column("Name");
    shellTable.column("Status");
    shellTable.column("Duration");
    shellTable.column("# Pulled");
    shellTable.column("# Pull Ingests Failed");
    shellTable.column("MB Pulled");
    shellTable.column("# Pushed");
    shellTable.column("# Push Ingests Failed");
    shellTable.column("MB Pushed");
    shellTable.column("Start Time");
    shellTable.emptyTableText("There are no running, pending, or completed replication jobs.");

    final List<ReplicationStatus> replicationStatuses = new ArrayList<>();
    replicationStatuses.addAll(
        replicator
            .getPendingSyncRequests()
            .stream()
            .map(SyncRequest::getStatus)
            .collect(Collectors.toSet()));
    replicationStatuses.addAll(
        replicator
            .getActiveSyncRequests()
            .stream()
            .map(SyncRequest::getStatus)
            .collect(Collectors.toSet()));
    replicationStatuses.addAll(
        replicatorHistory
            .getReplicationEvents()
            .stream()
            .limit(historyCount)
            .collect(Collectors.toList()));

    int rowCounter = 1;
    for (ReplicationStatus replicationStatus : replicationStatuses) {
      shellTable
          .addRow()
          .addContent(
              rowCounter++,
              replicationStatus.getReplicatorName(),
              replicationStatus.getStatus(),
              replicationStatus.getDuration(),
              replicationStatus.getPullCount(),
              replicationStatus.getPullFailCount(),
              String.format("%.2f", (double) replicationStatus.getPullBytes() / MB_PER_BYTE),
              replicationStatus.getPushCount(),
              replicationStatus.getPushFailCount(),
              String.format("%.2f", (double) replicationStatus.getPushBytes() / MB_PER_BYTE),
              replicationStatus.getStartTime());
    }

    shellTable.print(console);

    return null;
  }
}
