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
package com.connexta.replication.api.impl.queue;

import com.connexta.replication.api.data.MetadataInfo;
import com.connexta.replication.api.data.OperationType;
import com.connexta.replication.api.data.ResourceInfo;
import com.connexta.replication.api.data.TaskInfo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractTaskTest {
  private static final byte PRIORITY = (byte) 3;
  private static final String ID = "id";
  private static final OperationType OPERATION = OperationType.DELETE;
  private static final Instant LAST_MODIFIED = Instant.MIN;
  private static final ResourceInfo RESOURCE = Mockito.mock(ResourceInfo.class);
  private static final MetadataInfo METADATA = Mockito.mock(MetadataInfo.class);

  private final TaskInfo info = Mockito.mock(TaskInfo.class);
  private final TaskInfo task =
      Mockito.mock(
          AbstractTask.class,
          Mockito.withSettings().useConstructor(info).defaultAnswer(Mockito.CALLS_REAL_METHODS));

  @Test
  public void testGetPriority() throws Exception {
    Mockito.when(info.getPriority()).thenReturn(AbstractTaskTest.PRIORITY);

    Assert.assertThat(task.getPriority(), Matchers.equalTo(AbstractTaskTest.PRIORITY));

    Mockito.verify(info).getPriority();
  }

  @Test
  public void testGetId() throws Exception {
    Mockito.when(info.getId()).thenReturn(AbstractTaskTest.ID);

    Assert.assertThat(task.getId(), Matchers.equalTo(AbstractTaskTest.ID));

    Mockito.verify(info).getId();
  }

  @Test
  public void testGetOperation() throws Exception {
    Mockito.when(info.getOperation()).thenReturn(AbstractTaskTest.OPERATION);

    Assert.assertThat(task.getOperation(), Matchers.equalTo(AbstractTaskTest.OPERATION));

    Mockito.verify(info).getOperation();
  }

  @Test
  public void testGetLastModified() throws Exception {
    Mockito.when(info.getLastModified()).thenReturn(AbstractTaskTest.LAST_MODIFIED);

    Assert.assertThat(task.getLastModified(), Matchers.equalTo(AbstractTaskTest.LAST_MODIFIED));

    Mockito.verify(info).getLastModified();
  }

  @Test
  public void testGetResource() throws Exception {
    Mockito.when(info.getResource()).thenReturn(Optional.of(AbstractTaskTest.RESOURCE));

    Assert.assertThat(
        task.getResource(), OptionalMatchers.isPresentAndIs(AbstractTaskTest.RESOURCE));

    Mockito.verify(info).getResource();
  }

  @Test
  public void testGetResourceWhenEmpty() throws Exception {
    Mockito.when(info.getResource()).thenReturn(Optional.empty());

    Assert.assertThat(task.getResource(), OptionalMatchers.isEmpty());

    Mockito.verify(info).getResource();
  }

  @Test
  public void testMetadatas() throws Exception {
    Mockito.when(info.metadatas()).thenReturn(Stream.of(AbstractTaskTest.METADATA));

    Assert.assertThat(
        task.metadatas().collect(Collectors.toList()),
        Matchers.contains(AbstractTaskTest.METADATA));

    Mockito.verify(info).metadatas();
  }

  @Test
  public void testMetadatasWhenEmpty() throws Exception {
    Mockito.when(info.metadatas()).thenReturn(Stream.empty());

    Assert.assertThat(
        task.metadatas().collect(Collectors.toList()),
        Matchers.emptyCollectionOf(MetadataInfo.class));

    Mockito.verify(info).metadatas();
  }
}
