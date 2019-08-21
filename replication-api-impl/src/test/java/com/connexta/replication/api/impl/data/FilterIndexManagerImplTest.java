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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.impl.persistence.pojo.FilterIndexPojo;
import com.connexta.replication.api.impl.persistence.spring.FilterIndexRepository;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.time.Instant;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FilterIndexManagerImplTest {

  private static final String ID = "id";

  private static final String ID1 = "id1";
  private static final String FILTER_ID1 = "filterId1";
  private static final Instant MODIFIED_SINCE1 = Instant.ofEpochSecond(100);
  private static final FilterIndexPojo POJO1 =
      new FilterIndexPojo().setId(ID1).setFilterId(FILTER_ID1).setModifiedSince(MODIFIED_SINCE1);

  private static final String ID2 = "id2";
  private static final String FILTER_ID2 = "filterId2";
  private static final Instant MODIFIED_SINCE2 = Instant.ofEpochSecond(200);
  private static final FilterIndexPojo POJO2 =
      new FilterIndexPojo().setId(ID2).setFilterId(FILTER_ID2).setModifiedSince(MODIFIED_SINCE2);

  private static final FilterIndexPojo INVALID_POJO =
      new FilterIndexPojo().setId(ID).setFilterId(null).setModifiedSince(null);

  private FilterIndexManager sites;

  @Mock FilterIndexRepository repository;

  @Rule public ExpectedException exception = ExpectedException.none();

  @Before
  public void setup() {
    sites = new FilterIndexManagerImpl(repository);
  }

  @Test
  public void testGetById() {
    when(repository.findById(ID1)).thenReturn(Optional.of(POJO1));
    FilterIndex index = sites.get(ID1);
    assertThat(index.getId(), is(ID1));
  }

  @Test
  public void testGetByIdNotFound() {
    exception.expect(NotFoundException.class);
    when(repository.findById(ID)).thenReturn(Optional.empty());
    sites.get(ID);
  }

  @Test
  public void testGetByIdInvalidPojo() {
    exception.expect(ReplicationPersistenceException.class);
    when(repository.findById(ID)).thenReturn(Optional.of(INVALID_POJO));
    sites.get(ID);
  }

  @Test
  public void testGetByFilterId() {
    when(repository.findByFilterId(FILTER_ID1)).thenReturn(Optional.of(POJO1));
    FilterIndex index = sites.getByFilter(FILTER_ID1).get();
    assertThat(index.getFilterId(), is(FILTER_ID1));
  }

  @Test
  public void testGetByFilterIdNotFound() {
    when(repository.findByFilterId(ID)).thenReturn(Optional.empty());
    assertThat(sites.getByFilter(ID), OptionalMatchers.isEmpty());
  }

  @Test
  public void testGetByFilterIdInvalidPojo() {
    exception.expect(ReplicationPersistenceException.class);
    when(repository.findByFilterId(ID)).thenReturn(Optional.of(INVALID_POJO));
    sites.getByFilter(ID);
  }

  @Test
  public void testSaveWrongObjectType() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("Expected FilterIndexImpl");
    sites.save(mock(FilterIndex.class));
  }

  //  @Test
  //  public void testObjects() {
  //    when(repository.findAll()).thenReturn(List.of(POJO1, POJO2));
  //    assertThat(sites.objects().collect(Collectors.toList()), containsInAnyOrder(POJO1, POJO2));
  //  }

  @Test
  public void testRemove() {
    sites.remove(ID);
    verify(repository).deleteById(ID);
  }
}
