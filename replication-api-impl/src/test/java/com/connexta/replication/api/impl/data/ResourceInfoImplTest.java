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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.data.Metadata;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ResourceInfoImplTest {
  private static final String ID = "id";

  private static final String RESOURCE_URI = "https://test:123";

  private static final Instant LAST_MODIFIED = Instant.now();

  private static final long SIZE = 1;

  private ResourceInfoImpl resourceInfo;

  @Test
  public void getters() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    assertThat(resourceInfo.getUri().get(), is(new URI(RESOURCE_URI)));
    assertThat(resourceInfo.getLastModified(), is(LAST_MODIFIED));
    assertThat(resourceInfo.getSize().getAsLong(), is(SIZE));
  }

  @Test
  public void metadataConstructor() throws Exception {
    Metadata metadata = mock(Metadata.class);
    when(metadata.getResourceUri()).thenReturn(new URI(RESOURCE_URI));
    when(metadata.getResourceModified()).thenReturn(Date.from(LAST_MODIFIED));
    when(metadata.getResourceSize()).thenReturn(SIZE);
    resourceInfo = new ResourceInfoImpl(metadata);
    assertThat(resourceInfo.getUri().get(), is(new URI(RESOURCE_URI)));
    assertThat(resourceInfo.getLastModified(), is(LAST_MODIFIED.truncatedTo(ChronoUnit.MILLIS)));
    assertThat(resourceInfo.getSize().getAsLong(), is(SIZE));
  }

  @Test
  public void twoParamConstructor() {
    resourceInfo = new ResourceInfoImpl(LAST_MODIFIED, SIZE);
    assertThat(resourceInfo.getUri(), is(Optional.empty()));
    assertThat(resourceInfo.getLastModified(), is(LAST_MODIFIED));
    assertThat(resourceInfo.getSize().getAsLong(), is(SIZE));
  }

  @Test
  public void testHashCodeWhenEquals() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    final ResourceInfoImpl resourceInfo2 =
        new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);

    resourceInfo.setId(ResourceInfoImplTest.ID);
    resourceInfo2.setId(ResourceInfoImplTest.ID);

    Assert.assertThat(resourceInfo.hashCode(), Matchers.equalTo(resourceInfo2.hashCode()));
  }

  @Test
  public void testHashCodeWhenDifferent() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    final ResourceInfoImpl resourceInfo2 =
        new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);

    resourceInfo.setId(ResourceInfoImplTest.ID);
    resourceInfo2.setId(ResourceInfoImplTest.ID + "2");

    Assert.assertThat(
        resourceInfo.hashCode(), Matchers.not(Matchers.equalTo(resourceInfo2.hashCode())));
  }

  @Test
  public void testEqualsWhenEquals() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    final ResourceInfoImpl resourceInfo2 =
        new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);

    resourceInfo.setId(ResourceInfoImplTest.ID);
    resourceInfo2.setId(ResourceInfoImplTest.ID);

    Assert.assertThat(resourceInfo.equals(resourceInfo2), Matchers.equalTo(true));
  }

  @Test
  public void testEqualsWhenIdentical() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    resourceInfo.setId(ResourceInfoImplTest.ID);

    Assert.assertThat(resourceInfo.equals(resourceInfo), Matchers.equalTo(true));
  }

  @SuppressWarnings("PMD.EqualsNull" /* purposely testing equals() when called with null */)
  @Test
  public void testEqualsWhenNull() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    resourceInfo.setId(ResourceInfoImplTest.ID);

    Assert.assertThat(resourceInfo.equals(null), Matchers.equalTo(false));
  }

  @SuppressWarnings(
      "PMD.PositionLiteralsFirstInComparisons" /* purposely testing equals() when call with
                                               something else than expected */)
  @Test
  public void testEqualsWhenNotAMetadataInfoPojo() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    resourceInfo.setId(ResourceInfoImplTest.ID);

    Assert.assertThat(resourceInfo.equals("test"), Matchers.equalTo(false));
  }

  @Test
  public void testEqualsWhenIdIsDifferent() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    final ResourceInfoImpl resourceInfo2 =
        new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);

    resourceInfo.setId(ResourceInfoImplTest.ID);
    resourceInfo2.setId(ResourceInfoImplTest.ID + "2");

    Assert.assertThat(resourceInfo.equals(resourceInfo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenUriIsDifferent() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    final ResourceInfoImpl resourceInfo2 =
        new ResourceInfoImpl(new URI(RESOURCE_URI + 2), LAST_MODIFIED, SIZE);

    resourceInfo.setId(ResourceInfoImplTest.ID);
    resourceInfo2.setId(ResourceInfoImplTest.ID);

    Assert.assertThat(resourceInfo.equals(resourceInfo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenLastModifiedIsDifferent() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    final ResourceInfoImpl resourceInfo2 =
        new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED.plusSeconds(2L), SIZE);

    resourceInfo.setId(ResourceInfoImplTest.ID);
    resourceInfo2.setId(ResourceInfoImplTest.ID);

    Assert.assertThat(resourceInfo.equals(resourceInfo2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testEqualsWhenSizeIsDifferent() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    final ResourceInfoImpl resourceInfo2 =
        new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE + 2L);

    resourceInfo.setId(ResourceInfoImplTest.ID);
    resourceInfo2.setId(ResourceInfoImplTest.ID);

    Assert.assertThat(resourceInfo.equals(resourceInfo2), Matchers.not(Matchers.equalTo(true)));
  }
}
