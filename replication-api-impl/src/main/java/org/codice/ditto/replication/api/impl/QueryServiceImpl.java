package org.codice.ditto.replication.api.impl;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Attribute;
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
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
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

  private final InputTransformer xmlTransformer;

  /**
   * Creates a new QueryService
   *
   * @param framework The catalog framework to retrieve metacards from.
   * @param builder A filter builder
   * @param xmlTransformer An xml input transformer for transforming the query metacard xml on a
   *     workspace metacard into a {@link Metacard} object.
   */
  public QueryServiceImpl(
      CatalogFramework framework, FilterBuilder builder, InputTransformer xmlTransformer) {
    this.framework = framework;
    this.builder = builder;
    this.xmlTransformer = xmlTransformer;
  }

  /**
   * Provides a stream for retrieving all saved user queries as {@link Query}s.
   *
   * @return all the user queries saved on the system
   * @throws QueryException if an exception occurs while querying the local catalog
   */
  @Override
  public Stream<Query> queries() {
    Filter filter = builder.attribute(Core.METACARD_TAGS).is().equalTo().text("workspace");
    PropertyName sortProperty = new PropertyNameImpl(Core.METACARD_MODIFIED);
    SortBy sortPolicy = new SortByImpl(sortProperty, SortOrder.DESCENDING);
    QueryImpl query = new QueryImpl(filter, 1, 100, sortPolicy, false, 0L);
    QueryResponse queryResponse;
    try {
      queryResponse = framework.query(new QueryRequestImpl(query));
    } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
      throw new QueryException("Failed to retrieve Query metacards.", e);
    }

    return queryResponse
        .getResults()
        .stream()
        .map(Result::getMetacard)
        .flatMap(this::queryXmlStrings)
        .map(this::xmlToMetacard)
        .map(this::createQueryFromMetacard);
  }

  private Stream<String> queryXmlStrings(Metacard metacard) {
    Attribute attribute = metacard.getAttribute("queries");
    if (attribute != null) {
      return attribute.getValues().stream().map(String::valueOf);
    }
    return Stream.empty();
  }

  private Metacard xmlToMetacard(String xml) {
    try (InputStream is = IOUtils.toInputStream(xml, Charset.defaultCharset())) {
      return xmlTransformer.transform(is);
    } catch (IOException | CatalogTransformerException ex) {
      throw new QueryException("Exception occurred while transforming query Metacards", ex);
    }
  }

  private org.codice.ditto.replication.api.impl.data.QueryImpl createQueryFromMetacard(
      Metacard mcard) {
    String title = (String) mcard.getAttribute(Core.TITLE).getValue();
    String cql = (String) mcard.getAttribute("cql").getValue();
    return new org.codice.ditto.replication.api.impl.data.QueryImpl(title, cql);
  }
}
