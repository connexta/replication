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

import java.time.Instant;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DataInfoPojoTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final Instant LAST_MODIFIED = Instant.now();
  private static final long SIZE = 1234L;

  private static final DataInfoPojo<?> POJO =
      (DataInfoPojo<?>)
          Mockito.mock(
                  DataInfoPojo.class,
                  Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
              .setLastModified(DataInfoPojoTest.LAST_MODIFIED)
              .setSize(DataInfoPojoTest.SIZE)
              .setVersion(DataInfoPojoTest.VERSION)
              .setId(DataInfoPojoTest.ID);

  @Test
  public void testSetAndGetId() throws Exception {
    final DataInfoPojo<?> pojo =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setId(DataInfoPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(DataInfoPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final DataInfoPojo<?> pojo =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setVersion(DataInfoPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(DataInfoPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetLastModified() throws Exception {
    final DataInfoPojo<?> pojo =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setLastModified(DataInfoPojoTest.LAST_MODIFIED);

    Assert.assertThat(pojo.getLastModified(), Matchers.equalTo(DataInfoPojoTest.LAST_MODIFIED));
  }

  @Test
  public void testSetAndGetSize() throws Exception {
    final DataInfoPojo<?> pojo =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setSize(DataInfoPojoTest.SIZE);

    Assert.assertThat(pojo.getSize(), Matchers.equalTo(DataInfoPojoTest.SIZE));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final DataInfoPojo<?> pojo2 =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setLastModified(DataInfoPojoTest.LAST_MODIFIED)
                .setSize(DataInfoPojoTest.SIZE)
                .setVersion(DataInfoPojoTest.VERSION)
                .setId(DataInfoPojoTest.ID);

    Assert.assertThat(DataInfoPojoTest.POJO.hashCode0(), Matchers.equalTo(pojo2.hashCode0()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final DataInfoPojo<?> pojo2 =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setLastModified(DataInfoPojoTest.LAST_MODIFIED)
                .setSize(DataInfoPojoTest.SIZE)
                .setVersion(DataInfoPojoTest.VERSION)
                .setId(DataInfoPojoTest.ID + "2");

    Assert.assertThat(
        DataInfoPojoTest.POJO.hashCode0(), Matchers.not(Matchers.equalTo(pojo2.hashCode0())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final DataInfoPojo<?> pojo2 =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setLastModified(DataInfoPojoTest.LAST_MODIFIED)
                .setSize(DataInfoPojoTest.SIZE)
                .setVersion(DataInfoPojoTest.VERSION)
                .setId(DataInfoPojoTest.ID);

    Assert.assertThat(DataInfoPojoTest.POJO.equals0(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(DataInfoPojoTest.POJO.equals0(DataInfoPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(DataInfoPojoTest.POJO.equals0(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotADataInfoPojo() throws Exception {
    Assert.assertThat(DataInfoPojoTest.POJO.equals0("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final DataInfoPojo<?> pojo2 =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setLastModified(DataInfoPojoTest.LAST_MODIFIED)
                .setSize(DataInfoPojoTest.SIZE)
                .setVersion(DataInfoPojoTest.VERSION)
                .setId(DataInfoPojoTest.ID + "2");

    Assert.assertThat(DataInfoPojoTest.POJO.equals0(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final DataInfoPojo<?> pojo2 =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setLastModified(DataInfoPojoTest.LAST_MODIFIED)
                .setSize(DataInfoPojoTest.SIZE)
                .setVersion(DataInfoPojoTest.VERSION + 2)
                .setId(DataInfoPojoTest.ID);

    Assert.assertThat(DataInfoPojoTest.POJO.equals0(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenLastModifiedIsDifferent() throws Exception {
    final DataInfoPojo<?> pojo2 =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setLastModified(DataInfoPojoTest.LAST_MODIFIED.plusSeconds(2))
                .setSize(DataInfoPojoTest.SIZE)
                .setVersion(DataInfoPojoTest.VERSION)
                .setId(DataInfoPojoTest.ID);

    Assert.assertThat(DataInfoPojoTest.POJO.equals0(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSizeIsDifferent() throws Exception {
    final DataInfoPojo<?> pojo2 =
        (DataInfoPojo<?>)
            Mockito.mock(
                    DataInfoPojo.class,
                    Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS))
                .setLastModified(DataInfoPojoTest.LAST_MODIFIED)
                .setSize(DataInfoPojoTest.SIZE + 2L)
                .setVersion(DataInfoPojoTest.VERSION)
                .setId(DataInfoPojoTest.ID);

    Assert.assertThat(DataInfoPojoTest.POJO.equals0(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
