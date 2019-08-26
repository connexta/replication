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

import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.NonTransientReplicationPersistenceException;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.RecoverableReplicationPersistenceException;
import com.connexta.replication.api.data.TransientReplicationPersistenceException;
import com.connexta.replication.api.impl.persistence.pojo.FilterPojo;
import com.connexta.replication.api.impl.persistence.spring.FilterRepository;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.FilterManager;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;

/** Performs CRUD operations for {@link Filter}s. */
public class FilterManagerImpl implements FilterManager {
  private final FilterRepository filterRepository;

  private final FilterIndexManager filterIndexManager;

  public FilterManagerImpl(
      FilterRepository filterRepository, FilterIndexManager filterIndexManager) {
    this.filterRepository = filterRepository;
    this.filterIndexManager = filterIndexManager;
  }

  @Override
  public Filter get(String id) {
    try {
      return filterRepository
          .findById(id)
          .map(FilterImpl::new)
          .orElseThrow(
              () -> new NotFoundException(String.format("Cannot find filter with ID: %s", id)));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public Stream<Filter> objects() {
    try {
      return filterStreamOf(filterRepository.findAll().spliterator());
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void save(Filter filter) {
    if (!(filter instanceof FilterImpl)) {
      throw new IllegalArgumentException(
          "Expected a FilterImpl but got a " + filter.getClass().getSimpleName());
    }
    try {
      filterRepository.save(((FilterImpl) filter).writeTo(new FilterPojo()));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void remove(String id) {
    try {
      filterRepository.deleteById(id);
      filterIndexManager.remove(id);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public Stream<Filter> filtersForSite(String siteId) {
    try {
      return filterStreamOf(filterRepository.findBySiteId(siteId).spliterator());
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  // coverts the pojos to Filters and returns them as a stream
  private Stream<Filter> filterStreamOf(Spliterator<FilterPojo> filterPojos) {
    return StreamSupport.stream(filterPojos, false).map(FilterImpl::new).map(Filter.class::cast);
  }
}
