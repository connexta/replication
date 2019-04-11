package org.codice.ditto.replication.api.impl.temp.resources;

import java.io.InputStream;
import java.net.URI;
import org.codice.ditto.replication.api.temp.metadata.Metadata;
import org.codice.ditto.replication.api.temp.resources.Resource;

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
