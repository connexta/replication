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
package org.codice.ditto.replication.api.impl;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.types.Core;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.impl.PropertyNameImpl;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codice.ditto.replication.api.QueryException;
import org.codice.ditto.replication.api.QueryService;
import org.codice.ditto.replication.api.data.Query;
import org.geotools.filter.SortByImpl;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

/** A service which fetches query metacards from the catalog and returns them as {@link Query}s. */
public class QueryServiceImpl implements QueryService {

  private static final int START_INDEX = 1;

  private static final int PAGE_SIZE = 100;

  private final CatalogFramework framework;

  private final FilterBuilder builder;

  /**
   * Creates a new QueryService
   *
   * @param framework The catalog framework to retrieve metacards from.
   * @param builder A filter builder
   */
  public QueryServiceImpl(CatalogFramework framework, FilterBuilder builder) {
    this.framework = framework;
    this.builder = builder;
  }

  /**
   * Provides a stream for retrieving all saved user queries as {@link Query}s.
   *
   * @return all the user queries saved on the system
   * @throws QueryException if an exception occurs while querying the local catalog
   */
  @Override
  public Stream<Query> queries() {
    Filter filter = builder.attribute(Core.METACARD_TAGS).is().equalTo().text("query");
    PropertyName sortProperty = new PropertyNameImpl(Core.METACARD_MODIFIED);
    SortBy sortPolicy = new SortByImpl(sortProperty, SortOrder.DESCENDING);
    QueryImpl query = new QueryImpl(filter, START_INDEX, PAGE_SIZE, sortPolicy, false, 0L);
    QueryResponse queryTypeResponse;
    try {
      queryTypeResponse = framework.query(new QueryRequestImpl(query));
    } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
      throw new QueryException("Failed to retrieve Query metacards.", e);
    }

    return queryTypeResponse.getResults().stream()
        .map(Result::getMetacard)
        .map(this::createQueryFromMetacard);
  }

  private org.codice.ditto.replication.api.impl.data.QueryImpl createQueryFromMetacard(
      Metacard mcard) {
    String title = (String) mcard.getAttribute(Core.TITLE).getValue();
    String cql = (String) mcard.getAttribute("cql").getValue();
    String sortJson =
        mcard.getAttribute("sorts").getValues().stream()
            .map(value -> value.toString().replace('=', ':'))
            .collect(Collectors.joining(",", "::[", "]"));
    return new org.codice.ditto.replication.api.impl.data.QueryImpl(title, cql + sortJson);
  }
}
