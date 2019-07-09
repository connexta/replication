package org.codice.ditto.replication.api.data;

import javax.annotation.Nullable;

/** A POJO for representing user queries that are retrieved from the catalog. */
public interface Query {

  /**
   * Get the title of this query.
   *
   * @return the title of the query
   */
  @Nullable
  String getTitle();

  /**
   * Get the cql of the query.
   *
   * @return the cql of the query
   */
  String getCql();
}
