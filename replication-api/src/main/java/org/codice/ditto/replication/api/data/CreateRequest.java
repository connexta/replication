package org.codice.ditto.replication.api.data;

import java.util.List;

/**
 * A create request object to be sent to {@link org.codice.ditto.replication.api.NodeAdapter}s to
 * create {@link Metadata}.
 */
public interface CreateRequest {

  /**
   * Gets a list of {@link Metadata} to be stored.
   *
   * @return the list of {@link Metadata}
   */
  List<Metadata> getMetadata();
}
