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
package com.connexta.ion.replication.api.impl.data;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ReplicationSiteImplTest {
  private static final String TEST_NAME = "testName";

  private static final String TEST_URL = "https://host:44";

  private ReplicationSiteImpl site;

  @Before
  public void setup() {
    site = new ReplicationSiteImpl();
  }

  @Test
  public void testSetGetName() {
    site.setName(TEST_NAME);
    assertThat(site.getName(), equalTo(TEST_NAME));
  }

  @Test
  public void testSetGetUrl() {
    site.setUrl(TEST_URL);
    assertThat(site.getUrl(), equalTo(TEST_URL));
  }
}
