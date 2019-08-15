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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.InvalidFieldException;
import com.connexta.ion.replication.api.NonTransientReplicationPersistenceException;
import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.RecoverableReplicationPersistenceException;
import com.connexta.ion.replication.api.TransientReplicationPersistenceException;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.impl.persistence.pojo.FilterPojo;
import com.connexta.replication.api.impl.persistence.spring.FilterRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;

@RunWith(MockitoJUnitRunner.class)
public class FilterManagerImplTest {

  private static final String ID = "id";

  private static final String SITE_ID = "site_id";

  private static final String FILTER = "filter";

  private static final String NAME = "name";

  private static final String DESCRIPTION = "description";

  private static final byte PRIORITY = 2;

  @Mock private FilterRepository filterRepository;

  private FilterManagerImpl filterManager;

  @Before
  public void setup() {
    filterManager = new FilterManagerImpl(filterRepository);
  }

  @Test
  public void get() {
    when(filterRepository.findById(anyString())).thenReturn(Optional.of(makeDefaultPojo()));
    Filter filter = filterManager.get(ID);
    assertThat(filter.getId(), is(ID));
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getPriority(), is(PRIORITY));
  }

  @Test(expected = NotFoundException.class)
  public void getFilterNotFound() {
    when(filterRepository.findById(anyString())).thenReturn(Optional.empty());
    filterManager.get("id");
  }

  @Test(expected = InvalidFieldException.class)
  public void getFailedToDeserialize() {
    when(filterRepository.findById(anyString()))
        .thenReturn(Optional.of(makeDefaultPojo().setFilter(null)));
    filterManager.get("id");
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testGetWithNonTransientFailure() throws Exception {
    Mockito.when(filterRepository.findById(ID))
        .thenThrow(new NonTransientDataAccessResourceException("testing"));

    filterManager.get(ID);
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testGetWithTransientFailure() throws Exception {
    Mockito.when(filterRepository.findById(ID))
        .thenThrow(new TransientDataAccessResourceException("testing"));

    filterManager.get(ID);
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testGetWithRecoverableFailure() throws Exception {
    Mockito.when(filterRepository.findById(ID))
        .thenThrow(new RecoverableDataAccessException("testing"));

    filterManager.get(ID);
  }

  @Test
  public void objects() {
    when(filterRepository.findAll())
        .thenReturn(List.of(makeDefaultPojo(), makeDefaultPojo().setId("id2")));
    List<Filter> filters = filterManager.objects().collect(Collectors.toList());

    assertThat(filters.size(), is(2));
    assertThat(filters.get(0).getId(), is(ID));
    assertThat(filters.get(0).getSiteId(), is(SITE_ID));
    assertThat(filters.get(0).getDescription().get(), is(DESCRIPTION));
    assertThat(filters.get(0).getFilter(), is(FILTER));
    assertThat(filters.get(0).getName(), is(NAME));
    assertThat(filters.get(0).getPriority(), is(PRIORITY));
    assertThat(filters.get(1).getId(), is("id2"));
    assertThat(filters.get(1).getSiteId(), is(SITE_ID));
    assertThat(filters.get(1).getDescription().get(), is(DESCRIPTION));
    assertThat(filters.get(1).getFilter(), is(FILTER));
    assertThat(filters.get(1).getName(), is(NAME));
    assertThat(filters.get(1).getPriority(), is(PRIORITY));
  }

  @Test
  public void objectsNoneFound() {
    when(filterRepository.findAll()).thenReturn(Collections.emptyList());
    Filter filter = filterManager.objects().findFirst().orElse(null);
    assertThat(filter, Matchers.nullValue());
  }

  @Test(expected = InvalidFieldException.class)
  public void objectsFailToDeserialize() {
    when(filterRepository.findAll())
        .thenReturn(List.of(makeDefaultPojo(), makeDefaultPojo().setId("id2").setFilter(null)));

    filterManager.objects().collect(Collectors.toList());
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testObjectsWithNonTransientFailure() {
    Mockito.when(filterRepository.findAll())
        .thenThrow(new NonTransientDataAccessResourceException("testing"));

    filterManager.objects().count();
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testObjectsWithTransientFailure() {
    Mockito.when(filterRepository.findAll())
        .thenThrow(new TransientDataAccessResourceException("testing"));

    filterManager.objects().count();
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testObjectsWithRecoverableFailure() {
    Mockito.when(filterRepository.findAll())
        .thenThrow(new RecoverableDataAccessException("testing"));

    filterManager.objects().collect(Collectors.toList());
  }

  @Test
  public void save() {
    FilterPojo filterPojo = makeDefaultPojo();
    filterManager.save(new FilterImpl(filterPojo));
    verify(filterRepository).save(eq(filterPojo));
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveIllegalObject() {
    Filter testFilter = mock(Filter.class);
    filterManager.save(testFilter);
  }

  @Test(expected = InvalidFieldException.class)
  public void saveFailToSerialize() {
    filterManager.save(new FilterImpl());
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testSaveWithNonTransientFailure() throws Exception {
    Mockito.doThrow(new NonTransientDataAccessResourceException("testing"))
        .when(filterRepository)
        .save(Mockito.any());

    filterManager.save(new FilterImpl(makeDefaultPojo()));
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testSaveWithTransientFailure() throws Exception {
    Mockito.doThrow(new TransientDataAccessResourceException("testing"))
        .when(filterRepository)
        .save(Mockito.any());

    filterManager.save(new FilterImpl(makeDefaultPojo()));
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testSaveWithRecoverableFailure() throws Exception {
    Mockito.doThrow(new RecoverableDataAccessException("testing"))
        .when(filterRepository)
        .save(Mockito.any());

    filterManager.save(new FilterImpl(makeDefaultPojo()));
  }

  @Test
  public void remove() {
    filterManager.remove("id");
    verify(filterRepository).deleteById("id");
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testRemoveWithNonTransientFailure() throws Exception {
    Mockito.doThrow(new NonTransientDataAccessResourceException("testing"))
        .when(filterRepository)
        .deleteById("id");

    filterManager.remove("id");
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testRemoveWithTransientFailure() throws Exception {
    Mockito.doThrow(new TransientDataAccessResourceException("testing"))
        .when(filterRepository)
        .deleteById("id");

    filterManager.remove("id");
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testRemoveWithRecoverableFailure() throws Exception {
    Mockito.doThrow(new RecoverableDataAccessException("testing"))
        .when(filterRepository)
        .deleteById("id");

    filterManager.remove("id");
  }

  @Test
  public void filtersForSite() {
    when(filterRepository.findBySiteId(anyString())).thenReturn(List.of(makeDefaultPojo()));
    Filter filter = filterManager.filtersForSite(SITE_ID).findFirst().orElse(null);
    assertThat(filter.getId(), is(ID));
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getPriority(), is(PRIORITY));
  }

  @Test(expected = InvalidFieldException.class)
  public void filtersForSiteFailToDeserialize() {
    when(filterRepository.findBySiteId(anyString()))
        .thenReturn(List.of(makeDefaultPojo().setFilter(null)));
    filterManager.filtersForSite(SITE_ID).findFirst().orElse(null);
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testFiltersForSiteWithNonTransientFailure() throws Exception {
    Mockito.doThrow(new NonTransientDataAccessResourceException("testing"))
        .when(filterRepository)
        .findBySiteId(SITE_ID);

    filterManager.filtersForSite(SITE_ID);
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testFiltersForSiteWithTransientFailure() throws Exception {
    Mockito.doThrow(new TransientDataAccessResourceException("testing"))
        .when(filterRepository)
        .findBySiteId(SITE_ID);

    filterManager.filtersForSite(SITE_ID);
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testFiltersForSiteWithRecoverableFailure() throws Exception {
    Mockito.doThrow(new RecoverableDataAccessException("testing"))
        .when(filterRepository)
        .findBySiteId(SITE_ID);

    filterManager.filtersForSite(SITE_ID);
  }

  private FilterPojo makeDefaultPojo() {
    return new FilterPojo()
        .setId(ID)
        .setVersion(1)
        .setSiteId(SITE_ID)
        .setFilter(FILTER)
        .setName(NAME)
        .setDescription(DESCRIPTION)
        .setSuspended(true)
        .setPriority(PRIORITY);
  }
}
