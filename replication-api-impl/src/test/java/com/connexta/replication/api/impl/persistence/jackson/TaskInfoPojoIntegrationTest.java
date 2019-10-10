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

import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.impl.jackson.JsonUtils;
import com.connexta.replication.api.impl.persistence.pojo.DdfMetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.ResourceInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.TaskInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownTaskInfoPojo;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class TaskInfoPojoIntegrationTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final byte PRIORITY = 3;
  private static final String INTEL_ID = "intel.id";
  private static final String OPERATION = OperationType.HARVEST.name();
  private static final Instant LAST_MODIFIED = Instant.now();

  private static final int RESOURCE_VERSION = 2;
  private static final String RESOURCE_ID = "12345";
  private static final Instant RESOURCE_LAST_MODIFIED = Instant.now().minusSeconds(10L);
  private static final long RESOURCE_SIZE = 1234L;
  private static final String RESOURCE_URI = "https://some.uri.com";

  private static final int METADATA_VERSION = 3;
  private static final String METADATA_ID = "123456";
  private static final Instant METADATA_LAST_MODIFIED = Instant.now().minusSeconds(30L);
  private static final long METADATA_SIZE = 12345L;
  private static final String METADATA_TYPE = "ddms";

  private static final int DDF_METADATA_VERSION = 4;
  private static final int DDF_METADATA_DDF_VERSION = 11;
  private static final String DDF_METADATA_ID = "1234444";
  private static final Instant DDF_METADATA_LAST_MODIFIED = Instant.now();
  private static final long DDF_METADATA_SIZE = 1234L;
  private static final String DDF_METADATA_TYPE = "ddms";
  private static final String DDF_METADATA_DATA_CLASS = Map.class.getName();
  private static final String DDF_METADATA_DATA;

  private static final JSONObject RESOURCE_JSON;
  private static final JSONObject METADATA_JSON;
  private static final JSONObject DDF_METADATA_JSON;

  static {
    try {
      DDF_METADATA_DATA = new JSONObject().put("k", "v").put("k2", "v2").toString();
      RESOURCE_JSON =
          new JSONObject()
              .put("clazz", "resource")
              .put("id", TaskInfoPojoIntegrationTest.RESOURCE_ID)
              .put("version", TaskInfoPojoIntegrationTest.RESOURCE_VERSION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskInfoPojoIntegrationTest.RESOURCE_LAST_MODIFIED.getEpochSecond(),
                      TaskInfoPojoIntegrationTest.RESOURCE_LAST_MODIFIED.getNano()))
              .put("size", TaskInfoPojoIntegrationTest.RESOURCE_SIZE)
              .put("uri", TaskInfoPojoIntegrationTest.RESOURCE_URI);
      METADATA_JSON =
          new JSONObject()
              .put("clazz", "metadata")
              .put("id", TaskInfoPojoIntegrationTest.METADATA_ID)
              .put("version", TaskInfoPojoIntegrationTest.METADATA_VERSION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskInfoPojoIntegrationTest.METADATA_LAST_MODIFIED.getEpochSecond(),
                      TaskInfoPojoIntegrationTest.METADATA_LAST_MODIFIED.getNano()))
              .put("size", TaskInfoPojoIntegrationTest.METADATA_SIZE)
              .put("type", TaskInfoPojoIntegrationTest.METADATA_TYPE);
      DDF_METADATA_JSON =
          new JSONObject()
              .put("clazz", "ddf_metadata")
              .put("id", TaskInfoPojoIntegrationTest.DDF_METADATA_ID)
              .put("version", TaskInfoPojoIntegrationTest.DDF_METADATA_VERSION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskInfoPojoIntegrationTest.DDF_METADATA_LAST_MODIFIED.getEpochSecond(),
                      TaskInfoPojoIntegrationTest.DDF_METADATA_LAST_MODIFIED.getNano()))
              .put("size", TaskInfoPojoIntegrationTest.DDF_METADATA_SIZE)
              .put("type", TaskInfoPojoIntegrationTest.DDF_METADATA_TYPE)
              .put("ddf_version", TaskInfoPojoIntegrationTest.DDF_METADATA_DDF_VERSION)
              .put("data_class", TaskInfoPojoIntegrationTest.DDF_METADATA_DATA_CLASS)
              .put("data", TaskInfoPojoIntegrationTest.DDF_METADATA_DATA);
    } catch (JSONException e) {
      throw new AssertionError(e);
    }
  }

  private static final ResourceInfoPojo RESOURCE =
      new ResourceInfoPojo()
          .setVersion(TaskInfoPojoIntegrationTest.RESOURCE_VERSION)
          .setId(TaskInfoPojoIntegrationTest.RESOURCE_ID)
          .setLastModified(TaskInfoPojoIntegrationTest.RESOURCE_LAST_MODIFIED)
          .setSize(TaskInfoPojoIntegrationTest.RESOURCE_SIZE)
          .setUri(TaskInfoPojoIntegrationTest.RESOURCE_URI);

  private static final MetadataInfoPojo<?> METADATA =
      new MetadataInfoPojo<>()
          .setVersion(TaskInfoPojoIntegrationTest.METADATA_VERSION)
          .setId(TaskInfoPojoIntegrationTest.METADATA_ID)
          .setLastModified(TaskInfoPojoIntegrationTest.METADATA_LAST_MODIFIED)
          .setSize(TaskInfoPojoIntegrationTest.METADATA_SIZE)
          .setType(TaskInfoPojoIntegrationTest.METADATA_TYPE);

  private static final DdfMetadataInfoPojo DDF_METADATA =
      new DdfMetadataInfoPojo()
          .setVersion(TaskInfoPojoIntegrationTest.DDF_METADATA_VERSION)
          .setId(TaskInfoPojoIntegrationTest.DDF_METADATA_ID)
          .setLastModified(TaskInfoPojoIntegrationTest.DDF_METADATA_LAST_MODIFIED)
          .setSize(TaskInfoPojoIntegrationTest.DDF_METADATA_SIZE)
          .setType(TaskInfoPojoIntegrationTest.DDF_METADATA_TYPE)
          .setDdfVersion(TaskInfoPojoIntegrationTest.DDF_METADATA_DDF_VERSION)
          .setDataClass(TaskInfoPojoIntegrationTest.DDF_METADATA_DATA_CLASS)
          .setData(TaskInfoPojoIntegrationTest.DDF_METADATA_DATA);

  private static final TaskInfoPojo POJO =
      new TaskInfoPojo()
          .setVersion(TaskInfoPojoIntegrationTest.VERSION)
          .setId(TaskInfoPojoIntegrationTest.ID)
          .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
          .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
          .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
          .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
          .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
          .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
          .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);

  @Test
  public void testPojoJsonPersistence() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final String json = JsonUtils.write(TaskInfoPojoIntegrationTest.POJO);
    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(TaskInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenVersionIsMissing() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(0)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsNull() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(null)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final String json = JsonUtils.write(pojo);
    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsMissing() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(null)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenOperationIsNull() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation((String) null)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final String json = JsonUtils.write(pojo);
    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenOperationIsMissing() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation((String) null)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenLastModifiedIsNull() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(null)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final String json = JsonUtils.write(pojo);
    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenLastModifiedIsMissing() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(null)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenResourceIsNull() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(null)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final String json = JsonUtils.write(pojo);
    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenResourceIsMissing() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(null)
            .addMetadata(TaskInfoPojoIntegrationTest.METADATA)
            .addMetadata(TaskInfoPojoIntegrationTest.DDF_METADATA);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenMetatadasIsNull() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .setMetadatas((List<MetadataInfoPojo<?>>) null);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenMetadatasIsMissing() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON);

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenMetatadasIsEmptyInPojo() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE)
            .setMetadatas(Collections.emptyList());
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenMetadatasIsEmptyInJson() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoIntegrationTest.VERSION)
            .setId(TaskInfoPojoIntegrationTest.ID)
            .setPriority(TaskInfoPojoIntegrationTest.PRIORITY)
            .setIntelId(TaskInfoPojoIntegrationTest.INTEL_ID)
            .setOperation(TaskInfoPojoIntegrationTest.OPERATION)
            .setLastModified(TaskInfoPojoIntegrationTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoIntegrationTest.RESOURCE);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put("metadata", new JSONArray());

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWithExtraJsonProperties() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("extra", "EXTRA")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(TaskInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskInfoPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsMissing() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(TaskInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownTaskInfoPojo.class));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsUnknown() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "new_task")
            .put("extra", "EXTRA")
            .put("id", TaskInfoPojoIntegrationTest.ID)
            .put("version", TaskInfoPojoIntegrationTest.VERSION)
            .put("priority", TaskInfoPojoIntegrationTest.PRIORITY)
            .put("intel_id", TaskInfoPojoIntegrationTest.INTEL_ID)
            .put("operation", TaskInfoPojoIntegrationTest.OPERATION)
            .put(
                "last_modified",
                DecimalUtils.toBigDecimal(
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getEpochSecond(),
                    TaskInfoPojoIntegrationTest.LAST_MODIFIED.getNano()))
            .put("resource", TaskInfoPojoIntegrationTest.RESOURCE_JSON)
            .put(
                "metadata",
                new JSONArray()
                    .put(0, TaskInfoPojoIntegrationTest.METADATA_JSON)
                    .put(1, TaskInfoPojoIntegrationTest.DDF_METADATA_JSON));

    final TaskInfoPojo pojo2 = JsonUtils.read(TaskInfoPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(TaskInfoPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownTaskInfoPojo.class));
  }
}
