package org.codice.ditto.replication.api.data;

import java.util.List;

/**
 * A create request object to be sent to {@link org.codice.ditto.replication.api.NodeAdapter}s to
 * create {@link Resource}s.
 */
public interface CreateStorageRequest {

  /**
   * Gets a list of {@link Resource}s to be stored.
   *
   * @return the list of {@link Resource}s
   */
  List<Resource> getResources();
}
