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
package com.connexta.replication.api.impl.persistence.pojo;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;
import org.junit.Test;

public class FilterPojoTest {

  private static final String SITE_ID = "site_id";

  private static final String FILTER = "filter";

  private static final String NAME = "name";

  private static final String DESCRIPTION = "description";

  private static final byte PRIORITY = 2;

  private FilterPojo filterPojo = makeDefaultPojo();

  @Test
  public void gettersAndSetters() {
    assertThat(filterPojo.getSiteId(), is(SITE_ID));
    assertThat(filterPojo.getFilter(), is(FILTER));
    assertThat(filterPojo.getName(), is(NAME));
    assertThat(filterPojo.getDescription(), is(DESCRIPTION));
    assertThat(filterPojo.isSuspended(), is(true));
    assertThat(filterPojo.getPriority(), is(PRIORITY));
  }

  @Test
  public void equals() {
    FilterPojo filterPojo2 = makeDefaultPojo();
    assertTrue(filterPojo.equals(filterPojo2));
    assertTrue(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentId() {
    FilterPojo filterPojo2 = makeDefaultPojo().setId("numbatwo");
    assertFalse(filterPojo.equals(filterPojo2));
    assertFalse(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentVersion() {
    FilterPojo filterPojo2 = makeDefaultPojo().setVersion(2077);
    assertFalse(filterPojo.equals(filterPojo2));
    assertFalse(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentName() {
    FilterPojo filterPojo2 = makeDefaultPojo().setName("numbatwo");
    assertFalse(filterPojo.equals(filterPojo2));
    assertFalse(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentSiteId() {
    FilterPojo filterPojo2 = makeDefaultPojo().setSiteId("numbatwo");
    assertFalse(filterPojo.equals(filterPojo2));
    assertFalse(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentFilter() {
    FilterPojo filterPojo2 = makeDefaultPojo().setFilter("numbatwo");
    assertFalse(filterPojo.equals(filterPojo2));
    assertFalse(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentDescription() {
    FilterPojo filterPojo2 = makeDefaultPojo().setDescription("numbatwo");
    assertFalse(filterPojo.equals(filterPojo2));
    assertFalse(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentSuspendedValue() {
    FilterPojo filterPojo2 = makeDefaultPojo().setSuspended(false);
    assertFalse(filterPojo.equals(filterPojo2));
    assertFalse(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentPriority() {
    FilterPojo filterPojo2 = makeDefaultPojo().setPriority((byte) 5);
    assertFalse(filterPojo.equals(filterPojo2));
    assertFalse(filterPojo2.equals(filterPojo));
  }

  @Test
  public void equalsWithDifferentObject() {
    assertFalse(filterPojo.equals(new Object()));
  }

  @Test
  public void equalsWithNull() {
    assertFalse(filterPojo.equals(null));
  }

  @Test
  public void hashcode() {
    FilterPojo filterPojo2 = makeDefaultPojo();
    assertThat(filterPojo.hashCode(), is(filterPojo2.hashCode()));
    filterPojo2.setName("numbatwo");
    assertThat(filterPojo.hashCode(), not(filterPojo2.hashCode()));
  }

  @Test
  public void testToString() {
    filterPojo.setId("1234");

    ToStringVerifier.forClass(FilterPojo.class).withClassName(NameStyle.SIMPLE_NAME).verify();
  }

  private FilterPojo makeDefaultPojo() {
    return new FilterPojo()
        .setSiteId(SITE_ID)
        .setFilter(FILTER)
        .setName(NAME)
        .setDescription(DESCRIPTION)
        .setSuspended(true)
        .setPriority(PRIORITY);
  }
}
