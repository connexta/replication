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
package org.codice.ditto.replication.api.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.source.CatalogProvider;
import ddf.catalog.source.IngestException;
import java.io.Serializable;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.codice.ddf.security.Security;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.impl.mcard.ReplicationHistoryAttributes;
import org.codice.ditto.replication.api.mcard.ReplicationHistory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengis.filter.Filter;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorHistoryImplTest {

  ReplicatorHistoryImpl history;

  @Mock Security security;

  @Mock CatalogFramework framework;

  @Mock CatalogProvider provider;

  @Mock Metacards metacards;

  @Before
  public void setUp() throws Exception {
    FilterBuilder builder = new GeotoolsFilterBuilder();
    List<MetacardType> types = new ArrayList<>();
    types.add(new CoreAttributes());
    types.add(new ReplicationHistoryAttributes());
    MetacardType type = new MetacardTypeImpl("replication-history", types);
    doCallRealMethod()
        .when(metacards)
        .setIfPresent(any(Metacard.class), any(String.class), any(Serializable.class));
    doCallRealMethod()
        .when(metacards)
        .setIfPresentOrDefault(
            any(Metacard.class),
            any(String.class),
            any(Serializable.class),
            any(Serializable.class));

    when(security.runWithSubjectOrElevate(any(Callable.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, Callable.class).call());
    when(security.runAsAdmin(any(PrivilegedAction.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, PrivilegedAction.class).run());
    history = new ReplicatorHistoryImpl(framework, provider, builder, metacards, type, security);
  }

  @Test
  public void init() throws Exception {
    List<ReplicationStatus> events =
        generateOldStatus(
            "test", new Date(0), new Date(TimeUnit.DAYS.toMillis(1)), TimeUnit.MINUTES.toMillis(5));

    when(metacards.getTypeForFilter(any(Filter.class), any(Function.class))).thenReturn(events);
    history.init();
    ArgumentCaptor<CreateRequest> captor = ArgumentCaptor.forClass(CreateRequest.class);
    verify(framework).create(captor.capture());
    Metacard mcard = captor.getValue().getMetacards().get(0);
    assertThat(mcard.getAttribute(ReplicationHistory.LAST_SUCCESS), notNullValue());
    assertThat(mcard.getAttribute(ReplicationHistory.LAST_RUN), notNullValue());
    assertThat(
        mcard.getAttribute(ReplicationHistory.PULL_COUNT).getValue(), is((long) events.size()));
    assertThat(
        mcard.getAttribute(ReplicationHistory.PUSH_COUNT).getValue(), is((long) events.size()));
    assertThat(
        mcard.getAttribute(ReplicationHistory.PULL_FAIL_COUNT).getValue(),
        is((long) events.size()));
    assertThat(
        mcard.getAttribute(ReplicationHistory.PUSH_FAIL_COUNT).getValue(),
        is((long) events.size()));
    assertThat(
        mcard.getAttribute(ReplicationHistory.PULL_BYTES).getValue(),
        is(10L * (long) events.size()));
    assertThat(
        mcard.getAttribute(ReplicationHistory.PUSH_BYTES).getValue(),
        is(10L * (long) events.size()));
    ArgumentCaptor<DeleteRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteRequest.class);

    verify(provider).delete(deleteCaptor.capture());
    assertThat(deleteCaptor.getValue().getAttributeValues().size(), is(events.size()));
  }

  @Test
  public void initPreviouslyCondensedEvents() throws Exception {
    Date origTime = new Date(0);
    ReplicationStatus oldStatus = new ReplicationStatusImpl("test");
    oldStatus.setStartTime(origTime);
    oldStatus.setLastRun(origTime);
    oldStatus.setLastSuccess(origTime);
    oldStatus.setStatus(Status.SUCCESS);
    when(metacards.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.singletonList(oldStatus));
    history.init();
    verify(framework, never()).create(any(CreateRequest.class));
    verify(provider, never()).update(any(UpdateRequest.class));
  }

  @Test
  public void addReplicationEventNoPreviousEventsSuccess() throws Exception {
    when(metacards.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(new ArrayList());
    Date start = new Date();
    ReplicationStatus status = new ReplicationStatusImpl("test");
    status.setStartTime(start);
    status.setStatus(Status.SUCCESS);
    history.addReplicationEvent(status);
    ArgumentCaptor<CreateRequest> captor = ArgumentCaptor.forClass(CreateRequest.class);
    verify(framework).create(captor.capture());
    Metacard mcard = captor.getValue().getMetacards().get(0);
    assertThat(mcard.getAttribute(ReplicationHistory.LAST_SUCCESS).getValue(), is(start));
    assertThat(mcard.getAttribute(ReplicationHistory.LAST_RUN).getValue(), is(start));
    verify(provider, never()).update(any(UpdateRequest.class));
  }

  @Test
  public void addReplicationEventNoPreviousEventsFailure() throws Exception {
    when(metacards.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(new ArrayList());
    Date start = new Date();
    ReplicationStatus status = new ReplicationStatusImpl("test");
    status.setStartTime(start);
    status.setStatus(Status.FAILURE);
    history.addReplicationEvent(status);
    ArgumentCaptor<CreateRequest> captor = ArgumentCaptor.forClass(CreateRequest.class);
    verify(framework).create(captor.capture());
    Metacard mcard = captor.getValue().getMetacards().get(0);
    assertThat(mcard.getAttribute(ReplicationHistory.LAST_SUCCESS), nullValue());
    assertThat(mcard.getAttribute(ReplicationHistory.LAST_RUN).getValue(), is(start));
    verify(provider, never()).update(any(UpdateRequest.class));
  }

  @Test
  public void addReplicationEventPreviousEvents() throws Exception {
    Date origTime = new Date(0);
    ReplicationStatus oldStatus = new ReplicationStatusImpl("test");
    oldStatus.setStartTime(origTime);
    oldStatus.setLastRun(origTime);
    oldStatus.setLastSuccess(origTime);
    oldStatus.setStatus(Status.SUCCESS);

    when(metacards.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.singletonList(oldStatus));
    Date start = new Date();
    ReplicationStatus status = new ReplicationStatusImpl("test");
    status.setStartTime(start);
    status.setStatus(Status.SUCCESS);
    history.addReplicationEvent(status);
    verify(framework, never()).create(any(CreateRequest.class));

    ArgumentCaptor<UpdateRequest> captor = ArgumentCaptor.forClass(UpdateRequest.class);

    verify(provider).update(captor.capture());
    Metacard mcard = captor.getValue().getUpdates().get(0).getValue();
    assertThat(mcard.getAttribute(ReplicationHistory.LAST_SUCCESS).getValue(), is(start));
    assertThat(mcard.getAttribute(ReplicationHistory.LAST_RUN).getValue(), is(start));
    assertThat(mcard.getAttribute(ReplicationHistory.START_TIME).getValue(), is(origTime));
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void addReplicationEventStorageException() throws Exception {
    when(metacards.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(new ArrayList());
    when(framework.create(any(CreateRequest.class))).thenThrow(new IngestException());
    Date start = new Date();
    ReplicationStatus status = new ReplicationStatusImpl("test");
    status.setStartTime(start);
    status.setStatus(Status.SUCCESS);
    history.addReplicationEvent(status);
  }

  @Test
  public void removeReplicationEvent() throws Exception {
    ReplicationStatus event = new ReplicationStatusImpl("myid", "myname");
    history.removeReplicationEvent(event);
    verify(provider).delete(any(DeleteRequest.class));
    verify(framework, never()).delete(any(DeleteRequest.class));
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void removeReplicationEventError() throws Exception {
    when(provider.delete(any(DeleteRequest.class))).thenThrow(new IngestException());
    ReplicationStatus event = new ReplicationStatusImpl("myid", "myname");
    history.removeReplicationEvent(event);
  }

  @Test
  public void removeReplicationEvents() throws Exception {
    history.removeReplicationEvents(Collections.singleton("myid"));
    verify(provider).delete(any(DeleteRequest.class));
    verify(framework, never()).delete(any(DeleteRequest.class));
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void removeReplicationEventsError() throws Exception {
    when(provider.delete(any(DeleteRequest.class))).thenThrow(new IngestException());
    history.removeReplicationEvents(Collections.singleton("myid"));
  }

  private List<ReplicationStatus> generateOldStatus(
      String configName, Date start, Date end, long timeBetween) {
    List<ReplicationStatus> results = new ArrayList<>();
    Date current = end;
    Status s = Status.SUCCESS;
    while (current.after(start) || current.getTime() == start.getTime()) {
      ReplicationStatus status = new ReplicationStatusImpl(configName);
      status.setStartTime(current);
      status.setStatus(s);
      status.setDuration(10L);
      status.setPushCount(1);
      status.setPushBytes(10);
      status.setPushFailCount(1);
      status.setPullBytes(10);
      status.setPullCount(1);
      status.setPullFailCount(1);
      results.add(status);
      s = s.equals(Status.SUCCESS) ? Status.FAILURE : Status.SUCCESS;
      current = new Date(current.getTime() - timeBetween);
    }
    return results;
  }
}
