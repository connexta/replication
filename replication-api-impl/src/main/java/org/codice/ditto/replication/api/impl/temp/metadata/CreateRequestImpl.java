package org.codice.ditto.replication.api.impl.temp.metadata;

import java.util.List;
import org.codice.ditto.replication.api.temp.metadata.CreateRequest;
import org.codice.ditto.replication.api.temp.metadata.Metadata;

public class CreateRequestImpl implements CreateRequest {

  private final List<Metadata> metadata;

  public CreateRequestImpl(List<Metadata> metadata) {
    this.metadata = metadata;
  }

  @Override
  public List<Metadata> getMetadata() {
    return metadata;
  }
}
