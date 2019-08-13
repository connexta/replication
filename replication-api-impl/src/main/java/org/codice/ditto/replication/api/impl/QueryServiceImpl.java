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
    QueryImpl query = new QueryImpl(filter, 1, 100, sortPolicy, false, 0L);
    QueryResponse queryTypeResponse;
    try {
      queryTypeResponse = framework.query(new QueryRequestImpl(query));
    } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
      throw new QueryException("Failed to retrieve Query metacards.", e);
    }

    return queryTypeResponse
        .getResults()
        .stream()
        .map(Result::getMetacard)
        .map(this::createQueryFromMetacard);
  }

  private org.codice.ditto.replication.api.impl.data.QueryImpl createQueryFromMetacard(
      Metacard mcard) {
    String title = (String) mcard.getAttribute(Core.TITLE).getValue();
    String cql = (String) mcard.getAttribute("cql").getValue();
    return new org.codice.ditto.replication.api.impl.data.QueryImpl(title, cql);
  }
}
