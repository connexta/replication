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
import org.codice.ditto.replication.admin.query.requests.GraphQLRequests.GraphQLRequest;

public class QueriesGraphQL {

  private static long WAIT_TIME = 30L;

  private GraphQLRequests requestFactory;

  public QueriesGraphQL(String graphqlEndpoint) {
    requestFactory =
        new GraphQLRequests(
            QueriesGraphQL.class,
            "/query/queries/query/",
            "/query/queries/mutation/",
            graphqlEndpoint);
  }

  public List<Map<String, Object>> getAllQueries() {
    GraphQLRequest graphQLRequest = requestFactory.createRequest();
    graphQLRequest.usingQuery("AllQueries.graphql");

    ExtractableResponse response = graphQLRequest.send().getResponse();
    if (GraphQLRequests.responseHasErrors(response)) {
      return Collections.emptyList();
    }

    return GraphQLRequests.extractResponse(response, "data.replication.queries");
  }

  public void waitForQuery(Map<String, Object> expectedConfig) {
    Awaitility.await("failed to retrieve expected query")
        .atMost(WAIT_TIME, TimeUnit.SECONDS)
        .until(() -> getAllQueries().stream().anyMatch(expectedConfig::equals));
  }
}
