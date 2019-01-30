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
package org.codice.ditto.replication.admin.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.codice.ditto.replication.api.modern.ReplicationSite;

public class QueryHelper {

  public static final String GRAPHQL_ENDPOINT = "https://localhost:8993/admin/hub/graphql";

  public static RequestSpecification asAdmin() {
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

  public static String makeCreateSiteQuery(String name, String url) {
    return String.format(
        "{\"query\":\"mutation{ createReplicationSite(name: \\\"%s\\\", address: { url: \\\"%s\\\"}){ id name address{ host{ hostname port } url }}}\"}",
        name, url);
  }

  public static String makeGetSitesQuery() {
    return "{\"query\":\"{ replication{ sites{ id name address{ host{ hostname port } url }}}}\"}";
  }

  public static String makeUpdateSiteQuery(String id, String name, String url) {
    return String.format(
        "{\"query\":\"mutation{ updateReplicationSite(id: \\\"%s\\\", name: \\\"%s\\\", address: { url: \\\"%s\\\"}){ id name address{ host{ hostname port } url }}}\"}",
        id, name, url);
  }

  public static String makeDeleteSiteQuery(String siteId) {
    return String.format(
        "{\"query\":\"mutation{ deleteReplicationSite(id: \\\"%s\\\")}\"}", siteId);
  }

  public static ValidatableResponse performQuery(String body){
    return asAdmin()
        .body(body)
        .when()
        .post(GRAPHQL_ENDPOINT)
        .then()
        .statusCode(200)
        .header("Content-Type", is("application/json;charset=utf-8"));
  }

  public static ValidatableResponse performCreateSiteQuery(String name, String url){
    return performQuery(makeCreateSiteQuery(name, url));
  }

  public static ValidatableResponse performUpdateSiteQuery(String id, String name, String url){
    return performQuery(makeUpdateSiteQuery(id, name, url));
  }

  public static ValidatableResponse performDeleteSiteQuery(String id){
    return performQuery(makeDeleteSiteQuery(id));
  }

  public static ValidatableResponse performGetSitesQuery(){
    return performQuery(makeGetSitesQuery());
  }

  public static ReplicationSite createSite(String name, String url) throws MalformedURLException {
    return extractSiteFromJsonPath(performCreateSiteQuery(name, url).extract().jsonPath().setRoot("data.createReplicationSite"));
  }

  public static ReplicationSite updateSite(String id, String name, String url) throws MalformedURLException {
    return extractSiteFromJsonPath(performUpdateSiteQuery(id, name, url).extract().jsonPath().setRoot("data.updateReplicationSite"));
  }

  public static boolean deleteSite(String id){
    return performDeleteSiteQuery(id).extract().jsonPath().getBoolean("data.deleteReplicationSite");
  }

  public static List<ReplicationSite> getSites() throws MalformedURLException {
    List<ReplicationSite> sites = new ArrayList<>();
    JsonPath json = performGetSitesQuery().extract().jsonPath();

    //if the response has a site we haven't retrieved yet, set the root at the next site
    //and pass the jsonPath on so the site can be extracted. Then, reset and check the next site.
    int i = 0;
    String nextRoot = String.format("data.replication.sites[%d]", i );
    while(json.get(nextRoot) != null){
      json.setRoot(nextRoot);
      sites.add(extractSiteFromJsonPath(json));
      i++;
      nextRoot = String.format("data.replication.sites[%d]", i );
      json.setRoot(""); //reset the root so it doesn't mess up our path when we check for the next site
    }
    return sites;
  }

  public static ReplicationSite extractSiteFromJsonPath(JsonPath json)
      throws MalformedURLException {
    String id = json.getString("id");
    String name = json.getString("name");
    String urlString = json.getString("address.url");
    URL url = new URL(urlString);
    return new ReplicationSiteImpl(id, name, url);
  }

}
