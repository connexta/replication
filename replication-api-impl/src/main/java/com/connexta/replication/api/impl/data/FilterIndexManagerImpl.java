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
import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.impl.persistence.pojo.FilterIndexPojo;
import com.connexta.replication.api.impl.persistence.spring.FilterIndexRepository;
import com.connexta.replication.api.persistence.FilterIndexManager;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

  // TODO 8/21/2019 PeterHuffer: Fix after kiefer's PR is merged
  @Override
  public FilterIndex getOrCreate(String filterId) {
    return null;
  }

  @Override
  public Optional<FilterIndex> getByFilter(String filterId) {
    return indexRepository.findByFilterId(filterId).map(FilterIndexImpl::new);
  }

  @Override
  public FilterIndex get(String id) {
    return indexRepository
        .findById(id)
        .map(FilterIndexImpl::new)
        .orElseThrow(() -> new NotFoundException("Unable to find filter index with id=" + id));
  }

  @Override
  public Stream<FilterIndex> objects() {
    return StreamSupport.stream(indexRepository.findAll().spliterator(), false)
        .map(FilterIndexImpl::new);
  }

  @Override
  public void save(FilterIndex index) {
    if (index instanceof FilterIndexImpl) {
      indexRepository.save(((FilterIndexImpl) index).writeTo(new FilterIndexPojo()));
    } else {
      throw new ReplicationPersistenceException(
          "Expected FilterIndexImpl but got " + index.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    indexRepository.deleteById(id);
  }
}
