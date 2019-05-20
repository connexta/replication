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
import org.codice.ditto.replication.api.Heartbeater;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.impl.data.SyncRequestImpl;

@Service
@Command(scope = "replication", name = "run", description = "Run replication configurations")
public class RunCommand extends ExistingConfigsCommands {

  @Reference Replicator replicator;
  @Reference Heartbeater heartbeater;

  @Override
  protected void executeWithExistingConfig(
      ReplicatorConfig config, ReplicationSite source, ReplicationSite destination) {
    if (source.isRemoteManaged() || destination.isRemoteManaged()) {
      heartbeatConfig(config, source, destination);
    } else {
      // none of them are remote or they have not been verified yet
      // either way, let the replicator handle it as it will verify them first if need be
      replicateConfig(config, source, destination);
    }
  }

  private void replicateConfig(
      ReplicatorConfig config, ReplicationSite source, ReplicationSite destination) {
    final String configName = config.getName();
    final SyncRequestImpl syncRequest =
        new SyncRequestImpl(config, source, destination, new ReplicationStatusImpl(configName));

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

  private void heartbeatConfig(
      ReplicatorConfig config, ReplicationSite source, ReplicationSite destination) {
    try {
      if (source.isRemoteManaged() || (source.getVerifiedUrl() == null)) {
        heartbeater.heartbeat(source);
        printSuccessMessage(
            "A heartbeat request was submitted for source \""
                + source.getName()
                + "\" of replication \""
                + config.getName()
                + "\".");
      }
      if (destination.isRemoteManaged() || (destination.getVerifiedUrl() == null)) {
        heartbeater.heartbeat(destination);
        printSuccessMessage(
            "A heartbeat request was submitted for destination \""
                + destination.getName()
                + "\" of replication \""
                + config.getName()
                + "\".");
      }
    } catch (InterruptedException e) {
      printErrorMessage(
          "There was an interrupted exception while submitting heartbeat requests for the replication configuration with the name \""
              + config.getName()
              + "\". See logs for more details.");
      Thread.currentThread().interrupt();
    }
  }
}
