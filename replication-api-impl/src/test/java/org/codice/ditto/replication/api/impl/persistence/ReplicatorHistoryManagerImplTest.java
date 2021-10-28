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
package org.codice.ditto.replication.api.impl.persistence;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.stream.Stream;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.persistence.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorHistoryManagerImplTest {

  ReplicatorHistoryManagerImpl history;

  @Mock ReplicationPersistentStore persistentStore;

  @Before
  public void setUp() throws Exception {
    history = new ReplicatorHistoryManagerImpl(persistentStore);
  }

  @Test
  public void create() {
    assertThat(history.create(), instanceOf(ReplicationStatusImpl.class));
  }

  @Test
  public void get() {
    history.get("test");
    verify(persistentStore).get(ReplicationStatusImpl.class, "test");
  }

  @Test
  public void objects() {
    when(persistentStore.objects(eq(ReplicationStatusImpl.class))).thenReturn(Stream.empty());
    history.objects();
    verify(persistentStore).objects(ReplicationStatusImpl.class);
  }

  @Test
  public void getByReplicatorId() {
    ReplicationStatusImpl status = new ReplicationStatusImpl();
    status.setReplicatorId("test");
    when(persistentStore.objects(eq(ReplicationStatusImpl.class))).thenReturn(Stream.of(status));
    assertThat(history.getByReplicatorId("test"), is(status));
  }

  @Test(expected = NotFoundException.class)
  public void getByReplicatorIdNotFoundException() {
    ReplicationStatusImpl status = new ReplicationStatusImpl();
    status.setReplicatorId("test");
    when(persistentStore.objects(eq(ReplicationStatusImpl.class))).thenReturn(Stream.empty());
    history.getByReplicatorId("test");
  }

  @Test
  public void save() {
    ReplicationStatusImpl prevStatus = loadStatus(new ReplicationStatusImpl(), 1);
    ReplicationStatusImpl freshStatus = loadStatus(new ReplicationStatusImpl(), 2);
    freshStatus.setReplicatorId(prevStatus.getReplicatorId());
    when(persistentStore.objects(eq(ReplicationStatusImpl.class)))
        .thenReturn(Stream.of(prevStatus));
    history.save(freshStatus);
    verify(persistentStore).save(prevStatus);
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveInvalidStatus() {
    ReplicationStatus mockStatus = mock(ReplicationStatus.class);
    history.save(mockStatus);
  }

  @Test
  public void saveFirstEvent() {
    ReplicationStatusImpl freshStatus = loadStatus(new ReplicationStatusImpl(), 1);
    freshStatus.setStartTime(new Date(0));
    when(persistentStore.objects(eq(ReplicationStatusImpl.class))).thenReturn(Stream.empty());
    history.save(freshStatus);
    verify(persistentStore).save(freshStatus);
    assertThat(freshStatus.getLastRun(), is(freshStatus.getStartTime()));
    assertThat(freshStatus.getLastSuccess(), is(new Date(1)));
  }

  @Test
  public void saveSuccessfulFirstEvent() {
    ReplicationStatusImpl freshStatus = loadStatus(new ReplicationStatusImpl(), 1);
    freshStatus.setStartTime(new Date(0));
    freshStatus.setStatus(Status.SUCCESS);
    when(persistentStore.objects(eq(ReplicationStatusImpl.class))).thenReturn(Stream.empty());
    history.save(freshStatus);
    verify(persistentStore).save(freshStatus);
    assertThat(freshStatus.getLastRun(), is(freshStatus.getStartTime()));
    assertThat(freshStatus.getLastSuccess(), is(freshStatus.getStartTime()));
  }

  @Test
  public void remove() {
    history.remove("test");
    verify(persistentStore).delete(eq(ReplicationStatusImpl.class), anyString());
  }

  private ReplicationStatusImpl loadStatus(ReplicationStatusImpl status, int num) {
    status.setReplicatorId("id" + num);
    status.setStartTime(new Date(num));
    status.setLastSuccess(new Date(num));
    status.setLastRun(new Date(num));
    status.setDuration(num);
    status.setStatus(Status.values()[num]);
    status.setPushCount(num);
    status.setPullCount(num);
    status.setPushFailCount(num);
    status.setPullFailCount(num);
    status.setPushBytes(num);
    status.setPullBytes(num);
    return status;
  }
}
