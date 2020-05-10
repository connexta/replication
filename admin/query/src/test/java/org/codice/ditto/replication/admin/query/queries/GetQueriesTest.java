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
package org.codice.ditto.replication.admin.query.queries;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ditto.replication.api.QueryService;
import org.codice.ditto.replication.api.data.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetQueriesTest {

  @Mock QueryService queryService;

  GetQueries getQueries;

  @Before
  public void setup() {
    getQueries = new GetQueries(queryService);
  }

  @Test
  public void performFunction() {
    Query mockQuery = mock(Query.class);
    when(mockQuery.getTitle()).thenReturn("title");
    when(mockQuery.getCql()).thenReturn("cql");
    when(queryService.queries()).thenReturn(Stream.of(mockQuery));
    ListField<QueryField> queryFields = getQueries.performFunction();
    verify(queryService).queries();
    QueryField queryField = queryFields.getList().get(0);
    assertThat(queryField.title(), is("title"));
    assertThat(queryField.cql(), is("cql"));
  }

  @Test
  public void getFunctionErrorCodes() {
    assertThat(getQueries.getFunctionErrorCodes(), is(Collections.emptySet()));
  }

  @Test
  public void getArguments() {
    assertThat(getQueries.getArguments(), is(Collections.emptyList()));
  }

  @Test
  public void getReturnType() {
    assertThat(getQueries.getReturnType(), is(instanceOf(QueryField.ListImpl.class)));
  }

  @Test
  public void newInstance() {
    FunctionField<ListField<QueryField>> newInstance = getQueries.newInstance();
    assertThat(newInstance, is(instanceOf(GetQueries.class)));
    assertThat(newInstance.getReturnType(), is(instanceOf(QueryField.ListImpl.class)));
  }
}
