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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.junit.rules.RestoreSystemProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SiteManagerImplTest {

  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  private static final String LOCAL_SITE_ID = "local-site-id-1234567890";

  private SiteManagerImpl siteManager;

  @Mock ReplicationPersistentStore persistentStore;

  @Before
  public void setUp() {
    System.setProperty("org.codice.ddf.system.siteName", "testSite");
    siteManager = new SiteManagerImpl(persistentStore);
  }

  @Test
  public void init() {
    when(persistentStore.get(any(Class.class), anyString())).thenThrow(new NotFoundException());
    siteManager.init();
    ArgumentCaptor<ReplicationSiteImpl> captor = ArgumentCaptor.forClass(ReplicationSiteImpl.class);
    verify(persistentStore).save(captor.capture());
    ReplicationSite site = captor.getValue();
    assertThat(site.getName(), is(SystemInfo.getSiteName()));
    assertThat(site.getUrl(), is(SystemBaseUrl.EXTERNAL.constructUrl(null, true)));
  }

  @Test
  public void initFromConfig() {
    System.setProperty("karaf.home", "src/test/resources");
    when(persistentStore.get(any(Class.class), anyString())).thenThrow(new NotFoundException());
    when(persistentStore.objects(any(Class.class))).thenReturn(Stream.empty());
    siteManager.init();
    ArgumentCaptor<ReplicationSiteImpl> captor = ArgumentCaptor.forClass(ReplicationSiteImpl.class);
    verify(persistentStore, times(3)).save(captor.capture());
    Set<String> ids =
        captor.getAllValues().stream().map(ReplicationSite::getId).collect(Collectors.toSet());
    assertThat(ids.contains("source-1"), is(true));
    assertThat(ids.contains("destination-1"), is(true));
  }

  @Test
  public void initBadConfigPath() {
    System.setProperty("karaf.home", "not/the/right/path");
    when(persistentStore.get(any(Class.class), anyString())).thenThrow(new NotFoundException());
    when(persistentStore.objects(any(Class.class))).thenReturn(Stream.empty());
    siteManager.init();
    verify(persistentStore, times(1)).save(any());
  }

  @Test
  public void initUpdateName() {
    ReplicationSiteImpl orig = new ReplicationSiteImpl();
    orig.setId(LOCAL_SITE_ID);
    orig.setName("oldName");
    orig.setUrl(SystemBaseUrl.EXTERNAL.getBaseUrl());
    when(persistentStore.get(eq(ReplicationSiteImpl.class), anyString())).thenReturn(orig);
    siteManager.init();
    ArgumentCaptor<ReplicationSiteImpl> captor = ArgumentCaptor.forClass(ReplicationSiteImpl.class);
    verify(persistentStore).save(captor.capture());
    ReplicationSiteImpl site = captor.getValue();
    assertThat(site.getName(), is(SystemInfo.getSiteName()));
    assertThat(site.getUrl(), is(SystemBaseUrl.EXTERNAL.constructUrl(null, true)));
  }

  @Test
  public void initUpdateURL() {
    ReplicationSiteImpl orig = new ReplicationSiteImpl();
    orig.setId(LOCAL_SITE_ID);
    orig.setName(SystemInfo.getSiteName());
    orig.setUrl("https://asdf:1234");
    when(persistentStore.get(eq(ReplicationSiteImpl.class), anyString())).thenReturn(orig);
    siteManager.init();
    ArgumentCaptor<ReplicationSiteImpl> captor = ArgumentCaptor.forClass(ReplicationSiteImpl.class);
    verify(persistentStore).save(captor.capture());
    ReplicationSiteImpl site = captor.getValue();
    assertThat(site.getName(), is(SystemInfo.getSiteName()));
    assertThat(site.getUrl(), is(SystemBaseUrl.EXTERNAL.constructUrl(null, true)));
  }

  @Test
  public void initNoOp() {
    ReplicationSiteImpl orig = new ReplicationSiteImpl();
    orig.setId(LOCAL_SITE_ID);
    orig.setName(SystemInfo.getSiteName());
    orig.setUrl(SystemBaseUrl.EXTERNAL.constructUrl(null, true));
    when(persistentStore.get(eq(ReplicationSiteImpl.class), anyString())).thenReturn(orig);
    siteManager.init();
    verify(persistentStore, never()).save(any(ReplicationSiteImpl.class));
  }

  @Test
  public void getSites() {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    Stream<ReplicationSiteImpl> siteStream = Stream.of(site);
    when(persistentStore.objects(eq(ReplicationSiteImpl.class))).thenReturn(siteStream);

    assertThat(siteManager.objects().anyMatch(site::equals), is(true));
  }

  @Test
  public void createSite() {
    ReplicationSite site = siteManager.createSite("name", "url");
    assertThat(site.getName(), is("name"));
    assertThat(site.getUrl(), is("url"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveSiteBadSite() {
    ReplicationSite site = mock(ReplicationSite.class);
    siteManager.save(site);
  }

  @Test
  public void deleteSite() {
    siteManager.remove("id");
    verify(persistentStore).delete(eq(ReplicationSiteImpl.class), eq("id"));
  }

  @Test
  public void testExistsNotFound() {
    when(persistentStore.get(ReplicationSiteImpl.class, "id")).thenThrow(NotFoundException.class);
    assertThat(siteManager.exists("id"), is(false));
  }

  @Test
  public void testExistsConfigFound() {
    assertThat(siteManager.exists("id"), is(true));
  }
}
