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
import static junit.framework.TestCase.fail;
import static org.codice.ddf.test.common.options.TestResourcesOptions.getTestResource;
import static org.hamcrest.core.Is.is;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.boon.Boon;
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
    Map<String, Object> args = new HashMap<>();
    args.put("name", name);
    args.put("address", new Address(url));

    return makeQuery("query/createReplicationSite.graphql", args);
  }

  public static String makeGetSitesQuery() {
    return makeQuery("query/getSites.graphql", null);
  }

  public static String makeQuery(String resourceUrl, Map<String, Object> args){
    Map<String, String> query = new HashMap<>();
    String queryBody = getResourceAsString(resourceUrl);
    query.put("query", queryBody);
    query.put("variables", Boon.toJson(args));

    return Boon.toPrettyJson(query);
  }

  public static String makeUpdateSiteQuery(String id, String name, String url) {
    Map<String, Object> args = new HashMap<>();
    args.put("id", id);
    args.put("name", name);
    args.put("address", new Address(url));

    return makeQuery("query/updateReplicationSite.graphql", args);
  }

  public static String makeDeleteSiteQuery(String id) {
    Map<String, Object> args = new HashMap<>();
    args.put("id", id);

    return makeQuery("query/deleteReplicationSite.graphql", args);
  }

  public static String makeCreateRepsyncQuery(String name, String sourceId, String destinationId, String filter){
    Map<String, Object> args = new HashMap<>();
    args.put("name", name);
    args.put("sourceId", sourceId);
    args.put("destinationId", destinationId);
    args.put("filter", filter);

    return makeQuery("query/createRepsync.graphql", args);
  }

  public static String makeUpdateRepsyncQuery(String id, String name, String sourceId, String destinationId, String filter){
    Map<String, Object> args = new HashMap<>();
    args.put("id", id);
    args.put("name", name);
    args.put("sourceId", sourceId);
    args.put("destinationId", destinationId);
    args.put("filter", filter);

    return makeQuery("query/updateRepsync.graphql", args);
  }

  public static String makeDeleteRepsyncQuery(String id){
    Map<String, Object> args = new HashMap<>();
    args.put("id", id);

    return makeQuery("query/deleteRepsync.graphql", args);
  }

  public static String makeGetRepsyncsQuery(){
    return makeQuery("query/getRepsyncs.graphql", null);
  }

  public static ValidatableResponse performCreateRepsyncQuery(String name, String sourceId, String destinationId, String filter){
    return performQuery(makeCreateRepsyncQuery(name, sourceId, destinationId, filter));
  }

  public static ValidatableResponse performUpdateRepsyncQuery(String id, String name, String sourceId, String destinationId, String filter) {
    return performQuery(makeUpdateRepsyncQuery(id, name, sourceId, destinationId, filter));
  }

  public static ValidatableResponse performDeleteRepsyncQuery(String id){
    return performQuery(makeDeleteRepsyncQuery(id));
  }

  public static ValidatableResponse performGetRepsyncsQuery(){
    return performQuery(makeGetRepsyncsQuery());
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

  private static String getResourceAsString(String resourcePath) {
    try (InputStream is = QueryHelper.class.getClassLoader().getResourceAsStream(resourcePath)) {
      return IOUtils.toString(is, "UTF-8");
    } catch (IOException e) {
      fail("Unable to retrieve resource: " + resourcePath);
    }

    return null;
  }


private static class Address {

  private String url;

  public Address(String url){
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}


}
