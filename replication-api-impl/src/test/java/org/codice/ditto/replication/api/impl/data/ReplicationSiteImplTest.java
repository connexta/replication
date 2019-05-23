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
package org.codice.ditto.replication.api.impl.data;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ReplicationSiteImplTest {
  private static final String ID = "id";

  private static final String NAME = "name";

  private static final String URL = "url";

  private static final String VERIFIED_URL = "verified-url";

  private static final String IS_REMOTE_MANAGED = "is-remote-managed";

  private static final String VERSION = "version";

  private static final String TEST_ID = "testId";

  private static final String TEST_NAME = "testName";

  private static final String TEST_URL = "https://host:44/services";

  private static final String TEST_VERIFIED_URL = "https://host2:44/services";

  private static final String V1_URL = "https://host3:33";

  private static final String REMOTE_MANAGED = "false";

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
    site.setVerifiedUrl(TEST_VERIFIED_URL);
    site.setUrl(TEST_URL);
    assertThat(site.getUrl(), equalTo(TEST_URL));
    assertThat(site.getVerifiedUrl(), nullValue());
  }

  @Test
  public void testSetGetVerifiedUrl() {
    site.setVerifiedUrl(TEST_VERIFIED_URL);
    assertThat(site.getVerifiedUrl(), equalTo(TEST_VERIFIED_URL));
  }

  @Test
  public void testWriteToMap() {
    loadSite(site);
    Map<String, Object> map = site.toMap();

    assertThat(map.get(ID), equalTo(TEST_ID));
    assertThat(map.get(NAME), equalTo(TEST_NAME));
    assertThat(map.get(URL), equalTo(TEST_URL));
    assertThat(map.get(VERIFIED_URL), equalTo(TEST_VERIFIED_URL));
    assertThat(map.get(VERSION), equalTo(ReplicationSiteImpl.CURRENT_VERSION));
  }

  @Test
  public void testWriteToMapNoVerifiedUrl() {
    loadSite(site);
    site.setVerifiedUrl(null);
    Map<String, Object> map = site.toMap();

    assertThat(map.get(ID), equalTo(TEST_ID));
    assertThat(map.get(NAME), equalTo(TEST_NAME));
    assertThat(map.get(URL), equalTo(TEST_URL));
    assertThat(map.get(VERIFIED_URL), nullValue());
    assertThat(map.get(VERSION), equalTo(ReplicationSiteImpl.CURRENT_VERSION));
  }

  @Test
  public void testReadFromMap() {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, TEST_ID);
    map.put(NAME, TEST_NAME);
    map.put(URL, TEST_URL);
    map.put(VERIFIED_URL, TEST_VERIFIED_URL);
    map.put(IS_REMOTE_MANAGED, REMOTE_MANAGED);
    map.put(VERSION, ReplicationSiteImpl.CURRENT_VERSION);

    site.fromMap(map);

    assertThat(site.getId(), equalTo(TEST_ID));
    assertThat(site.getName(), equalTo(TEST_NAME));
    assertThat(site.getUrl(), equalTo(TEST_URL));
    assertThat(site.isRemoteManaged(), is(false));
    assertThat(site.getVerifiedUrl(), equalTo(TEST_VERIFIED_URL));
    assertThat(site.getVersion(), equalTo(ReplicationSiteImpl.CURRENT_VERSION));
  }

  @Test
  public void testReadFromMapWhenNotVerified() {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, TEST_ID);
    map.put(NAME, TEST_NAME);
    map.put(URL, TEST_URL);
    map.put(VERIFIED_URL, null);
    map.put(VERSION, ReplicationSiteImpl.CURRENT_VERSION);

    site.fromMap(map);

    assertThat(site.getId(), equalTo(TEST_ID));
    assertThat(site.getName(), equalTo(TEST_NAME));
    assertThat(site.getUrl(), equalTo(TEST_URL));
    assertThat(site.getVerifiedUrl(), nullValue());
    assertThat(site.getVersion(), equalTo(ReplicationSiteImpl.CURRENT_VERSION));
  }

  @Test
  public void testReadFromMapWhenNotVerifiedAndMapDoesNotHaveVerifiedUrl() {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, TEST_ID);
    map.put(NAME, TEST_NAME);
    map.put(URL, TEST_URL);
    map.put(VERSION, ReplicationSiteImpl.CURRENT_VERSION);

    site.fromMap(map);

    assertThat(site.getId(), equalTo(TEST_ID));
    assertThat(site.getName(), equalTo(TEST_NAME));
    assertThat(site.getUrl(), equalTo(TEST_URL));
    assertThat(site.getVerifiedUrl(), nullValue());
    assertThat(site.getVersion(), equalTo(ReplicationSiteImpl.CURRENT_VERSION));
  }

  @Test
  public void testReadVersionOne() {
    Map<String, Object> map = new HashMap<>();
    map.put(ID, TEST_ID);
    map.put(NAME, TEST_NAME);
    map.put(URL, V1_URL);
    map.put(VERSION, 1);

    site.fromMap(map);

    assertThat(site.getId(), is(TEST_ID));
    assertThat(site.getName(), is(TEST_NAME));
    assertThat(site.getUrl(), is(V1_URL + "/services"));
    assertThat(site.getVerifiedUrl(), is(V1_URL + "/services"));
    assertThat(site.isRemoteManaged(), is(false));
    assertThat(site.getVersion(), equalTo(ReplicationSiteImpl.CURRENT_VERSION));
  }

  @Test(expected = IllegalStateException.class)
  public void testReadUnknownVersion() {
    Map<String, Object> map = new HashMap<>();
    map.put(VERSION, -1);

    site.fromMap(map);
  }

  private ReplicationSiteImpl loadSite(ReplicationSiteImpl site) {
    site.setId(TEST_ID);
    site.setName(TEST_NAME);
    site.setUrl(TEST_URL);
    site.setVerifiedUrl(TEST_VERIFIED_URL);
    return site;
  }
}
