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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SiteManagerImplTest {

  SiteManagerImpl store;

  @Mock ReplicationPersistentStore persistentStore;

  @Before
  public void setUp() {
    System.setProperty("org.codice.ddf.system.siteName", "testSite");
    store = new SiteManagerImpl(persistentStore);
  }

  @Test
  public void getSites() {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    Stream<ReplicationSiteImpl> siteStream = Stream.of(site);
    when(persistentStore.objects(eq(ReplicationSiteImpl.class))).thenReturn(siteStream);

    assertThat(store.objects().anyMatch(site::equals), is(true));
  }

  @Test
  public void createSite() {
    ReplicationSite site = store.createSite("name", "url");
    assertThat(site.getName(), is("name"));
    assertThat(site.getUrl(), is("url"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveSiteBadSite() {
    ReplicationSite site = mock(ReplicationSite.class);
    store.save(site);
  }

  @Test
  public void deleteSite() {
    store.remove("id");
    verify(persistentStore).delete(eq(ReplicationSiteImpl.class), eq("id"));
  }
}
