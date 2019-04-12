package org.codice.ditto.replication.api.impl;

import java.util.List;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;

public class NodeAdapters {

  private List<NodeAdapterFactory> nodeAdapterFactories;

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
