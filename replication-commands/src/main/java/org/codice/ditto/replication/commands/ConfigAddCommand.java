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

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.codice.ddf.commands.catalog.SubjectCommands;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.modern.ReplicationSite;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;
import org.codice.ditto.replication.commands.completers.DirectionStringCompleter;
import org.codice.ditto.replication.commands.completers.ReplicationTypeCompleter;

@Service
@Command(
  scope = "replication",
  name = "config-add",
  description = "Add a new replication configuration"
)
public class ConfigAddCommand extends SubjectCommands {

  @Argument(
    name = "configurationName",
    description = "Name of the new replication configuration",
    required = true
  )
  String configName = null;

  @Option(
    name = "--direction",
    description =
        "Valid values are \"PUSH\", \"PULL\", or \"BOTH\", where\n\t"
            + "\"PUSH\" indicates that records in the source catalog will be replicated to the destination system,\n\t"
            + "\"PULL\" (Deprecated) indicates that records from the destination system will be replicated to the source catalog, and\n\t"
            + "\"BOTH\" indicates that records from each system will be replicated to the other.",
    required = true
  )
  @Completion(DirectionStringCompleter.class)
  String directionString = null;

  @Option(
    name = "--replicationType",
    aliases = {"-t"},
    description =
        "Valid values are \"METACARD\" or \"RESOURCE\", where\n\t"
            + "\"METACARD\" indicates that only metacards will be replicated and\n\t"
            + "\"RESOURCE\" indicates that both metacards and resources will be replicated.",
    required = true
  )
  @Completion(ReplicationTypeCompleter.class)
  String replicationType = null;

  @Option(
    name = "--destination",
    aliases = "--url", // for backwards compatibility
    description = "URL of a destination system to which to replicate",
    required = true
  )
  String destination = null;

  @Option(name = "--source", description = "URL of a source system from which to replicate")
  String source = null;

  @Option(
    name = "--cql",
    description =
        "Replicate only records that match a CQL Filter expression.\n\n"
            + "CQL Examples:\n\t"
            + "Textual:   --cql \"title like 'some text'\"\n\t"
            + "Temporal:  --cql \"modified before 2012-09-01T12:30:00Z\"\n\t"
            + "Spatial:   --cql \"DWITHIN(location, POINT (1 2) , 10, kilometers)\"\n\t"
            + "Complex:   --cql \"title like 'some text' AND modified before 2012-09-01T12:30:00Z\"",
    required = true
  )
  String cqlFilter = null;

  @Option(name = "--description", description = "Description of this replication configuration")
  String description = null;

  @Option(
    name = "--failureRetryCount",
    aliases = {"-f"},
    description = "Amount of times to retry replication on an item that previously failed."
  )
  int failureRetryCount = 5;

  @Option(
    name = "--suspend",
    description =
        "Suspend this configuration. Suspended configurations will not be run. Defaults to false"
  )
  boolean suspend = false;

  @Reference ReplicatorConfigLoader replicatorConfigLoader;

  @Reference ReplicationSitePersistentStore siteStore;

  @Override
  @SuppressWarnings(
      "squid:S3516" /*Method signature requires a return value but none is needed here*/)
  protected final Object executeWithSubject() {
    if (replicatorConfigLoader.getConfig(configName).isPresent()) {
      printErrorMessage(
          "A replication configuration already exists with the name \"" + configName + "\".");
      printErrorMessage("A new replication configuration was not added.");
    } else {
      final ReplicatorConfigImpl config = new ReplicatorConfigImpl();
      config.setName(configName);
      config.setCql(cqlFilter);

      try {
        config.setDestination(getOrCreateSite(destination).getId());
      } catch (MalformedURLException e) {
        printErrorMessage("The URL \"" + destination + "\" is malformed: " + e.getMessage());
        printErrorMessage("A new replication configuration was not added.");
        return null;
      }

      try {
        if (source == null) {
          source = SystemBaseUrl.INTERNAL.getBaseUrl();
        }
        config.setSource(getOrCreateSite(source).getId());
      } catch (MalformedURLException e) {
        printErrorMessage("The URL \"" + source + "\" is malformed: " + e.getMessage());
        printErrorMessage("A new replication configuration was not added.");
        return null;
      }
      config.setDescription(description);
      config.setReplicationType(ReplicationType.valueOf(replicationType.toUpperCase()));

      if (Direction.PULL.equals(Direction.valueOf(directionString.toUpperCase()))) {
        printErrorMessage(
            "The PULL direction is deprecated. PULL can be achieved with a PUSH and switching the source/destination. Switching source/destination and changing direction to PUSH.");
        String tmpDest = config.getDestination();
        config.setDestination(config.getSource());
        config.setSource(tmpDest);
        config.setDirection(Direction.PUSH);
      } else {
        config.setDirection(Direction.valueOf(directionString.toUpperCase()));
      }
      config.setFailureRetryCount(failureRetryCount);
      config.setSuspended(suspend);

      replicatorConfigLoader.saveConfig(config);
      printSuccessMessage(
          "A new replication configuration with the name \"" + configName + "\" was added.");
    }

    return null;
  }

  private ReplicationSite getOrCreateSite(String site) throws MalformedURLException {
    ReplicationSite replicationSite =
        siteStore
            .getSites()
            .stream()
            .filter(s -> s.getUrl().toString().equals(site))
            .findFirst()
            .orElse(null);
    if (replicationSite != null) {
      return replicationSite;
    } else {
      URL url = new URL(site);
      return siteStore.saveSite(url.getHost(), site);
    }
  }
}
