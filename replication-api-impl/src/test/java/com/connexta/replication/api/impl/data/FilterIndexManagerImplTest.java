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

import com.connexta.ion.replication.api.InvalidFieldException;
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
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;

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
  public void testGetExistingIndex() {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID1);
    when(repository.findById(ID1)).thenReturn(Optional.of(POJO1));
    FilterIndex filterIndex = indices.getOrCreate(filter);
    assertThat(filterIndex.getId(), is(ID1));
    assertThat(filterIndex.getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE1)));
  }

  @Test
  public void testGetExistingIndexIsInvalid() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage("missing filter_index id");
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
    when(repository.findById(ID)).thenReturn(Optional.of(INVALID_POJO));
    indices.getOrCreate(filter);
  }

  @Test
  public void testCreateNewIndex() {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
    when(repository.findById(ID)).thenReturn(Optional.empty());
    FilterIndex filterIndex = indices.getOrCreate(filter);
    assertThat(filterIndex.getId(), is(ID));
    assertThat(filterIndex.getModifiedSince(), OptionalMatchers.isEmpty());
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testGetOrCreateWithNonTransientFailure() throws Exception {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
    Mockito.when(repository.findById(ID))
        .thenThrow(new NonTransientDataAccessResourceException("testing"));

    indices.getOrCreate(filter);
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testGetOrCreateWithTransientFailure() throws Exception {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
    Mockito.when(repository.findById(ID))
        .thenThrow(new TransientDataAccessResourceException("testing"));

    indices.getOrCreate(filter);
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testGetOrCreateWithRecoverableFailure() throws Exception {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
    Mockito.when(repository.findById(ID)).thenThrow(new RecoverableDataAccessException("testing"));

    indices.getOrCreate(filter);
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
    exception.expect(InvalidFieldException.class);
    when(repository.findById(ID)).thenReturn(Optional.of(INVALID_POJO));
    indices.get(ID);
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testGetWithNonTransientFailure() throws Exception {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
    Mockito.when(repository.findById(ID))
        .thenThrow(new NonTransientDataAccessResourceException("testing"));

    indices.getOrCreate(filter);
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testGetWithTransientFailure() throws Exception {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
    Mockito.when(repository.findById(ID))
        .thenThrow(new TransientDataAccessResourceException("testing"));

    indices.getOrCreate(filter);
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testGetWithRecoverableFailure() throws Exception {
    Filter filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
    Mockito.when(repository.findById(ID)).thenThrow(new RecoverableDataAccessException("testing"));

    indices.getOrCreate(filter);
  }

  @Test
  public void testSaveInvalidFilterIndex() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage("missing filter_index id");
    indices.save(new FilterIndexImpl(INVALID_POJO));
  }

  @Test
  public void testSaveWrongObjectType() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("Expected FilterIndexImpl");
    indices.save(mock(FilterIndex.class));
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testSaveWithNonTransientFailure() throws Exception {
    Mockito.doThrow(new NonTransientDataAccessResourceException("testing"))
        .when(repository)
        .save(Mockito.any());

    indices.save(new FilterIndexImpl(new FilterImpl()));
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testSaveWithTransientFailure() throws Exception {
    Mockito.doThrow(new TransientDataAccessResourceException("testing"))
        .when(repository)
        .save(Mockito.any());

    indices.save(new FilterIndexImpl(new FilterImpl()));
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testSaveWithRecoverableFailure() throws Exception {
    Mockito.doThrow(new RecoverableDataAccessException("testing"))
        .when(repository)
        .save(Mockito.any());

    indices.save(new FilterIndexImpl(new FilterImpl()));
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
    exception.expect(InvalidFieldException.class);
    when(repository.findAll()).thenReturn(List.of(POJO1, INVALID_POJO));
    indices.objects().collect(Collectors.toList());
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testObjectsWithNonTransientFailure() {
    Mockito.when(repository.findAll())
        .thenThrow(new NonTransientDataAccessResourceException("testing"));

    indices.objects().count();
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testObjectsWithTransientFailure() {
    Mockito.when(repository.findAll())
        .thenThrow(new TransientDataAccessResourceException("testing"));

    indices.objects().count();
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testObjectsWithRecoverableFailure() {
    Mockito.when(repository.findAll()).thenThrow(new RecoverableDataAccessException("testing"));

    indices.objects().collect(Collectors.toList());
  }

  @Test
  public void testRemove() {
    indices.remove(ID);
    verify(repository).deleteById(ID);
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testRemoveWithNonTransientFailure() throws Exception {
    Mockito.doThrow(new NonTransientDataAccessResourceException("testing"))
        .when(repository)
        .deleteById(ID);

    indices.remove(ID);
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testRemoveWithTransientFailure() throws Exception {
    Mockito.doThrow(new TransientDataAccessResourceException("testing"))
        .when(repository)
        .deleteById(ID);

    indices.remove(ID);
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testRemoveWithRecoverableFailure() throws Exception {
    Mockito.doThrow(new RecoverableDataAccessException("testing")).when(repository).deleteById(ID);

    indices.remove(ID);
  }
}
