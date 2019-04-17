package org.codice.ditto.replication.api.data;

import java.util.Date;
import java.util.List;

/**
 * A query request object to be sent to {@link org.codice.ditto.replication.api.NodeAdapter}s to
 * fetch {@link Metadata}.
 */
public interface QueryRequest {

  /**
   * A CQL string representing a filter to search on.
   *
   * <p>If other criteria of the query request is available, it must be respected in the final
   * query.
   *
   * @return the cql
   */
  String getCql();

  /**
   * A list of system names that should be compared against the lineage of a {@link Metadata}. If
   * the {@link Metadata}s lineage contains the {@link
   * org.codice.ditto.replication.api.NodeAdapter}s name, it should not be returned in the query.
   *
   * @return a list of system names to exclude, or empty list if there are none.
   */
  List<String> getExcludedNodes();

  /**
   * A list of {@link Metadata} IDs that have failed to be replicated between {@link
   * org.codice.ditto.replication.api.NodeAdapter}s by the {@link
   * org.codice.ditto.replication.api.Replicator}.
   *
   * @return a list of IDs, or empty list if there are none
   */
  List<String> getFailedItemIds();

  /**
   * A {@link Date} indicating that queried {@link Metadata} should have a modified date after.
   *
   * @return the modified after {@link Date}
   */
  Date getModifiedAfter();
}
