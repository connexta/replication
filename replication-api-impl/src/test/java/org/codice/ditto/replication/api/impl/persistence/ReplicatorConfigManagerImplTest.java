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
package org.codice.ditto.replication.api.impl.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorConfigManagerImplTest {

  private ReplicatorConfigManagerImpl manager;

  @Mock ReplicationPersistentStore persistentStore;

  @Before
  public void setup() {
    manager = new ReplicatorConfigManagerImpl(persistentStore);
  }

  @Test
  public void getConfig() {
    when(persistentStore.get(eq(ReplicatorConfigImpl.class), anyString()))
        .thenReturn(new ReplicatorConfigImpl());
    manager.get("id");
    verify(persistentStore).get(eq(ReplicatorConfigImpl.class), anyString());
  }

  @Test
  public void getAllConfigs() {
    when(persistentStore.objects(eq(ReplicatorConfigImpl.class))).thenReturn(Stream.empty());
    manager.objects();
    verify(persistentStore).objects(eq(ReplicatorConfigImpl.class));
  }

  @Test
  public void saveConfig() {
    manager.save(new ReplicatorConfigImpl());
    verify(persistentStore).save(any(ReplicatorConfigImpl.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveConfigBadConfig() {
    ReplicatorConfig mock = mock(ReplicatorConfig.class);
    manager.save(mock);
  }

  @Test
  public void removeConfig() {
    manager.remove("id");
    verify(persistentStore).delete(eq(ReplicatorConfigImpl.class), anyString());
  }

  @Test
  public void testExistsNotFound() {
    when(persistentStore.get(ReplicatorConfigImpl.class, "id")).thenThrow(NotFoundException.class);
    assertThat(manager.exists("id"), is(false));
  }

  @Test
  public void testExistsConfigFound() {
    assertThat(manager.exists("id"), is(true));
  }
}
