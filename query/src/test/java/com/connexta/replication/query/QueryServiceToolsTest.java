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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.data.Filter;
import com.connexta.replication.api.data.FilterIndex;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.persistence.FilterIndexManager;
import com.connexta.replication.api.persistence.FilterManager;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.QueueBroker;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryServiceToolsTest {

  @Mock private NodeAdapters nodeAdapterFactories;

  @Mock private SiteManager siteManager;

  @Mock private FilterManager filterManager;

  @Mock private FilterIndexManager filterIndexManager;

  @Mock private QueueBroker queueBroker;

  private long period = 60L;

  private QueryServiceTools queryServiceTools;

  @Before
  public void setUp() throws Exception {
    queryServiceTools =
        new QueryServiceTools(
            nodeAdapterFactories,
            siteManager,
            filterManager,
            filterIndexManager,
            queueBroker,
            period);
  }

  @Test
  public void getPeriod() {
    assertThat(queryServiceTools.getGlobalPeriod(), Matchers.is(period));
  }

  @Test
  public void constructorSetsDefaultPeriod() {
    queryServiceTools =
        new QueryServiceTools(
            nodeAdapterFactories, siteManager, filterManager, filterIndexManager, queueBroker, 0);
    assertThat(queryServiceTools.getGlobalPeriod(), Matchers.is(TimeUnit.MINUTES.toSeconds(5)));
  }

  @Test
  public void allSites() {
    queryServiceTools.sites();
    verify(siteManager).objects();
  }

  @Test
  public void getAdapterFor() {
    Site site = mock(Site.class);
    when(site.getType()).thenReturn(SiteType.DDF);
    NodeAdapterFactory nodeAdapterFactory = mock(NodeAdapterFactory.class);
    when(nodeAdapterFactories.factoryFor(any(SiteType.class))).thenReturn(nodeAdapterFactory);
    queryServiceTools.getAdapterFor(site);
    verify(nodeAdapterFactory).create(null);
  }

  @Test
  public void activeFiltersFor() {
    Filter filter = mock(Filter.class);
    Site site = mock(Site.class);
    when(filterManager.filtersForSite(null)).thenReturn(Stream.of(filter));
    assertThat(
        queryServiceTools.activeFiltersFor(site).collect(Collectors.toSet()),
        Matchers.contains(filter));
  }

  @Test
  public void getOrCreateFilterIndex() {
    Filter filter = mock(Filter.class);
    queryServiceTools.getOrCreateFilterIndex(filter);
    verify(filterIndexManager).getOrCreate(filter);
  }

  @Test
  public void saveFilterIndex() {
    FilterIndex filterIndex = mock(FilterIndex.class);
    queryServiceTools.saveFilterIndex(filterIndex);
    verify(filterIndexManager).save(filterIndex);
  }

  @Test
  public void getQueueFor() {
    Site site = mock(Site.class);
    when(site.getId()).thenReturn("siteid");
    queryServiceTools.getQueueFor(site);
    verify(queueBroker).getQueue("siteid");
  }
}
