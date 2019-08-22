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

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.connexta.ion.replication.api.InvalidFieldException;
import com.connexta.ion.replication.api.UnsupportedVersionException;
import com.connexta.replication.api.impl.persistence.pojo.FilterPojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterImplTest {

  private static final String ID = "id";

  private static final String SITE_ID = "site_id";

  private static final String FILTER = "filter";

  private static final String NAME = "name";

  private static final String DESCRIPTION = "description";

  private static final byte PRIORITY = 2;

  private FilterImpl filter;

  @Rule public ExpectedException exception = ExpectedException.none();

  @Before
  public void setup() {
    filter = new FilterImpl();
  }

  @Test
  public void testDefaultCtor() throws Exception {
    final FilterImpl persistable = new FilterImpl();

    Assert.assertThat(persistable.getId(), Matchers.not(Matchers.emptyOrNullString()));
    Assert.assertThat(persistable.getName(), Matchers.nullValue());
    Assert.assertThat(persistable.getDescription(), OptionalMatchers.isEmpty());
    Assert.assertThat(persistable.getSiteId(), Matchers.nullValue());
    Assert.assertThat(persistable.getFilter(), Matchers.nullValue());
    Assert.assertThat(persistable.isSuspended(), Matchers.equalTo(false));
    Assert.assertThat(persistable.getPriority(), Matchers.equalTo((byte) 1));
  }

  @Test
  public void testCtorWithPojo() throws Exception {
    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter = new FilterImpl(pojo);
    assertThat(filter.getId(), is(ID));
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.isSuspended(), is(true));
    assertThat(filter.getPriority(), is(PRIORITY));
  }

  @Test
  public void gettersAndSetters() {
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.isSuspended(), is(true));
    assertThat(filter.getPriority(), is(PRIORITY));
  }

  @Test
  public void readFrom() {
    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter.readFrom(pojo);
    assertThat(filter.getId(), is(ID));
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.isSuspended(), is(true));
    assertThat(filter.getPriority(), is(PRIORITY));
  }

  @Test
  public void readFromFutureVersion() {
    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION + 1)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter.readFrom(pojo);
    assertThat(filter.getId(), is(ID));
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.isSuspended(), is(true));
    assertThat(filter.getPriority(), is(PRIORITY));
  }

  @Test
  public void readFromWithUnsupportedVersion() {
    exception.expect(UnsupportedVersionException.class);
    exception.expectMessage(Matchers.matchesPattern(".*unsupported.*version.*"));

    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(-1)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);

    filter.readFrom(pojo);
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.isSuspended(), is(true));
    assertThat(filter.getPriority(), is(PRIORITY));
  }

  @Test
  public void readFromWithNullSiteId() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*siteId.*"));

    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter.readFrom(pojo);
  }

  @Test
  public void readFromWithEmptySiteId() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*siteId.*"));

    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId("")
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter.readFrom(pojo);
  }

  @Test
  public void readFromWithNullFilter() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*filter.*"));

    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId(SITE_ID)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter.readFrom(pojo);
  }

  @Test
  public void readFromWithEmptyFilter() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*filter.*"));

    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId(SITE_ID)
            .setFilter("")
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter.readFrom(pojo);
  }

  @Test
  public void readFromWithNullName() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*name.*"));

    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter.readFrom(pojo);
  }

  @Test
  public void readFromWithEmptyName() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*name.*"));

    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName("")
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority(PRIORITY);
    filter.readFrom(pojo);
  }

  @Test
  public void readfromWithPriorityLessThanOne() {
    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority((byte) 0);
    filter.readFrom(pojo);
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.isSuspended(), is(true));
    assertThat(filter.getPriority(), is((byte) 1));
  }

  @Test
  public void readfromWithPriorityGreaterThanTen() {
    FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(FilterPojo.CURRENT_VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(true)
            .setPriority((byte) 11);
    filter.readFrom(pojo);
    assertThat(filter.getSiteId(), is(SITE_ID));
    assertThat(filter.getFilter(), is(FILTER));
    assertThat(filter.getName(), is(NAME));
    assertThat(filter.getDescription().get(), is(DESCRIPTION));
    assertThat(filter.isSuspended(), is(true));
    assertThat(filter.getPriority(), is((byte) 10));
  }

  @Test
  public void writeTo() {
    FilterPojo pojo = new FilterPojo();
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    filter.writeTo(pojo);
    assertThat(pojo.getId(), is(ID));
    assertThat(pojo.getVersion(), is(FilterPojo.CURRENT_VERSION));
    assertThat(pojo.getSiteId(), is(SITE_ID));
    assertThat(pojo.getFilter(), is(FILTER));
    assertThat(pojo.getName(), is(NAME));
    assertThat(pojo.getDescription(), is(DESCRIPTION));
    assertThat(pojo.isSuspended(), is(true));
    assertThat(pojo.getPriority(), is(PRIORITY));
  }

  @Test
  public void writeToWithNullSiteId() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*siteId.*"));

    FilterPojo pojo = new FilterPojo();
    filter.setId(ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    filter.writeTo(pojo);
  }

  @Test
  public void writeToWithEmptySiteId() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*siteId.*"));

    FilterPojo pojo = new FilterPojo();
    filter.setId(ID);
    filter.setSiteId("");
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    filter.writeTo(pojo);
  }

  @Test
  public void writeToWithNullFilter() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*filter.*"));

    FilterPojo pojo = new FilterPojo();
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    filter.writeTo(pojo);
  }

  @Test
  public void writeToWithEmptyFilter() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*filter.*"));

    FilterPojo pojo = new FilterPojo();
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter("");
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    filter.writeTo(pojo);
  }

  @Test
  public void writeToWithNullName() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*name.*"));

    FilterPojo pojo = new FilterPojo();
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    filter.writeTo(pojo);
  }

  @Test
  public void writeToWithEmptyName() {
    exception.expect(InvalidFieldException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*name.*"));

    FilterPojo pojo = new FilterPojo();
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName("");
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    filter.writeTo(pojo);
  }

  @Test
  public void equals() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId(ID);
    filter2.setSiteId(SITE_ID);
    filter2.setFilter(FILTER);
    filter2.setName(NAME);
    filter2.setDescription(DESCRIPTION);
    filter2.setSuspended(true);
    filter2.setPriority(PRIORITY);

    assertTrue(filter.equals(filter2));
    assertTrue(filter2.equals(filter2));
  }

  @Test
  public void equalsWithDifferentId() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId("numatwo");
    filter2.setSiteId(SITE_ID);
    filter2.setFilter(FILTER);
    filter2.setName(NAME);
    filter2.setDescription(DESCRIPTION);
    filter2.setSuspended(true);
    filter2.setPriority(PRIORITY);

    assertFalse(filter.equals(filter2));
    assertFalse(filter2.equals(filter));
  }

  @Test
  public void equalsWithDifferentName() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId(ID);
    filter2.setSiteId(SITE_ID);
    filter2.setFilter(FILTER);
    filter2.setName("numatwo");
    filter2.setDescription(DESCRIPTION);
    filter2.setSuspended(true);
    filter2.setPriority(PRIORITY);

    assertFalse(filter.equals(filter2));
    assertFalse(filter2.equals(filter));
  }

  @Test
  public void equalsWithDifferentSiteId() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId(ID);
    filter2.setSiteId("numatwo");
    filter2.setFilter(FILTER);
    filter2.setName(NAME);
    filter2.setDescription(DESCRIPTION);
    filter2.setSuspended(true);
    filter2.setPriority(PRIORITY);

    assertFalse(filter.equals(filter2));
    assertFalse(filter2.equals(filter));
  }

  @Test
  public void equalsWithDifferentFilter() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId(ID);
    filter2.setSiteId(SITE_ID);
    filter2.setFilter("numatwo");
    filter2.setName(NAME);
    filter2.setDescription(DESCRIPTION);
    filter2.setSuspended(true);
    filter2.setPriority(PRIORITY);

    assertFalse(filter.equals(filter2));
    assertFalse(filter2.equals(filter));
  }

  @Test
  public void equalsWithDifferentDescription() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId(ID);
    filter2.setSiteId(SITE_ID);
    filter2.setFilter(FILTER);
    filter2.setName(NAME);
    filter2.setDescription("numatwo");
    filter2.setSuspended(true);
    filter2.setPriority(PRIORITY);

    assertFalse(filter.equals(filter2));
    assertFalse(filter2.equals(filter));
  }

  @Test
  public void equalsWithDifferentSuspendedValue() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId(ID);
    filter2.setSiteId(SITE_ID);
    filter2.setFilter(FILTER);
    filter2.setName(NAME);
    filter2.setDescription(DESCRIPTION);
    filter2.setSuspended(false);
    filter2.setPriority(PRIORITY);

    assertFalse(filter.equals(filter2));
    assertFalse(filter2.equals(filter));
  }

  @Test
  public void equalsWithDifferentPriority() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId(ID);
    filter2.setSiteId(SITE_ID);
    filter2.setFilter(FILTER);
    filter2.setName(NAME);
    filter2.setDescription(DESCRIPTION);
    filter2.setSuspended(true);
    filter2.setPriority((byte) 5);

    assertFalse(filter.equals(filter2));
    assertFalse(filter2.equals(filter));
  }

  @Test
  public void equalsWithDifferentObject() {
    assertFalse(filter.equals(new Object()));
  }

  @Test
  public void equalsWithNull() {
    assertFalse(filter.equals(null));
  }

  @Test
  public void hashcode() {
    filter.setId(ID);
    filter.setSiteId(SITE_ID);
    filter.setFilter(FILTER);
    filter.setName(NAME);
    filter.setDescription(DESCRIPTION);
    filter.setSuspended(true);
    filter.setPriority(PRIORITY);
    FilterImpl filter2 = new FilterImpl();

    filter2.setId(ID);
    filter2.setSiteId(SITE_ID);
    filter2.setFilter(FILTER);
    filter2.setName(NAME);
    filter2.setDescription(DESCRIPTION);
    filter2.setSuspended(true);
    filter2.setPriority(PRIORITY);

    assertThat(filter.hashCode(), is(filter2.hashCode()));
    filter2.setName("numbatwo");
    assertThat(filter.hashCode(), not(filter2.hashCode()));
  }
}
