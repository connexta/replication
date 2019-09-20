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
package com.connexta.replication.api.impl.query;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.data.Metadata;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class DdfMetadataInfoImplTest {

  private static final String TYPE = "Metacard XML";

  private static final Instant MODIFIED = Instant.now();

  private static final long SIZE = 1;

  private static final Class DATA_CLASS = Map.class;

  private static final Map DATA = new HashMap();

  private DdfMetadataInfoImpl<Map> metadata =
      new DdfMetadataInfoImpl(TYPE, MODIFIED, SIZE, DATA_CLASS, DATA);

  @Test
  public void getters() {
    assertThat(metadata.getType(), is(TYPE));
    assertThat(metadata.getLastModified(), is(MODIFIED));
    assertThat(metadata.getSize().getAsLong(), is(SIZE));
    assertThat(metadata.getDataClass(), is(DATA_CLASS));
    assertThat(metadata.getData(), is(DATA));
  }

  @Test
  public void metadataConstructor() {
    Metadata originalMetadata = mock(Metadata.class);
    when(originalMetadata.getMetadataModified()).thenReturn(Date.from(MODIFIED));
    when(originalMetadata.getMetadataSize()).thenReturn(SIZE);
    when(originalMetadata.getType()).thenReturn(DATA_CLASS);
    when(originalMetadata.getRawMetadata()).thenReturn(DATA);
    metadata = new DdfMetadataInfoImpl<>(TYPE, originalMetadata);
    assertThat(metadata.getType(), is(TYPE));
    assertThat(metadata.getLastModified(), is(MODIFIED.truncatedTo(ChronoUnit.MILLIS)));
    assertThat(metadata.getSize().getAsLong(), is(SIZE));
    assertThat(metadata.getDataClass(), is(DATA_CLASS));
    assertThat(metadata.getData(), is(DATA));
  }
}
