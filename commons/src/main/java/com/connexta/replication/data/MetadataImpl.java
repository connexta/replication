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
package com.connexta.replication.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.data.Metadata;

public class MetadataImpl implements Metadata {

  private final Object metadata;

  private final String id;

  private final Class type;

  private final Date metadataModified;

  private final Set<String> tags;

  private final List<String> lineage;

  private long resourceSize = 0;

  private URI resourceUri;

  private Date resourceModified;

  public MetadataImpl(Object metadata, Class type, String id, Date metadataModified) {
    this.metadata = metadata;
    this.type = type;
    this.metadataModified = metadataModified;
    this.id = id;

    this.tags = new HashSet<>();
    this.lineage = new ArrayList<>();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Class getType() {
    return type;
  }

  @Override
  public Object getRawMetadata() {
    return metadata;
  }

  @Override
  @Nullable
  public URI getResourceUri() {
    return resourceUri;
  }

  @Override
  public void setResourceUri(URI resourceUri) {
    this.resourceUri = resourceUri;
  }

  @Override
  public long getResourceSize() {
    return resourceSize;
  }

  @Override
  public void setResourceSize(long resourceSize) {
    this.resourceSize = resourceSize;
  }

  @Override
  public Date getMetadataModified() {
    return metadataModified;
  }

  @Override
  @Nullable
  public Date getResourceModified() {
    return resourceModified;
  }

  @Override
  public void setResourceModified(Date resourceModified) {
    this.resourceModified = resourceModified;
  }

  @Override
  public Set<String> getTags() {
    return tags;
  }

  @Override
  public void addTag(String tag) {
    tags.add(tag);
  }

  @Override
  public List<String> getLineage() {
    return lineage;
  }

  @Override
  public void addLineage(String name) {
    lineage.add(name);
  }
}
