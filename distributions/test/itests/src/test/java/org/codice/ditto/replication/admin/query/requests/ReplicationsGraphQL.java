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
package org.codice.ditto.replication.admin.query.requests;

import com.jayway.restassured.response.ExtractableResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.codice.ditto.replication.admin.query.requests.GraphQLRequests.GraphQLRequest;

public class ReplicationsGraphQL {

  private GraphQLRequests requestFactory;

  public ReplicationsGraphQL(String graphqlEndpoint) {
    requestFactory =
        new GraphQLRequests(
            ReplicationsGraphQL.class,
            "/query/replications/query/",
            "/query/replications/mutation/",
            graphqlEndpoint);
  }

  public List<Map<String, Object>> getAllReplications() {
    GraphQLRequest graphQLRequest = requestFactory.createRequest();
    graphQLRequest.usingQuery("AllReplications.graphql");

    ExtractableResponse response = graphQLRequest.send().getResponse();
    if (GraphQLRequests.responseHasErrors(response)) {
      return Collections.emptyList();
    }

    return GraphQLRequests.extractResponse(response, "data.replication.replications");
  }

  public void waitForNoReplications() {
    Awaitility.await("wait for no replications")
        .atMost(30L, TimeUnit.SECONDS)
        .until(() -> getAllReplications().size() == 0);
  }

  public Map<String, Object> createReplication(
      String name,
      String sourceId,
      String destinationId,
      String filter,
      boolean biDirectional,
      boolean suspended) {
    ExtractableResponse response =
        requestFactory
            .createRequest()
            .usingMutation("CreateReplication.graphql")
            .addArgument("name", name)
            .addArgument("sourceId", sourceId)
            .addArgument("destinationId", destinationId)
            .addArgument("filter", filter)
            .addArgument("biDirectional", biDirectional)
            .addArgument("suspended", suspended)
            .send()
            .getResponse();

    if (GraphQLRequests.responseHasErrors(response)) {
      return Collections.emptyMap();
    }

    return GraphQLRequests.extractResponse(response, "data.createReplication");
  }

  public boolean updateReplication(
      String id,
      String name,
      String sourceId,
      String destinationId,
      String filter,
      boolean biDirectional,
      boolean suspended) {
    ExtractableResponse response =
        requestFactory
            .createRequest()
            .usingMutation("UpdateReplication.graphql")
            .addArgument("id", id)
            .addArgument("name", name)
            .addArgument("sourceId", sourceId)
            .addArgument("destinationId", destinationId)
            .addArgument("filter", filter)
            .addArgument("biDirectional", biDirectional)
            .addArgument("suspended", suspended)
            .send()
            .getResponse();

    if (GraphQLRequests.responseHasErrors(response)) {
      return false;
    }

    return GraphQLRequests.extractResponse(response, "data.updateReplication");
  }

  public void waitForReplicationWithName(String name) {
    Awaitility.await(String.format("failed to retrieve expected replication with name %s", name))
        .atMost(30L, TimeUnit.SECONDS)
        .until(
            () ->
                getAllReplications().stream().anyMatch(config -> config.get("name").equals(name)));
  }

  public boolean deleteReplication(String pid) {
    ExtractableResponse response =
        requestFactory
            .createRequest()
            .usingMutation("DeleteReplication.graphql")
            .addArgument("id", pid)
            .send()
            .getResponse();

    if (GraphQLRequests.responseHasErrors(response)) {
      return false;
    }

    return GraphQLRequests.extractResponse(response, "data.deleteReplication");
  }

  public void waitForReplication(Map<String, Object> expectedConfig) {
    Awaitility.await("failed to retrieve expected replication")
        .atMost(30L, TimeUnit.SECONDS)
        .until(() -> getAllReplications().stream().anyMatch(expectedConfig::equals));
  }
}
