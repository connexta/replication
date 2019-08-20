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
package com.connexta.replication.api.impl.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.data.ReplicatorConfig;
import com.connexta.replication.api.impl.persistence.pojo.ConfigPojo;
import com.connexta.replication.api.impl.persistence.spring.ConfigRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorConfigManagerImplTest {
  private static final String ID = "id";

  private ReplicatorConfigManagerImpl manager;

  @Mock ConfigRepository configRepository;

  @Before
  public void setup() {
    manager = new ReplicatorConfigManagerImpl(configRepository);
  }

  @Test
  public void getConfig() {
    when(configRepository.findById(anyString()))
        .thenReturn(
            Optional.of(
                new ConfigPojo()
                    .setId(ReplicatorConfigManagerImplTest.ID)
                    .setName("name")
                    .setSource("source")
                    .setDestination("destination")
                    .setFilter("filter")));
    final ReplicatorConfig cfg = manager.get(ReplicatorConfigManagerImplTest.ID);

    verify(configRepository).findById(ReplicatorConfigManagerImplTest.ID);
    assertThat(cfg.getId(), equalTo(ReplicatorConfigManagerImplTest.ID));
    assertThat(cfg.getName(), equalTo("name"));
    assertThat(cfg.getSource(), equalTo("source"));
    assertThat(cfg.getDestination(), equalTo("destination"));
    assertThat(cfg.getFilter(), equalTo("filter"));
  }

  @Test
  public void getAllConfigs() {
    when(configRepository.findAll()).thenReturn(Collections.emptyList());
    manager.objects();
    verify(configRepository).findAll();
  }

  @Test
  public void saveConfig() {
    final ReplicatorConfigImpl config = new ReplicatorConfigImpl();

    config.setId("id");
    config.setName("name");
    config.setSource("source");
    config.setDestination("destination");
    config.setFilter("filter");
    manager.save(config);
    verify(configRepository).save(any(ConfigPojo.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveConfigBadConfig() {
    ReplicatorConfig mock = mock(ReplicatorConfig.class);
    manager.save(mock);
  }

  @Test
  public void removeConfig() {
    manager.remove("id");
    verify(configRepository).deleteById(anyString());
  }

  @Test
  public void configExists() {
    when(configRepository.findById("id")).thenReturn(Optional.of(new ConfigPojo()));
    boolean exists = manager.configExists("id");
    assertThat(exists, is(true));
  }

  @Test
  public void configExistsNotFound() {
    when(configRepository.findById("id")).thenReturn(Optional.empty());
    boolean exists = manager.configExists("id");
    assertThat(exists, is(false));
  }
}
