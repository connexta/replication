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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import com.jayway.restassured.specification.RequestSpecification;
import java.net.MalformedURLException;
import org.codice.ddf.dominion.commons.options.DDFCommonOptions;
import org.codice.ditto.replication.dominion.options.ReplicationOptions;
import org.codice.dominion.Dominion;
import org.codice.dominion.interpolate.Interpolate;
import org.codice.junit.TestDelimiter;
import org.codice.pax.exam.junit.ConfigurationAdmin;
import org.codice.pax.exam.junit.ServiceAdmin;
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
@RunWith(Dominion.class)
public class ITReplicationQuery {

  @Interpolate
  private static String GRAPHQL_ENDPOINT = "https://localhost:{port.https}/admin/hub/graphql";

  private static final String URL = "https://localhost:9999/services";

  // ----------------------------------- Site Tests -----------------------------------//

  @Test
  public void createReplicationSite() throws MalformedURLException {
    String name = "create";
    asAdmin()
        .body(makeCreateSiteQuery(name, URL))
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
  public void updateReplicationSite() {
    asAdmin()
        .body(makeUpdateSiteQuery("siteId", "newName", URL))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body(
            "errors.message",
            hasItem(
                "Internal Server Error(s) while executing query")); // what we currently get when a
    // site with the given ID
    // doesn't exist
  }

  @Test
  public void deleteReplicationSite() {
    asAdmin()
        .body(makeDeleteSiteQuery("fakeId"))
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"))
        .body("data.deleteReplicationSite", is(false));
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

  private static RequestSpecification asAdmin() {
    return given()
        .log()
        .all()
        .header("Content-Type", "application/json")
        .relaxedHTTPSValidation()
        .auth()
        .preemptive()
        .basic("admin", "admin")
        .header("X-Requested-With", "XMLHttpRequest");
  }

  private static String makeCreateSiteQuery(String name, String url) {
    return String.format(
        "{\"query\":\"mutation{ createReplicationSite(name: \\\"%s\\\", address: { url: \\\"%s\\\"}, rootContext: \\\"services\\\"){ id name address{ host{ hostname port } url }}}\"}",
        name, url);
  }

  private static String makeGetSitesQuery() {
    return "{\"query\":\"{ replication{ sites{ id name address{ host{ hostname port } url }}}}\"}";
  }

  private static String makeUpdateSiteQuery(String id, String name, String url) {
    return String.format(
        "{\"query\":\"mutation{ updateReplicationSite(id: \\\"%s\\\", name: \\\"%s\\\", address: { url: \\\"%s\\\"}, rootContext: \\\"services\\\"){ id name address{ host{ hostname port } url }}}\"}",
        id, name, url);
  }

  private String makeDeleteSiteQuery(String siteId) {
    return String.format(
        "{\"query\":\"mutation{ deleteReplicationSite(id: \\\"%s\\\")}\"}", siteId);
  }
}
