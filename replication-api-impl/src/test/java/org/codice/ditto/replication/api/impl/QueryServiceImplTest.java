package org.codice.ditto.replication.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.transform.InputTransformer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.codice.ditto.replication.api.QueryException;
import org.codice.ditto.replication.api.data.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryServiceImplTest {

  @Mock private CatalogFramework framework;

  @Mock private InputTransformer xmlTransformer;

  private QueryServiceImpl queryService;

  @Before
  public void setup() {
    this.queryService =
        new QueryServiceImpl(framework, new GeotoolsFilterBuilder(), xmlTransformer);
  }

  @Test
  public void queries() throws Exception {
    QueryResponse mockQueryResponse = mock(QueryResponse.class);
    when(framework.query(any(QueryRequestImpl.class))).thenReturn(mockQueryResponse);

    Metacard workspace = new MetacardImpl();
    workspace.setAttribute(new AttributeImpl(Core.METACARD_TAGS, "workspace"));
    workspace.setAttribute(new AttributeImpl("queries", "mockXmlString"));
    Result result = new ResultImpl(workspace);
    when(mockQueryResponse.getResults()).thenReturn(Collections.singletonList(result));

    Metacard query = new MetacardImpl();
    query.setAttribute(new AttributeImpl(Core.TITLE, "mockTitle"));
    query.setAttribute(new AttributeImpl("cql", "mockCql"));
    when(xmlTransformer.transform(any(InputStream.class))).thenReturn(query);

    Query resultingQuery = queryService.queries().findFirst().orElse(null);
    assertThat(resultingQuery.getTitle(), is("mockTitle"));
    assertThat(resultingQuery.getCql(), is("mockCql"));
  }

  @Test(expected = QueryException.class)
  public void queriesFrameworkException() throws Exception {
    when(framework.query(any(QueryRequestImpl.class))).thenThrow(new UnsupportedQueryException());
    queryService.queries();
  }

  @Test
  public void queriesNullQueriesReturnsEmpty() throws Exception {
    QueryResponse mockQueryResponse = mock(QueryResponse.class);
    when(framework.query(any(QueryRequestImpl.class))).thenReturn(mockQueryResponse);

    Metacard workspace = new MetacardImpl();
    workspace.setAttribute(new AttributeImpl(Core.METACARD_TAGS, "workspace"));
    Result result = new ResultImpl(workspace);
    when(mockQueryResponse.getResults()).thenReturn(Collections.singletonList(result));

    Query resultingQuery = queryService.queries().findFirst().orElse(null);
    assertThat(resultingQuery, is(nullValue()));
  }

  @Test(expected = QueryException.class)
  public void queriesTransformException() throws Exception {
    QueryResponse mockQueryResponse = mock(QueryResponse.class);
    when(framework.query(any(QueryRequestImpl.class))).thenReturn(mockQueryResponse);

    Metacard workspace = new MetacardImpl();
    workspace.setAttribute(new AttributeImpl(Core.METACARD_TAGS, "workspace"));
    workspace.setAttribute(new AttributeImpl("queries", "mockXmlString"));
    Result result = new ResultImpl(workspace);
    when(mockQueryResponse.getResults()).thenReturn(Collections.singletonList(result));

    when(xmlTransformer.transform(any(InputStream.class))).thenThrow(new IOException());
    Query resultingQuery = queryService.queries().findFirst().orElse(null);
  }
}
