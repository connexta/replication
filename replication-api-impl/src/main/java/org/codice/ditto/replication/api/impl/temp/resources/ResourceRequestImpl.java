package org.codice.ditto.replication.api.impl.temp.resources;

import org.codice.ditto.replication.api.temp.metadata.Metadata;
import org.codice.ditto.replication.api.temp.resources.ResourceRequest;

public class ResourceRequestImpl implements ResourceRequest {

  private final Metadata metadata;

  public ResourceRequestImpl(Metadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public Metadata getMetadata() {
    return metadata;
  }
}
