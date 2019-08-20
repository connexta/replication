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
package com.connexta.replication.api.impl.data;

import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.impl.persistence.pojo.FilterPojo;
import com.connexta.replication.api.impl.persistence.spring.FilterRepository;
import com.connexta.replication.api.persistence.FilterManager;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Performs CRUD operations for {@link Filter}s. */
public class FilterManagerImpl implements FilterManager {

  private final FilterRepository filterRepository;

  public FilterManagerImpl(FilterRepository filterRepository) {
    this.filterRepository = filterRepository;
  }

  @Override
  public Filter create() {
    return new FilterImpl();
  }

  @Override
  public Filter get(String id) {
    return filterRepository
        .findById(id)
        .map(FilterImpl::new)
        .orElseThrow(
            () -> new NotFoundException(String.format("Cannot find filter with ID: %s", id)));
  }

  @Override
  public Stream<Filter> objects() {
    return filterStreamOf(filterRepository.findAll().spliterator());
  }

  @Override
  public void save(Filter filter) {
    if (filter instanceof FilterImpl) {
      filterRepository.save(((FilterImpl) filter).writeTo(new FilterPojo()));
    } else {
      throw new IllegalArgumentException(
          "Expected a FilterImpl but got a " + filter.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    filterRepository.deleteById(id);
  }

  @Override
  public Stream<Filter> filtersForSite(String siteId) {
    return filterStreamOf(filterRepository.findBySiteId(siteId).spliterator());
  }

  // coverts the pojos to Filters and returns them as a stream
  private Stream<Filter> filterStreamOf(Spliterator<FilterPojo> filterPojos) {
    return StreamSupport.stream(filterPojos, false).map(FilterImpl::new).map(Filter.class::cast);
  }
}
