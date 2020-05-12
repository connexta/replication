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
package org.codice.ditto.replication.admin.query.requests;

import com.jayway.restassured.response.ExtractableResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.requests.GraphQLRequests.GraphQLRequest;

public class SitesGraphQL {

  private GraphQLRequests requestFactory;

  private static long WAIT_TIME = 30L;

  public SitesGraphQL(String graphqlEndpoint) {
    requestFactory =
        new GraphQLRequests(
            SitesGraphQL.class, "/query/sites/query/", "/query/sites/mutation/", graphqlEndpoint);
  }

  public void waitForSitesInSchema() {
    Awaitility.await("get sites in schema")
        .atMost(WAIT_TIME, TimeUnit.SECONDS)
        .until(
            () -> {
              GraphQLRequest graphQLRequest = requestFactory.createRequest();
              graphQLRequest.usingQuery("AllSites.graphql");

              ExtractableResponse response = graphQLRequest.send().getResponse();
              if (GraphQLRequests.responseHasErrors(response)) {
                return false;
              }

              return GraphQLRequests.extractResponse(response, "data") != null;
            });
  }

  public List<Map<String, Object>> getAllSites() {
    GraphQLRequest graphQLRequest = requestFactory.createRequest();
    graphQLRequest.usingQuery("AllSites.graphql");

    ExtractableResponse response = graphQLRequest.send().getResponse();
    if (GraphQLRequests.responseHasErrors(response)) {
      return Collections.emptyList();
    }

    return GraphQLRequests.extractResponse(response, "data.replication.sites");
  }

  public Map<String, Object> createSite(
      String name, String rootContext, AddressField address, boolean remoteManaged) {
    ExtractableResponse response =
        requestFactory
            .createRequest()
            .usingMutation("CreateSite.graphql")
            .addArgument("name", name)
            .addArgument("rootContext", rootContext)
            .addArgument("address", address.getValue())
            .addArgument("remoteManaged", remoteManaged)
            .send()
            .getResponse();

    if (GraphQLRequests.responseHasErrors(response)) {
      return Collections.emptyMap();
    }

    return GraphQLRequests.extractResponse(response, "data.createReplicationSite");
  }

  public boolean updateSite(
      String id, String name, String rootContext, AddressField address, boolean remoteManaged) {
    ExtractableResponse response =
        requestFactory
            .createRequest()
            .usingMutation("UpdateSite.graphql")
            .addArgument("id", id)
            .addArgument("name", name)
            .addArgument("rootContext", rootContext)
            .addArgument("address", address.getValue())
            .addArgument("remoteManaged", remoteManaged)
            .send()
            .getResponse();

    if (GraphQLRequests.responseHasErrors(response)) {
      return false;
    }

    return GraphQLRequests.extractResponse(response, "data.updateReplicationSite");
  }

  public void waitForSite(Map<String, Object> expectedConfig) {
    Awaitility.await("failed to retrieve expected site")
        .atMost(WAIT_TIME, TimeUnit.SECONDS)
        .until(() -> getAllSites().stream().anyMatch(expectedConfig::equals));
  }

  public void waitForSiteWithName(String name) {
    Awaitility.await(String.format("failed to retrieve expected site with name %s", name))
        .atMost(WAIT_TIME, TimeUnit.SECONDS)
        .until(() -> getAllSites().stream().anyMatch(config -> config.get("name").equals(name)));
  }

  public boolean deleteSite(String pid) {
    PidField pidField = new PidField();
    pidField.setValue(pid);

    ExtractableResponse response =
        requestFactory
            .createRequest()
            .usingMutation("DeleteSite.graphql")
            .addArgument("id", pidField.getValue())
            .send()
            .getResponse();

    if (GraphQLRequests.responseHasErrors(response)) {
      return false;
    }

    return GraphQLRequests.extractResponse(response, "data.deleteReplicationSite");
  }
}
