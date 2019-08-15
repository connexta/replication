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
package com.connexta.replication.api.impl.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.impl.persistence.spring.SiteRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SiteManagerImplTest {

  private SiteManagerImpl store;

  @Mock SiteRepository siteRepository;

  @Before
  public void setUp() {
    System.setProperty("org.codice.ddf.system.siteName", "testSite");
    store = new SiteManagerImpl(siteRepository);
  }

  // BOB: fix
  //  @Test
  //  public void getSites() {
  //    SiteImpl site = new SiteImpl();
  //    when(siteRepository.findAll()).thenReturn(Collections.singletonList(site));
  //
  //    assertThat(store.objects().anyMatch(site::equals), is(true));
  //  }

  @Test
  public void createSite() {
    Site site = store.create("name", "url");
    assertThat(site.getName(), is("name"));
    assertThat(site.getUrl(), is("url"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveSiteBadSite() {
    Site site = mock(Site.class);
    store.save(site);
  }

  @Test
  public void deleteSite() {
    store.remove("id");
    verify(siteRepository).deleteById(eq("id"));
  }
}
