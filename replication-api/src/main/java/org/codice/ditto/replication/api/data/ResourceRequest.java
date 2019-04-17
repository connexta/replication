package org.codice.ditto.replication.api.data;

/**
 * A resource request object to be sent to {@link org.codice.ditto.replication.api.NodeAdapter}s to
 * fetch {@link Resource}s.
 */
public interface ResourceRequest {

  /** @return the {@link Metadata} of the {@link Resource} to fetch */
  Metadata getMetadata();
}
