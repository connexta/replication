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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.ResultImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.UnsupportedQueryException;
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

  private QueryServiceImpl queryService;

  @Before
  public void setup() {
    this.queryService = new QueryServiceImpl(framework, new GeotoolsFilterBuilder());
  }

  @Test
  public void queries() throws Exception {
    QueryResponse mockQueryResponse = mock(QueryResponse.class);
    when(framework.query(any(QueryRequestImpl.class))).thenReturn(mockQueryResponse);

    Metacard query = new MetacardImpl();
    query.setAttribute(new AttributeImpl(Core.METACARD_TAGS, "query"));
    query.setAttribute(new AttributeImpl(Core.TITLE, "mockTitle"));
    query.setAttribute(new AttributeImpl("cql", "mockCql"));
    when(mockQueryResponse.getResults())
        .thenReturn(Collections.singletonList(new ResultImpl(query)));

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

    when(mockQueryResponse.getResults()).thenReturn(Collections.emptyList());

    Query resultingQuery = queryService.queries().findFirst().orElse(null);
    assertThat(resultingQuery, is(nullValue()));
  }
}
