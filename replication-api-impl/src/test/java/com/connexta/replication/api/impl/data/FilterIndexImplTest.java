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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.impl.persistence.pojo.FilterIndexPojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterIndexImplTest {

  private static final Instant MODIFIED_SINCE = Instant.now();

  private static final String ID = "id";

  @Rule public ExpectedException exception = ExpectedException.none();

  private Filter filter;

  @Before
  public void setup() {
    filter = mock(Filter.class);
    when(filter.getId()).thenReturn(ID);
  }

  @Test
  public void testFromFilterCtor() {
    FilterIndexImpl index = new FilterIndexImpl(filter);
    assertThat(index.getModifiedSince(), OptionalMatchers.isEmpty());
    assertThat(index.getId(), is(ID));
  }

  @Test
  public void testFromPojoCtor() {
    FilterIndexPojo pojo = new FilterIndexPojo().setModifiedSince(MODIFIED_SINCE).setId(ID);
    FilterIndexImpl index = new FilterIndexImpl(pojo);
    assertThat(index.getId(), is(ID));
    assertThat(index.getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE)));
  }

  @Test
  public void testWriteTo() {
    FilterIndexImpl index = new FilterIndexImpl(filter);
    index.setModifiedSince(MODIFIED_SINCE);
    FilterIndexPojo pojo = index.writeTo(new FilterIndexPojo());
    assertThat(pojo.getId(), is(ID));
    assertThat(pojo.getModifiedSince(), is(MODIFIED_SINCE));
    assertThat(pojo.getVersion(), is(FilterIndexPojo.CURRENT_VERSION));
  }

  @Test
  public void testWriteToInvalidIndex() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("missing filter_index id");
    FilterIndexImpl index = new FilterIndexImpl();
    index.writeTo(new FilterIndexPojo());
  }

  @Test
  public void testReadFrom() {
    FilterIndexPojo pojo = new FilterIndexPojo().setModifiedSince(MODIFIED_SINCE).setId(ID);
    FilterIndexImpl index = new FilterIndexImpl();
    index.readFrom(pojo);
    assertThat(index.getId(), is(ID));
    assertThat(index.getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE)));
  }

  @Test
  public void testReadFromModifiedSinceNull() {
    FilterIndexPojo pojo = new FilterIndexPojo().setModifiedSince(null).setId(ID);
    FilterIndexImpl index = new FilterIndexImpl();
    index.readFrom(pojo);
    assertThat(index.getModifiedSince(), OptionalMatchers.isEmpty());
  }

  @Test
  public void testReadFromPreviousVersion() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("unsupported");
    FilterIndexPojo pojo =
        new FilterIndexPojo().setId(ID).setVersion(FilterIndexPojo.MINIMUM_VERSION - 1);
    FilterIndexImpl index = new FilterIndexImpl();
    index.readFrom(pojo);
  }

  @Test
  public void testReadFromFutureVersion() {
    FilterIndexPojo pojo =
        new FilterIndexPojo()
            .setModifiedSince(MODIFIED_SINCE)
            .setId(ID)
            .setVersion(FilterIndexPojo.CURRENT_VERSION + 1);
    FilterIndexImpl index = new FilterIndexImpl(pojo);
    assertThat(index.getId(), is(ID));
    assertThat(index.getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE)));
  }

  @Test
  public void testSetAndGetModifiedSince() {
    final Instant modifiedSince = Instant.now();
    FilterIndex index = new FilterIndexImpl();
    index.setModifiedSince(modifiedSince);
    assertThat(index.getModifiedSince(), isPresentAnd(is(modifiedSince)));
  }
}
