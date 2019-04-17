package org.codice.ditto.replication.api.data;

import java.util.List;

/**
 * A delete request object to be sent to {@link org.codice.ditto.replication.api.NodeAdapter}s to
 * delete {@link Metadata}.
 */
public interface DeleteRequest {

  /**
   * Gets a list of {@link Metadata} to be deleted.
   *
   * @return the list of {@link Metadata}
   */
  List<Metadata> getMetadata();
}
