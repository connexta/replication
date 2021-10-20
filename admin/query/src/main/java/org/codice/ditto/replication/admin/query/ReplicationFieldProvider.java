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
import org.codice.ditto.replication.admin.query.queries.GetQueries;
import org.codice.ditto.replication.admin.query.replications.discover.GetReplications;
import org.codice.ditto.replication.admin.query.replications.persist.CancelReplication;
import org.codice.ditto.replication.admin.query.replications.persist.CreateReplication;
import org.codice.ditto.replication.admin.query.replications.persist.DeleteReplication;
import org.codice.ditto.replication.admin.query.replications.persist.RunReplication;
import org.codice.ditto.replication.admin.query.replications.persist.SuspendReplication;
import org.codice.ditto.replication.admin.query.replications.persist.UpdateReplication;
import org.codice.ditto.replication.admin.query.sites.discover.GetReplicationSites;
import org.codice.ditto.replication.admin.query.sites.persist.CreateReplicationSite;
import org.codice.ditto.replication.admin.query.sites.persist.DeleteReplicationSite;
import org.codice.ditto.replication.admin.query.sites.persist.UpdateReplicationSite;
import org.codice.ditto.replication.admin.query.status.persist.UpdateReplicationStats;
import org.codice.ditto.replication.admin.query.ui.GetUiConfig;

/** Provides the graphql endpoint with the functionality we would like to expose. */
public class ReplicationFieldProvider extends BaseFieldProvider {

  private static final String DEFAULT_FIELD_NAME = "replication";

  private static final String TYPE_NAME = "Replication";

  private static final String DESCRIPTION =
      "Provides methods for querying and modifying replication configurations and sites.";

  private final GetReplications getReplications;

  private final GetReplicationSites getReplicationSites;

  private final CreateReplication createReplication;

  private final UpdateReplication updateReplication;

  private final DeleteReplication deleteReplication;

  private final CreateReplicationSite createReplicationSite;

  private final UpdateReplicationSite updateReplicationSite;

  private final DeleteReplicationSite deleteReplicationSite;

  private final CancelReplication cancelReplication;

  private final RunReplication runReplication;

  private final SuspendReplication suspendReplication;

  private final GetUiConfig getUiConfig;

  private final UpdateReplicationStats updateReplicationStats;

  private final GetQueries getQueries;

  /**
   * Creates a new ReplicationFieldProvider. The parameters all extend {@link
   * org.codice.ddf.admin.common.fields.base.BaseFunctionField} and will provide functionality which
   * we will expose through the graphql endpoint.
   */
  public ReplicationFieldProvider(
      GetReplications getReplications,
      GetReplicationSites getReplicationSites,
      CreateReplication createReplication,
      UpdateReplication updateReplication,
      DeleteReplication deleteReplication,
      CancelReplication cancelReplication,
      RunReplication runReplication,
      SuspendReplication suspendReplication,
      CreateReplicationSite createReplicationSite,
      UpdateReplicationSite updateReplicationSite,
      DeleteReplicationSite deleteReplicationSite,
      GetUiConfig getUiConfig,
      UpdateReplicationStats updateReplicationStats,
      GetQueries getQueries) {
    super(DEFAULT_FIELD_NAME, TYPE_NAME, DESCRIPTION);
    this.getReplications = getReplications;
    this.getReplicationSites = getReplicationSites;
    this.createReplication = createReplication;
    this.updateReplication = updateReplication;
    this.deleteReplication = deleteReplication;
    this.cancelReplication = cancelReplication;
    this.runReplication = runReplication;
    this.suspendReplication = suspendReplication;
    this.createReplicationSite = createReplicationSite;
    this.updateReplicationSite = updateReplicationSite;
    this.deleteReplicationSite = deleteReplicationSite;
    this.getUiConfig = getUiConfig;
    this.updateReplicationStats = updateReplicationStats;
    this.getQueries = getQueries;
  }

  /**
   * Returns a list of {@link FunctionField}s which, when accessed by the graphql endpoint, will
   * allow their functionality to be triggered through the graphql endpoint.
   *
   * @return a list of {@link FunctionField}s that will allow retrieval of data through the graphql
   *     endpoint.
   */
  @Override
  public List<FunctionField> getDiscoveryFunctions() {
    return ImmutableList.of(getReplications, getReplicationSites, getUiConfig, getQueries);
  }

  /**
   * Returns a list of {@link FunctionField}s which, when accessed by the graphql endpoint, will
   * allow their functionality to be triggered through the graphql endpoint.
   *
   * @return a list of {@link FunctionField}s that will allow mutation of data through the graphql
   *     endpoint.
   */
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
        runReplication,
        suspendReplication,
        updateReplicationStats);
  }
}
