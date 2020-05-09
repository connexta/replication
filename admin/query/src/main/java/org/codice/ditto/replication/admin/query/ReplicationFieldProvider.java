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
package org.codice.ditto.replication.admin.query;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.common.fields.base.function.BaseFieldProvider;
import org.codice.ditto.replication.admin.query.replications.discover.GetReplications;
import org.codice.ditto.replication.admin.query.replications.persist.CancelReplication;
import org.codice.ditto.replication.admin.query.replications.persist.CreateReplication;
import org.codice.ditto.replication.admin.query.replications.persist.DeleteReplication;
import org.codice.ditto.replication.admin.query.replications.persist.SuspendReplication;
import org.codice.ditto.replication.admin.query.replications.persist.UpdateReplication;
import org.codice.ditto.replication.admin.query.sites.discover.GetReplicationSites;
import org.codice.ditto.replication.admin.query.sites.persist.CreateReplicationSite;
import org.codice.ditto.replication.admin.query.sites.persist.DeleteReplicationSite;
import org.codice.ditto.replication.admin.query.sites.persist.UpdateReplicationSite;
import org.codice.ditto.replication.admin.query.status.persist.UpdateReplicationStats;
import org.codice.ditto.replication.admin.query.ui.GetUiConfig;

public class ReplicationFieldProvider extends BaseFieldProvider {

  public static final String DEFAULT_FIELD_NAME = "replication";

  public static final String TYPE_NAME = "Replication";

  public static final String DESCRIPTION =
      "Provides methods for querying and modifying replication configurations and sites.";

  private GetReplications getReplications;

  private GetReplicationSites getReplicationSites;

  private CreateReplication createReplication;

  private UpdateReplication updateReplication;

  private DeleteReplication deleteReplication;

  private CreateReplicationSite createReplicationSite;

  private UpdateReplicationSite updateReplicationSite;

  private DeleteReplicationSite deleteReplicationSite;

  private CancelReplication cancelReplication;

  private SuspendReplication suspendReplication;

  private GetUiConfig getUiConfig;

  private UpdateReplicationStats updateReplicationStats;

  public ReplicationFieldProvider(
      GetReplications getReplications,
      GetReplicationSites getReplicationSites,
      CreateReplication createReplication,
      UpdateReplication updateReplication,
      DeleteReplication deleteReplication,
      CancelReplication cancelReplication,
      SuspendReplication suspendReplication,
      CreateReplicationSite createReplicationSite,
      UpdateReplicationSite updateReplicationSite,
      DeleteReplicationSite deleteReplicationSite,
      GetUiConfig getUiConfig,
      UpdateReplicationStats updateReplicationStats) {
    super(DEFAULT_FIELD_NAME, TYPE_NAME, DESCRIPTION);
    this.getReplications = getReplications;
    this.getReplicationSites = getReplicationSites;
    this.createReplication = createReplication;
    this.updateReplication = updateReplication;
    this.deleteReplication = deleteReplication;
    this.cancelReplication = cancelReplication;
    this.suspendReplication = suspendReplication;
    this.createReplicationSite = createReplicationSite;
    this.updateReplicationSite = updateReplicationSite;
    this.deleteReplicationSite = deleteReplicationSite;
    this.getUiConfig = getUiConfig;
    this.updateReplicationStats = updateReplicationStats;
  }

  @Override
  public List<FunctionField> getDiscoveryFunctions() {
    return ImmutableList.of(getReplications, getReplicationSites, getUiConfig);
  }

  @Override
  public List<FunctionField> getMutationFunctions() {
    return ImmutableList.of(
        createReplication,
        updateReplication,
        deleteReplication,
        createReplicationSite,
        updateReplicationSite,
        deleteReplicationSite,
        cancelReplication,
        suspendReplication,
        updateReplicationStats);
  }
}
