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
import java.util.Map;

public class StatsGraphQL {

  private GraphQLRequests requestFactory;

  public StatsGraphQL(String graphqlEndpoint) {
    requestFactory =
        new GraphQLRequests(
            StatsGraphQL.class, "/query/stats/query/", "/query/stats/mutation/", graphqlEndpoint);
  }

  public boolean updateStats(String replicationName, Map<String, Object> stats) {
    ExtractableResponse response =
        requestFactory
            .createRequest()
            .usingMutation("UpdateStats.graphql")
            .addArgument("replicationName", replicationName)
            .addArgument("stats", stats)
            .send()
            .getResponse();

    if (GraphQLRequests.responseHasErrors(response)) {
      return false;
    }

    return GraphQLRequests.extractResponse(response, "data.updateReplicationStats");
  }
}
