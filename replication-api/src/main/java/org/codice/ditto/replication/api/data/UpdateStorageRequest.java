package org.codice.ditto.replication.api.data;

import java.util.List;

/**
 * An update request object to be sent to {@link org.codice.ditto.replication.api.NodeAdapter}s to
 * update {@link Resource}s.
 */
public interface UpdateStorageRequest {

  /**
   * Gets a list of {@link Resource}s to be updated.
   *
   * @return the list of {@link Resource}s
   */
  List<Resource> getResources();
}
