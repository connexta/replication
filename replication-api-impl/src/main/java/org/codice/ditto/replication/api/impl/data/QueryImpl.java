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
