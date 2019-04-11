package org.codice.ditto.replication.api.impl.temp.resources;

import java.util.List;
import org.codice.ditto.replication.api.temp.resources.Resource;
import org.codice.ditto.replication.api.temp.resources.UpdateStorageRequest;

public class UpdateStorageRequestImpl implements UpdateStorageRequest {

  private final List<Resource> updatedResources;

  public UpdateStorageRequestImpl(List<Resource> updatedResources) {
    this.updatedResources = updatedResources;
  }

  @Override
  public List<Resource> getResources() {
    return updatedResources;
  }
}
