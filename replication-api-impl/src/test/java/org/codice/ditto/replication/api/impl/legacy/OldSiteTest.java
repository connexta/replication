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
package org.codice.ditto.replication.api.impl.legacy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.junit.Before;
import org.junit.Test;

public class OldSiteTest {
  private static final String ID = "id";

  private static final String NAME = "name";

  private static final String URL = "url";

  private static final String VERSION = "version";

  private static final String TEST_ID = "testId";

  private static final String TEST_NAME = "testName";

  private static final String TEST_URL = "https://host:44";

  private OldSite site;

  @Before
  public void setup() {
    site = new OldSite();
  }

  @Test
  public void testReadFromMap() {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, TEST_ID);
    map.put(NAME, TEST_NAME);
    map.put(URL, TEST_URL);
    map.put(VERSION, -9999);

    site.fromMap(map);

    assertThat(site.getId(), equalTo(TEST_ID));
    assertThat(site.getName(), equalTo(TEST_NAME));
    assertThat(site.getUrl(), equalTo(TEST_URL + "/services"));
    assertThat(site.isRemoteManaged(), equalTo(false));
    assertThat(site.getVerifiedUrl(), equalTo(TEST_URL + "/services"));
    assertThat(site.getVersion(), equalTo(ReplicationSiteImpl.CURRENT_VERSION));
  }
}
