package org.codice.ditto.replication.api.data;

public interface QueryResponse {

  /**
   * Returns a delegate iterable for accessing an adapters metadata
   *
   * @return
   */
  Iterable<Metadata> getMetadata();
}
