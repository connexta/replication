/**
 * Copyright (c) Codice Foundation
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.util.Map;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ddf.dominion.commons.options.DDFCommonOptions;
import org.codice.ditto.replication.admin.query.requests.ReplicationsGraphQL;
import org.codice.ditto.replication.admin.query.requests.SitesGraphQL;
import org.codice.ditto.replication.admin.query.requests.StatsGraphQL;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.dominion.options.ReplicationOptions;
import org.codice.dominion.Dominion;
import org.codice.dominion.interpolate.Interpolate;
import org.codice.dominion.options.karaf.KarafOptions.InstallBundle;
import org.codice.junit.TestDelimiter;
import org.codice.maven.MavenUrl;
import org.codice.pax.exam.junit.ConfigurationAdmin;
import org.codice.pax.exam.junit.ServiceAdmin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@DDFCommonOptions.ConfigureVMOptionsForTesting
@DDFCommonOptions.ConfigureDebugging
@DDFCommonOptions.ConfigurePorts
@DDFCommonOptions.ConfigureLogging
@ReplicationOptions.Install
@TestDelimiter(stdout = true, elapsed = true)
@InstallBundle(bundle = @MavenUrl(groupId = "org.awaitility", artifactId = "awaitility"))
@ServiceAdmin
@ConfigurationAdmin
@RunWith(Dominion.class)
public class ITReplicationQuery {

  @Interpolate
  private static String GRAPHQL_ENDPOINT = "https://localhost:{port.https}/admin/hub/graphql";

  private final SitesGraphQL sitesGraphql = new SitesGraphQL(GRAPHQL_ENDPOINT);

  private final ReplicationsGraphQL replicationsGraphql = new ReplicationsGraphQL(GRAPHQL_ENDPOINT);

  private final StatsGraphQL statsGraphql = new StatsGraphQL(GRAPHQL_ENDPOINT);

  @Before
  public void before() {
    sitesGraphql.waitForSitesInSchema();
  }

  @Test
  public void testReplicationSites() {
    // create site
    AddressField address = new AddressField().hostname("localhost").port(8993);
    Map<String, Object> config = sitesGraphql.createSite("siteName", "/services", address, false);
    final String siteId = (String) config.get("id");

    sitesGraphql.waitForSite(config);

    // update site
    AddressField updatedAddress = new AddressField().hostname("anotherone").port(8080);
    boolean updated =
        sitesGraphql.updateSite(siteId, "newSiteName", "/potato", updatedAddress, true);

    assertThat("update site", updated, is(true));

    sitesGraphql.waitForSiteWithName("newSiteName");

    // delete site
    boolean deleted = sitesGraphql.deleteSite(siteId);
    assertThat("deleted site", deleted, is(true));
  }

  @Test
  public void testReplications() {
    // create sites
    AddressField address = new AddressField().hostname("localhost").port(8993);
    Map<String, Object> sourceSite = sitesGraphql.createSite("source", "/services", address, false);
    final String sourceId = (String) sourceSite.get("id");
    sitesGraphql.waitForSite(sourceSite);

    Map<String, Object> destinationSite =
        sitesGraphql.createSite("destination", "/services", address, false);
    final String destinationId = (String) destinationSite.get("id");
    sitesGraphql.waitForSite(destinationSite);

    // create replication
    Map<String, Object> replicationConfig =
        replicationsGraphql.createReplication(
            "replicationName", sourceId, destinationId, "title like '*'", false, false);
    final String replicationId = (String) replicationConfig.get("id");

    replicationsGraphql.waitForReplication(replicationConfig);

    // update replication
    boolean updated =
        replicationsGraphql.updateReplication(
            replicationId, "newName", sourceId, destinationId, "title like 'potato'", true, true);

    assertThat("replication updated", updated, is(true));

    replicationsGraphql.waitForReplicationWithName("newName");

    // delete replication
    boolean deleted = replicationsGraphql.deleteReplication(replicationId);
    assertThat("delete replication", deleted, is(true));

    // make sure we can delete sites
    replicationsGraphql.waitForNoReplications();

    // cleanup sites
    boolean sourceDeleted = sitesGraphql.deleteSite(sourceId);
    assertThat("delete source site", sourceDeleted, is(true));

    boolean destinationDeleted = sitesGraphql.deleteSite(destinationId);
    assertThat("delete destination site", destinationDeleted, is(true));
  }

  @Test
  public void updateReplicationStats() {
    // create sites
    AddressField address = new AddressField().hostname("localhost").port(8993);
    Map<String, Object> sourceSite = sitesGraphql.createSite("source", "/services", address, false);
    final String sourceId = (String) sourceSite.get("id");
    sitesGraphql.waitForSite(sourceSite);

    Map<String, Object> destinationSite =
        sitesGraphql.createSite("destination", "/services", address, false);
    final String destinationId = (String) destinationSite.get("id");
    sitesGraphql.waitForSite(destinationSite);

    // create replication
    Map<String, Object> replicationConfig =
        replicationsGraphql.createReplication(
            "replicationName", sourceId, destinationId, "title like '*'", false, false);
    final String replicationId = (String) replicationConfig.get("id");

    replicationsGraphql.waitForReplication(replicationConfig);

    // update replication stats
    boolean updated = statsGraphql.updateStats("replicationName", createStatsMap());
    assertThat("replication stats updated", updated, is(true));

    // delete replication
    boolean deleted = replicationsGraphql.deleteReplication(replicationId);
    assertThat("delete replication", deleted, is(true));

    // make sure we can delete sites
    replicationsGraphql.waitForNoReplications();

    // cleanup sites
    boolean sourceDeleted = sitesGraphql.deleteSite(sourceId);
    assertThat("delete source site", sourceDeleted, is(true));

    boolean destinationDeleted = sitesGraphql.deleteSite(destinationId);
    assertThat("delete destination site", destinationDeleted, is(true));
  }

  public Map<String, Object> createStatsMap() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("pid", "testPid1234");
    stats.put("startTime", "2019-04-20T15:25:46.327Z");
    stats.put("lastRun", "2019-05-20T15:25:46.327Z");
    stats.put("lastSuccess", "2019-06-20T15:25:46.327Z");
    stats.put("duration", 5);
    stats.put("replicationStatus", Status.FAILURE);
    stats.put("pushCount", 32);
    stats.put("pullCount", 37);
    stats.put("pushFailCount", 2);
    stats.put("pullFailCount", 1);
    stats.put("pushBytes", 524288099999999L);
    stats.put("pullBytes", 524288099999999L);
    return stats;
  }
}
