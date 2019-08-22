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

import com.connexta.ion.replication.api.NonTransientReplicationPersistenceException;
import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.RecoverableReplicationPersistenceException;
import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.ion.replication.api.TransientReplicationPersistenceException;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.impl.persistence.pojo.FilterIndexPojo;
import com.connexta.replication.api.impl.persistence.spring.FilterIndexRepository;
import com.connexta.replication.api.persistence.FilterIndexManager;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;

/**
 * Simple implementation for FilterIndexManager which wraps the {@link FilterIndexRepository} and
 * exposes a limited set of functionality.
 */
public class FilterIndexManagerImpl implements FilterIndexManager {

  private final FilterIndexRepository indexRepository;

  /**
   * Creates a new FilterIndexManager.
   *
   * @param indexRepository repository for site indices
   */
  public FilterIndexManagerImpl(FilterIndexRepository indexRepository) {
    this.indexRepository = indexRepository;
  }

  @Override
  public FilterIndex getOrCreate(Filter filter) {
    try {
      return indexRepository
          .findById(filter.getId())
          .map(FilterIndexImpl::new)
          .orElse(new FilterIndexImpl(filter));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public FilterIndex get(String id) {
    try {
      return indexRepository
          .findById(id)
          .map(FilterIndexImpl::new)
          .orElseThrow(() -> new NotFoundException("Unable to find filter index with id=" + id));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public Stream<FilterIndex> objects() {
    try {
      return StreamSupport.stream(indexRepository.findAll().spliterator(), false)
          .map(FilterIndexImpl::new);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void save(FilterIndex index) {
    if (!(index instanceof FilterIndexImpl)) {
      throw new ReplicationPersistenceException(
          "Expected FilterIndexImpl but got " + index.getClass().getSimpleName());
    }
    try {
      indexRepository.save(((FilterIndexImpl) index).writeTo(new FilterIndexPojo()));
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
      indexRepository.deleteById(id);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }
}
