package org.codice.ditto.replication.api.impl.temp.metadata;

import java.util.List;
import org.codice.ditto.replication.api.temp.metadata.DeleteRequest;
import org.codice.ditto.replication.api.temp.metadata.Metadata;

public class DeleteRequestImpl implements DeleteRequest {

  private final List<Metadata> metadata;

  public DeleteRequestImpl(List<Metadata> metadata) {
    this.metadata = metadata;
  }

  @Override
  public List<Metadata> getMetadata() {
    return metadata;
  }
}
