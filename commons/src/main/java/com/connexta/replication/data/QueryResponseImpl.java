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
package com.connexta.replication.data;

import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.QueryResponse;
import java.util.Iterator;
import java.util.function.Function;

/** Simple implementation of {@link QueryResponse}. */
public class QueryResponseImpl implements QueryResponse {

  private final Iterable<Metadata> metadata;

  private final Function<Exception, RuntimeException> exceptionHandler;

  /**
   * Instantiates a new query response.
   *
   * @param metadata the metadata to provide as part of this response
   * @param exceptionHandler a exception handler which will be consulted whenever an exception
   *     occurs while iterating the metadata capable of transforming the exception if needed
   */
  public QueryResponseImpl(
      Iterable<Metadata> metadata, Function<Exception, RuntimeException> exceptionHandler) {
    this.metadata = metadata;
    this.exceptionHandler = exceptionHandler;
  }

  private Iterator<Metadata> iterator() {
    return new Iterator<>() {
      final Iterator<Metadata> iterator = metadata.iterator();

      @Override
      public Metadata next() {
        if (Thread.interrupted()) {
          throw exceptionHandler.apply(new InterruptedException());
        }
        try {
          return iterator.next();
        } catch (RuntimeException e) {
          throw exceptionHandler.apply(e);
        }
      }

      @Override
      public boolean hasNext() {
        if (Thread.interrupted()) {
          throw exceptionHandler.apply(new InterruptedException());
        }
        try {
          return iterator.hasNext();
        } catch (RuntimeException e) {
          throw exceptionHandler.apply(e);
        }
      }

      @Override
      public void remove() {
        if (Thread.interrupted()) {
          throw exceptionHandler.apply(new InterruptedException());
        }
        try {
          iterator.remove();
        } catch (RuntimeException e) {
          throw exceptionHandler.apply(e);
        }
      }
    };
  }

  @Override
  public Iterable<Metadata> getMetadata() {
    return this::iterator;
  }
}
