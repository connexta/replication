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

public class MetadataInfoPojoTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final Instant LAST_MODIFIED = Instant.now();
  private static final long SIZE = 1234L;
  private static final String TYPE = "ddms";
  private static final String TYPE2 = "metacard";

  private static final MetadataInfoPojo<?> POJO =
      new MetadataInfoPojo<>()
          .setVersion(MetadataInfoPojoTest.VERSION)
          .setId(MetadataInfoPojoTest.ID)
          .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED)
          .setSize(MetadataInfoPojoTest.SIZE)
          .setType(MetadataInfoPojoTest.TYPE);

  @Test
  public void testSetAndGetId() throws Exception {
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>().setId(MetadataInfoPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(MetadataInfoPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>().setVersion(MetadataInfoPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(MetadataInfoPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetLastModified() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>().setLastModified(MetadataInfoPojoTest.LAST_MODIFIED);

    Assert.assertThat(pojo.getLastModified(), Matchers.equalTo(MetadataInfoPojoTest.LAST_MODIFIED));
  }

  @Test
  public void testSetAndGetSize() throws Exception {
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>().setSize(MetadataInfoPojoTest.SIZE);

    Assert.assertThat(pojo.getSize(), Matchers.equalTo(MetadataInfoPojoTest.SIZE));
  }

  @Test
  public void testSetAndGetType() throws Exception {
    final MetadataInfoPojo<?> pojo = new MetadataInfoPojo<>().setType(MetadataInfoPojoTest.TYPE);

    Assert.assertThat(pojo.getType(), Matchers.equalTo(MetadataInfoPojoTest.TYPE));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final MetadataInfoPojo pojo2 =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoTest.VERSION)
            .setId(MetadataInfoPojoTest.ID)
            .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoTest.SIZE)
            .setType(MetadataInfoPojoTest.TYPE);

    Assert.assertThat(MetadataInfoPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final MetadataInfoPojo pojo2 =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoTest.VERSION)
            .setId(MetadataInfoPojoTest.ID + "2")
            .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoTest.SIZE)
            .setType(MetadataInfoPojoTest.TYPE);

    Assert.assertThat(
        MetadataInfoPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final MetadataInfoPojo pojo2 =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoTest.VERSION)
            .setId(MetadataInfoPojoTest.ID)
            .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoTest.SIZE)
            .setType(MetadataInfoPojoTest.TYPE);

    Assert.assertThat(MetadataInfoPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(
        MetadataInfoPojoTest.POJO.equals(MetadataInfoPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(MetadataInfoPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotAMetadataInfoPojo() throws Exception {
    Assert.assertThat(MetadataInfoPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final MetadataInfoPojo<?> pojo2 =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoTest.VERSION)
            .setId(MetadataInfoPojoTest.ID + "2")
            .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoTest.SIZE)
            .setType(MetadataInfoPojoTest.TYPE);

    Assert.assertThat(
        MetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final MetadataInfoPojo<?> pojo2 =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoTest.VERSION + 2)
            .setId(MetadataInfoPojoTest.ID)
            .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoTest.SIZE)
            .setType(MetadataInfoPojoTest.TYPE);

    Assert.assertThat(
        MetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenLastModifiedIsDifferent() throws Exception {
    final MetadataInfoPojo<?> pojo2 =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoTest.VERSION)
            .setId(MetadataInfoPojoTest.ID)
            .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED.plusSeconds(2))
            .setSize(MetadataInfoPojoTest.SIZE)
            .setType(MetadataInfoPojoTest.TYPE);

    Assert.assertThat(
        MetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSizeIsDifferent() throws Exception {
    final MetadataInfoPojo<?> pojo2 =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoTest.VERSION)
            .setId(MetadataInfoPojoTest.ID)
            .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoTest.SIZE + 2L)
            .setType(MetadataInfoPojoTest.TYPE);

    Assert.assertThat(
        MetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenTypeIsDifferent() throws Exception {
    final MetadataInfoPojo<?> pojo2 =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoTest.VERSION)
            .setId(MetadataInfoPojoTest.ID)
            .setLastModified(MetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoTest.SIZE)
            .setType(MetadataInfoPojoTest.TYPE2);

    Assert.assertThat(
        MetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
