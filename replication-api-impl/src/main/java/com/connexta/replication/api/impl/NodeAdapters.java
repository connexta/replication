/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.replication.api.impl;

import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.data.SiteType;
import java.util.Collections;
import java.util.List;

/** Utility class for getting {@link NodeAdapterFactory}s. */
public class NodeAdapters {
  private List<NodeAdapterFactory> nodeAdapterFactories;

  /** Initialize a default node adapters with no registered factories. */
  public NodeAdapters() {
    this.nodeAdapterFactories = Collections.emptyList();
  }

  /**
   * Returns a {@link NodeAdapterFactory} which is used to create a {@link NodeAdapter} for the
   * given site type.
   *
   * @param type the type of site for which to get a {@link NodeAdapter}
   * @return the {@link NodeAdapter}s factory
   * @throws IllegalArgumentException if no factory of the specified type exist
   */
  public NodeAdapterFactory factoryFor(SiteType type) {
    return nodeAdapterFactories.stream()
        .filter(factory -> factory.getType().equals(type))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("No node adapter factory with type %s registered", type)));
  }

  public void setNodeAdapterFactories(List<NodeAdapterFactory> nodeAdapterFactories) {
    this.nodeAdapterFactories = nodeAdapterFactories;
  }
}
