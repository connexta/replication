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
package com.connexta.replication.api.impl.persistence.pojo;

import com.connexta.ion.replication.api.NodeAdapterType;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class SitePojoTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final boolean REMOTE_MANAGED = true;
  private static final String NAME = "site.name";
  private static final String URL = "http://localhost/service";
  private static final String TYPE = NodeAdapterType.DDF.name();

  private static final SitePojo POJO =
      new SitePojo()
          .setVersion(SitePojoTest.VERSION)
          .setId(SitePojoTest.ID)
          .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
          .setName(SitePojoTest.NAME)
          .setUrl(SitePojoTest.URL)
          .setType(SitePojoTest.TYPE);

  @Test
  public void testSetAndGetId() throws Exception {
    final SitePojo pojo = new SitePojo().setId(SitePojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(SitePojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final SitePojo pojo = new SitePojo().setVersion(SitePojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(SitePojoTest.VERSION));
  }

  @Test
  public void testSetAndGetName() throws Exception {
    final SitePojo pojo = new SitePojo().setName(SitePojoTest.NAME);

    Assert.assertThat(pojo.getName(), Matchers.equalTo(SitePojoTest.NAME));
  }

  @Test
  public void testSetAndGetUrl() throws Exception {
    final SitePojo pojo = new SitePojo().setUrl(SitePojoTest.URL);

    Assert.assertThat(pojo.getUrl(), Matchers.equalTo(SitePojoTest.URL));
  }

  @Test
  public void testSetAndGetRemoteManaged() throws Exception {
    final SitePojo pojo = new SitePojo().setRemoteManaged(SitePojoTest.REMOTE_MANAGED);

    Assert.assertThat(pojo.isRemoteManaged(), Matchers.equalTo(SitePojoTest.REMOTE_MANAGED));
  }

  @Test
  public void testSetAndGetType() throws Exception {
    final SitePojo pojo = new SitePojo().setType(SitePojoTest.TYPE);

    Assert.assertThat(pojo.getType(), Matchers.equalTo(SitePojoTest.TYPE));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION)
            .setId(SitePojoTest.ID)
            .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME)
            .setUrl(SitePojoTest.URL)
            .setType(SitePojoTest.TYPE);

    Assert.assertThat(SitePojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION)
            .setId(SitePojoTest.ID + "2")
            .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME)
            .setUrl(SitePojoTest.URL)
            .setType(SitePojoTest.TYPE);

    Assert.assertThat(
        SitePojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION)
            .setId(SitePojoTest.ID)
            .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME)
            .setUrl(SitePojoTest.URL)
            .setType(SitePojoTest.TYPE);

    Assert.assertThat(SitePojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(SitePojoTest.POJO.equals(SitePojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(SitePojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotASitePojo() throws Exception {
    Assert.assertThat(SitePojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION)
            .setId(SitePojoTest.ID + "2")
            .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME)
            .setUrl(SitePojoTest.URL)
            .setType(SitePojoTest.TYPE);

    Assert.assertThat(SitePojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION + 2)
            .setId(SitePojoTest.ID)
            .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME)
            .setUrl(SitePojoTest.URL)
            .setType(SitePojoTest.TYPE);

    Assert.assertThat(SitePojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenRemoteManagedIsDifferent() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION)
            .setId(SitePojoTest.ID)
            .setRemoteManaged(!SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME)
            .setUrl(SitePojoTest.URL)
            .setType(SitePojoTest.TYPE);

    Assert.assertThat(SitePojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenNameIsDifferent() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION)
            .setId(SitePojoTest.ID)
            .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME + "2")
            .setUrl(SitePojoTest.URL)
            .setType(SitePojoTest.TYPE);

    Assert.assertThat(SitePojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenUrlIsDifferent() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION)
            .setId(SitePojoTest.ID)
            .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME)
            .setUrl(SitePojoTest.URL + "2")
            .setType(SitePojoTest.TYPE);

    Assert.assertThat(SitePojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenTypesIsDifferent() throws Exception {
    final SitePojo pojo2 =
        new SitePojo()
            .setVersion(SitePojoTest.VERSION)
            .setId(SitePojoTest.ID)
            .setRemoteManaged(SitePojoTest.REMOTE_MANAGED)
            .setName(SitePojoTest.NAME)
            .setUrl(SitePojoTest.URL)
            .setType(SitePojoTest.TYPE + "2");

    Assert.assertThat(SitePojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
