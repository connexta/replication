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
import com.connexta.replication.api.data.Task.State;
import com.connexta.replication.api.impl.jackson.JsonUtils;
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.ResourceInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.TaskInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.TaskPojo;
import com.connexta.replication.api.impl.persistence.pojo.unknown.UnknownTaskPojo;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import java.time.Duration;
import java.time.Instant;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class TaskPojoIntegrationTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final int ATTEMPTS = 5;
  private static final String STATE = State.PENDING.name();
  private static final Instant ORIGINAL_QUEUED_TIME = Instant.ofEpochMilli(10L);
  private static final Instant QUEUED_TIME = Instant.ofEpochMilli(10000L);
  private static final long FROZEN_TIME = 23453L;
  private static final Duration DURATION = Duration.ofMinutes(20L);
  private static final Duration PENDING_DURATION = Duration.ofMinutes(10L);
  private static final Duration ACTIVE_DURATION = Duration.ofMinutes(5L);

  private static final int TASK_INFO_VERSION = 1;
  private static final String TASK_INFO_ID = "1234";
  private static final byte TASK_INFO_PRIORITY = 3;
  private static final String TASK_INFO_INTEL_ID = "intel.id";
  private static final String TASK_INFO_OPERATION = OperationType.HARVEST.name();
  private static final Instant TASK_INFO_LAST_MODIFIED = Instant.now().minusSeconds(5L);

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

  private static final JSONObject RESOURCE_JSON;
  private static final JSONObject METADATA_JSON;
  private static final JSONObject TASK_INFO_JSON;

  static {
    try {
      RESOURCE_JSON =
          new JSONObject()
              .put("clazz", "resource")
              .put("id", TaskPojoIntegrationTest.RESOURCE_ID)
              .put("version", TaskPojoIntegrationTest.RESOURCE_VERSION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskPojoIntegrationTest.RESOURCE_LAST_MODIFIED.getEpochSecond(),
                      TaskPojoIntegrationTest.RESOURCE_LAST_MODIFIED.getNano()))
              .put("size", TaskPojoIntegrationTest.RESOURCE_SIZE)
              .put("uri", TaskPojoIntegrationTest.RESOURCE_URI);
      METADATA_JSON =
          new JSONObject()
              .put("clazz", "metadata")
              .put("id", TaskPojoIntegrationTest.METADATA_ID)
              .put("version", TaskPojoIntegrationTest.METADATA_VERSION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskPojoIntegrationTest.METADATA_LAST_MODIFIED.getEpochSecond(),
                      TaskPojoIntegrationTest.METADATA_LAST_MODIFIED.getNano()))
              .put("size", TaskPojoIntegrationTest.METADATA_SIZE)
              .put("type", TaskPojoIntegrationTest.METADATA_TYPE);
      TASK_INFO_JSON =
          new JSONObject()
              .put("clazz", "task")
              .put("id", TaskPojoIntegrationTest.ID)
              .put("version", TaskPojoIntegrationTest.VERSION)
              .put("priority", TaskPojoIntegrationTest.TASK_INFO_PRIORITY)
              .put("intel_id", TaskPojoIntegrationTest.TASK_INFO_INTEL_ID)
              .put("operation", TaskPojoIntegrationTest.TASK_INFO_OPERATION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskPojoIntegrationTest.TASK_INFO_LAST_MODIFIED.getEpochSecond(),
                      TaskPojoIntegrationTest.TASK_INFO_LAST_MODIFIED.getNano()))
              .put("resource", TaskPojoIntegrationTest.RESOURCE_JSON)
              .put("metadata", new JSONArray().put(0, TaskPojoIntegrationTest.METADATA_JSON));
    } catch (JSONException e) {
      throw new AssertionError(e);
    }
  }

  private static final ResourceInfoPojo RESOURCE =
      new ResourceInfoPojo()
          .setVersion(TaskPojoIntegrationTest.RESOURCE_VERSION)
          .setId(TaskPojoIntegrationTest.RESOURCE_ID)
          .setLastModified(TaskPojoIntegrationTest.RESOURCE_LAST_MODIFIED)
          .setSize(TaskPojoIntegrationTest.RESOURCE_SIZE)
          .setUri(TaskPojoIntegrationTest.RESOURCE_URI);

  private static final MetadataInfoPojo<?> METADATA =
      new MetadataInfoPojo<>()
          .setVersion(TaskPojoIntegrationTest.METADATA_VERSION)
          .setId(TaskPojoIntegrationTest.METADATA_ID)
          .setLastModified(TaskPojoIntegrationTest.METADATA_LAST_MODIFIED)
          .setSize(TaskPojoIntegrationTest.METADATA_SIZE)
          .setType(TaskPojoIntegrationTest.METADATA_TYPE);

  private static final TaskInfoPojo TASK_INFO =
      new TaskInfoPojo()
          .setVersion(TaskPojoIntegrationTest.TASK_INFO_VERSION)
          .setId(TaskPojoIntegrationTest.TASK_INFO_ID)
          .setPriority(TaskPojoIntegrationTest.TASK_INFO_PRIORITY)
          .setIntelId(TaskPojoIntegrationTest.TASK_INFO_INTEL_ID)
          .setOperation(TaskPojoIntegrationTest.TASK_INFO_OPERATION)
          .setLastModified(TaskPojoIntegrationTest.TASK_INFO_LAST_MODIFIED)
          .setResource(TaskPojoIntegrationTest.RESOURCE)
          .addMetadata(TaskPojoIntegrationTest.METADATA);

  private static final TaskPojo POJO =
      new TaskPojo()
          .setVersion(TaskPojoIntegrationTest.VERSION)
          .setId(TaskPojoIntegrationTest.ID)
          .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
          .setState(TaskPojoIntegrationTest.STATE)
          .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
          .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
          .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
          .setDuration(TaskPojoIntegrationTest.DURATION)
          .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
          .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
          .setInfo(TaskPojoIntegrationTest.TASK_INFO);

  @Test
  public void testPojoJsonPersistence() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final String json = JsonUtils.write(TaskPojoIntegrationTest.POJO);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(TaskPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenVersionIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(0)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsNull() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(null)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIdIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenTotalAttemptsIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(0)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("id", TaskPojoIntegrationTest.ID)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenOriginalQueuedTimeIsNull() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(null)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(null)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenQueuedTimeIsNull() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(null)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenQueuedTimeIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(null)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenFrozenTimeIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(0L)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenDurationIsNull() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(null)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenDurationIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(null)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenPendingDurationIsNull() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(null)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenPendingDurationIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(null)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenActiveDurationIsNull() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(null)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenActiveDurationIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(null)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenTaskInfoIsNull() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(null);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()));

    final String json = JsonUtils.write(pojo);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenTaskInfoIsMissing() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(null);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()));

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenIsNull() throws Exception {
    final TaskPojo pojo =
        new TaskPojo()
            .setVersion(TaskPojoIntegrationTest.VERSION)
            .setId(TaskPojoIntegrationTest.ID)
            .setTotalAttempts(TaskPojoIntegrationTest.ATTEMPTS)
            .setState(TaskPojoIntegrationTest.STATE)
            .setOriginalQueuedTime(TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME)
            .setQueuedTime(TaskPojoIntegrationTest.QUEUED_TIME)
            .setFrozenTime(TaskPojoIntegrationTest.FROZEN_TIME)
            .setDuration(TaskPojoIntegrationTest.DURATION)
            .setPendingDuration(TaskPojoIntegrationTest.PENDING_DURATION)
            .setActiveDuration(TaskPojoIntegrationTest.ACTIVE_DURATION)
            .setInfo(TaskPojoIntegrationTest.TASK_INFO);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final String json = JsonUtils.write(pojo);
    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, json);

    Assert.assertThat(json, SameJSONAs.sameJSONAs(jsonObject.toString()));
    Assert.assertThat(pojo2, Matchers.equalTo(pojo));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWithExtraJsonProperties() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("extra", "EXTRA")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(TaskPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.not(Matchers.isA(UnknownTaskPojo.class)));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsMissing() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(TaskPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownTaskPojo.class));
  }

  @Test
  public void testPojoJsonPersistenceWhenClassIsUnknown() throws Exception {
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "new_task")
            .put("extra", "EXTRA")
            .put("id", TaskPojoIntegrationTest.ID)
            .put("version", TaskPojoIntegrationTest.VERSION)
            .put("attempts", TaskPojoIntegrationTest.ATTEMPTS)
            .put("state", TaskPojoIntegrationTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.QUEUED_TIME.getEpochSecond(),
                    TaskPojoIntegrationTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskPojoIntegrationTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.DURATION.getSeconds(),
                    TaskPojoIntegrationTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.PENDING_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getSeconds(),
                    TaskPojoIntegrationTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskPojoIntegrationTest.TASK_INFO_JSON);

    final TaskPojo pojo2 = JsonUtils.read(TaskPojo.class, jsonObject.toString());

    Assert.assertThat(pojo2, Matchers.equalTo(TaskPojoIntegrationTest.POJO));
    Assert.assertThat(pojo2, Matchers.isA(UnknownTaskPojo.class));
  }
}
