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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ResourceInfoPojoTest {
  private static final int VERSION = 1;
  private static final String ID = "1234";
  private static final Instant LAST_MODIFIED = Instant.now();
  private static final long SIZE = 1234L;
  private static final String URI = "https://some.uri.com";
  private static final String URI2 = "https://some.uri.com/2";
  private static final URI URI3;

  static {
    try {
      URI3 = new URI("https://some.uri.com/3");
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  private static final ResourceInfoPojo POJO =
      new ResourceInfoPojo()
          .setVersion(ResourceInfoPojoTest.VERSION)
          .setId(ResourceInfoPojoTest.ID)
          .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED)
          .setSize(ResourceInfoPojoTest.SIZE)
          .setUri(ResourceInfoPojoTest.URI);

  @Test
  public void testSetAndGetId() throws Exception {
    final ResourceInfoPojo pojo = new ResourceInfoPojo().setId(ResourceInfoPojoTest.ID);

    Assert.assertThat(pojo.getId(), Matchers.equalTo(ResourceInfoPojoTest.ID));
  }

  @Test
  public void testSetAndGetVersion() throws Exception {
    final ResourceInfoPojo pojo = new ResourceInfoPojo().setVersion(ResourceInfoPojoTest.VERSION);

    Assert.assertThat(pojo.getVersion(), Matchers.equalTo(ResourceInfoPojoTest.VERSION));
  }

  @Test
  public void testSetAndGetLastModified() throws Exception {
    final ResourceInfoPojo pojo =
        new ResourceInfoPojo().setLastModified(ResourceInfoPojoTest.LAST_MODIFIED);

    Assert.assertThat(pojo.getLastModified(), Matchers.equalTo(ResourceInfoPojoTest.LAST_MODIFIED));
  }

  @Test
  public void testSetAndGetSize() throws Exception {
    final ResourceInfoPojo pojo = new ResourceInfoPojo().setSize(ResourceInfoPojoTest.SIZE);

    Assert.assertThat(pojo.getSize(), Matchers.equalTo(ResourceInfoPojoTest.SIZE));
  }

  @Test
  public void testSetAndGetUri() throws Exception {
    final ResourceInfoPojo pojo = new ResourceInfoPojo().setUri(ResourceInfoPojoTest.URI);

    Assert.assertThat(pojo.getUri(), Matchers.equalTo(ResourceInfoPojoTest.URI));
  }

  @Test
  public void testSetUriWithJavaUri() throws Exception {
    final ResourceInfoPojo pojo = new ResourceInfoPojo().setUri(ResourceInfoPojoTest.URI3);

    Assert.assertThat(pojo.getUri(), Matchers.equalTo(ResourceInfoPojoTest.URI3.toString()));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    final ResourceInfoPojo pojo2 =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoTest.VERSION)
            .setId(ResourceInfoPojoTest.ID)
            .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoTest.SIZE)
            .setUri(ResourceInfoPojoTest.URI);

    Assert.assertThat(ResourceInfoPojoTest.POJO.hashCode(), Matchers.equalTo(pojo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    final ResourceInfoPojo pojo2 =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoTest.VERSION)
            .setId(ResourceInfoPojoTest.ID + "2")
            .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoTest.SIZE)
            .setUri(ResourceInfoPojoTest.URI);

    Assert.assertThat(
        ResourceInfoPojoTest.POJO.hashCode(), Matchers.not(Matchers.equalTo(pojo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    final ResourceInfoPojo pojo2 =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoTest.VERSION)
            .setId(ResourceInfoPojoTest.ID)
            .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoTest.SIZE)
            .setUri(ResourceInfoPojoTest.URI);

    Assert.assertThat(ResourceInfoPojoTest.POJO.equals(pojo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    Assert.assertThat(
        ResourceInfoPojoTest.POJO.equals(ResourceInfoPojoTest.POJO), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    Assert.assertThat(ResourceInfoPojoTest.POJO.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with something else than expected */)
  @Test
  public void testEqualsWhenNotAResourceInfoPojo() throws Exception {
    Assert.assertThat(ResourceInfoPojoTest.POJO.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    final ResourceInfoPojo pojo2 =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoTest.VERSION)
            .setId(ResourceInfoPojoTest.ID + "2")
            .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoTest.SIZE)
            .setUri(ResourceInfoPojoTest.URI);

    Assert.assertThat(
        ResourceInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenVersionIsDifferent() throws Exception {
    final ResourceInfoPojo pojo2 =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoTest.VERSION + 2)
            .setId(ResourceInfoPojoTest.ID)
            .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoTest.SIZE)
            .setUri(ResourceInfoPojoTest.URI);

    Assert.assertThat(
        ResourceInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenLastModifiedIsDifferent() throws Exception {
    final ResourceInfoPojo pojo2 =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoTest.VERSION)
            .setId(ResourceInfoPojoTest.ID)
            .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED.plusSeconds(2))
            .setSize(ResourceInfoPojoTest.SIZE)
            .setUri(ResourceInfoPojoTest.URI);

    Assert.assertThat(
        ResourceInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSizeIsDifferent() throws Exception {
    final ResourceInfoPojo pojo2 =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoTest.VERSION)
            .setId(ResourceInfoPojoTest.ID)
            .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoTest.SIZE + 2L)
            .setUri(ResourceInfoPojoTest.URI);

    Assert.assertThat(
        ResourceInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenUriIsDifferent() throws Exception {
    final ResourceInfoPojo pojo2 =
        new ResourceInfoPojo()
            .setVersion(ResourceInfoPojoTest.VERSION)
            .setId(ResourceInfoPojoTest.ID)
            .setLastModified(ResourceInfoPojoTest.LAST_MODIFIED)
            .setSize(ResourceInfoPojoTest.SIZE)
            .setUri(ResourceInfoPojoTest.URI2);

    Assert.assertThat(
        ResourceInfoPojoTest.POJO.equals(pojo2), Matchers.not(Matchers.equalTo(true)));
  }
}
