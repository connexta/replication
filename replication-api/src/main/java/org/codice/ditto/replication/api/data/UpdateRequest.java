package org.codice.ditto.replication.api.data;

import java.util.List;

/**
 * An update request object to be sent to {@link org.codice.ditto.replication.api.NodeAdapter}s to
 * update {@link Metadata}.
 */
public interface UpdateRequest {

  /**
   * Gets a list of {@link Metadata} to be updated.
   *
   * @return the list of {@link Metadata}
   */
  List<Metadata> getUpdatedMetadata();
}
