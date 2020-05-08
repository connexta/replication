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
package com.connexta.replication.adapters.ddf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.data.MetadataImpl;
import com.connexta.replication.data.QueryRequestImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultIterableTest {

  @Mock Function<QueryRequest, List<Metadata>> queryFunction;

  @Test
  public void resultIterableOnlyOnePage() {
    when(queryFunction.apply(any(QueryRequest.class)))
        .thenReturn(Collections.singletonList(getMetadata()), Collections.emptyList());
    Iterator<Metadata> iter =
        ResultIterable.resultIterable(queryFunction, new QueryRequestImpl("title like '*'"))
            .iterator();
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.hasNext(), is(false));
    verify(queryFunction, times(2)).apply(any(QueryRequest.class));
  }

  @Test
  public void resultIterableMultiplePages() {
    when(queryFunction.apply(any(QueryRequest.class)))
        .thenReturn(
            Arrays.asList(getMetadata(), getMetadata()),
            Collections.singletonList(getMetadata()),
            Collections.emptyList());
    Iterator<Metadata> iter =
        ResultIterable.resultIterable(queryFunction, new QueryRequestImpl("title like '*'"))
            .iterator();
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.hasNext(), is(false));
    verify(queryFunction, times(3)).apply(any(QueryRequest.class));
  }

  @Test
  public void resultIterablePageSize() {
    when(queryFunction.apply(any(QueryRequest.class)))
        .thenReturn(
            Collections.singletonList(getMetadata()),
            Collections.singletonList(getMetadata()),
            Collections.singletonList(getMetadata()),
            Collections.emptyList());
    Iterator<Metadata> iter =
        ResultIterable.resultIterable(queryFunction, new QueryRequestImpl("title like '*'", 1, 1))
            .iterator();
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.hasNext(), is(false));
    verify(queryFunction, times(4)).apply(any(QueryRequest.class));
  }

  @Test
  public void stream() {
    when(queryFunction.apply(any(QueryRequest.class)))
        .thenReturn(Collections.singletonList(getMetadata()), Collections.emptyList());
    ResultIterable resultIterable =
        ResultIterable.resultIterable(queryFunction, new QueryRequestImpl("title like '*'"));
    assertThat(resultIterable.stream().count(), is(1l));
  }

  @Test
  public void multipleQueryRequests() {
    String filter1 = "title like 'failed'";
    String filter2 = "title like '*'";
    QueryRequest request1 = new QueryRequestImpl(filter1);
    QueryRequest request2 = new QueryRequestImpl(filter2);
    when(queryFunction.apply(any(QueryRequest.class))).thenReturn(Collections.emptyList());
    Iterator<Metadata> iter =
        ResultIterable.resultIterable(queryFunction, request1, request2).iterator();
    assertThat(iter.hasNext(), is(false));
    ArgumentCaptor<QueryRequest> requestCaptor = ArgumentCaptor.forClass(QueryRequest.class);
    verify(queryFunction, times(2)).apply(requestCaptor.capture());
    List<String> queryFilters =
        requestCaptor.getAllValues().stream()
            .map(QueryRequest::getCql)
            .collect(Collectors.toList());
    assertTrue(queryFilters.contains(filter1));
    assertTrue(queryFilters.contains(filter2));
  }

  @Test
  public void multipleQueryRequestsMultiplePages() {
    String filter1 = "title like 'failed'";
    String filter2 = "title like '*'";
    QueryRequest request1 = new QueryRequestImpl(filter1);
    QueryRequest request2 = new QueryRequestImpl(filter2);
    when(queryFunction.apply(any(QueryRequest.class)))
        .thenReturn(
            Arrays.asList(getMetadata(), getMetadata()),
            Collections.emptyList(),
            Collections.singletonList(getMetadata()),
            Collections.emptyList());
    Iterator<Metadata> iter =
        ResultIterable.resultIterable(queryFunction, request1, request2).iterator();
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.next(), is(notNullValue()));
    assertThat(iter.hasNext(), is(false));
    verify(queryFunction, times(4)).apply(any(QueryRequest.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void allNullQueryRequestsThrowsException() {
    ResultIterable.resultIterable(queryFunction, null);
  }

  @Test
  public void nullQueryRequestIsRemoved() {
    String filter1 = "title like '*'";
    QueryRequest request1 = new QueryRequestImpl(filter1);
    when(queryFunction.apply(any(QueryRequest.class))).thenReturn(Collections.emptyList());
    Iterator<Metadata> iter =
        ResultIterable.resultIterable(queryFunction, null, request1).iterator();
    assertThat(iter.hasNext(), is(false));
    verify(queryFunction, times(1)).apply(any(QueryRequest.class));
  }

  private Metadata getMetadata() {
    return new MetadataImpl(
        new HashMap<String, String>(), Map.class, UUID.randomUUID().toString(), new Date());
  }
}
