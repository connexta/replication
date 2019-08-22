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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.jparams.verifier.tostring.NameStyle;
import com.jparams.verifier.tostring.ToStringVerifier;
import java.time.Instant;
import org.junit.Before;
import org.junit.Test;

public class FilterIndexPojoTest {

  private static final Instant MODIFIED_SINCE = Instant.now();

  private static final String ID = "id";

  private static final FilterIndexPojo POJO =
      new FilterIndexPojo().setId(ID).setModifiedSince(MODIFIED_SINCE);

  private FilterIndexPojo pojo;

  @Before
  public void setup() {
    pojo = new FilterIndexPojo();
  }

  @Test
  public void testEqualsReflexive() {
    assertThat(POJO.equals(POJO), is(true));
  }

  @Test
  public void testEqualsSymmetric() {
    final FilterIndexPojo pojo2 = new FilterIndexPojo().setId(ID).setModifiedSince(MODIFIED_SINCE);
    assertThat(POJO.equals(pojo2), is(true));
    assertThat(pojo2.equals(POJO), is(true));
  }

  @Test
  public void testEqualsTransitive() {
    final FilterIndexPojo pojo2 = new FilterIndexPojo().setId(ID).setModifiedSince(MODIFIED_SINCE);
    final FilterIndexPojo pojo3 = new FilterIndexPojo().setId(ID).setModifiedSince(MODIFIED_SINCE);
    assertThat(POJO.equals(pojo2), is(true));
    assertThat(pojo2.equals(pojo3), is(true));
    assertThat(POJO.equals(pojo3), is(true));
  }

  @Test
  public void testEqualsConsistent() {
    final FilterIndexPojo pojo2 = new FilterIndexPojo().setId(ID).setModifiedSince(MODIFIED_SINCE);
    assertThat(POJO.equals(pojo2), is(true));
    assertThat(POJO.equals(pojo2), is(true));
  }

  @Test
  public void testEqualsNull() {
    assertThat(POJO.equals(null), is(false));
  }

  @Test
  public void testEqualsDifferentObject() {
    assertThat(POJO.equals(new Object()), is(false));
  }

  @Test
  public void testHashIsConsistent() {
    final int hash = POJO.hashCode();
    assertThat(POJO.hashCode(), is(hash));
  }

  @Test
  public void testEqualsWithDifferentModifiedSince() {
    final FilterIndexPojo pojo2 =
        new FilterIndexPojo().setId(ID).setModifiedSince(Instant.ofEpochSecond(500));
    assertThat(POJO.equals(pojo2), is(false));
  }

  @Test
  public void testSetAndGetId() {
    pojo.setId("anotherId");
    assertThat(pojo.getId(), is("anotherId"));
  }

  @Test
  public void testSetAndGetVersion() {
    pojo.setVersion(FilterIndexPojo.CURRENT_VERSION + 1);
    assertThat(pojo.getVersion(), is(FilterIndexPojo.CURRENT_VERSION + 1));
  }

  @Test
  public void testSetAndGetModifiedSince() {
    final Instant modifiedSince = Instant.ofEpochSecond(100);
    pojo.setModifiedSince(modifiedSince);
    assertThat(pojo.getModifiedSince(), is(modifiedSince));
  }

  @Test
  public void testToString() {
    ToStringVerifier.forClass(FilterIndexPojo.class)
        .withClassName(NameStyle.SIMPLE_NAME)
        .withPrefabValue(Instant.class, Instant.now())
        .verify();
  }
}
