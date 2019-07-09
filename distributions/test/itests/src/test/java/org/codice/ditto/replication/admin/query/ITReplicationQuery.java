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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ddf.dominion.commons.options.DDFCommonOptions;
import org.codice.ditto.replication.admin.query.requests.QueriesGraphQL;
import org.codice.ditto.replication.admin.query.requests.ReplicationsGraphQL;
import org.codice.ditto.replication.admin.query.requests.SitesGraphQL;
import org.codice.ditto.replication.admin.query.requests.StatsGraphQL;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.dominion.options.ReplicationOptions;
import org.codice.dominion.Dominion;
import org.codice.dominion.interpolate.Interpolate;
import org.codice.dominion.options.Options.SetSystemProperty;
import org.codice.dominion.options.karaf.KarafOptions.InstallBundle;
import org.codice.junit.TestDelimiter;
import org.codice.maven.MavenUrl;
import org.codice.pax.exam.junit.ConfigurationAdmin;
import org.codice.pax.exam.junit.ServiceAdmin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DDFCommonOptions.ConfigureVMOptionsForTesting
@DDFCommonOptions.ConfigureDebugging
@DDFCommonOptions.ConfigurePorts
@DDFCommonOptions.ConfigureLogging
@ReplicationOptions.Install
@TestDelimiter(stdout = true, elapsed = true)
@InstallBundle(
  bundle =
      @MavenUrl(
        groupId = "org.awaitility",
        artifactId = "awaitility",
        version = MavenUrl.AS_IN_PROJECT
      )
)
@ServiceAdmin
@ConfigurationAdmin
// keeps solr from changing the password so we can use "admin:admin" with basic auth to contact solr
@SetSystemProperty(key = "solr.attemptAutoPasswordChange", value = "false")
@RunWith(Dominion.class)
public class ITReplicationQuery {

  private static final Logger LOGGER = LoggerFactory.getLogger(ITReplicationQuery.class);

  @Interpolate
  private static String GRAPHQL_ENDPOINT = "https://localhost:{port.https}/admin/hub/graphql";

  @Interpolate
  private static String WORKSPACES_API_PATH =
      "https://localhost:{port.https}/search/catalog/internal/workspaces";

  @Interpolate
  private static String SOLR_DELETE_ENDPOINT =
      "https://localhost:{solr.http.port}/solr/catalog/update?commit=true";

  private final SitesGraphQL sitesGraphql = new SitesGraphQL(GRAPHQL_ENDPOINT);

  private final ReplicationsGraphQL replicationsGraphql = new ReplicationsGraphQL(GRAPHQL_ENDPOINT);

  private final StatsGraphQL statsGraphql = new StatsGraphQL(GRAPHQL_ENDPOINT);

  private final QueriesGraphQL queriesGraphql = new QueriesGraphQL(GRAPHQL_ENDPOINT);

  @Before
  public void before() {
    sitesGraphql.waitForSitesInSchema();
  }

  @After
  public void after() {
    // clears the catalog
    given()
        .log()
        .all()
        .header("Content-Type", "application/xml")
        .auth()
        .preemptive()
        .basic("admin", "admin")
        .header("X-Requested-With", "XMLHttpRequest")
        .body("<delete><query>*:*</query></delete>")
        .post(SOLR_DELETE_ENDPOINT);
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

  @Test
  public void testQueries() throws Exception {
    Map<String, Object> query = ImmutableMap.of("title", "test title", "cql", "test cql");
    List<Map<String, Object>> queries = ImmutableList.of(query);
    Map<String, Object> workspace = ImmutableMap.of("queries", queries);
    Gson gson = new Gson();
    String json = gson.toJson(workspace);

    asAdmin()
        .header("Origin", WORKSPACES_API_PATH)
        .body(json)
        .expect()
        .statusCode(201)
        .when()
        .post(WORKSPACES_API_PATH);

    Map<String, Object> query2 = ImmutableMap.of("title", "test title2", "cql", "test cql2");
    List<Map<String, Object>> queries2 = ImmutableList.of(query2);
    Map<String, Object> workspace2 = ImmutableMap.of("queries", queries2);
    String json2 = gson.toJson(workspace2);

    asAdmin()
        .header("Origin", WORKSPACES_API_PATH)
        .body(json2)
        .expect()
        .statusCode(201)
        .when()
        .post(WORKSPACES_API_PATH);

    queriesGraphql.waitForQuery(query2);
    List<Map<String, Object>> resultingQueries = queriesGraphql.getAllQueries();
    // Since the queries are meant to be sorted by last modified date,
    // we assert that we received them in the correct order.
    assertThat(resultingQueries, contains(query2, query));
  }

  private static RequestSpecification asAdmin() {
    return given()
        .log()
        .all()
        .header("Content-Type", "application/json")
        .auth()
        .preemptive()
        .basic("admin", "admin")
        .header("X-Requested-With", "XMLHttpRequest");
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
