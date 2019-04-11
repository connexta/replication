package org.codice.ditto.replication.api.impl.temp.resources;

import org.codice.ditto.replication.api.temp.resources.Resource;
import org.codice.ditto.replication.api.temp.resources.ResourceResponse;

public class ResourceResponseImpl implements ResourceResponse {

  private final Resource resource;

  public ResourceResponseImpl(Resource resource) {
    this.resource = resource;
  }

  @Override
  public Resource getResource() {
    return resource;
  }
}
