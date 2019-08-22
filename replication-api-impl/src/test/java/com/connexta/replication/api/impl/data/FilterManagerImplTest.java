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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.impl.persistence.pojo.FilterPojo;
import com.connexta.replication.api.impl.persistence.spring.FilterRepository;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

  @Test(expected = ReplicationPersistenceException.class)
  public void getThrowsPersistenceException() {
    when(filterRepository.findById(anyString())).thenThrow(new ReplicationPersistenceException());
    filterManager.get("id");
  }

  @Test
  public void objects() {
    when(filterRepository.findAll()).thenReturn(List.of(makeDefaultPojo()));
    Filter filter = filterManager.objects().findFirst().orElse(null);
    assertThat(filter.getId(), is(ID));
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getPriority(), is(PRIORITY));
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void objectsThrowsPersistenceException() {
    when(filterRepository.findAll()).thenThrow(new ReplicationPersistenceException());
    filterManager.objects().findFirst().orElse(null);
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

  @Test(expected = ReplicationPersistenceException.class)
  public void saveThrowsPersistenceException() {
    when(filterRepository.save(any(FilterPojo.class)))
        .thenThrow(new ReplicationPersistenceException());
    filterManager.save(new FilterImpl(makeDefaultPojo()));
  }

  @Test
  public void remove() {
    filterManager.remove("id");
    verify(filterRepository).deleteById("id");
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

  @Test(expected = ReplicationPersistenceException.class)
  public void filtersForSiteThrowsPersistenceException() {
    when(filterRepository.findBySiteId(anyString()))
        .thenThrow(new ReplicationPersistenceException());
    filterManager.filtersForSite(SITE_ID).findFirst().orElse(null);
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
