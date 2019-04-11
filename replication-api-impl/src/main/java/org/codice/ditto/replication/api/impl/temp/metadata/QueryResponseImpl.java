package org.codice.ditto.replication.api.impl.temp.metadata;

import org.codice.ditto.replication.api.temp.metadata.Metadata;
import org.codice.ditto.replication.api.temp.metadata.QueryResponse;

public class QueryResponseImpl implements QueryResponse {

  private final Iterable<Metadata> metadata;

  public QueryResponseImpl(Iterable<Metadata> metadata) {
    this.metadata = metadata;
  }

  @Override
  public Iterable<Metadata> getMetadata() {
    return metadata;
  }
}
