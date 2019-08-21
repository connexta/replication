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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.impl.persistence.pojo.FilterIndexPojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterIndexImplTest {

  private static final Instant MODIFIED_SINCE = Instant.now();

  private static final String FILTER_ID = "filterId";

  private static final String ID = "id";

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testDefaultCtor() {
    FilterIndexImpl index = new FilterIndexImpl();
    assertThat(index.getModifiedSince(), OptionalMatchers.isEmpty());
    assertThat(index.getFilterId(), nullValue());
    assertThat(index.getId(), notNullValue());
  }

  @Test
  public void testWriteTo() {
    FilterIndexImpl index = new FilterIndexImpl(MODIFIED_SINCE, FILTER_ID);
    FilterIndexPojo pojo = index.writeTo(new FilterIndexPojo());
    assertThat(pojo.getId(), notNullValue());
    assertThat(pojo.getFilterId(), is(FILTER_ID));
    assertThat(pojo.getModifiedSince(), is(MODIFIED_SINCE));
    assertThat(pojo.getVersion(), is(FilterIndexPojo.CURRENT_VERSION));
  }

  @Test
  public void testWriteToWithNullFilterString() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("filterId");
    FilterIndexImpl index = new FilterIndexImpl(MODIFIED_SINCE, null);
    index.writeTo(new FilterIndexPojo());
  }

  @Test
  public void testWriteToWithEmptyFilterString() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("filterId");
    FilterIndexImpl index = new FilterIndexImpl(MODIFIED_SINCE, "");
    index.writeTo(new FilterIndexPojo());
  }

  @Test
  public void testPojoCtor() {
    FilterIndexPojo pojo =
        new FilterIndexPojo().setFilterId(FILTER_ID).setModifiedSince(MODIFIED_SINCE).setId(ID);
    FilterIndexImpl index = new FilterIndexImpl(pojo);
    assertThat(index.getFilterId(), is(FILTER_ID));
    assertThat(index.getId(), is(ID));
    assertThat(index.getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE)));
  }

  @Test
  public void testReadFrom() {
    FilterIndexPojo pojo =
        new FilterIndexPojo().setFilterId(FILTER_ID).setModifiedSince(MODIFIED_SINCE).setId(ID);
    FilterIndexImpl index = new FilterIndexImpl();
    index.readFrom(pojo);
    assertThat(index.getFilterId(), is(FILTER_ID));
    assertThat(index.getId(), is(ID));
    assertThat(index.getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE)));
  }

  @Test
  public void testReadFromFilterIdEmpty() {
    exception.expect(ReplicationPersistenceException.class);
    FilterIndexPojo pojo =
        new FilterIndexPojo().setFilterId("").setModifiedSince(MODIFIED_SINCE).setId(ID);
    FilterIndexImpl index = new FilterIndexImpl();
    index.readFrom(pojo);
  }

  @Test
  public void testReadFromFilterIdNull() {
    exception.expect(ReplicationPersistenceException.class);
    FilterIndexPojo pojo =
        new FilterIndexPojo().setFilterId(null).setModifiedSince(MODIFIED_SINCE).setId(ID);
    FilterIndexImpl index = new FilterIndexImpl();
    index.readFrom(pojo);
  }

  @Test
  public void testReadFromModifiedSinceNull() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("modifiedSince");
    FilterIndexPojo pojo =
        new FilterIndexPojo().setFilterId(FILTER_ID).setModifiedSince(null).setId(ID);
    FilterIndexImpl index = new FilterIndexImpl();
    index.readFrom(pojo);
  }

  @Test
  public void testReadFromPreviousVersion() {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage("unsupported");
    FilterIndexPojo pojo =
        new FilterIndexPojo().setId(ID).setVersion(FilterIndexPojo.MINIMUM_VERSION - 1);
    new FilterIndexImpl(pojo);
  }

  @Test
  public void testReadFromFutureVersion() {
    FilterIndexPojo pojo =
        new FilterIndexPojo()
            .setFilterId(FILTER_ID)
            .setModifiedSince(MODIFIED_SINCE)
            .setId(ID)
            .setVersion(FilterIndexPojo.CURRENT_VERSION + 1);
    FilterIndexImpl index = new FilterIndexImpl();
    index.readFrom(pojo);
    assertThat(index.getFilterId(), is(FILTER_ID));
    assertThat(index.getId(), is(ID));
    assertThat(index.getModifiedSince(), isPresentAnd(is(MODIFIED_SINCE)));
  }
}
