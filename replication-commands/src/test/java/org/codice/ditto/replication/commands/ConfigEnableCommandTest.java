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
package org.codice.ditto.replication.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Optional;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigEnableCommandTest {
  ConfigEnableCommand command;

  @Mock ReplicatorConfigLoader replicatorConfigLoader;

  @Mock Replicator replicator;

  @Before
  public void setUp() throws Exception {
    command = new ConfigEnableCommand();
    command.replicator = replicator;
    command.replicatorConfigLoader = replicatorConfigLoader;
  }

  @Test
  public void suspendConfig() throws Exception {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setName("test");
    config.setId("id");
    command.configNames = Collections.singletonList(config.getName());
    command.suspend = true;
    when(replicatorConfigLoader.getConfig("test")).thenReturn(Optional.of(config));
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());
    when(replicator.getPendingSyncRequests()).thenReturn(new ArrayDeque<>());
    command.executeWithSubject();
    ArgumentCaptor<ReplicatorConfig> captor = ArgumentCaptor.forClass(ReplicatorConfig.class);
    verify(replicatorConfigLoader).saveConfig(captor.capture());
    assertThat(captor.getValue().isSuspended(), is(true));
  }

  @Test
  public void enableConfig() throws Exception {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setName("test");
    config.setId("id");
    command.configNames = Collections.singletonList(config.getName());
    command.suspend = false;
    when(replicatorConfigLoader.getConfig("test")).thenReturn(Optional.of(config));
    command.executeWithSubject();
    ArgumentCaptor<ReplicatorConfig> captor = ArgumentCaptor.forClass(ReplicatorConfig.class);
    verify(replicatorConfigLoader).saveConfig(captor.capture());
    assertThat(captor.getValue().isSuspended(), is(false));
  }

  @Test
  public void configDoesNotExist() throws Exception {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setName("test");
    config.setId("id");
    command.configNames = Collections.singletonList(config.getName());
    when(replicatorConfigLoader.getConfig("test")).thenReturn(Optional.empty());
    command.executeWithSubject();
    verify(replicatorConfigLoader, never()).saveConfig(any(ReplicatorConfig.class));
  }
}
