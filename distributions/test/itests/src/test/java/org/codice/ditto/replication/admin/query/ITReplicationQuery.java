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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import java.net.MalformedURLException;
import org.codice.ditto.replication.admin.test.QueryHelper;
import org.junit.Test;

public class ITReplicationQuery {

  private static final String URL = "https://localhost:9999";

  // ----------------------------------- Site Tests -----------------------------------//

  @Test
  public void createReplicationSite() throws MalformedURLException {
    String name = "create";
    QueryHelper.performCreateSiteQuery(name, URL)
        .body("data.createReplicationSite.name", is(name))
        .body("data.createReplicationSite.address.url", is(URL))
        .body("data.createReplicationSite.address.host.hostname", is("localhost"))
        .body("data.createReplicationSite.address.host.port", is(9999))
        .body("data.createReplicationSite.id", is(notNullValue()));
  }

  @Test
  public void createReplicationSiteWithHostnameAndPort() {
    String name = "createHostnameAndPort";
    QueryHelper.performQuery(
            String.format(
                "{\"query\":\"mutation{ createReplicationSite(name: \\\"%s\\\", address: { host: { hostname: \\\"localhost\\\" port: 9999 }}){ id name address{ host{ hostname port } url }}}\"}",
                name))
        .body("data.createReplicationSite.name", is(name))
        .body("data.createReplicationSite.address.url", is(URL))
        .body("data.createReplicationSite.address.host.hostname", is("localhost"))
        .body("data.createReplicationSite.address.host.port", is(9999))
        .body("data.createReplicationSite.id", is(notNullValue()));
  }

  @Test
  public void createReplicationSiteWithEmptyName() {
    QueryHelper.performCreateSiteQuery("", URL).body("errors.message", hasItem("EMPTY_FIELD"));
  }

  @Test
  public void createReplicationSiteWithInvalidUrl() {
    QueryHelper.performCreateSiteQuery("badUrl", "localhost:9999")
        .body("errors.message", hasItem("INVALID_URL"));
  }

  @Test
  public void createReplicationSiteWithIntegerName() {
    QueryHelper.performQuery(
            String.format(
                "{\"query\":\"mutation{ createReplicationSite(name: 25, address: { url: \\\"%s\\\"}){ id name address{ host{ hostname port } url }}}\"}",
                URL))
        .body("errors.errorType", hasItem("ValidationError"));
  }

  @Test
  public void getReplicationSites() {
    QueryHelper.performGetSitesQuery().body("errors", is(nullValue()));
  }

  @Test
  public void updateReplicationSite() {
    QueryHelper.performUpdateSiteQuery("siteId", "newName", URL)
        .body("errors", is(nullValue())); // what we currently get when a
    // site with the given ID
    // doesn't exist
  }

  @Test
  public void deleteReplicationSite() {
    assertThat(QueryHelper.deleteSite("fakeId"), is(false));
  }

  // ----------------------------------- General Tests -----------------------------------//

  @Test
  public void undefinedFieldInQuery() {
    QueryHelper.performQuery("{\"query\":\"mutation{ unknownField }\"}")
        .body("errors.errorType", hasItem("ValidationError"));
  }
}
