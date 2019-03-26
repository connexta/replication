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
package org.codice.ditto.replication.api.impl.persistence;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.types.Core;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.impl.DeleteRequestImpl;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.util.impl.ResultIterable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metacards {

  private static final Logger LOGGER = LoggerFactory.getLogger(Metacards.class);

  private static final int BATCH_SIZE = 250;

  private final CatalogFramework catalogFramework;

  private final FilterBuilder filterBuilder;

  public Metacards(CatalogFramework catalogFramework, FilterBuilder filterBuilder) {
    this.catalogFramework = catalogFramework;
    this.filterBuilder = filterBuilder;
  }

  public Set<String> getIdsOfMetacardsInCatalog(Set<String> ids) {
    List<Filter> filters = new ArrayList<>();

    for (String idString : ids) {
      filters.add(filterBuilder.attribute(Core.ID).is().equalTo().text(idString));
    }
    Filter filter = filterBuilder.anyOf(filters);

    QueryRequest request =
        new QueryRequestImpl(
            new QueryImpl(
                filter, 1, ids.size(), new SortByImpl(Core.ID, SortOrder.ASCENDING), false, 0L));

    ResultIterable results = ResultIterable.resultIterable(catalogFramework::query, request);
    return results
        .stream()
        .map(Result::getMetacard)
        .map(Metacard::getId)
        .collect(Collectors.toSet());
  }

  public void doDelete(String[] idsToDelete) throws SourceUnavailableException {
    if (idsToDelete.length > 0) {
      int start = 0;
      int end;

      while (start < idsToDelete.length) {
        end = start + BATCH_SIZE;
        if (end > idsToDelete.length) {
          end = idsToDelete.length;
        }
        deleteBatch(Arrays.copyOfRange(idsToDelete, start, end));
        start += BATCH_SIZE;
      }
    }
  }

  private void deleteBatch(String[] idsToDelete) throws SourceUnavailableException {
    try {
      catalogFramework.delete(new DeleteRequestImpl(idsToDelete));
    } catch (IngestException ie) {

      // One metacard failing to delete will cause the entire batch to not be deleted. So,
      // if the batch fails, perform the deletes individually and just skip over the ones that fail.
      for (String id : idsToDelete) {
        try {
          catalogFramework.delete(new DeleteRequestImpl(id));
        } catch (IngestException e) {
          LOGGER.debug("Failed to delete metacard with id: {}", id, e);
        }
      }
    }
  }
}
