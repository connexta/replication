package org.codice.ditto.replication.api.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterRegistry;
import org.codice.ditto.replication.api.NodeAdapterType;

public class NodeAdapters implements NodeAdapterRegistry {

  private final Map<NodeAdapterType, NodeAdapterFactory> nodeAdaptersFactories =
      new ConcurrentHashMap<>();

  @Override
  public NodeAdapterFactory factoryFor(NodeAdapterType type) {
    if (!nodeAdaptersFactories.containsKey(type)) {
      throw new IllegalArgumentException(
          String.format("No node adapter factory with type %s registered", type.toString()));
    }
    return nodeAdaptersFactories.get(type);
  }

  @Override
  public void register(NodeAdapterType type, NodeAdapterFactory factory) {
    if (nodeAdaptersFactories.containsKey(type)) {
      throw new IllegalArgumentException(
          String.format(
              "Node adapter factory with type %s is already registered", type.toString()));
    }
    nodeAdaptersFactories.put(type, factory);
  }
}
