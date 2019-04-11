package org.codice.ditto.replication.api.impl.temp.resources;

import java.util.List;
import org.codice.ditto.replication.api.temp.resources.CreateStorageRequest;
import org.codice.ditto.replication.api.temp.resources.Resource;

public class CreateStorageRequestImpl implements CreateStorageRequest {

  private final List<Resource> resources;

  public CreateStorageRequestImpl(List<Resource> resources) {
    this.resources = resources;
  }

  @Override
  public List<Resource> getResources() {
    return resources;
  }
}
