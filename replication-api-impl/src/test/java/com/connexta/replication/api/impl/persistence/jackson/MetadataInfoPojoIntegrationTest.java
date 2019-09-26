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
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownMetadataInfoPojo;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import java.time.Instant;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class MetadataInfoPojoIntegrationTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final Instant LAST_MODIFIED = Instant.now();
  private static final long SIZE = 1234L;
  private static final String TYPE = "ddms";

  private static final MetadataInfoPojo<?> POJO =
      new MetadataInfoPojo<>()
          .setVersion(MetadataInfoPojoIntegrationTest.VERSION)
          .setId(MetadataInfoPojoIntegrationTest.ID)
          .setLastModified(MetadataInfoPojoIntegrationTest.LAST_MODIFIED)
          .setSize(MetadataInfoPojoIntegrationTest.SIZE)
          .setType(MetadataInfoPojoIntegrationTest.TYPE);

  @Test
  public void testPojoJsonPersistence() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final String json = JsonUtils.write(MetadataInfoPojoIntegrationTest.POJO);
    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(MetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenVersionIsMissing() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(0)
            .setId(MetadataInfoPojoIntegrationTest.ID)
            .setLastModified(MetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoIntegrationTest.SIZE)
            .setType(MetadataInfoPojoIntegrationTest.TYPE);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsNull() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoIntegrationTest.VERSION)
            .setId(null)
            .setLastModified(MetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoIntegrationTest.SIZE)
            .setType(MetadataInfoPojoIntegrationTest.TYPE);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final String json = JsonUtils.write(pojo);
    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsMissing() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoIntegrationTest.VERSION)
            .setId(null)
            .setLastModified(MetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoIntegrationTest.SIZE)
            .setType(MetadataInfoPojoIntegrationTest.TYPE);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenLastModifiedIsNull() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoIntegrationTest.VERSION)
            .setId(MetadataInfoPojoIntegrationTest.ID)
            .setLastModified(null)
            .setSize(MetadataInfoPojoIntegrationTest.SIZE)
            .setType(MetadataInfoPojoIntegrationTest.TYPE);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final String json = JsonUtils.write(pojo);
    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenLastModifiedIsMissing() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoIntegrationTest.VERSION)
            .setId(MetadataInfoPojoIntegrationTest.ID)
            .setLastModified(null)
            .setSize(MetadataInfoPojoIntegrationTest.SIZE)
            .setType(MetadataInfoPojoIntegrationTest.TYPE);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenSizeIsMissing() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoIntegrationTest.VERSION)
            .setId(MetadataInfoPojoIntegrationTest.ID)
            .setLastModified(MetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(-1L)
            .setType(MetadataInfoPojoIntegrationTest.TYPE);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenTypeIsNull() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoIntegrationTest.VERSION)
            .setId(MetadataInfoPojoIntegrationTest.ID)
            .setLastModified(MetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoIntegrationTest.SIZE)
            .setType((String) null);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE);

    final String json = JsonUtils.write(pojo);
    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenTypeIsMissing() throws Exception {
    final MetadataInfoPojo<?> pojo =
        new MetadataInfoPojo<>()
            .setVersion(MetadataInfoPojoIntegrationTest.VERSION)
            .setId(MetadataInfoPojoIntegrationTest.ID)
            .setLastModified(MetadataInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(MetadataInfoPojoIntegrationTest.SIZE)
            .setType((String) null);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE);

    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWithExtraJsonProperties() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "metadata")
            .put("extra", "EXTRA")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(MetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownMetadataInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsMissing() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(MetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownMetadataInfoPojo.class));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsUnknown() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "new_metadata")
            .put("extra", "EXTRA")
            .put("id", MetadataInfoPojoIntegrationTest.ID)
            .put("version", MetadataInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    MetadataInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", MetadataInfoPojoIntegrationTest.SIZE)
            .put("type", MetadataInfoPojoIntegrationTest.TYPE);

    final MetadataInfoPojo<?> pojo2 = JsonUtils.read(MetadataInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(MetadataInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownMetadataInfoPojo.class));
  }
}
