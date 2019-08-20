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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class SiteImplTest {
  private static final boolean REMOTE_MANAGED = true;
  private static final String NAME = "testName";
  private static final String URL = "https://host:44";
  private static final String TYPE = "some-type";

  private SiteImpl site = new SiteImpl();

  public SiteImplTest() {
    site.setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    site.setName(SiteImplTest.NAME);
    site.setUrl(SiteImplTest.URL);
    site.setType(SiteImplTest.TYPE);
  }

  @Test
  public void testGetName() throws Exception {
    Assert.assertThat(site.getName(), Matchers.equalTo(SiteImplTest.NAME));
  }

  @Test
  public void testGetUrl() throws Exception {
    Assert.assertThat(site.getUrl(), Matchers.equalTo(SiteImplTest.URL));
  }

  @Test
  public void testSetUrl() throws Exception {
    final String url = "new-url";

    site.setUrl(url);

    Assert.assertThat(site.getUrl(), Matchers.equalTo(url));
  }

  @Test
  public void testIsRemoteManaged() throws Exception {
    Assert.assertThat(site.isRemoteManaged(), Matchers.equalTo(SiteImplTest.REMOTE_MANAGED));
  }

  @Test
  public void testGetType() throws Exception {
    Assert.assertThat(site.getType(), Matchers.equalTo(SiteImplTest.TYPE));
  }

  @Test
  public void testSetType() throws Exception {
    final String type = "new-type";

    site.setType(type);

    Assert.assertThat(site.getType(), Matchers.equalTo(type));
  }
}
