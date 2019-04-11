package org.codice.ditto.replication.api.impl.temp.metadata;

import java.util.List;
import org.codice.ditto.replication.api.temp.metadata.Metadata;
import org.codice.ditto.replication.api.temp.metadata.UpdateRequest;

public class UpdateRequestImpl implements UpdateRequest {

  private final List<Metadata> metadata;

  public UpdateRequestImpl(List<Metadata> metadata) {
    this.metadata = metadata;
  }

  @Override
  public List<Metadata> getUpdatedMetadata() {
    return metadata;
  }
}
