package org.codice.ditto.replication.api.data;

public interface ResourceResponse {

  /** @return the {@link Resource} fetched from the {@link ResourceRequest} */
  Resource getResource();
}
