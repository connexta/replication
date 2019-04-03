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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.service.SecurityServiceException;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ddf.persistence.PersistenceException;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.ReplicationItemManager;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledReplicatorDeleterTest {

  private static final String SITE_NAME = "siteName";

  private TestSheduledReplicatorDeleter scheduledReplicatorDeleter;

  private final int pageSize = 1;

  private final String SOURCE = "source";

  @Mock ReplicatorConfigManager replicatorConfigManager;

  @Mock ScheduledExecutorService scheduledExecutorService;

  @Mock ReplicationItemManager replicationItemManager;

  @Mock ReplicatorHistory replicatorHistory;

  @Mock Metacards metacards;

  @Mock Security security;

  @Before
  public void setup() throws SecurityServiceException, InvocationTargetException {
    when(security.runWithSubjectOrElevate(any(Callable.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, Callable.class).call());
    when(security.runAsAdmin(any(PrivilegedAction.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, PrivilegedAction.class).run());

    scheduledReplicatorDeleter =
        new TestSheduledReplicatorDeleter(
            replicatorConfigManager,
            scheduledExecutorService,
            replicationItemManager,
            replicatorHistory,
            metacards,
            security,
            1,
            pageSize);
  }

  @Test
  public void testCleanupOrphanedItems() throws PersistenceException {
    final int pageSize = 2;
    scheduledReplicatorDeleter =
        new TestSheduledReplicatorDeleter(
            replicatorConfigManager,
            scheduledExecutorService,
            replicationItemManager,
            replicatorHistory,
            metacards,
            security,
            1,
            pageSize);

    final String configId = "configId";
    final String configName = "configName";
    ReplicatorConfig config = mockConfig(configId, configName, false, false);
    when(replicatorConfigManager.objects()).thenReturn(Stream.of(config));

    final String metacardId1 = "mId1";
    final String metacardId2 = "mId2";
    final String metacardId3 = "mId3";
    final String metacardId4 = "mId4";
    final String metacardId5 = "mId5";
    final String metacardId6 = "mId6";

    ReplicationItem rep1 = mockRepItem(metacardId1, SITE_NAME);
    ReplicationItem rep2 = mockRepItem(metacardId2, SITE_NAME);
    ReplicationItem rep3 = mockRepItem(metacardId3, SITE_NAME);
    ReplicationItem rep4 = mockRepItem(metacardId4, SITE_NAME);
    ReplicationItem rep5 = mockRepItem(metacardId5, SITE_NAME);
    ReplicationItem rep6 = mockRepItem(metacardId6, SITE_NAME);
    when(rep1.getConfigurationId()).thenReturn("noExistingConfigId");
    when(rep2.getConfigurationId()).thenReturn(configId);
    when(rep3.getConfigurationId()).thenReturn(configId);
    when(rep4.getConfigurationId()).thenReturn(configId);
    when(rep5.getConfigurationId()).thenReturn("noExistingConfigId");
    when(rep6.getConfigurationId()).thenReturn("noExistingConfigId");

    when(replicationItemManager.getItemsForConfig("", 0, pageSize))
        .thenReturn(ImmutableList.of(rep1, rep2));
    when(replicationItemManager.getItemsForConfig("", 1, pageSize))
        .thenReturn(ImmutableList.of(rep3, rep4));
    when(replicationItemManager.getItemsForConfig("", 3, pageSize))
        .thenReturn(ImmutableList.of(rep5, rep6));

    when(metacards.getIdsOfMetacardsInCatalog(ImmutableSet.of(metacardId1)))
        .thenReturn(Collections.emptySet());
    when(metacards.getIdsOfMetacardsInCatalog(ImmutableSet.of(metacardId5, metacardId6)))
        .thenReturn(Collections.singleton(metacardId5));

    scheduledReplicatorDeleter.cleanup();

    verify(replicationItemManager, times(1)).deleteItem(metacardId1, SOURCE, SITE_NAME);
    verify(replicationItemManager, never()).deleteItem(metacardId2, SOURCE, SITE_NAME);
    verify(replicationItemManager, never()).deleteItem(metacardId3, SOURCE, SITE_NAME);
    verify(replicationItemManager, never()).deleteItem(metacardId4, SOURCE, SITE_NAME);
    verify(replicationItemManager, never()).deleteItem(metacardId5, SOURCE, SITE_NAME);
    verify(replicationItemManager, times(1)).deleteItem(metacardId6, SOURCE, SITE_NAME);
  }

  @Test
  public void testOrphanedItemsToFailGetItems() throws PersistenceException {
    final String configId = "configId";
    final String configName = "configName";
    ReplicatorConfig config = mockConfig(configId, configName, true, true);

    when(replicatorConfigManager.objects()).thenReturn(Stream.of(config));

    doThrow(PersistenceException.class).when(replicationItemManager).getItemsForConfig("", 0, 1);
    verify(replicationItemManager, never()).deleteItem(anyString(), anyString(), anyString());

    scheduledReplicatorDeleter.cleanup();
  }

  @Test
  public void testCleanupDeletedConfigs() throws PersistenceException, SourceUnavailableException {
    final String deletedConfigId = "deletedConfigId";
    final String deletedConfigName = "deletedConfigName";
    final String metacardId1 = "mId1";
    final String metacardId2 = "mId2";
    final String metacardId3 = "mId3";
    ReplicatorConfig deletedConfig = mockConfig(deletedConfigId, deletedConfigName, true, true);

    final String configId = "configId";
    final String configName = "configName";
    ReplicatorConfig config = mockConfig(configId, configName, false, false);

    when(replicatorConfigManager.objects()).thenReturn(Stream.of(config, deletedConfig));

    ReplicationItem rep1 = mockRepItem(metacardId1, SITE_NAME);
    ReplicationItem rep2 = mockRepItem(metacardId2, "anotherSite");
    ReplicationItem rep3 = mockRepItem(metacardId3, SITE_NAME);

    when(replicationItemManager.getItemsForConfig(deletedConfigId, 0, pageSize))
        .thenReturn(Collections.singletonList(rep1));
    when(replicationItemManager.getItemsForConfig(deletedConfigId, 1, pageSize))
        .thenReturn(Collections.singletonList(rep2));
    when(replicationItemManager.getItemsForConfig(deletedConfigId, 2, pageSize))
        .thenReturn(Collections.singletonList(rep3));

    when(metacards.getIdsOfMetacardsInCatalog(Collections.singleton(metacardId1)))
        .thenReturn(Collections.singleton(metacardId1));
    when(metacards.getIdsOfMetacardsInCatalog(Collections.singleton(metacardId3)))
        .thenReturn(Collections.singleton(metacardId3));

    ReplicationStatus mockStatus = mockRepStatus("id");
    List<ReplicationStatus> replicationStatuses = Collections.singletonList(mockStatus);

    when(replicatorHistory.getReplicationEvents(deletedConfigName)).thenReturn(replicationStatuses);
    Set<String> eventIds =
        replicationStatuses.stream().map(ReplicationStatus::getId).collect(Collectors.toSet());

    scheduledReplicatorDeleter.cleanup();

    // deleted config
    verify(metacards, times(1)).doDelete(Collections.singleton(metacardId1).toArray(new String[0]));
    verify(metacards, times(1)).doDelete(Collections.singleton(metacardId3).toArray(new String[0]));
    verify(replicatorHistory, times(1)).getReplicationEvents(deletedConfigName);
    verify(replicatorHistory, times(1)).removeReplicationEvents(eventIds);
    verify(replicatorConfigManager, times(1)).remove(deletedConfigId);

    // not deleted config
    verify(replicationItemManager, never()).getItemsForConfig(configId, 0, pageSize);
    verify(replicatorHistory, never()).getReplicationEvents(configName);
    verify(replicationItemManager, times(1)).deleteItemsForConfig(deletedConfigId);
    verify(replicatorConfigManager, times(1)).remove(deletedConfigId);
  }

  @Test
  public void testConfigNotMarkedForDeleteData()
      throws PersistenceException, SourceUnavailableException {
    final String configId = "configId";
    final String configName = "configName";
    final String metacardId = "metacardId";
    ReplicatorConfig config = mockConfig(configId, configName, true, false);
    when(replicatorConfigManager.objects()).thenReturn(Stream.of(config));

    List<ReplicationItem> mockItems = new ArrayList<>();
    mockItems.add(mockRepItem(metacardId, SITE_NAME));
    when(replicationItemManager.getItemsForConfig(configId, 0, pageSize)).thenReturn(mockItems);

    ReplicationStatus mockStatus = mockRepStatus("id");
    List<ReplicationStatus> replicationStatuses = Collections.singletonList(mockStatus);

    when(replicatorHistory.getReplicationEvents(configName)).thenReturn(replicationStatuses);
    Set<String> eventIds =
        replicationStatuses.stream().map(ReplicationStatus::getId).collect(Collectors.toSet());

    scheduledReplicatorDeleter.cleanup();

    verify(metacards, never()).doDelete(any());
    verify(replicatorHistory, times(1)).getReplicationEvents(configName);
    verify(replicatorHistory, times(1)).removeReplicationEvents(eventIds);
    verify(replicatorConfigManager, times(1)).remove(configId);
  }

  @Test
  public void testFailToRetrieveItemsDoesntRemoveMetacardsOrHistoryOrConfig()
      throws SourceUnavailableException, PersistenceException {
    final String configId = "configId";
    ReplicatorConfig config = mockConfig(configId, "name", true, true);
    when(replicatorConfigManager.objects()).thenReturn(Stream.of(config));

    when(replicationItemManager.getItemsForConfig(configId, 0, pageSize))
        .thenThrow(PersistenceException.class);

    scheduledReplicatorDeleter.cleanup();

    verify(metacards, never()).doDelete(any());
    verify(replicationItemManager, never()).deleteItemsForConfig(configId);
    verify(replicatorHistory, never()).getReplicationEvents(configId);
    verify(replicatorHistory, never()).removeReplicationEvents(anySet());
    verify(replicatorConfigManager, never()).remove(configId);
  }

  @Test
  public void testFailDeleteMetacardsDoesntRemoveItemsOrConfig()
      throws SourceUnavailableException, PersistenceException {
    final String configId = "configId";
    ReplicatorConfig config = mockConfig(configId, "name", true, true);
    when(replicatorConfigManager.objects()).thenReturn(Stream.of(config));

    List<ReplicationItem> mockItems = new ArrayList<>();
    mockItems.add(mockRepItem("metcardId", SITE_NAME));
    when(replicationItemManager.getItemsForConfig(configId, 0, pageSize)).thenReturn(mockItems);

    doThrow(SourceUnavailableException.class).when(metacards).doDelete(any());

    scheduledReplicatorDeleter.cleanup();

    verify(replicationItemManager, never()).deleteItemsForConfig(configId);
    verify(replicatorHistory, never()).getReplicationEvents(configId);
    verify(replicatorHistory, never()).removeReplicationEvents(anySet());
    verify(replicatorConfigManager, never()).remove(configId);
  }

  @Test
  public void testFailCleanupHistoryDoesntRemoveConfig()
      throws PersistenceException, SourceUnavailableException {
    final String configId = "configId";
    final String configName = "configName";
    final String metacardId1 = "mId1";
    ReplicatorConfig config = mockConfig(configId, configName, true, true);
    when(replicatorConfigManager.objects()).thenReturn(Stream.of(config));

    List<ReplicationItem> mockItems = new ArrayList<>();
    mockItems.add(mockRepItem(metacardId1, SITE_NAME));
    when(replicationItemManager.getItemsForConfig(configId, 0, pageSize)).thenReturn(mockItems);

    Set<String> itemMetacardIds = Collections.singleton(metacardId1);
    Set<String> idsInCatalog = Collections.singleton(metacardId1);
    when(metacards.getIdsOfMetacardsInCatalog(itemMetacardIds)).thenReturn(idsInCatalog);

    ReplicationStatus mockStatus = mockRepStatus("id");
    List<ReplicationStatus> replicationStatuses = Collections.singletonList(mockStatus);

    when(replicatorHistory.getReplicationEvents(configName)).thenReturn(replicationStatuses);
    Set<String> eventIds =
        replicationStatuses.stream().map(ReplicationStatus::getId).collect(Collectors.toSet());
    doThrow(ReplicationPersistenceException.class)
        .when(replicatorHistory)
        .removeReplicationEvents(eventIds);

    scheduledReplicatorDeleter.cleanup();

    verify(metacards, times(1)).doDelete(idsInCatalog.toArray(new String[0]));
    verify(replicatorHistory, times(1)).getReplicationEvents(configName);
    verify(replicatorHistory, times(1)).removeReplicationEvents(eventIds);
    verify(replicatorConfigManager, never()).remove(configId);
  }

  private ReplicatorConfig mockConfig(
      String id, String name, boolean isDeleted, boolean deleteData) {
    ReplicatorConfig config = mock(ReplicatorConfig.class);
    when(config.getId()).thenReturn(id);
    when(config.getName()).thenReturn(name);
    when(config.isDeleted()).thenReturn(isDeleted);
    when(config.shouldDeleteData()).thenReturn(deleteData);
    return config;
  }

  private ReplicationStatus mockRepStatus(String id) {
    ReplicationStatus status = mock(ReplicationStatus.class);
    when(status.getId()).thenReturn(id);
    return status;
  }

  private ReplicationItem mockRepItem(String metacardId, String destination) {
    ReplicationItem item = mock(ReplicationItem.class);
    when(item.getMetacardId()).thenReturn(metacardId);
    when(item.getDestination()).thenReturn(destination);
    when(item.getSource()).thenReturn(SOURCE);
    return item;
  }

  /**
   * {@link ScheduledReplicatorDeleter} extension solely for overriding a static method call to
   * {@link SystemInfo#getSiteName()}
   */
  private class TestSheduledReplicatorDeleter extends ScheduledReplicatorDeleter {

    public TestSheduledReplicatorDeleter(
        ReplicatorConfigManager replicatorConfigManager,
        ScheduledExecutorService scheduledExecutorService,
        ReplicationItemManager replicationItemManager,
        ReplicatorHistory replicatorHistory,
        Metacards metacards,
        Security security,
        long pollPeriod,
        int pageSize) {
      super(
          replicatorConfigManager,
          scheduledExecutorService,
          replicationItemManager,
          replicatorHistory,
          metacards,
          security,
          pollPeriod,
          pageSize);
    }

    @Override
    String getSiteName() {
      return SITE_NAME;
    }
  }
}
