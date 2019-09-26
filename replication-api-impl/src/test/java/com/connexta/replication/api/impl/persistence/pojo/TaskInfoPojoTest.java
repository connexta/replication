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

import com.connexta.replication.api.data.OperationType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TaskInfoPojoTest {
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

  static {
    try {
      DDF_METADATA_DATA = new JSONObject().put("k", "v").put("k2", "v2").toString();
    } catch (JSONException e) {
      throw new AssertionError(e);
    }
  }

  private static final ResourceInfoPojo RESOURCE =
      new ResourceInfoPojo()
          .setVersion(TaskInfoPojoTest.RESOURCE_VERSION)
          .setId(TaskInfoPojoTest.RESOURCE_ID)
          .setLastModified(TaskInfoPojoTest.RESOURCE_LAST_MODIFIED)
          .setSize(TaskInfoPojoTest.RESOURCE_SIZE)
          .setUri(TaskInfoPojoTest.RESOURCE_URI);

  private static final MetadataInfoPojo<?> METADATA =
      new MetadataInfoPojo<>()
          .setVersion(TaskInfoPojoTest.METADATA_VERSION)
          .setId(TaskInfoPojoTest.METADATA_ID)
          .setLastModified(TaskInfoPojoTest.METADATA_LAST_MODIFIED)
          .setSize(TaskInfoPojoTest.METADATA_SIZE)
          .setType(TaskInfoPojoTest.METADATA_TYPE);

  private static final DdfMetadataInfoPojo DDF_METADATA =
      new DdfMetadataInfoPojo()
          .setVersion(TaskInfoPojoTest.DDF_METADATA_VERSION)
          .setId(TaskInfoPojoTest.DDF_METADATA_ID)
          .setLastModified(TaskInfoPojoTest.DDF_METADATA_LAST_MODIFIED)
          .setSize(TaskInfoPojoTest.DDF_METADATA_SIZE)
          .setType(TaskInfoPojoTest.DDF_METADATA_TYPE)
          .setDdfVersion(TaskInfoPojoTest.DDF_METADATA_DDF_VERSION)
          .setDataClass(TaskInfoPojoTest.DDF_METADATA_DATA_CLASS)
          .setData(TaskInfoPojoTest.DDF_METADATA_DATA);

  private static final TaskInfoPojo POJO =
      new TaskInfoPojo()
          .setVersion(TaskInfoPojoTest.VERSION)
          .setId(TaskInfoPojoTest.ID)
          .setPriority(TaskInfoPojoTest.PRIORITY)
          .setIntelId(TaskInfoPojoTest.INTEL_ID)
          .setOperation(TaskInfoPojoTest.OPERATION)
          .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
          .setResource(TaskInfoPojoTest.RESOURCE)
          .addMetadata(TaskInfoPojoTest.METADATA)
          .addMetadata(TaskInfoPojoTest.DDF_METADATA);

  @Test
  public void testSetAndGetId() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().setId(TaskInfoPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(TaskInfoPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().setVersion(TaskInfoPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(TaskInfoPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetPriority() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().setPriority(TaskInfoPojoTest.PRIORITY);

    Assert.assertThat(pojo.getPriority(), Matchers.equalTo(TaskInfoPojoTest.PRIORITY));
  }

  @Test
  public void testSetAndGetIntelId() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().setIntelId(TaskInfoPojoTest.INTEL_ID);

    Assert.assertThat(pojo.getIntelId(), Matchers.equalTo(TaskInfoPojoTest.INTEL_ID));
  }

  @Test
  public void testSetAndGetOperation() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().setOperation(TaskInfoPojoTest.OPERATION);

    Assert.assertThat(pojo.getOperation(), Matchers.equalTo(TaskInfoPojoTest.OPERATION));
  }

  @Test
  public void testSetAndGetOperationWithEnum() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().setOperation(OperationType.DELETE);

    Assert.assertThat(pojo.getOperation(), Matchers.equalTo(OperationType.DELETE.name()));
  }

  @Test
  public void testSetAndGetLastModified() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().setLastModified(TaskInfoPojoTest.LAST_MODIFIED);

    Assert.assertThat(pojo.getLastModified(), Matchers.equalTo(TaskInfoPojoTest.LAST_MODIFIED));
  }

  @Test
  public void testSetAndGetResource() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().setResource(TaskInfoPojoTest.RESOURCE);

    Assert.assertThat(pojo.getResource(), Matchers.equalTo(TaskInfoPojoTest.RESOURCE));
  }

  @Test
  public void testSetAndGetMetadatas() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setMetadatas(List.of(TaskInfoPojoTest.METADATA, TaskInfoPojoTest.DDF_METADATA));

    Assert.assertThat(
        pojo.getMetadatas(),
        Matchers.equalTo(List.of(TaskInfoPojoTest.METADATA, TaskInfoPojoTest.DDF_METADATA)));
  }

  @Test
  public void testSetAndGetMetadatasViaStreams() throws Exception {
    final TaskInfoPojo pojo =
        new TaskInfoPojo()
            .setMetadatas(Stream.of(TaskInfoPojoTest.DDF_METADATA, TaskInfoPojoTest.METADATA));

    Assert.assertThat(
        pojo.metadatas().collect(Collectors.toList()),
        Matchers.equalTo(List.of(TaskInfoPojoTest.DDF_METADATA, TaskInfoPojoTest.METADATA)));
  }

  @Test
  public void testAddMetadata() throws Exception {
    final TaskInfoPojo pojo = new TaskInfoPojo().addMetadata(TaskInfoPojoTest.METADATA);
    Assert.assertThat(pojo.getMetadatas(), Matchers.equalTo(List.of(TaskInfoPojoTest.METADATA)));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID)
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID + "2")
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(
        TaskInfoPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID)
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(TaskInfoPojoTest.POJO.equals(TaskInfoPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(TaskInfoPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotATaskInfoPojo() throws Exception {
    Assert.assertThat(TaskInfoPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID + "2")
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION + 2)
            .setId(TaskInfoPojoTest.ID)
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenPriorityIsDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID)
            .setPriority((byte) (TaskInfoPojoTest.PRIORITY + 2))
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenIntelIdIsDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID)
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID + "2")
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenOperationIsDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID)
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION + "2")
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);
    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenLastModifiedIsDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID)
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED.plusSeconds(120L))
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenResourceIsDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID)
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(
                new ResourceInfoPojo()
                    .setVersion(TaskInfoPojoTest.RESOURCE_VERSION)
                    .setId(TaskInfoPojoTest.RESOURCE_ID + "2")
                    .setLastModified(TaskInfoPojoTest.RESOURCE_LAST_MODIFIED)
                    .setSize(TaskInfoPojoTest.RESOURCE_SIZE)
                    .setUri(TaskInfoPojoTest.RESOURCE_URI))
            .addMetadata(TaskInfoPojoTest.METADATA)
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenMetadataIsDifferent() throws Exception {
    final TaskInfoPojo pojo2 =
        new TaskInfoPojo()
            .setVersion(TaskInfoPojoTest.VERSION)
            .setId(TaskInfoPojoTest.ID)
            .setPriority(TaskInfoPojoTest.PRIORITY)
            .setIntelId(TaskInfoPojoTest.INTEL_ID)
            .setOperation(TaskInfoPojoTest.OPERATION)
            .setLastModified(TaskInfoPojoTest.LAST_MODIFIED)
            .setResource(TaskInfoPojoTest.RESOURCE)
            .addMetadata(
                new MetadataInfoPojo<>()
                    .setVersion(TaskInfoPojoTest.METADATA_VERSION)
                    .setId(TaskInfoPojoTest.METADATA_ID + "2")
                    .setLastModified(TaskInfoPojoTest.METADATA_LAST_MODIFIED)
                    .setSize(TaskInfoPojoTest.METADATA_SIZE)
                    .setType(TaskInfoPojoTest.METADATA_TYPE))
            .addMetadata(TaskInfoPojoTest.DDF_METADATA);

    Assert.assertThat(TaskInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
