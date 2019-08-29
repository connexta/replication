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
package com.connexta.replication.query;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.data.Metadata;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import org.junit.Test;

public class ResourceInfoImplTest {

  private static final String RESOURCE_URI = "https://test:123";

  private static final Instant LAST_MODIFIED = Instant.now();

  private static final long SIZE = 1;

  private ResourceInfoImpl resourceInfo;

  @Test
  public void getters() throws Exception {
    resourceInfo = new ResourceInfoImpl(new URI(RESOURCE_URI), LAST_MODIFIED, SIZE);
    assertThat(resourceInfo.getResourceUri().get(), is(new URI(RESOURCE_URI)));
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
    assertThat(resourceInfo.getResourceUri().get(), is(new URI(RESOURCE_URI)));
    assertThat(resourceInfo.getLastModified(), is(LAST_MODIFIED.truncatedTo(ChronoUnit.MILLIS)));
    assertThat(resourceInfo.getSize().getAsLong(), is(SIZE));
  }

  @Test
  public void twoParamConstructor() {
    resourceInfo = new ResourceInfoImpl(LAST_MODIFIED, SIZE);
    assertThat(resourceInfo.getResourceUri(), is(Optional.empty()));
    assertThat(resourceInfo.getLastModified(), is(LAST_MODIFIED));
    assertThat(resourceInfo.getSize().getAsLong(), is(SIZE));
  }
}
