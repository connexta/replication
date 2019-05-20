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
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.codice.ddf.commands.catalog.SubjectCommands;
import org.codice.ditto.replication.api.data.Persistable;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;

abstract class ExistingConfigsCommands extends SubjectCommands {

  @Argument(
    name = "configNames",
    description = "Names of the replication configurations",
    multiValued = true,
    required = true
  )
  List<String> configNames;

  @Reference ReplicatorConfigManager replicatorConfigManager;

  @Reference SiteManager siteManager;

  @Override
  protected final Object executeWithSubject() throws Exception {
    final Map<String, ReplicationSite> sites =
        siteManager.objects().collect(Collectors.toMap(Persistable::getId, Function.identity()));
    final Map<String, ReplicatorConfig> configs =
        replicatorConfigManager
            .objects()
            .collect(Collectors.toMap(ReplicatorConfig::getName, Function.identity()));

    configNames
        .stream()
        .distinct()
        .map(config -> Pair.of(config, configs.get(config)))
        .forEach(config -> executeWithExistingConfig(config.getKey(), config.getValue(), sites));
    return null;
  }

  private void executeWithExistingConfig(
      String configName, @Nullable ReplicatorConfig config, Map<String, ReplicationSite> sites) {
    if (config == null) {
      printErrorMessage(
          "There are no replication configurations with the name \""
              + configName
              + "\". Nothing was executed for that name.");
      return;
    }
    final ReplicationSite source = sites.get(config.getSource());
    final ReplicationSite destination = sites.get(config.getDestination());

    if (source == null) {
      printErrorMessage(
          "Unable to determine the source \""
              + config.getSource()
              + "\" for replication \""
              + configName
              + "\". Nothing was executed for that name.");
    } else if (destination == null) {
      printErrorMessage(
          "Unable to determine the destination \""
              + config.getDestination()
              + "\" for replication \""
              + configName
              + "\". Nothing was executed for that name.");
    } else {
      executeWithExistingConfig(config, source, destination);
    }
  }

  /**
   * Executes the command for the specified configuration and sites.
   *
   * @param config the replication configuration for which to execute the command
   * @param source the source replication site associated with the configuration
   * @param destination the destination replication site associated with the configuration
   */
  protected abstract void executeWithExistingConfig(
      ReplicatorConfig config, ReplicationSite source, ReplicationSite destination);
}
