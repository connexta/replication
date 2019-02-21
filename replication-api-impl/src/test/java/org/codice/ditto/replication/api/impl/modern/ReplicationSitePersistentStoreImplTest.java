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
package org.codice.ditto.replication.api.impl.modern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ddf.persistence.PersistentItem;
import org.codice.ddf.persistence.PersistentStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationSitePersistentStoreImplTest {

  ReplicationSitePersistentStoreImpl store;

  @Mock PersistentStore persistentStore;

  @Before
  public void setUp() throws Exception {
    System.setProperty("org.codice.ddf.system.siteName", "testSite");
    store = new ReplicationSitePersistentStoreImpl(persistentStore);
  }

  @Test
  public void init() throws Exception {
    store.init();
    ArgumentCaptor<PersistentItem> captor = ArgumentCaptor.forClass(PersistentItem.class);
    verify(persistentStore).add(any(String.class), captor.capture());
    PersistentItem item = captor.getValue();
    assertThat(item.getTextProperty("name"), is(SystemInfo.getSiteName()));
    assertThat(item.getTextProperty("url"), is(SystemBaseUrl.EXTERNAL.getBaseUrl()));
  }

  @Test
  public void initUpdateName() throws Exception {
    PersistentItem orig = new PersistentItem();
    orig.addIdProperty("local-site-id-1234567890");
    orig.addProperty("name", "oldName");
    orig.addProperty("url", SystemBaseUrl.EXTERNAL.getBaseUrl());
    when(persistentStore.get(anyString(), anyString())).thenReturn(Collections.singletonList(orig));
    store.init();
    ArgumentCaptor<PersistentItem> captor = ArgumentCaptor.forClass(PersistentItem.class);
    verify(persistentStore).add(any(String.class), captor.capture());
    PersistentItem item = captor.getValue();
    assertThat(item.getTextProperty("name"), is(SystemInfo.getSiteName()));
    assertThat(item.getTextProperty("url"), is(SystemBaseUrl.EXTERNAL.getBaseUrl()));
  }

  @Test
  public void initUpdateURL() throws Exception {
    PersistentItem orig = new PersistentItem();
    orig.addIdProperty("local-site-id-1234567890");
    orig.addProperty("name", SystemInfo.getSiteName());
    orig.addProperty("url", "https://asdf:1234");
    when(persistentStore.get(anyString(), anyString())).thenReturn(Collections.singletonList(orig));
    store.init();
    ArgumentCaptor<PersistentItem> captor = ArgumentCaptor.forClass(PersistentItem.class);
    verify(persistentStore).add(any(String.class), captor.capture());
    PersistentItem item = captor.getValue();
    assertThat(item.getTextProperty("name"), is(SystemInfo.getSiteName()));
    assertThat(item.getTextProperty("url"), is(SystemBaseUrl.EXTERNAL.getBaseUrl()));
  }

  @Test
  public void initNoOp() throws Exception {
    PersistentItem orig = new PersistentItem();
    orig.addIdProperty("local-site-id-1234567890");
    orig.addProperty("name", SystemInfo.getSiteName());
    orig.addProperty("url", SystemBaseUrl.EXTERNAL.getBaseUrl());
    when(persistentStore.get(anyString(), anyString())).thenReturn(Collections.singletonList(orig));
    store.init();
    verify(persistentStore, never()).add(any(String.class), any(PersistentItem.class));
  }
}
