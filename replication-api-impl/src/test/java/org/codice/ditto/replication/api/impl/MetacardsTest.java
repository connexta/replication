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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MetacardsTest {

  private Metacards metacards;

  @Mock CatalogFramework catalogFramework;

  @Before
  public void setup() {
    FilterBuilder filterBuilder = new GeotoolsFilterBuilder();
    metacards = new Metacards(catalogFramework, filterBuilder);
  }

  @Test
  public void testGetIdsOfMetacardsInCatalog()
      throws UnsupportedQueryException, FederationException, SourceUnavailableException {
    Set<String> ids = Sets.newHashSet(getIds(5));
    QueryResponse queryResponse = mock(QueryResponse.class);
    List<Result> results = mockResults(ids);
    when(queryResponse.getResults()).thenReturn(results);
    when(catalogFramework.query(any(QueryRequest.class))).thenReturn(queryResponse);

    assertThat(metacards.getIdsOfMetacardsInCatalog(ids), equalTo(ids));
  }

  @Test
  public void testBatchDelete() throws SourceUnavailableException, IngestException {
    final int batchSize = 5;
    String[] idsToDelete = getIds(batchSize * 3 + 1);

    metacards.doDelete(idsToDelete, batchSize);
    verify(catalogFramework, times(4)).delete(any(DeleteRequest.class));
  }

  @Test
  public void testBatchDeleteWithSomeFailures() throws SourceUnavailableException, IngestException {
    final int batchSize = 5;
    String[] idsToDelete = getIds(batchSize * 3);

    when(catalogFramework.delete(any(DeleteRequest.class)))
        .thenReturn(mock(DeleteResponse.class))
        .thenThrow(IngestException.class)
        .thenReturn(mock(DeleteResponse.class));

    metacards.doDelete(idsToDelete, batchSize);
    verify(catalogFramework, times(8)).delete(any(DeleteRequest.class));
  }

  private String[] getIds(int size) {
    String[] ids = new String[size];
    for (int i = 0; i < size; i++) {
      ids[i] = i + "";
    }
    return ids;
  }

  private List<Result> mockResults(Set<String> ids) {
    List<Result> results = new ArrayList<>();
    ids.forEach(id -> results.add(mockResult(id)));
    return results;
  }

  private Result mockResult(String metacardId) {
    Result result = mock(Result.class);
    Metacard metacard = mock(Metacard.class);
    when(metacard.getId()).thenReturn(metacardId);
    when(result.getMetacard()).thenReturn(metacard);
    return result;
  }
}
