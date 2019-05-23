package org.codice.ditto.replication.api;

import java.net.URL;

/**
 * Factories which enable the {@link Replicator} to create {@link NodeAdapter}s to perform
 * replication.
 */
public interface NodeAdapterFactory {

  /**
   * Creates a new {@link NodeAdapter}.
   *
   * @param url the base url of the system
   * @return the adapter
   * @throws AdapterException if the adapter couldn't be created from the url
   */
  NodeAdapter create(URL url);

  /** @return the type of the node that will be created */
  NodeAdapterType getType();
}
