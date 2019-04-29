package org.codice.ditto.replication.api.impl;

import java.util.List;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;

/** Utility class for getting {@link NodeAdapterFactory}s. */
public class NodeAdapters {

  private List<NodeAdapterFactory> nodeAdapterFactories;

  /**
   * Returns a {@link NodeAdapterFactory} which is used to create a {@link
   * org.codice.ditto.replication.api.NodeAdapter} for the given {@link NodeAdapterType}.
   *
   * @param type the type of {@link org.codice.ditto.replication.api.NodeAdapter}
   * @return the {@link org.codice.ditto.replication.api.NodeAdapter}s factory.
   */
  public NodeAdapterFactory factoryFor(NodeAdapterType type) {
    return nodeAdapterFactories
        .stream()
        .filter(factory -> factory.getType().equals(type))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format(
                        "No node adapter factory with type %s registered", type.toString())));
  }

  public void setNodeAdapterFactories(List<NodeAdapterFactory> nodeAdapterFactories) {
    this.nodeAdapterFactories = nodeAdapterFactories;
  }
}
