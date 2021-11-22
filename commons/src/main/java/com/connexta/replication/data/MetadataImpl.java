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

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.data.Metadata;

/** Simple implementation of {@link Metadata}. */
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

  private boolean isDeleted = false;

  private String source;

  /**
   * @param metadata the raw metadata to wrap, cannot be null
   * @param type the type of the metadata, cannot be null
   * @param id id the of the metadata, cannot be null or empty
   * @param metadataModified the last time the metadata was modified, cannot be null
   */
  public MetadataImpl(Object metadata, Class type, String id, Date metadataModified) {
    this.metadata = notNull(metadata, "Argument metadata may not be null");
    this.type = notNull(type, "Argument type may not be null");
    this.id = notEmpty(id, "Argument id may not be null or empty");
    this.metadataModified = notNull(metadataModified, "Argument metadataModified may not be null");

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
    // don't include remote updates back to source in lineage
    if (!lineage.contains(name)) {
      lineage.add(name);
    }
  }

  @Override
  public boolean isDeleted() {
    return isDeleted;
  }

  @Override
  public void setIsDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  @Override
  public String getSource() {
    return source;
  }

  @Override
  public void setSource(String source) {
    this.source = source;
  }
}
