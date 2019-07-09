package org.codice.ditto.replication.admin.query.requests;

import com.jayway.restassured.response.ExtractableResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.codice.ditto.replication.admin.query.requests.GraphQLRequests.GraphQLRequest;

public class QueriesGraphQL {

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
        .atMost(30L, TimeUnit.SECONDS)
        .until(() -> getAllQueries().stream().anyMatch(expectedConfig::equals));
  }
}
