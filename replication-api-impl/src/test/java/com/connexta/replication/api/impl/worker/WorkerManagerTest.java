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
package com.connexta.replication.api.impl.worker;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.spring.ReplicationProperties;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkerManagerTest {

  private static final String SITE1_ID = "site1";
  private static final int SITE1_PARALLELISM = 2;
  private Site site1;

  private static final String SITE2_ID = "site2";
  private static final int SITE2_PARALLELISM = 3;
  private Site site2;

  private static final String SITE3_ID = "site3";
  private static final int SITE3_PARALLELISM = 4;
  private Site site3;

  private static final String LOCAL_SITE = "localSite";
  private static final int LOCAL_PARALLELISM = 1;
  private Site localSite;

  private SiteManager siteManager;

  private WorkerManager workerManager;

  private WorkerThreadPoolFactory threadPoolFactory;

  private ReplicationProperties properties;

  @BeforeEach
  void setup() {
    site1 = mockSite(SITE1_ID, SITE1_PARALLELISM, SiteType.DDF, SiteKind.TACTICAL);
    site2 = mockSite(SITE2_ID, SITE2_PARALLELISM, SiteType.DDF, SiteKind.TACTICAL);
    site3 = mockSite(SITE3_ID, SITE3_PARALLELISM, SiteType.DDF, SiteKind.TACTICAL);
    localSite = mockSite(LOCAL_SITE, LOCAL_PARALLELISM, SiteType.DDF, SiteKind.TACTICAL);
    when(localSite.getName()).thenReturn(LOCAL_SITE);

    siteManager = mock(SiteManager.class);
    when(siteManager.get(LOCAL_SITE)).thenReturn(localSite);
    when(siteManager.objects()).thenReturn(Stream.of(site1, site2, site3, localSite));

    properties = mock(ReplicationProperties.class);
    when(properties.getSites()).thenReturn(Set.of(SITE1_ID, SITE2_ID, SITE3_ID));
    when(properties.getLocalSite()).thenReturn(LOCAL_SITE);

    threadPoolFactory = mock(WorkerThreadPoolFactory.class);

    workerManager = new WorkerManager(siteManager, properties, threadPoolFactory);
  }

  @Test
  void testNoSitesPropertyMonitorsAllSites() {
    // setup
    when(properties.getSites()).thenReturn(Set.of());
    when(threadPoolFactory.create(anyString(), anyInt())).thenReturn(mock(WorkerThreadPool.class));
    workerManager = new WorkerManager(siteManager, properties, threadPoolFactory);

    // when
    workerManager.monitorSites();

    // then
    verify(threadPoolFactory).create(SITE1_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory).create(SITE2_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory).create(SITE3_ID, LOCAL_PARALLELISM);
  }

  @Test
  void testOnlyMonitorConfiguredSites() {
    // setup
    when(properties.getSites()).thenReturn(Set.of(SITE1_ID, SITE3_ID));
    when(siteManager.objects()).thenReturn(Stream.of(site1, site2, site3));
    when(threadPoolFactory.create(anyString(), anyInt())).thenReturn(mock(WorkerThreadPool.class));
    workerManager = new WorkerManager(siteManager, properties, threadPoolFactory);

    // when
    workerManager.monitorSites();

    // then
    verify(threadPoolFactory).create(SITE1_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory, never()).create(SITE2_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory).create(SITE3_ID, LOCAL_PARALLELISM);
  }

  @Test
  void testUnknownSiteTypesAreNotMonitored() {
    // setup
    site1 = mockSite(SITE1_ID, SITE1_PARALLELISM, SiteType.UNKNOWN, SiteKind.TACTICAL);
    when(siteManager.objects()).thenReturn(Stream.of(site1, site2, site3));

    when(threadPoolFactory.create(anyString(), anyInt())).thenReturn(mock(WorkerThreadPool.class));
    workerManager = new WorkerManager(siteManager, properties, threadPoolFactory);

    // when
    workerManager.monitorSites();

    // then
    verify(threadPoolFactory, never()).create(SITE1_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory).create(SITE2_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory).create(SITE3_ID, LOCAL_PARALLELISM);
  }

  @Test
  void testUnknownSiteKindsAreNotMonitored() {
    // setup
    site1 = mockSite(SITE1_ID, SITE1_PARALLELISM, SiteType.DDF, SiteKind.UNKNOWN);
    when(siteManager.objects()).thenReturn(Stream.of(site1, site2, site3));

    when(threadPoolFactory.create(anyString(), anyInt())).thenReturn(mock(WorkerThreadPool.class));
    workerManager = new WorkerManager(siteManager, properties, threadPoolFactory);

    // when
    workerManager.monitorSites();

    // then
    verify(threadPoolFactory, never()).create(SITE1_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory).create(SITE2_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory).create(SITE3_ID, LOCAL_PARALLELISM);
  }

  @Test
  void testSiteWasMonitoredThenDeletedFromStorage() {
    // setup
    when(properties.getSites()).thenReturn(Set.of());
    WorkerThreadPool pool1 = mock(WorkerThreadPool.class);
    when(pool1.getSize()).thenReturn(LOCAL_PARALLELISM);
    WorkerThreadPool pool2 = mock(WorkerThreadPool.class);
    when(pool2.getSize()).thenReturn(LOCAL_PARALLELISM);

    when(threadPoolFactory.create(anyString(), anyInt())).thenReturn(pool1).thenReturn(pool2);

    when(siteManager.objects()).thenReturn(Stream.of(site1, site2));

    // when
    workerManager.monitorSites();

    // then
    verify(threadPoolFactory).create(SITE1_ID, LOCAL_PARALLELISM);
    verify(threadPoolFactory).create(SITE2_ID, LOCAL_PARALLELISM);

    // when
    when(siteManager.objects()).thenReturn(Stream.of(site1));
    workerManager.monitorSites();

    // then
    verify(pool2).shutdown();
    verify(pool1, never()).shutdown();
  }

  @Test
  void testLocalHasMinParallelismFactor() {
    // setup
    when(siteManager.objects()).thenReturn(Stream.of(site1));

    when(threadPoolFactory.create(anyString(), anyInt())).thenReturn(mock(WorkerThreadPool.class));
    workerManager = new WorkerManager(siteManager, properties, threadPoolFactory);

    // when
    workerManager.monitorSites();

    // then
    verify(threadPoolFactory).create(SITE1_ID, LOCAL_PARALLELISM);
  }

  @Test
  void testRemoteHasMinParallelismFactor() {
    // setup
    site1 = mockSite(SITE1_ID, 5, SiteType.DDF, SiteKind.TACTICAL);
    localSite = mockSite(LOCAL_SITE, 10, SiteType.DDF, SiteKind.TACTICAL);
    when(siteManager.get(LOCAL_SITE)).thenReturn(localSite);
    when(siteManager.objects()).thenReturn(Stream.of(site1));

    when(threadPoolFactory.create(anyString(), anyInt())).thenReturn(mock(WorkerThreadPool.class));
    workerManager = new WorkerManager(siteManager, properties, threadPoolFactory);

    // when
    workerManager.monitorSites();

    // then
    verify(threadPoolFactory).create(SITE1_ID, 5);
  }

  @Test
  void testLocalSiteIsNotMonitored() {
    // setup
    when(properties.getSites()).thenReturn(Set.of());
    when(threadPoolFactory.create(anyString(), anyInt())).thenReturn(mock(WorkerThreadPool.class));

    // when
    workerManager.monitorSites();

    // then
    verify(threadPoolFactory, never()).create(LOCAL_SITE, LOCAL_PARALLELISM);
  }

  @Test
  void testDestroy() {
    // setup
    when(properties.getSites()).thenReturn(Set.of());
    WorkerThreadPool pool1 = mock(WorkerThreadPool.class);
    WorkerThreadPool pool2 = mock(WorkerThreadPool.class);
    WorkerThreadPool pool3 = mock(WorkerThreadPool.class);

    when(threadPoolFactory.create(anyString(), anyInt()))
        .thenReturn(pool1)
        .thenReturn(pool2)
        .thenReturn(pool3);

    // when
    workerManager.monitorSites();
    workerManager.destroy();

    // then
    verify(pool1).shutdown();
    verify(pool2).shutdown();
    verify(pool3).shutdown();
  }

  @Test
  void testNoLocalSiteConfigured() {
    // given
    when(siteManager.get(LOCAL_SITE)).thenThrow(NotFoundException.class);

    // expect
    Assertions.assertThrows(
        NotFoundException.class,
        () -> new WorkerManager(siteManager, properties, threadPoolFactory));
  }

  private Site mockSite(
      String siteId, int parallelismFactor, SiteType siteType, SiteKind siteKind) {
    Site site = mock(Site.class);
    when(site.getId()).thenReturn(siteId);
    when(site.getParallelismFactor()).thenReturn(OptionalInt.of(parallelismFactor));
    when(site.getType()).thenReturn(siteType);
    when(site.getKind()).thenReturn(siteKind);
    return site;
  }
}
