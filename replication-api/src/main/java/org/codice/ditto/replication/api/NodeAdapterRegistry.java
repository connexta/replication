package org.codice.ditto.replication.api;

public interface NodeAdapterRegistry {

  NodeAdapterFactory factoryFor(NodeAdapterType type);

  void register(NodeAdapterType type, NodeAdapterFactory factory);
}
