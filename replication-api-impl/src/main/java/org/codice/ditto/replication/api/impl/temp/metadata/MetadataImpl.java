package org.codice.ditto.replication.api.impl.temp.metadata;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.temp.metadata.Metadata;

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

  public MetadataImpl(Object metadata, Class type, Date metadataModified) {
    this.metadata = metadata;
    this.type = type;
    this.metadataModified = metadataModified;

    this.id = UUID.randomUUID().toString();
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
