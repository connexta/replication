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

import com.connexta.ion.replication.api.ReplicationPersistenceException;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SiteImplTest {
  private static final boolean REMOTE_MANAGED = true;
  private static final String ID = "id";
  private static final String NAME = "testName";
  private static final String URL = "https://host:44";
  private static final String TYPE = "some-type";

  @Rule public ExpectedException exception = ExpectedException.none();

  private final SiteImpl persistable = new SiteImpl();

  public SiteImplTest() {
    persistable.setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    persistable.setName(SiteImplTest.NAME);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);
  }

  @Test
  public void testDefaultCtor() throws Exception {
    final SiteImpl persistable = new SiteImpl();

    Assert.assertThat(persistable.getId(), Matchers.not(Matchers.emptyOrNullString()));
    Assert.assertThat(persistable.getName(), Matchers.nullValue());
    Assert.assertThat(persistable.getUrl(), Matchers.nullValue());
    Assert.assertThat(persistable.getType(), Matchers.nullValue());
    Assert.assertThat(persistable.isRemoteManaged(), Matchers.equalTo(false));
  }

  @Test
  public void testCtorWithPojo() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setRemoteManaged(SiteImplTest.REMOTE_MANAGED)
            .setName(SiteImplTest.NAME)
            .setUrl(SiteImplTest.URL)
            .setType(SiteImplTest.TYPE);

    final SiteImpl persistable = new SiteImpl(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getUrl(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.isRemoteManaged(), Matchers.equalTo(pojo.isRemoteManaged()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(pojo.getType()));
  }

  @Test
  public void testGetName() throws Exception {
    Assert.assertThat(persistable.getName(), Matchers.equalTo(SiteImplTest.NAME));
  }

  @Test
  public void testGetUrl() throws Exception {
    Assert.assertThat(persistable.getUrl(), Matchers.equalTo(SiteImplTest.URL));
  }

  @Test
  public void testSetUrl() throws Exception {
    final String url = "new-url";

    persistable.setUrl(url);

    Assert.assertThat(persistable.getUrl(), Matchers.equalTo(url));
  }

  @Test
  public void testIsRemoteManaged() throws Exception {
    Assert.assertThat(persistable.isRemoteManaged(), Matchers.equalTo(SiteImplTest.REMOTE_MANAGED));
  }

  @Test
  public void testGetType() throws Exception {
    Assert.assertThat(persistable.getType(), Matchers.equalTo(SiteImplTest.TYPE));
  }

  @Test
  public void testSetType() throws Exception {
    final String type = "new-type";

    persistable.setType(type);

    Assert.assertThat(persistable.getType(), Matchers.equalTo(type));
  }

  @Test
  public void testWriteTo() throws Exception {
    final SitePojo pojo = new SitePojo();

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(SitePojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getName(), Matchers.equalTo(persistable.getName()));
    Assert.assertThat(pojo.getUrl(), Matchers.equalTo(persistable.getUrl()));
    Assert.assertThat(pojo.isRemoteManaged(), Matchers.equalTo(persistable.isRemoteManaged()));
    Assert.assertThat(pojo.getType(), Matchers.equalTo(persistable.getType()));
  }

  @Test
  public void testWriteToWithNameIsNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*name.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWithNameIsEmpty() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*name.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    persistable.setName("");
    persistable.setUrl(SiteImplTest.URL);
    persistable.setType(SiteImplTest.TYPE);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWithUrlIsNull() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*url.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    persistable.setName(SiteImplTest.NAME);
    persistable.setType(SiteImplTest.TYPE);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWithUrlIsEmpty() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*url.*"));

    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    persistable.setName(SiteImplTest.NAME);
    persistable.setUrl("");
    persistable.setType(SiteImplTest.TYPE);

    persistable.writeTo(pojo);
  }

  @Test
  public void testWriteToWithTypeIsNull() throws Exception {
    final SiteImpl persistable = new SiteImpl();
    final SitePojo pojo = new SitePojo();

    persistable.setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    persistable.setName(SiteImplTest.NAME);
    persistable.setUrl(SiteImplTest.URL);

    persistable.writeTo(pojo);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(persistable.getId()));
    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(SitePojo.CURRENT_VERSION));
    Assert.assertThat(pojo.getName(), Matchers.equalTo(persistable.getName()));
    Assert.assertThat(pojo.getUrl(), Matchers.equalTo(persistable.getUrl()));
    Assert.assertThat(pojo.isRemoteManaged(), Matchers.equalTo(persistable.isRemoteManaged()));
    Assert.assertThat(pojo.getType(), Matchers.nullValue());
  }

  @Test
  public void testReadFromCurrentVersion() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setUrl(SiteImplTest.URL)
            .setType(SiteImplTest.TYPE)
            .setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getUrl(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.isRemoteManaged(), Matchers.equalTo(pojo.isRemoteManaged()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(pojo.getType()));
  }

  @Test
  public void testReadFromUnsupportedVersion() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*unsupported.*version.*"));

    final SitePojo pojo = new SitePojo().setVersion(-1).setId(SiteImplTest.ID);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromWithNullName() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*name.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setUrl(SiteImplTest.URL)
            .setType(SiteImplTest.TYPE)
            .setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromWithEmptyName() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*name.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName("")
            .setUrl(SiteImplTest.URL)
            .setType(SiteImplTest.TYPE)
            .setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromWithNullUrl() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*missing.*url.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setType(SiteImplTest.TYPE)
            .setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromWithEmptylUrl() throws Exception {
    exception.expect(ReplicationPersistenceException.class);
    exception.expectMessage(Matchers.matchesPattern(".*empty.*url.*"));

    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setUrl("")
            .setType(SiteImplTest.TYPE)
            .setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);
  }

  @Test
  public void testReadFromWithNullType() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setId(SiteImplTest.ID)
            .setName(SiteImplTest.NAME)
            .setUrl(SiteImplTest.URL)
            .setType(null)
            .setRemoteManaged(SiteImplTest.REMOTE_MANAGED);
    final SiteImpl persistable = new SiteImpl();

    persistable.readFrom(pojo);

    Assert.assertThat(persistable.getId(), Matchers.equalTo(pojo.getId()));
    Assert.assertThat(persistable.getName(), Matchers.equalTo(pojo.getName()));
    Assert.assertThat(persistable.getUrl(), Matchers.equalTo(pojo.getUrl()));
    Assert.assertThat(persistable.isRemoteManaged(), Matchers.equalTo(pojo.isRemoteManaged()));
    Assert.assertThat(persistable.getType(), Matchers.equalTo(pojo.getType()));
  }
}
