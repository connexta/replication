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
package com.connexta.replication.api.impl.data;

import com.connexta.replication.api.ReplicationException;
import com.connexta.replication.api.data.ErrorCode;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ParsingException;
import com.connexta.replication.api.data.ProcessingException;
import com.connexta.replication.api.data.Task;
import com.connexta.replication.api.data.Task.State;
import com.connexta.replication.api.data.TaskInfo;
import com.connexta.replication.api.impl.persistence.pojo.DdfMetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.MetadataInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.ResourceInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.TaskInfoPojo;
import com.connexta.replication.api.impl.persistence.pojo.TaskPojo;
import com.connexta.replication.api.queue.SiteQueue;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import io.micrometer.core.instrument.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import uk.co.datumedge.hamcrest.json.SameJSONAs;

public class TaskManagerImplTest {
  private static final long NANOS = System.nanoTime();
  // using System.currentTimeMillis() ensures the same resolution when testing
  // as how the implementation currently creates Instant objects from a Clock
  private static final Instant NOW = Instant.ofEpochMilli(System.currentTimeMillis());

  private static final int VERSION = TaskPojo.CURRENT_VERSION;
  private static final String ID = "1234";
  private static final int ATTEMPTS = 5;
  private static final State STATE = State.SUCCESSFUL;
  private static final String STATE_NAME = TaskManagerImplTest.STATE.name();
  private static final Instant ORIGINAL_QUEUED_TIME = Instant.ofEpochMilli(10L);
  private static final Instant QUEUED_TIME = Instant.ofEpochMilli(10000L);
  private static final long FROZEN_TIME = TaskManagerImplTest.NOW.toEpochMilli() + 23453L;
  private static final long MONOTONIC_FROZEN_TIME =
      TaskManagerImplTest.NANOS + TimeUnit.MILLISECONDS.toNanos(23453L);
  private static final Duration DURATION = Duration.ofMinutes(20L);
  private static final Duration PENDING_DURATION = Duration.ofMinutes(10L);
  private static final Duration ACTIVE_DURATION = Duration.ofMinutes(5L);

  private static final int RESOURCE_VERSION = ResourceInfoPojo.CURRENT_VERSION;
  private static final String RESOURCE_ID = "12345";
  private static final Instant RESOURCE_LAST_MODIFIED = Instant.now().minusSeconds(10L);
  private static final long RESOURCE_SIZE = 1234L;
  private static final String RESOURCE_URI = "https://some.uri.com";

  private static final int METADATA_VERSION = MetadataInfoPojo.CURRENT_VERSION;
  private static final String METADATA_ID = "123456";
  private static final Instant METADATA_LAST_MODIFIED = Instant.now().minusSeconds(30L);
  private static final long METADATA_SIZE = 12345L;
  private static final String METADATA_TYPE = "ddms";

  private static final int DDF_METADATA_VERSION = MetadataInfoPojo.CURRENT_VERSION;
  private static final int DDF_METADATA_DDF_VERSION = DdfMetadataInfoPojo.CURRENT_VERSION;
  private static final String DDF_METADATA_ID = "1234444";
  private static final Instant DDF_METADATA_LAST_MODIFIED = Instant.now();
  private static final long DDF_METADATA_SIZE = 1234L;
  private static final String DDF_METADATA_TYPE = "ddms";
  private static final String DDF_METADATA_DATA_CLASS = Map.class.getName();
  private static final String DDF_METADATA_DATA;

  private static final int TASK_INFO_VERSION = TaskInfoPojo.CURRENT_VERSION;
  private static final String TASK_INFO_ID = "1234";
  private static final byte TASK_INFO_PRIORITY = 3;
  private static final String TASK_INFO_INTEL_ID = "intel.id";
  private static final String TASK_INFO_OPERATION = OperationType.HARVEST.name();
  private static final Instant TASK_INFO_LAST_MODIFIED = Instant.now().minusSeconds(5L);

  private static final RuntimeException EXCEPTION = new RuntimeException("testing");
  private static final ReplicationException REPLICATION_EXCEPTION =
      new ReplicationException("testing");

  private static final JSONObject RESOURCE_JSON;
  private static final JSONObject METADATA_JSON;
  private static final JSONObject DDF_METADATA_JSON;
  private static final JSONObject TASK_INFO_JSON;
  private static final JSONObject TASK_JSON;

  static {
    try {
      DDF_METADATA_DATA = new JSONObject().put("k", "v").put("k2", "v2").toString();
      RESOURCE_JSON =
          new JSONObject()
              .put("clazz", "resource")
              .put("id", TaskManagerImplTest.RESOURCE_ID)
              .put("version", TaskManagerImplTest.RESOURCE_VERSION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.RESOURCE_LAST_MODIFIED.getEpochSecond(),
                      TaskManagerImplTest.RESOURCE_LAST_MODIFIED.getNano()))
              .put("size", TaskManagerImplTest.RESOURCE_SIZE)
              .put("uri", TaskManagerImplTest.RESOURCE_URI);
      METADATA_JSON =
          new JSONObject()
              .put("clazz", "metadata")
              .put("id", TaskManagerImplTest.METADATA_ID)
              .put("version", TaskManagerImplTest.METADATA_VERSION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.METADATA_LAST_MODIFIED.getEpochSecond(),
                      TaskManagerImplTest.METADATA_LAST_MODIFIED.getNano()))
              .put("size", TaskManagerImplTest.METADATA_SIZE)
              .put("type", TaskManagerImplTest.METADATA_TYPE);
      DDF_METADATA_JSON =
          new JSONObject()
              .put("clazz", "ddf_metadata")
              .put("id", TaskManagerImplTest.DDF_METADATA_ID)
              .put("version", TaskManagerImplTest.DDF_METADATA_VERSION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.DDF_METADATA_LAST_MODIFIED.getEpochSecond(),
                      TaskManagerImplTest.DDF_METADATA_LAST_MODIFIED.getNano()))
              .put("size", TaskManagerImplTest.DDF_METADATA_SIZE)
              .put("type", TaskManagerImplTest.DDF_METADATA_TYPE)
              .put("ddf_version", TaskManagerImplTest.DDF_METADATA_DDF_VERSION)
              .put("data_class", TaskManagerImplTest.DDF_METADATA_DATA_CLASS)
              .put("data", TaskManagerImplTest.DDF_METADATA_DATA);
      TASK_INFO_JSON =
          new JSONObject()
              .put("clazz", "task")
              .put("id", TaskManagerImplTest.TASK_INFO_ID)
              .put("version", TaskManagerImplTest.TASK_INFO_VERSION)
              .put("priority", TaskManagerImplTest.TASK_INFO_PRIORITY)
              .put("intel_id", TaskManagerImplTest.TASK_INFO_INTEL_ID)
              .put("operation", TaskManagerImplTest.TASK_INFO_OPERATION)
              .put(
                  "last_modified",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.TASK_INFO_LAST_MODIFIED.getEpochSecond(),
                      TaskManagerImplTest.TASK_INFO_LAST_MODIFIED.getNano()))
              .put("resource", TaskManagerImplTest.RESOURCE_JSON)
              .put(
                  "metadata",
                  new JSONArray()
                      .put(0, TaskManagerImplTest.METADATA_JSON)
                      .put(1, TaskManagerImplTest.DDF_METADATA_JSON));
      TASK_JSON =
          new JSONObject()
              .put("clazz", "task")
              .put("id", TaskManagerImplTest.ID)
              .put("version", TaskManagerImplTest.VERSION)
              .put("attempts", TaskManagerImplTest.ATTEMPTS)
              .put("state", TaskManagerImplTest.STATE_NAME)
              .put(
                  "original_queued_time",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                      TaskManagerImplTest.ORIGINAL_QUEUED_TIME.getNano()))
              .put(
                  "queued_time",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.QUEUED_TIME.getEpochSecond(),
                      TaskManagerImplTest.QUEUED_TIME.getNano()))
              .put("frozen_time", TaskManagerImplTest.FROZEN_TIME)
              .put(
                  "duration",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.DURATION.getSeconds(),
                      TaskManagerImplTest.DURATION.getNano()))
              .put(
                  "pending_duration",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.PENDING_DURATION.getSeconds(),
                      TaskManagerImplTest.PENDING_DURATION.getNano()))
              .put(
                  "active_duration",
                  DecimalUtils.toBigDecimal(
                      TaskManagerImplTest.ACTIVE_DURATION.getSeconds(),
                      TaskManagerImplTest.ACTIVE_DURATION.getNano()))
              .put("info", TaskManagerImplTest.TASK_INFO_JSON);
    } catch (JSONException e) {
      throw new AssertionError(e);
    }
  }

  private static final ResourceInfoPojo RESOURCE =
      new ResourceInfoPojo()
          .setVersion(TaskManagerImplTest.RESOURCE_VERSION)
          .setId(TaskManagerImplTest.RESOURCE_ID)
          .setLastModified(TaskManagerImplTest.RESOURCE_LAST_MODIFIED)
          .setSize(TaskManagerImplTest.RESOURCE_SIZE)
          .setUri(TaskManagerImplTest.RESOURCE_URI);

  private static final MetadataInfoPojo<?> METADATA =
      new MetadataInfoPojo<>()
          .setVersion(TaskManagerImplTest.METADATA_VERSION)
          .setId(TaskManagerImplTest.METADATA_ID)
          .setLastModified(TaskManagerImplTest.METADATA_LAST_MODIFIED)
          .setSize(TaskManagerImplTest.METADATA_SIZE)
          .setType(TaskManagerImplTest.METADATA_TYPE);

  private static final DdfMetadataInfoPojo DDF_METADATA =
      new DdfMetadataInfoPojo()
          .setVersion(TaskManagerImplTest.DDF_METADATA_VERSION)
          .setId(TaskManagerImplTest.DDF_METADATA_ID)
          .setLastModified(TaskManagerImplTest.DDF_METADATA_LAST_MODIFIED)
          .setSize(TaskManagerImplTest.DDF_METADATA_SIZE)
          .setType(TaskManagerImplTest.DDF_METADATA_TYPE)
          .setDdfVersion(TaskManagerImplTest.DDF_METADATA_DDF_VERSION)
          .setDataClass(TaskManagerImplTest.DDF_METADATA_DATA_CLASS)
          .setData(TaskManagerImplTest.DDF_METADATA_DATA);

  private static final TaskInfoPojo TASK_INFO_POJO =
      new TaskInfoPojo()
          .setVersion(TaskManagerImplTest.TASK_INFO_VERSION)
          .setId(TaskManagerImplTest.TASK_INFO_ID)
          .setPriority(TaskManagerImplTest.TASK_INFO_PRIORITY)
          .setIntelId(TaskManagerImplTest.TASK_INFO_INTEL_ID)
          .setOperation(TaskManagerImplTest.TASK_INFO_OPERATION)
          .setLastModified(TaskManagerImplTest.TASK_INFO_LAST_MODIFIED)
          .setResource(TaskManagerImplTest.RESOURCE)
          .addMetadata(TaskManagerImplTest.METADATA)
          .addMetadata(TaskManagerImplTest.DDF_METADATA);

  private static final TaskInfo TASK_INFO = new TaskInfoImpl(TaskManagerImplTest.TASK_INFO_POJO);

  @Rule public ExpectedException exception = ExpectedException.none();

  private final Clock clock = Mockito.mock(Clock.class);

  private final TaskManagerImpl mgr = new TaskManagerImpl(clock);

  @Before
  public void setup() {
    Mockito.when(clock.wallTime())
        .thenReturn(TaskManagerImplTest.NOW.toEpochMilli(), TaskManagerImplTest.FROZEN_TIME);
    Mockito.when(clock.monotonicTime())
        .thenReturn(TaskManagerImplTest.NANOS, TaskManagerImplTest.MONOTONIC_FROZEN_TIME);
  }

  @Test
  public void testReadFrom() throws Exception {
    final TestTask task = mgr.readFrom(TestTask.class, TaskManagerImplTest.TASK_JSON.toString());

    Assert.assertThat(task.getId(), Matchers.equalTo(TaskManagerImplTest.ID));
    Assert.assertThat(task.clock, Matchers.sameInstance(clock));
    Assert.assertThat(task.getPriority(), Matchers.equalTo(TaskManagerImplTest.TASK_INFO_PRIORITY));
    Assert.assertThat(task.getTotalAttempts(), Matchers.equalTo(TaskManagerImplTest.ATTEMPTS));
    Assert.assertThat(task.getState(), Matchers.equalTo(TaskManagerImplTest.STATE));
    Assert.assertThat(
        task.getOriginalQueuedTime(), Matchers.equalTo(TaskManagerImplTest.ORIGINAL_QUEUED_TIME));
    Assert.assertThat(task.getQueuedTime(), Matchers.equalTo(TaskManagerImplTest.QUEUED_TIME));
    Assert.assertThat(task.getDuration(), Matchers.equalTo(TaskManagerImplTest.DURATION));
    Assert.assertThat(
        task.getPendingDuration(), Matchers.equalTo(TaskManagerImplTest.PENDING_DURATION));
    Assert.assertThat(
        task.getActiveDuration(), Matchers.equalTo(TaskManagerImplTest.ACTIVE_DURATION));
    Assert.assertThat(task.getInfo(), Matchers.equalTo(TaskManagerImplTest.TASK_INFO));
    Assert.assertThat(task.hasUnknowns(), Matchers.equalTo(false));
  }

  @Test
  public void testReadFromWithAnInvalidTaskClass() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(Matchers.matchesPattern("expected an AbstractTaskImpl.*"));

    mgr.readFrom(Task.class, TaskManagerImplTest.TASK_JSON.toString());
  }

  @Test
  public void testReadFromWhenTaskClassIsMissingPojoCtor() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(Matchers.matchesPattern("missing expected constructor.*"));

    mgr.readFrom(TestTask2.class, TaskManagerImplTest.TASK_JSON.toString());
  }

  @Test
  public void testReadFromThrowingParsingExceptionForEmptyInput() throws Exception {
    exception.expect(ParsingException.class);
    exception.expectCause(Matchers.isA(MismatchedInputException.class));

    mgr.readFrom(TestTask.class, "");
  }

  @Test
  public void testReadFromThrowingParsingExceptionForInvalidJson() throws Exception {
    exception.expect(ParsingException.class);
    exception.expectCause(Matchers.isA(JsonParseException.class));

    mgr.readFrom(TestTask.class, "{]");
  }

  @Test
  public void testReadFromWithAbstractClass() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(Matchers.matchesPattern("invalid abstract class.*"));

    mgr.readFrom(AbstractTaskImpl.class, TaskManagerImplTest.TASK_JSON.toString());
  }

  @Test
  public void testReadFromWithATaskClassThatFailsToInstantiateWithNonReplicationExeption()
      throws Exception {
    exception.expect(ProcessingException.class);
    exception.expectMessage(Matchers.matchesPattern("failed to instantiate.*"));
    exception.expectCause(Matchers.sameInstance(TaskManagerImplTest.EXCEPTION));

    mgr.readFrom(TestTask3.class, TaskManagerImplTest.TASK_JSON.toString());
  }

  @Test
  public void testReadFromWithATaskClassThatFailsToInstantiateWitReplicationExeption()
      throws Exception {
    exception.expect(Matchers.sameInstance(TaskManagerImplTest.REPLICATION_EXCEPTION));

    mgr.readFrom(TestTask4.class, TaskManagerImplTest.TASK_JSON.toString());
  }

  @Test
  public void testWriteTo() throws Exception {
    final TestTask task = new TestTask(TaskManagerImplTest.TASK_INFO, clock);

    task.setId(TaskManagerImplTest.ID);
    task.setTotalAttempts(TaskManagerImplTest.ATTEMPTS);
    task.setState(TaskManagerImplTest.STATE);
    task.setOriginalQueuedTime(TaskManagerImplTest.ORIGINAL_QUEUED_TIME);
    task.setQueuedTime(TaskManagerImplTest.QUEUED_TIME);
    task.setDuration(TaskManagerImplTest.DURATION);
    task.setPendingDuration(TaskManagerImplTest.PENDING_DURATION);
    task.setActiveDuration(TaskManagerImplTest.ACTIVE_DURATION);
    final JSONObject jsonObject =
        new JSONObject()
            .put("clazz", "task")
            .put("id", TaskManagerImplTest.ID)
            .put("version", TaskManagerImplTest.VERSION)
            .put("attempts", TaskManagerImplTest.ATTEMPTS)
            .put("state", TaskManagerImplTest.STATE)
            .put(
                "original_queued_time",
                DecimalUtils.toBigDecimal(
                    TaskManagerImplTest.ORIGINAL_QUEUED_TIME.getEpochSecond(),
                    TaskManagerImplTest.ORIGINAL_QUEUED_TIME.getNano()))
            .put(
                "queued_time",
                DecimalUtils.toBigDecimal(
                    TaskManagerImplTest.QUEUED_TIME.getEpochSecond(),
                    TaskManagerImplTest.QUEUED_TIME.getNano()))
            .put("frozen_time", TaskManagerImplTest.FROZEN_TIME)
            .put(
                "duration",
                DecimalUtils.toBigDecimal(
                    TaskManagerImplTest.DURATION.getSeconds(),
                    TaskManagerImplTest.DURATION.getNano()))
            .put(
                "pending_duration",
                DecimalUtils.toBigDecimal(
                    TaskManagerImplTest.PENDING_DURATION.getSeconds(),
                    TaskManagerImplTest.PENDING_DURATION.getNano()))
            .put(
                "active_duration",
                DecimalUtils.toBigDecimal(
                    TaskManagerImplTest.ACTIVE_DURATION.getSeconds(),
                    TaskManagerImplTest.ACTIVE_DURATION.getNano()))
            .put("info", TaskManagerImplTest.TASK_INFO_JSON);

    Assert.assertThat(mgr.writeTo(task), SameJSONAs.sameJSONAs(jsonObject.toString()));
  }

  @Test
  public void testWriteToWithAnInvalidTaskClass() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(Matchers.matchesPattern("expected an AbstractTaskImpl.*"));

    mgr.writeTo(Mockito.mock(Task.class));
  }

  public static class TestTask extends AbstractTaskImpl {
    public TestTask(TaskPojo pojo, Clock clock) {
      super(pojo, clock);
    }

    public TestTask(TaskInfo info, Clock clock) {
      super(info, clock);
    }

    @Override
    public SiteQueue getQueue() {
      return null;
    }

    @Override
    public boolean isLocked() {
      return false;
    }

    @Override
    public void unlock() throws InterruptedException {}

    @Override
    public void complete() throws InterruptedException {}

    @Override
    public void fail(ErrorCode code) throws InterruptedException {}

    @Override
    public void fail(ErrorCode code, String reason) throws InterruptedException {}
  }

  public static class TestTask2 extends AbstractTaskImpl {
    public TestTask2(Clock clock) {
      super((TaskPojo) null, clock);
    }

    @Override
    public SiteQueue getQueue() {
      return null;
    }

    @Override
    public boolean isLocked() {
      return false;
    }

    @Override
    public void unlock() throws InterruptedException {}

    @Override
    public void complete() throws InterruptedException {}

    @Override
    public void fail(ErrorCode code) throws InterruptedException {}

    @Override
    public void fail(ErrorCode code, String reason) throws InterruptedException {}
  }

  public static class TestTask3 extends AbstractTaskImpl {
    public TestTask3(TaskPojo pojo, Clock clock) {
      super(pojo, clock);
      throw TaskManagerImplTest.EXCEPTION;
    }

    @Override
    public SiteQueue getQueue() {
      return null;
    }

    @Override
    public boolean isLocked() {
      return false;
    }

    @Override
    public void unlock() throws InterruptedException {}

    @Override
    public void complete() throws InterruptedException {}

    @Override
    public void fail(ErrorCode code) throws InterruptedException {}

    @Override
    public void fail(ErrorCode code, String reason) throws InterruptedException {}
  }

  public static class TestTask4 extends AbstractTaskImpl {
    public TestTask4(TaskPojo pojo, Clock clock) {
      super(pojo, clock);
      throw TaskManagerImplTest.REPLICATION_EXCEPTION;
    }

    @Override
    public SiteQueue getQueue() {
      return null;
    }

    @Override
    public boolean isLocked() {
      return false;
    }

    @Override
    public void unlock() throws InterruptedException {}

    @Override
    public void complete() throws InterruptedException {}

    @Override
    public void fail(ErrorCode code) throws InterruptedException {}

    @Override
    public void fail(ErrorCode code, String reason) throws InterruptedException {}
  }
}
