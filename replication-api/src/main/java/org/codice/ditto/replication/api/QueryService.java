package org.codice.ditto.replication.api;

import java.util.stream.Stream;
import org.codice.ditto.replication.api.data.Query;

/** A service which fetches saved user {@link Query}s. */
public interface QueryService {

  /**
   * Provides a stream for retrieving all saved user queries as {@link Query}s.
   *
   * @return all the user queries saved on the system
   * @throws QueryException if an exception occurs while querying the local catalog
   */
  Stream<Query> queries();
}
