package org.codice.ditto.replication.api.temp.metadata;

public interface QueryResponse {

  /**
   * Returns a delegate iterable for accessing an adapters metadata
   *
   * @return
   */
  Iterable<Metadata> getMetadata();
}
