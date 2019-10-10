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
package com.connexta.replication.api.impl.persistence.jackson;

import com.connexta.replication.api.impl.jackson.JsonUtils;
import com.connexta.replication.api.impl.persistence.pojo.DdfMetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownDdfMetadataInfoPojo;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import java.time.Instant;
import java.util.Map;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class DdfMetadataInfoPojoIntegrationTest {
  private static final int VERSION = 1;
  private static final int DDF_VERSION = 11;
  private static final String ID = "1234";
  private static final Instant LAST_MODIFIED = Instant.now();
  private static final long SIZE = 1234L;
  private static final String TYPE = "ddms";
  private static final String DATA_CLASS = Map.class.getName();
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
          .setVersion(DdfMetadataInfoPojoIntegrationTest.VERSION)
          .setId(DdfMetadataInfoPojoIntegrationTest.ID)
          .setLastModified(DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED)
          .setSize(DdfMetadataInfoPojoIntegrationTest.SIZE)
          .setType(DdfMetadataInfoPojoIntegrationTest.TYPE)
          .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
          .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
          .setData(DdfMetadataInfoPojoIntegrationTest.DATA);

  @Test
  public void testPojoJsonPersistence() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final String json = JsonUtils.write(DdfMetadataInfoPojoIntegrationTest.POJO);
    final DdfMetadataInfoPojo pojo2 = JsonUtils.read(DdfMetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(DdfMetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceViaBaseClass() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final String json = JsonUtils.write(DdfMetadataInfoPojoIntegrationTest.POJO);
    final MetadataInfoPojo pojo2 = JsonUtils.read(MetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(DdfMetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(DdfMetadataInfoPojo.class));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenVersionIsMissing() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setVersion(0)
            .setId(DdfMetadataInfoPojoIntegrationTest.ID)
            .setLastModified(DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoIntegrationTest.SIZE)
            .setType(DdfMetadataInfoPojoIntegrationTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoIntegrationTest.DATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final DdfMetadataInfoPojo pojo2 =
        JsonUtils.read(DdfMetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsNull() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoIntegrationTest.VERSION)
            .setId(null)
            .setLastModified(DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoIntegrationTest.SIZE)
            .setType(DdfMetadataInfoPojoIntegrationTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoIntegrationTest.DATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final String json = JsonUtils.write(pojo);
    final DdfMetadataInfoPojo pojo2 = JsonUtils.read(DdfMetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsMissing() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoIntegrationTest.VERSION)
            .setId(null)
            .setLastModified(DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoIntegrationTest.SIZE)
            .setType(DdfMetadataInfoPojoIntegrationTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoIntegrationTest.DATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final DdfMetadataInfoPojo pojo2 =
        JsonUtils.read(DdfMetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenLastModifiedIsNull() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoIntegrationTest.VERSION)
            .setId(DdfMetadataInfoPojoIntegrationTest.ID)
            .setLastModified(null)
            .setSize(DdfMetadataInfoPojoIntegrationTest.SIZE)
            .setType(DdfMetadataInfoPojoIntegrationTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoIntegrationTest.DATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final String json = JsonUtils.write(pojo);
    final DdfMetadataInfoPojo pojo2 = JsonUtils.read(DdfMetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenLastModifiedIsMissing() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoIntegrationTest.VERSION)
            .setId(DdfMetadataInfoPojoIntegrationTest.ID)
            .setLastModified(null)
            .setSize(DdfMetadataInfoPojoIntegrationTest.SIZE)
            .setType(DdfMetadataInfoPojoIntegrationTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoIntegrationTest.DATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final DdfMetadataInfoPojo pojo2 =
        JsonUtils.read(DdfMetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenSizeIsMissing() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoIntegrationTest.VERSION)
            .setId(DdfMetadataInfoPojoIntegrationTest.ID)
            .setLastModified(DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(-1L)
            .setType(DdfMetadataInfoPojoIntegrationTest.TYPE)
            .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoIntegrationTest.DATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final DdfMetadataInfoPojo pojo2 =
        JsonUtils.read(DdfMetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenTypeIsNull() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoIntegrationTest.VERSION)
            .setId(DdfMetadataInfoPojoIntegrationTest.ID)
            .setLastModified(DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoIntegrationTest.SIZE)
            .setType((String) null)
            .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoIntegrationTest.DATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final String json = JsonUtils.write(pojo);
    final DdfMetadataInfoPojo pojo2 = JsonUtils.read(DdfMetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenTypeIsMissing() throws Exception {
    final DdfMetadataInfoPojo pojo =
        new DdfMetadataInfoPojo()
            .setVersion(DdfMetadataInfoPojoIntegrationTest.VERSION)
            .setId(DdfMetadataInfoPojoIntegrationTest.ID)
            .setLastModified(DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(DdfMetadataInfoPojoIntegrationTest.SIZE)
            .setType((String) null)
            .setDdfVersion(DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .setDataClass(DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .setData(DdfMetadataInfoPojoIntegrationTest.DATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final DdfMetadataInfoPojo pojo2 =
        JsonUtils.read(DdfMetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWithExtraJsonProperties() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "ddf_metadata")
            .put("extra", "EXTRA")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final DdfMetadataInfoPojo pojo2 =
        JsonUtils.read(DdfMetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(DdfMetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownDdfMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsMissing() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final DdfMetadataInfoPojo pojo2 =
        JsonUtils.read(DdfMetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(DdfMetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownDdfMetadataInfoPojo.class));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsUnknown() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "new_metadata")
            .put("extra", "EXTRA")
            .put("id", DdfMetadataInfoPojoIntegrationTest.ID)
            .put("version", DdfMetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    DdfMetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", DdfMetadataInfoPojoIntegrationTest.SIZE)
            .put("type", DdfMetadataInfoPojoIntegrationTest.TYPE)
            .put("ddf_version", DdfMetadataInfoPojoIntegrationTest.DDF_VERSION)
            .put("data_class", DdfMetadataInfoPojoIntegrationTest.DATA_CLASS)
            .put("data", DdfMetadataInfoPojoIntegrationTest.DATA);

    final DdfMetadataInfoPojo pojo2 =
        JsonUtils.read(DdfMetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(DdfMetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownDdfMetadataInfoPojo.class));
  }
}
