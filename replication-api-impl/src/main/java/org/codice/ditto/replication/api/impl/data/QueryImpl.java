package org.codice.ditto.replication.api.impl.data;

import org.codice.ditto.replication.api.data.Query;

/** A POJO for representing user queries that are retrieved from the catalog. */
public class QueryImpl implements Query {

  private String title;

  private String cql;

  /**
   * Creates a new Query object.
   *
   * @param title the title of the query
   * @param cql the cql of the query
   */
  public QueryImpl(String title, String cql) {
    this.title = title;
    this.cql = cql;
  }

  /**
   * Get the title of the query
   *
   * @return the title of the query
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Set the title of the query
   *
   * @param title the title of the query
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get the cql of the query
   *
   * @return the cql of the query
   */
  @Override
  public String getCql() {
    return cql;
  }

  /**
   * Set the cql of the query
   *
   * @param cql the cql of the query
   */
  public void setCql(String cql) {
    this.cql = cql;
  }
}
