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
package com.connexta.ion.replication.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.NodeAdapterFactory;
import com.connexta.ion.replication.api.NodeAdapterType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodeAdaptersTest {

  @Mock NodeAdapterFactory ddfFactory;

  @Mock NodeAdapterFactory ionFactory;

  private NodeAdapters nodeAdapters;

  @Before
  public void setup() {
    nodeAdapters = new NodeAdapters();
    when(ddfFactory.getType()).thenReturn(NodeAdapterType.DDF);
    when(ionFactory.getType()).thenReturn(NodeAdapterType.ION);
  }

  @Test
  public void testFactoryFor() {
    List<NodeAdapterFactory> factories = new ArrayList<>();
    factories.add(ddfFactory);
    factories.add(ionFactory);
    nodeAdapters.setNodeAdapterFactories(factories);
    assertThat(nodeAdapters.factoryFor(NodeAdapterType.DDF), is(ddfFactory));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFactoryForNoFactoryForType() {
    List<NodeAdapterFactory> factories = new ArrayList<>();
    factories.add(ionFactory);
    nodeAdapters.setNodeAdapterFactories(factories);
    nodeAdapters.factoryFor(NodeAdapterType.DDF);
  }
}
