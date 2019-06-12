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

import static org.awaitility.Awaitility.await;
import static org.codice.ditto.replication.admin.query.TestUtils.asAdmin;
import static org.codice.ditto.replication.admin.query.TestUtils.makeCreateSiteQuery;
import static org.codice.ditto.replication.admin.query.TestUtils.makeDeleteSiteQuery;
import static org.codice.ditto.replication.admin.query.TestUtils.makeGetSiteByIdQuery;
import static org.codice.ditto.replication.admin.query.TestUtils.makeGetSitesQuery;
import static org.codice.ditto.replication.admin.query.TestUtils.makeUpdateSiteQuery;
import static org.codice.ditto.replication.admin.query.TestUtils.writeUrl;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import com.jayway.restassured.response.Response;
import java.net.MalformedURLException;
import java.util.ArrayList;
import org.codice.ddf.dominion.commons.options.DDFCommonOptions;
import org.codice.ditto.replication.dominion.options.ReplicationOptions;
import org.codice.dominion.Dominion;
import org.codice.dominion.interpolate.Interpolate;
import org.codice.dominion.options.karaf.KarafOptions.InstallBundle;
import org.codice.junit.TestDelimiter;
import org.codice.maven.MavenUrl;
import org.codice.pax.exam.junit.ConfigurationAdmin;
import org.codice.pax.exam.junit.ServiceAdmin;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@DDFCommonOptions.ConfigureVMOptionsForTesting
@DDFCommonOptions.ConfigureDebugging
@DDFCommonOptions.ConfigurePorts
@DDFCommonOptions.ConfigureLogging
@ReplicationOptions.Install
@TestDelimiter(stdout = true, elapsed = true)
@ServiceAdmin
@ConfigurationAdmin
@InstallBundle(
  bundle =
      @MavenUrl(
        groupId = "org.awaitility",
        artifactId = "awaitility",
        version = MavenUrl.AS_IN_PROJECT
      )
)
@RunWith(Dominion.class)
public class ITReplicationQuery {

  private static final String NO_EXISTING_CONFIG = "NO_EXISTING_CONFIG";

  @Interpolate
  private static String GRAPHQL_ENDPOINT = "https://localhost:{port.https}/admin/hub/graphql";

  private static final String URL = "https://localhost:9999/services";

  private static String HOST = "host";

  private static int PORT = 9999;

  private static String SERVICES = "services";

  private static ArrayList<String> sitesToCleanup = new ArrayList<>();

  @After
  public void cleanup() {
    sitesToCleanup.forEach(this::cleanupSite);
  }

  private void cleanupSite(String siteId) {
    asAdmin().body(makeDeleteSiteQuery(siteId)).when().post(GRAPHQL_ENDPOINT);
  }

  // ----------------------------------- Site Tests -----------------------------------//

  @Test
  public void createReplicationSite() throws MalformedURLException {
    String name = "create";
    createSite(name, HOST, PORT, SERVICES);
  }

  @Test
  public void createReplicationSiteWithHostnameAndPort() {
    String name = "createHostnameAndPort";
    asAdmin()
        .body(
            String.format(
                "{\"query\":\"mutation{ createReplicationSite(name: \\\"%s\\\", address: { host: { hostname: \\\"localhost\\\" port: 9999 }}, rootContext: \\\"services\\\"){ id name address{ host{ hostname port } url }}}\"}",
                name))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("data.createReplicationSite.name", is(name))
        .body("data.createReplicationSite.address.url", is(URL))
        .body("data.createReplicationSite.address.host.hostname", is("localhost"))
        .body("data.createReplicationSite.address.host.port", is(9999))
        .body("data.createReplicationSite.id", is(notNullValue()));
  }

  @Test
  public void createReplicationSiteWithEmptyName() {
    asAdmin()
        .body(makeCreateSiteQuery("", URL))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("errors.message", hasItem("EMPTY_FIELD"));
  }

  @Test
  public void createReplicationSiteWithInvalidUrl() {
    asAdmin()
        .body(makeCreateSiteQuery("badUrl", "localhost:9999"))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("errors.message", hasItem("INVALID_URL"));
  }

  @Test
  public void createReplicationSiteWithIntegerName() {
    asAdmin()
        .body(
            String.format(
                "{\"query\":\"mutation{ createReplicationSite(name: 25, address: { url: \\\"%s\\\"}, rootContext: \\\"services\\\"){ id name address{ host{ hostname port } url }}}\"}",
                URL))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body(
            "errors.errorType",
            hasItem("ValidationError")); // returns an empty body for queries it doesn't recognize
  }

  @Test
  public void getReplicationSites() {
    asAdmin()
        .body(makeGetSitesQuery())
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("errors", is(nullValue()));
  }

  @Test
  public void updateReplicationSiteContractTest() {
    asAdmin()
        .body(makeUpdateSiteQuery("siteId", "newName", HOST, PORT, SERVICES))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("errors.message", hasItem(NO_EXISTING_CONFIG));
  }

  @Test
  public void deleteReplicationSiteContractTest() {
    asAdmin()
        .body(makeDeleteSiteQuery("fakeId"))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("errors.message", hasItem(NO_EXISTING_CONFIG));
  }

  /*Tests the create and get queries for sites but the focus here is making sure delete works*/
  @Test
  public void deleteReplicationSite() throws Exception {
    String name = "deleteReplicationSite";
    Response createResponse = createSite(name, HOST, PORT, SERVICES);
    String siteId = createResponse.jsonPath().getString("data.createReplicationSite.id");

    waitUntilStoreContains(1);

    deleteSite(siteId);

    waitUntilStoreContains(0);

    asAdmin()
        .body(makeGetSiteByIdQuery(siteId))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("data.replication.sites", is(nullValue()));
  }

  @Test
  public void updateReplicationSite() throws Exception {
    String name = "updateReplicationSite";
    Response createResponse = createSite(name, HOST, PORT, SERVICES);
    String siteId = createResponse.jsonPath().getString("data.createReplicationSite.id");

    waitUntilStoreContains(1);

    String newName = name + "newName";
    String newHost = "newHost";
    int newPort = 7777;
    String newContext = "newContext";
    asAdmin()
        .body(makeUpdateSiteQuery(siteId, newName, newHost, newPort, newContext))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("data.updateReplicationSite", is(true));

    await()
        .until(
            () ->
                asAdmin()
                    .body(makeGetSiteByIdQuery(siteId))
                    .when()
                    .post(GRAPHQL_ENDPOINT)
                    .jsonPath()
                    .get("data.replication.sites.name"),
            contains(newName));

    getSite(siteId, newName, newHost, newPort, newContext);
  }

  @Test
  public void getAllSites() {
    String name1 = "getAllSites1";
    String name2 = "getAllSites2";
    createSite(name1, HOST, PORT, SERVICES);
    createSite(name2, HOST, PORT, SERVICES);

    waitUntilStoreContains(2);

    asAdmin()
        .body(makeGetSitesQuery())
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("data.replication.sites.name", contains(name1, name2));
  }

  @Test
  public void getByIdOnlyReturnsOneSite() {
    String name1 = "getAllSites1";
    String name2 = "getAllSites2";
    Response createResponse1 = createSite(name1, HOST, PORT, SERVICES);
    createSite(name2, HOST, PORT, SERVICES);

    waitUntilStoreContains(2);

    String siteId1 = createResponse1.jsonPath().getString("data.createReplicationSite.id");
    getSite(siteId1, name1, HOST, PORT, SERVICES)
        .then()
        .body("data.replication.sites.name", hasSize(1));
  }

  private void waitUntilStoreContains(int size) {
    if (size == 0) {
      await()
          .until(
              () ->
                  asAdmin()
                      .body(makeGetSitesQuery())
                      .when()
                      .post(GRAPHQL_ENDPOINT)
                      .jsonPath()
                      .get("data.replication.sites.name"),
              hasSize(size));
    } else {
      await()
          .until(
              () ->
                  asAdmin()
                      .body(makeGetSitesQuery())
                      .when()
                      .post(GRAPHQL_ENDPOINT)
                      .jsonPath()
                      .get("data.replication.sites.name"),
              hasSize(size));
    }
  }

  private Response deleteSite(String siteId) {
    Response deleteResponse =
        asAdmin().body(makeDeleteSiteQuery(siteId)).when().post(GRAPHQL_ENDPOINT);

    deleteResponse
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("data.deleteReplicationSite", is(true));

    return deleteResponse;
  }

  private Response getSite(
      String siteId, String name, String hostname, int port, String rootContext) {
    Response getResponse =
        asAdmin().body(makeGetSiteByIdQuery(siteId)).when().post(GRAPHQL_ENDPOINT);

    getResponse
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("data.replication.sites.name", contains(name))
        .body("data.replication.sites.address.url", contains(writeUrl(hostname, port, rootContext)))
        .body("data.replication.sites.address.host.hostname", contains(hostname))
        .body("data.replication.sites.address.host.port", contains(port));

    return getResponse;
  }

  private Response createSite(String name, String hostname, int port, String rootContext) {
    String url = writeUrl(hostname, port, rootContext);
    Response createResponse =
        asAdmin().body(makeCreateSiteQuery(name, url)).when().post(GRAPHQL_ENDPOINT);
    String siteId = createResponse.jsonPath().getString("data.createReplicationSite.id");
    sitesToCleanup.add(siteId);

    createResponse
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("data.createReplicationSite.name", is(name))
        .body("data.createReplicationSite.address.url", is(url))
        .body("data.createReplicationSite.address.host.hostname", is(hostname))
        .body("data.createReplicationSite.address.host.port", is(port))
        .body("data.createReplicationSite.id", is(notNullValue()));

    return createResponse;
  }

  // ----------------------------------- General Tests -----------------------------------//

  @Test
  public void undefinedFieldInQuery() {
    asAdmin()
        .body("{\"query\":\"mutation{ unknownField }\"}")
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("errors.errorType", hasItem("ValidationError"));
  }
}
