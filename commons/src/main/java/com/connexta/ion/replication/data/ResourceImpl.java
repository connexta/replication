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
package com.connexta.ion.replication.data;

import com.connexta.ion.replication.api.data.Metadata;
import com.connexta.ion.replication.api.data.Resource;
import java.io.InputStream;
import java.net.URI;
import javax.annotation.Nullable;

/** Simple implementation of {@link Resource}. */
public class ResourceImpl implements Resource {

  private final String id;

  private final String name;

  private final URI uri;

  private final String qualifier;

  private final InputStream inputStream;

  private final String mimeType;

  private final long size;

  private final Metadata metadata;

  public ResourceImpl(
      String id,
      String name,
      URI uri,
      String qualifier,
      InputStream inputStream,
      String mimeType,
      long size,
      Metadata metadata) {
    this.id = id;
    this.name = name;
    this.uri = uri;
    this.qualifier = qualifier;
    this.inputStream = inputStream;
    this.mimeType = mimeType;
    this.size = size;
    this.metadata = metadata;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public URI getResourceUri() {
    return uri;
  }

  @Override
  @Nullable
  public String getQualifier() {
    return qualifier;
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }

  @Override
  public long getSize() {
    return size;
  }

  @Override
  public Metadata getMetadata() {
    return metadata;
  }
}
