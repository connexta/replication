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

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterIndexManagerImplTest {

  private static final String ID = "id";

  private static final String ID1 = "id1";
  private static final Instant MODIFIED_SINCE1 = Instant.ofEpochSecond(100);
  private static final FilterIndexPojo POJO1 =
      new FilterIndexPojo().setId(ID1).setModifiedSince(MODIFIED_SINCE1);

  private static final String ID2 = "id2";
  private static final Instant MODIFIED_SINCE2 = Instant.ofEpochSecond(200);
  private static final FilterIndexPojo POJO2 =
      new FilterIndexPojo().setId(ID2).setModifiedSince(MODIFIED_SINCE2);

  private static final FilterIndexPojo INVALID_POJO =
      new FilterIndexPojo().setId(null).setModifiedSince(null);

  private FilterIndexManager indices;

  private FilterIndexRepository repository;

  @Rule public ExpectedException exception = ExpectedException.none();

  @Before
  public void setup() {
    repository = mock(FilterIndexRepository.class);
    indices = new FilterIndexManagerImpl(repository);
  }

  @Test
  public void testGetById() {
    when(repository.findById(ID1)).thenReturn(Optional.of(POJO1));
    FilterIndex index = indices.get(ID1);
    assertThat(index.getId(), is(ID1));
  }

  @Test
  public void testGetByIdNotFound() {
    exception.expect(NotFoundException.class);
    when(repository.findById(ID)).thenReturn(Optional.empty());
    indices.get(ID);
  }

  @Test
  public void testGetByIdInvalidPojo() {
    exception.expect(ReplicationPersistenceException.class);
    when(repository.findById(ID)).thenReturn(Optional.of(INVALID_POJO));
    indices.get(ID);
  }

  @Test
  public void testSaveInvalidFilterIndex() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("missing filter_index id");
    indices.save(new FilterIndexImpl(INVALID_POJO));
  }

  @Test
  public void testSaveWrongObjectType() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("Expected FilterIndexImpl");
    indices.save(mock(FilterIndex.class));
  }

  @Test
  public void testObjects() {
    when(repository.findAll()).thenReturn(List.of(POJO1, POJO2));
    List<FilterIndex> indices = this.indices.objects().collect(Collectors.toList());
    assertThat(indices.size(), is(2));
    assertThat(indices.get(0).getId(), is(ID1));
    assertThat(indices.get(0).getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE1)));
    assertThat(indices.get(1).getId(), is(ID2));
    assertThat(indices.get(1).getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE2)));
  }

  @Test
  public void testObjectsWithInvalidObject() {
    exception.expect(ReplicationPersistenceException.class);
    when(repository.findAll()).thenReturn(List.of(POJO1, INVALID_POJO));
    indices.objects().collect(Collectors.toList());
  }

  @Test
  public void testRemove() {
    indices.remove(ID);
    verify(repository).deleteById(ID);
  }
}
