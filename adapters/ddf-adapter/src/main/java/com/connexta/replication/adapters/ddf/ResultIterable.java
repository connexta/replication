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

import static com.google.common.collect.Iterators.limit;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import com.connexta.replication.data.QueryRequestImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.QueryResponse;

/**
 * Copied from DDF and modified for replication purposes.
 *
 * <p>Class used to iterate over the {@link Metadata} objects contained in a {@link QueryResponse}
 * returned when executing a {@link QueryRequest}. The class will fetch new results as needed until
 * all results that match the query provided have been exhausted.
 *
 * <p>Since the class may use the page size provided in the {@link QueryRequest} to fetch the
 * results, its value should be carefully set to avoid any memory or performance issues.
 */
public class ResultIterable implements Iterable<Metadata> {

  private final Function<QueryRequest, List<Metadata>> queryFunction;

  private final QueryRequest queryRequest;

  private final int maxResultCount;

  private ResultIterable(
      Function<QueryRequest, List<Metadata>> queryFunction,
      QueryRequest queryRequest,
      int maxResultCount) {
    notNull(queryFunction, "Query function cannot be null");
    notNull(queryRequest, "Query request cannot be null");
    isTrue(maxResultCount >= 0, "Max Results cannot be negative", maxResultCount);

    this.queryFunction = queryFunction;
    this.queryRequest = queryRequest;
    this.maxResultCount = maxResultCount;
  }

  /**
   * Creates an iterable that will call a {@link Function} to retrieve the results that match the
   * {@link QueryRequest} provided. There will be no limit to the number of results returned.
   *
   * @param queryFunction reference to the {@link Function} to call to retrieve the results.
   * @param queryRequest request used to retrieve the results.
   */
  public static ResultIterable resultIterable(
      Function<QueryRequest, List<Metadata>> queryFunction, QueryRequest queryRequest) {
    return new ResultIterable(queryFunction, queryRequest, 0);
  }

  private static Stream<Metadata> stream(Iterator<Metadata> iterator) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
  }

  @Override
  public Iterator<Metadata> iterator() {
    if (maxResultCount > 0) {
      return limit(new ResultIterator(queryFunction, queryRequest), maxResultCount);
    }
    return new ResultIterator(queryFunction, queryRequest);
  }

  public Stream<Metadata> stream() {
    return stream(iterator());
  }

  private static class ResultIterator implements Iterator<Metadata> {

    private final Function<QueryRequest, List<Metadata>> queryFunction;
    private final Set<String> foundIds = new HashSet<>(2048);
    private int currentIndex;
    private QueryRequest queryRequestCopy;
    private Iterator<Metadata> results = Collections.emptyIterator();
    private boolean finished = false;

    ResultIterator(
        Function<QueryRequest, List<Metadata>> queryFunction, QueryRequest queryRequest) {
      this.queryFunction = queryFunction;

      copyQueryRequestAndQuery(queryRequest, queryRequest.getStartIndex());

      this.currentIndex = queryRequestCopy.getStartIndex();
    }

    @Override
    public boolean hasNext() {
      if (results.hasNext()) {
        return true;
      }

      if (finished) {
        return false;
      }

      fetchNextResults();

      return hasNext();
    }

    @Override
    public Metadata next() {
      if (results.hasNext()) {
        return results.next();
      }

      if (finished) {
        throw new NoSuchElementException("No more results match the specified getRecords");
      }

      fetchNextResults();

      if (!results.hasNext()) {
        throw new NoSuchElementException("No more results match the specified getRecords");
      }

      return results.next();
    }

    private void fetchNextResults() {
      copyQueryRequestAndQuery(queryRequestCopy, currentIndex);

      try {
        final List<Metadata> resultList = queryFunction.apply(queryRequestCopy);

        currentIndex += resultList.size();

        List<Metadata> dedupedResults = new ArrayList<>(resultList.size());
        for (Metadata result : resultList) {
          if (isDistinctResult(result)) {
            dedupedResults.add(result);
          }
          Optional.ofNullable(result).map(Metadata::getId).ifPresent(foundIds::add);
        }

        this.results = dedupedResults.iterator();

        if (dedupedResults.isEmpty()) {
          finished = true;
        }
      } catch (Exception e) {
        throw new ReplicationException(e);
      }
    }

    private boolean isDistinctResult(@Nullable Metadata result) {
      return result != null && (result.getId() == null || !foundIds.contains(result.getId()));
    }

    private void copyQueryRequestAndQuery(QueryRequest request, int index) {

      this.queryRequestCopy =
          new QueryRequestImpl(
              request.getCql(),
              request.getExcludedNodes(),
              request.getFailedItemIds(),
              request.getModifiedAfter(),
              index,
              request.getPageSize());
    }
  }
}
