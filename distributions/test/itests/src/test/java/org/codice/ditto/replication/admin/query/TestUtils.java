package org.codice.ditto.replication.admin.query;

import static com.jayway.restassured.RestAssured.given;

import com.jayway.restassured.specification.RequestSpecification;

public class TestUtils {

  public static String writeUrl(String hostname, int port, String rootContext) {
    return String.format("https://%s:%d/%s", hostname, port, rootContext);
  }

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
        "{\"query\":\"mutation{ createReplicationSite(name: \\\"%s\\\", address: { url: \\\"%s\\\"}, rootContext: \\\"services\\\"){ id name address{ host{ hostname port } url }}}\"}",
        name, url);
  }

  public static String makeGetSitesQuery() {
    return "{\"query\":\"{ replication{ sites{ id name address{ host{ hostname port } url }}}}\"}";
  }

  public static String makeGetSiteByIdQuery(String siteId) {
    return String.format(
        "{\"query\":\"{ replication{ sites(id: \\\"%s\\\"){ id name address{ host{ hostname port } url }}}}\"}",
        siteId);
  }

  public static String makeUpdateSiteQuery(
      String id, String name, String hostname, int port, String rootContext) {
    return String.format(
        "{\"query\":\"mutation{ updateReplicationSite(id: \\\"%s\\\", name: \\\"%s\\\", address: { host: { hostname: \\\"%s\\\", port: %d}}, rootContext: \\\"%s\\\")}\"}",
        id, name, hostname, port, rootContext);
  }

  public static String makeDeleteSiteQuery(String siteId) {
    return String.format(
        "{\"query\":\"mutation{ deleteReplicationSite(id: \\\"%s\\\")}\"}", siteId);
  }
}
