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
import java.util.Map;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class DdfMetadataInfoPojoTest {
  private static final int VERSION = 1;
  private static final int DDF_VERSION = 11;
  private static final String ID = "1234";
  private static final Instant LAST_MODIFIED = Instant.now();
  private static final long SIZE = 1234L;
  private static final String TYPE = "ddms";
  private static final String TYPE2 = "metacard";
  private static final String DATA_CLASS = Map.class.getName();
  private static final Class<?> DATA_CLAZZ = Map.class;
  private static final Map<String, String> DATA_OBJ = Map.of("k", "v", "k2", "v2");
  private static final String DATA;

  static {
    try {
      DATA = new JSONObject().put("k", "v").put("k2", "v2").toString();
    } catch (JSONException e) {
      throw new AssertionError(e);
    }
  }

  private static final DdfMetadataInfoPojo POJO =
      new DdfMetadataInfoPojo()
          .setVersion(DdfMetadataInfoPojoTest.VERSION)
          .setId(DdfMetadataInfoPojoTest.ID)
          .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
          .setSize(DdfMetadataInfoPojoTest.SIZE)
          .setType(DdfMetadataInfoPojoTest.TYPE)
          .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
          .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
          .setData(DdfMetadataInfoPojoTest.DATA);

  @Test
  public void testSetAndGetId() throws Exception {
    final DdfMetadataInfoPojo pojo = new DdfMetadataInfoPojo().setId(DdfMetadataInfoPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(DdfMetadataInfoPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo().setVersion(DdfMetadataInfoPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(DdfMetadataInfoPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetLastModified() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo().setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED);

    Assert.assertThat(
        pojo.getLastModified(), Matchers.equalTo(DdfMetadataInfoPojoTest.LAST_MODIFIED));
  }

  @Test
  public void testSetAndGetSize() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo().setSize(DdfMetadataInfoPojoTest.SIZE);

    Assert.assertThat(pojo.getSize(), Matchers.equalTo(DdfMetadataInfoPojoTest.SIZE));
  }

  @Test
  public void testSetAndGetType() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo().setType(DdfMetadataInfoPojoTest.TYPE);

    Assert.assertThat(pojo.getType(), Matchers.equalTo(DdfMetadataInfoPojoTest.TYPE));
  }

  @Test
  public void testSetAndGetDdfVersion() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo().setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION);

    Assert.assertThat(pojo.getDdfVersion(), Matchers.equalTo(DdfMetadataInfoPojoTest.DDF_VERSION));
  }

  @Test
  public void testSetAndGetDataClass() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo().setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS);

    Assert.assertThat(pojo.getDataClass(), Matchers.equalTo(DdfMetadataInfoPojoTest.DATA_CLASS));
  }

  @Test
  public void testSetAndGetDataClassWithJavaClass() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo().setDataClass(DdfMetadataInfoPojoTest.DATA_CLAZZ);

    Assert.assertThat(
        pojo.getJavaDataClass(), Matchers.equalTo(DdfMetadataInfoPojoTest.DATA_CLAZZ));
  }

  @Test
  public void testSetAndGetData() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo().setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(pojo.getData(), Matchers.equalTo(DdfMetadataInfoPojoTest.DATA));
  }

  @Test
  public void testSetAndGetDataClassWithJavaObject() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA_OBJ);

    Assert.assertThat(pojo.getJavaData(), Matchers.equalTo(DdfMetadataInfoPojoTest.DATA_OBJ));
  }

  @Test
  public void testSetAndGetDataClassWithJavaObjectWhenDataClassIsNull() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setDataClass((String) null)
            .setData(DdfMetadataInfoPojoTest.DATA_OBJ);

    Assert.assertThat(pojo.getJavaData(), Matchers.nullValue());
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(DdfMetadataInfoPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID + "2")
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(DdfMetadataInfoPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(DdfMetadataInfoPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotADdfMetadataInfoPojo() throws Exception {
    Assert.assertThat(DdfMetadataInfoPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID + "2")
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION + 2)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenLastModifiedIsDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED.plusSeconds(2))
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSizeIsDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE + 2L)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenTypeIsDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE2)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDdfVersionIsDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION + 2)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDataClassIsDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS + "2")
            .setData(DdfMetadataInfoPojoTest.DATA);

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenDataIsDifferent() throws Exception {
    final DdfMetadataInfoPojo pojo2 =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoTest.VERSION)
            .setId(DdfMetadataInfoPojoTest.ID)
            .setLastModified(DdfMetadataInfoPojoTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoTest.SIZE)
            .setType(DdfMetadataInfoPojoTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoTest.DATA + "2");

    Assert.assertThat(
        DdfMetadataInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
