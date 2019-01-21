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
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.SyncRequestImpl;

@Service
@Command(scope = "replication", name = "run", description = "Run replication configurations")
public class RunCommand extends ExistingConfigsCommands {

  @Reference Replicator replicator;

  @Override
  void executeWithExistingConfig(final String configName, ReplicatorConfig config) {
    final SyncRequestImpl syncRequest =
        new SyncRequestImpl(config, new ReplicationStatus(configName));
    try {
      replicator.submitSyncRequest(syncRequest);
      printSuccessMessage(
          "A replication request was submitted for the replication configuration with the name \""
              + configName
              + "\".");
    } catch (InterruptedException e) {
      printErrorMessage(
          "There was an interrupted exception while submitting replication request for the replication configuration with the name \""
              + configName
              + "\". See logs for more details.");
      Thread.currentThread().interrupt();
    }
  }
}
