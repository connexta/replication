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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.impl.persistence.spring.FilterIndexRepository;
import com.connexta.replication.api.persistence.FilterIndexManager;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FilterIndexManagerImplTest {

  private FilterIndexManager sites;

  @Mock FilterIndexRepository repository;

  @Before
  public void setup() {
    sites = new FilterIndexManagerImpl(repository);
  }

  @Test(expected = NotFoundException.class)
  public void testGetBySiteNotFound() {
    final String id = "id";
    when(repository.findById(id)).thenReturn(Optional.empty());
    sites.get(id);
  }

  @Test(expected = NotFoundException.class)
  public void testGetBySiteIdNotFound() {
    final String id = "id";
    when(repository.findByFilterId(id)).thenReturn(Optional.empty());
    sites.getByFilter(id);
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void testSaveWrongObjectType() {
    sites.save(mock(FilterIndex.class));
  }
}
