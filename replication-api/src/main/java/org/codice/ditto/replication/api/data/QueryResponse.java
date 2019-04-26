package org.codice.ditto.replication.api.data;

public interface QueryResponse {

  /**
   * An iterable of {@link Metadata} returned by a {@link QueryRequest} sent to a {@link
   * org.codice.ditto.replication.api.NodeAdapter}, which translates a node's specific metadata
   * format into {@link Metadata}.
   *
   * <p>The returned {@link Metadata} must be sorted in ascending order according to the {@link
   * Metadata#getMetadataModified()} field.
   *
   * <p>Considerations of paging when iterating should be taken into account in order to avoid
   * memory issues.
   *
   * @return the iterable of {@link Metadata}
   */
  Iterable<Metadata> getMetadata();
}
