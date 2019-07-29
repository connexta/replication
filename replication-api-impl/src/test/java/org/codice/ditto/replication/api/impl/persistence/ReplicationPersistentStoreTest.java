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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;
import org.codice.ddf.persistence.PersistenceException;
import org.codice.ddf.persistence.PersistentItem;
import org.codice.ddf.persistence.PersistentStore;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.AbstractPersistable;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ReplicationPersistentStoreTest {

  private static final String REPSYNC_TYPE = "replication_config";

  private static final String SITE_TYPE = "replication_site";

  private static final String ID = "id";

  private static final String NAME = "name";

  private static final String SOURCE = "source";

  private static final String DESTINATION = "destination";

  private static final String FILTER = "filter";

  private static final String BIDIRECTIONAL = "bidirectional";

  private static final String RETRY_COUNT = "retry_count";

  private static final String DESCRIPTION = "description";

  private static final String SUSPENDED = "suspended";

  private static final String URL = "url";

  private static final String VERIFIED_URL = "verified-url";

  private static final String VERSION = "version";

  private static final String METADATA_ONLY = "metadataOnly";

  private static final String MODIFIED = "modified";

  private static final String DELETED = "deleted";

  private static final String DELETE_DATA = "deleteData";

  private static final String IS_REMOTE_MANAGED = "is-remote-managed";

  private PersistentStore mockPersistentStore;

  private ReplicationPersistentStore persistentStore;

  private List<Map<String, Object>> repSyncResults;

  private List<Map<String, Object>> repSyncPersistentItems;

  private List<Map<String, Object>> siteResults;

  private List<Map<String, Object>> sitePersistentItems;

  @Before
  public void setup() throws Exception {
    repSyncResults = new ArrayList<>();
    repSyncResults.add(createRepSyncMap(0));
    repSyncResults.add(createRepSyncMap(1));

    repSyncPersistentItems =
        repSyncResults.stream().map(this::getPersistentItemFromMap).collect(Collectors.toList());

    siteResults = new ArrayList<>();
    siteResults.add(createSiteMap(0));
    siteResults.add(createSiteMap(1));

    sitePersistentItems =
        siteResults.stream().map(this::getPersistentItemFromMap).collect(Collectors.toList());

    mockPersistentStore = mock(PersistentStore.class);
    persistentStore = new ReplicationPersistentStore(mockPersistentStore);
  }

  private Map<String, Object> createRepSyncMap(int num) {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, ID + num);
    map.put(NAME, NAME + num);
    map.put(SOURCE, SOURCE + num);
    map.put(DESTINATION, DESTINATION + num);
    map.put(FILTER, FILTER + num);
    map.put(RETRY_COUNT, num);
    map.put(BIDIRECTIONAL, "true");
    map.put(SUSPENDED, "false");
    map.put(DELETED, "false");
    map.put(DELETE_DATA, "false");
    map.put(DESCRIPTION, DESCRIPTION + num);
    map.put(VERSION, ReplicatorConfigImpl.CURRENT_VERSION);
    map.put(METADATA_ONLY, "false");
    return map;
  }

  private Map<String, Object> createSiteMap(int num) {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, ID + num);
    map.put(NAME, NAME + num);
    map.put(URL, URL + num);
    map.put(VERIFIED_URL, URL + "/verified/" + num);
    map.put(VERSION, ReplicationSiteImpl.CURRENT_VERSION);
    map.put(IS_REMOTE_MANAGED, false);
    return map;
  }

  @Test
  public void testCreatePersistable() {
    assertNotNull(persistentStore.createPersistable(ReplicatorConfigImpl.class));
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void testCreatePersistableInstantiationException() throws Exception {
    persistentStore.createPersistable(TestPersistable.class);
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void testCreatePersistableIllegalAccessException() throws Exception {
    persistentStore.createPersistable(PrivateConstructorPersistable.class);
  }

  @Test
  public void testGetAll() throws Exception {
    when(mockPersistentStore.get(eq(REPSYNC_TYPE), eq(""), any(int.class), any(int.class)))
        .thenReturn(repSyncPersistentItems);

    List<ReplicatorConfig> results =
        persistentStore.objects(ReplicatorConfigImpl.class).collect(Collectors.toList());

    assertEquals(2, results.size());
    for (int i = 0; i < 2; i++) {
      assertEqualExcludingModified(
          ((ReplicatorConfigImpl) results.get(i)).toMap(), repSyncResults.get(i));
    }
  }

  @Test
  public void testGetAllWithSites() throws Exception {
    when(mockPersistentStore.get(eq(SITE_TYPE), eq(""), any(int.class), any(int.class)))
        .thenReturn(sitePersistentItems);

    List<ReplicationSite> results =
        persistentStore.objects(ReplicationSiteImpl.class).collect(Collectors.toList());

    assertEquals(2, results.size());
    for (int i = 0; i < 2; i++) {
      assertEqualExcludingModified(
          ((ReplicationSiteImpl) results.get(i)).toMap(), siteResults.get(i));
    }
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void testGetAllHandlesPersistenceException() throws Exception {
    Mockito.doThrow(new PersistenceException())
        .when(mockPersistentStore)
        .get(any(String.class), any(String.class), any(int.class), any(int.class));

    persistentStore.objects(ReplicatorConfigImpl.class);
  }

  @Test
  public void testGet() throws Exception {
    when(mockPersistentStore.get(
            eq(REPSYNC_TYPE), startsWith("'id' = "), any(int.class), any(int.class)))
        .thenReturn(Collections.singletonList(repSyncPersistentItems.get(0)));

    ReplicatorConfigImpl result = persistentStore.get(ReplicatorConfigImpl.class, "id0");

    assertEqualExcludingModified(result.toMap(), repSyncResults.get(0));
  }

  @Test(expected = NotFoundException.class)
  public void testGetWithNoResults() throws Exception {
    when(mockPersistentStore.get(
            eq(REPSYNC_TYPE), startsWith("'id' = "), any(int.class), any(int.class)))
        .thenReturn(Collections.emptyList());

    persistentStore.get(ReplicatorConfigImpl.class, "id0");
  }

  @Test(expected = IllegalStateException.class)
  public void testGetWithMultipleResults() throws Exception {
    when(mockPersistentStore.get(
            eq(REPSYNC_TYPE), startsWith("'id' = "), any(int.class), any(int.class)))
        .thenReturn(repSyncPersistentItems);

    persistentStore.get(ReplicatorConfigImpl.class, "id0");
  }

  @Test
  public void testGetLoadsInvalidPersistable() throws Exception {
    Map<String, Object> map = createRepSyncMap(1);
    map.put(NAME, null);
    when(mockPersistentStore.get(
            eq(REPSYNC_TYPE), startsWith("'id' = "), any(int.class), any(int.class)))
        .thenReturn(Collections.singletonList(repSyncPersistentItems.get(0)));

    ReplicatorConfigImpl result = persistentStore.get(ReplicatorConfigImpl.class, "id0");

    assertEqualExcludingModified(result.toMap(), repSyncResults.get(0));
  }

  @Test
  public void testGetWithSites() throws Exception {
    when(mockPersistentStore.get(
            eq(SITE_TYPE), startsWith("'id' = "), any(int.class), any(int.class)))
        .thenReturn(Collections.singletonList(sitePersistentItems.get(0)));

    ReplicationSiteImpl result = persistentStore.get(ReplicationSiteImpl.class, "id0");

    assertEqualExcludingModified(result.toMap(), siteResults.get(0));
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void testGetHandlesPersistenceException() throws Exception {
    Mockito.doThrow(new PersistenceException())
        .when(mockPersistentStore)
        .get(any(String.class), any(String.class), any(int.class), any(int.class));

    persistentStore.get(ReplicatorConfigImpl.class, "id0");
  }

  @Test
  public void testSave() throws Exception {
    ReplicatorConfigImpl repSync = new ReplicatorConfigImpl();
    repSync.fromMap(repSyncResults.get(0));
    persistentStore.save(repSync);

    ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(mockPersistentStore).add(eq(REPSYNC_TYPE), captor.capture());
    Map<String, Object> savedMap = PersistentItem.stripSuffixes(captor.getValue());
    assertEqualExcludingModified(savedMap, repSyncResults.get(0));
    // assert that the modified date was updated upon save
    assertThat(savedMap.get(MODIFIED), notNullValue());
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void testSaveThrowsException() throws Exception {
    Mockito.doThrow(new PersistenceException())
        .when(mockPersistentStore)
        .add(any(String.class), any(Map.class));
    ReplicatorConfigImpl repSync = new ReplicatorConfigImpl();
    repSync.fromMap(repSyncResults.get(0));

    persistentStore.save(repSync);
  }

  @Test
  public void testGetPersistenceType() throws Exception {
    String type = persistentStore.getPersistenceType(ReplicatorConfigImpl.class);
    assertThat(type, equalTo("replication_config"));
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void testGetPersistenceTypeThrowsException() throws Exception {
    persistentStore.getPersistenceType(TestPersistable.class);
  }

  @Test
  public void delete() throws Exception {
    when(mockPersistentStore.delete(anyString(), anyString())).thenReturn(1);

    persistentStore.delete(ReplicatorConfigImpl.class, "id");

    verify(mockPersistentStore).delete(anyString(), anyString());
  }

  @Test(expected = NotFoundException.class)
  public void deleteThrowsNotFoundException() throws Exception {
    when(mockPersistentStore.delete(anyString(), anyString())).thenReturn(0);

    persistentStore.delete(ReplicatorConfigImpl.class, "id");
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void deleteThrowsPersistenceException() throws Exception {
    when(mockPersistentStore.delete(anyString(), anyString()))
        .thenThrow(PersistenceException.class);

    persistentStore.delete(ReplicatorConfigImpl.class, "id");
  }

  private void assertEqualExcludingModified(
      Map<String, Object> actual, Map<String, Object> expected) {
    Map<String, Object> copy = new HashMap<>(actual);
    copy.remove(MODIFIED);
    assertThat(copy, is(expected));
  }

  private PersistentItem getPersistentItemFromMap(Map<String, Object> map) {
    PersistentItem persistentItem = new PersistentItem();
    map.forEach(persistentItem::addProperty);
    return persistentItem;
  }

  private static class TestPersistable extends AbstractPersistable {

    public TestPersistable(String noDefaultConstructor) {}
  }

  private static class PrivateConstructorPersistable extends AbstractPersistable {

    private PrivateConstructorPersistable() {}
  }
}
