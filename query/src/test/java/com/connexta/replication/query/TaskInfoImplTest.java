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
package com.connexta.replication.query;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.TaskInfo;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class TaskInfoImplTest {

  private static final String ID = "id";

  private static final byte PRIORITY = 0;

  private static final OperationType OPERATION = OperationType.HARVEST;

  private static final String RESOURCE_URI = "https://test:123";

  private static final Instant RESOURCE_MODIFIED = Instant.now();

  private static final Instant METADATA_MODIFIED = Instant.now();

  private static final long SIZE = 1;

  private static final String TYPE = "Metacard XML";

  private static final Class<Map> DATA_CLASS = Map.class;

  private static final Map DATA = new HashMap();

  private ResourceInfo resourceInfo;

  private MetadataInfo metadataInfo;

  private TaskInfoImpl taskInfo;

  @Before
  public void before() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), RESOURCE_MODIFIED, SIZE);
    metadataInfo = new DdfMetadataInfoImpl<>(TYPE, METADATA_MODIFIED, SIZE, DATA_CLASS, DATA);
    taskInfo =
        new TaskInfoImpl(
            ID, PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo, Set.of(metadataInfo));
  }

  @Test
  public void getters() {
    assertThat(taskInfo.getId(), is(ID));
    assertThat(taskInfo.getPriority(), is(PRIORITY));
    assertThat(taskInfo.getOperation(), is(OPERATION));
    assertThat(taskInfo.getLastModified(), is(METADATA_MODIFIED));
    assertThat(taskInfo.getResource().get(), is(resourceInfo));
    assertThat(taskInfo.metadatas().collect(Collectors.toSet()), Matchers.contains(metadataInfo));
  }

  @Test
  public void metadatasConstructor() {
    taskInfo = new TaskInfoImpl(ID, PRIORITY, OPERATION, METADATA_MODIFIED, Set.of(metadataInfo));
    assertThat(taskInfo.getId(), is(ID));
    assertThat(taskInfo.getPriority(), is(PRIORITY));
    assertThat(taskInfo.getOperation(), is(OPERATION));
    assertThat(taskInfo.getLastModified(), is(METADATA_MODIFIED));
    assertThat(taskInfo.getResource(), is(Optional.empty()));
    assertThat(taskInfo.metadatas().collect(Collectors.toSet()), Matchers.contains(metadataInfo));
  }

  @Test
  public void resourceConstructor() {
    taskInfo = new TaskInfoImpl(ID, PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo);
    assertThat(taskInfo.getId(), is(ID));
    assertThat(taskInfo.getPriority(), is(PRIORITY));
    assertThat(taskInfo.getOperation(), is(OPERATION));
    assertThat(taskInfo.getLastModified(), is(METADATA_MODIFIED));
    assertThat(taskInfo.getResource().get(), is(resourceInfo));
    assertThat(taskInfo.metadatas().collect(Collectors.toSet()), is(Matchers.empty()));
  }

  @Test
  public void addMetadata() {
    taskInfo = new TaskInfoImpl(ID, PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo);
    taskInfo.addMetadata(metadataInfo);
    assertThat(taskInfo.metadatas().collect(Collectors.toSet()), Matchers.contains(metadataInfo));
  }

  @Test
  public void addMetadataSet() {
    taskInfo = new TaskInfoImpl(ID, PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo);
    MetadataInfo metadataInfo1 =
        new DdfMetadataInfoImpl<>("something unique", METADATA_MODIFIED, SIZE, DATA_CLASS, DATA);
    taskInfo.addMetadata(Set.of(metadataInfo, metadataInfo1));
    assertThat(
        taskInfo.metadatas().collect(Collectors.toSet()),
        Matchers.containsInAnyOrder(metadataInfo, metadataInfo1));
  }

  @Test
  public void equalsSameObject() {
    assertTrue(taskInfo.equals(taskInfo));
  }

  @Test
  public void equalsAgainstNull() {
    assertFalse(taskInfo.equals(null));
  }

  @Test
  public void equalsAgainstDifferentClass() {
    assertFalse(taskInfo.equals(new HashMap<>()));
  }

  @Test
  public void equals() {
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            ID, PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo, Set.of(metadataInfo));
    assertTrue(taskInfo.equals(taskInfo1));
    assertTrue(taskInfo1.equals(taskInfo));
  }

  @Test
  public void equalsDifferentID() {
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            "ID1", PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo, Set.of(metadataInfo));
    assertFalse(taskInfo.equals(taskInfo1));
    assertFalse(taskInfo1.equals(taskInfo));
  }

  @Test
  public void equalsDifferentPriority() {
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            ID, (byte) 9, OPERATION, METADATA_MODIFIED, resourceInfo, Set.of(metadataInfo));
    assertFalse(taskInfo.equals(taskInfo1));
    assertFalse(taskInfo1.equals(taskInfo));
  }

  @Test
  public void equalsDifferentOperation() {
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            ID,
            PRIORITY,
            OperationType.UNKNOWN,
            METADATA_MODIFIED,
            resourceInfo,
            Set.of(metadataInfo));
    assertFalse(taskInfo.equals(taskInfo1));
    assertFalse(taskInfo1.equals(taskInfo));
  }

  @Test
  public void equalsDifferentModifiedDate() {
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            ID, PRIORITY, OPERATION, Instant.EPOCH, resourceInfo, Set.of(metadataInfo));
    assertFalse(taskInfo.equals(taskInfo1));
    assertFalse(taskInfo1.equals(taskInfo));
  }

  @Test
  public void equalsDifferentResourceInfo() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI("https://test:456"), RESOURCE_MODIFIED, SIZE);
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            ID, PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo, Set.of(metadataInfo));
    assertFalse(taskInfo.equals(taskInfo1));
    assertFalse(taskInfo1.equals(taskInfo));
  }

  @Test
  public void equalsDifferentMetadataInfo() throws Exception {
    metadataInfo =
        new DdfMetadataInfoImpl<>("something unique", METADATA_MODIFIED, SIZE, DATA_CLASS, DATA);
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            ID, PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo, Set.of(metadataInfo));
    assertFalse(taskInfo.equals(taskInfo1));
    assertFalse(taskInfo1.equals(taskInfo));
  }

  @Test
  public void hashCodeIsConsistent() {
    assertThat(taskInfo.hashCode(), is(taskInfo.hashCode()));
  }

  @Test
  public void equalHashCodes() {
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            ID, PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo, Set.of(metadataInfo));
    assertThat(taskInfo.hashCode(), is(taskInfo1.hashCode()));
  }

  @Test
  public void unequalHashCodes() {
    TaskInfo taskInfo1 =
        new TaskInfoImpl(
            "ID1", PRIORITY, OPERATION, METADATA_MODIFIED, resourceInfo, Set.of(metadataInfo));
    assertThat(taskInfo.hashCode(), is(not(taskInfo1.hashCode())));
  }
}
