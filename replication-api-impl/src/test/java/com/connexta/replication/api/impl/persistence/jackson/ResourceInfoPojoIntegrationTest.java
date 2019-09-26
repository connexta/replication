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
import com.connexta.replication.api.impl.persistence.pojo.ResourceInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownResourceInfoPojo;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import java.time.Instant;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class ResourceInfoPojoIntegrationTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final Instant LAST_MODIFIED = Instant.now();
  private static final long SIZE = 1234L;
  private static final String URI = "https://some.uri.com";

  private static final ResourceInfoPojo POJO =
      new ResourceInfoPojo()
          .setVersion(ResourceInfoPojoIntegrationTest.VERSION)
          .setId(ResourceInfoPojoIntegrationTest.ID)
          .setLastModified(ResourceInfoPojoIntegrationTest.LAST_MODIFIED)
          .setSize(ResourceInfoPojoIntegrationTest.SIZE)
          .setUri(ResourceInfoPojoIntegrationTest.URI);

  @Test
  public void testPojoJsonPersistence() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final String json = JsonUtils.write(ResourceInfoPojoIntegrationTest.POJO);
    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(ResourceInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenVersionIsMissing() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo()
            .setVersion(0)
            .setId(ResourceInfoPojoIntegrationTest.ID)
            .setLastModified(ResourceInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoIntegrationTest.SIZE)
            .setUri(ResourceInfoPojoIntegrationTest.URI);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsNull() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoIntegrationTest.VERSION)
            .setId(null)
            .setLastModified(ResourceInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoIntegrationTest.SIZE)
            .setUri(ResourceInfoPojoIntegrationTest.URI);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final String json = JsonUtils.write(pojo);
    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsMissing() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoIntegrationTest.VERSION)
            .setId(null)
            .setLastModified(ResourceInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoIntegrationTest.SIZE)
            .setUri(ResourceInfoPojoIntegrationTest.URI);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenLastModifiedIsNull() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoIntegrationTest.VERSION)
            .setId(ResourceInfoPojoIntegrationTest.ID)
            .setLastModified(null)
            .setSize(ResourceInfoPojoIntegrationTest.SIZE)
            .setUri(ResourceInfoPojoIntegrationTest.URI);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final String json = JsonUtils.write(pojo);
    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenLastModifiedIsMissing() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoIntegrationTest.VERSION)
            .setId(ResourceInfoPojoIntegrationTest.ID)
            .setLastModified(null)
            .setSize(ResourceInfoPojoIntegrationTest.SIZE)
            .setUri(ResourceInfoPojoIntegrationTest.URI);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenSizeIsMissing() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoIntegrationTest.VERSION)
            .setId(ResourceInfoPojoIntegrationTest.ID)
            .setLastModified(ResourceInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(-1L)
            .setUri(ResourceInfoPojoIntegrationTest.URI);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenUriIsNull() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoIntegrationTest.VERSION)
            .setId(ResourceInfoPojoIntegrationTest.ID)
            .setLastModified(ResourceInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoIntegrationTest.SIZE)
            .setUri((String) null);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE);

    final String json = JsonUtils.write(pojo);
    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenUrlIsMissing() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoIntegrationTest.VERSION)
            .setId(ResourceInfoPojoIntegrationTest.ID)
            .setLastModified(ResourceInfoPojoIntegrationTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoIntegrationTest.SIZE)
            .setUri((String) null);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE);

    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWithExtraJsonProperties() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "resource")
            .put("extra", "EXTRA")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(ResourceInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownResourceInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsMissing() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(ResourceInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownResourceInfoPojo.class));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsUnknown() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "new_resource")
            .put("extra", "EXTRA")
            .put("id", ResourceInfoPojoIntegrationTest.ID)
            .put("version", ResourceInfoPojoIntegrationTest.VERSION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    ResourceInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("size", ResourceInfoPojoIntegrationTest.SIZE)
            .put("uri", ResourceInfoPojoIntegrationTest.URI);

    final ResourceInfoPojo pojo2 = JsonUtils.read(ResourceInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(ResourceInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownResourceInfoPojo.class));
  }
}
