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
package org.codice.ditto.replication.admin.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.ReplicatorConfigLoader;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.modern.ReplicationSiteImpl;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationUtilsTest {

  ReplicationUtils utils;

  @Mock ReplicationSitePersistentStore persistentStore;

  @Mock ReplicatorConfigLoader configLoader;

  @Mock ReplicatorHistory history;

  @Mock Replicator replicator;

  @Before
  public void setUp() throws Exception {
    utils = new ReplicationUtils(persistentStore, configLoader, history, replicator);
  }

  @Test
  public void createSite() throws Exception {
    ReplicationSiteImpl site =
        new ReplicationSiteImpl("id", "site", new URL("https://localhost:1234"));
    when(persistentStore.saveSite(anyString(), anyString())).thenReturn(site);
    ReplicationSiteField field = utils.createSite(site.getName(), getAddress(site.getUrl()));
    assertThat(field.id(), is("id"));
    assertThat(field.name(), is("site"));
    assertThat(field.address().url(), is("https://localhost:1234"));
  }

  @Test(expected = ReplicationException.class)
  public void createSiteDuplicate() throws Exception {
    ReplicationSiteImpl site =
        new ReplicationSiteImpl("id", "site", new URL("https://localhost:1234"));
    when(persistentStore.getSites()).thenReturn(Collections.singleton(site));
    utils.createSite(site.getName(), getAddress(site.getUrl()));
  }

  @Test
  public void createReplication() throws Exception {
    ReplicationSiteImpl src =
        new ReplicationSiteImpl("srcId", "source", new URL("https://localhost:1234"));
    ReplicationSiteImpl dest =
        new ReplicationSiteImpl("destId", "destination", new URL("https://otherhost:1234"));

    when(persistentStore.getSite("srcId")).thenReturn(Optional.of(src));
    when(persistentStore.getSite("destId")).thenReturn(Optional.of(dest));

    when(configLoader.getConfig(anyString())).thenReturn(Optional.empty());
    ReplicationStatus status = new ReplicationStatus("test");
    status.setPushCount(1L);
    status.setPushBytes(1024 * 1024 * 5L);
    status.setPullCount(2L);
    status.setPullBytes(1024 * 1024 * 10L);
    when(history.getReplicationEvents("test")).thenReturn(Collections.singletonList(status));
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());
    ReplicationField field = utils.createReplication("test", "srcId", "destId", "cql", true);
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(true));
    assertThat(field.itemsTransferred(), is(3));
    assertThat(field.dataTransferred(), is("15 MB"));
    verify(configLoader).saveConfig(any(ReplicatorConfig.class));
  }

  @Test
  public void createReplicationNoHistory() throws Exception {
    ReplicationSiteImpl src =
        new ReplicationSiteImpl("srcId", "source", new URL("https://localhost:1234"));
    ReplicationSiteImpl dest =
        new ReplicationSiteImpl("destId", "destination", new URL("https://otherhost:1234"));

    when(persistentStore.getSite("srcId")).thenReturn(Optional.of(src));
    when(persistentStore.getSite("destId")).thenReturn(Optional.of(dest));

    when(configLoader.getConfig(anyString())).thenReturn(Optional.empty());
    when(history.getReplicationEvents("test")).thenReturn(Collections.emptyList());
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());
    ReplicationField field = utils.createReplication("test", "srcId", "destId", "cql", true);
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(true));
    assertThat(field.itemsTransferred(), is(0));
    assertThat(field.dataTransferred(), is("0 MB"));
    verify(configLoader).saveConfig(any(ReplicatorConfig.class));
  }

  @Test(expected = ReplicationException.class)
  public void createReplicationInvalidSourceId() throws Exception {
    ReplicationSiteImpl dest =
        new ReplicationSiteImpl("destId", "destination", new URL("https://otherhost:1234"));

    when(persistentStore.getSite("srcId")).thenReturn(Optional.empty());
    when(persistentStore.getSite("destId")).thenReturn(Optional.of(dest));

    when(configLoader.getConfig(anyString())).thenReturn(Optional.empty());
    utils.createReplication("test", "srcId", "destId", "cql", true);
  }

  @Test(expected = ReplicationException.class)
  public void createReplicationInvalidDestId() throws Exception {
    ReplicationSiteImpl src =
        new ReplicationSiteImpl("srcId", "source", new URL("https://localhost:1234"));

    when(persistentStore.getSite("srcId")).thenReturn(Optional.of(src));
    when(persistentStore.getSite("destId")).thenReturn(Optional.empty());

    when(configLoader.getConfig(anyString())).thenReturn(Optional.empty());
    utils.createReplication("test", "srcId", "destId", "cql", true);
  }

  @Test(expected = ReplicationException.class)
  public void createDuplicateReplication() throws Exception {
    when(configLoader.getConfig(anyString())).thenReturn(Optional.of(new ReplicatorConfigImpl()));
    utils.createReplication("test", "srcId", "destId", "cql", true);
  }

  @Test
  public void updateReplication() throws Exception {
    ReplicationSiteImpl src =
        new ReplicationSiteImpl("srcId", "source", new URL("https://localhost:1234"));
    ReplicationSiteImpl dest =
        new ReplicationSiteImpl("destId", "destination", new URL("https://otherhost:1234"));

    when(persistentStore.getSite("srcId")).thenReturn(Optional.of(src));
    when(persistentStore.getSite("destId")).thenReturn(Optional.of(dest));
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId("id");
    config.setName("oldName");
    config.setCql("oldCql");
    config.setFailureRetryCount(7);
    when(configLoader.getConfig(anyString())).thenReturn(Optional.of(config));
    ReplicationStatus status = new ReplicationStatus("test");
    status.setPushCount(1L);
    status.setPushBytes(1024 * 1024 * 5L);
    status.setPullCount(2L);
    status.setPullBytes(1024 * 1024 * 10L);
    when(history.getReplicationEvents("test")).thenReturn(Collections.singletonList(status));
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());
    ReplicationField field = utils.updateReplication("id", "test", "srcId", "destId", "cql", true);
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(true));
    assertThat(field.itemsTransferred(), is(3));
    assertThat(field.dataTransferred(), is("15 MB"));
    verify(configLoader).saveConfig(any(ReplicatorConfig.class));
  }

  @Test
  public void deleteConfig() {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId("id");
    config.setName("name");
    when(configLoader.getAllConfigs()).thenReturn(Collections.singletonList(config));
    assertThat(utils.deleteConfig("id"), is(true));
    verify(configLoader).removeConfig(any(ReplicatorConfig.class));
  }

  @Test
  public void deleteConfigFailed() {
    when(configLoader.getConfig(anyString())).thenReturn(Optional.empty());
    assertThat(utils.deleteConfig("id"), is(false));
    verify(configLoader, never()).removeConfig(any(ReplicatorConfig.class));
  }

  @Test
  public void getReplications() throws Exception {
    ReplicationSiteImpl src =
        new ReplicationSiteImpl("srcId", "source", new URL("https://localhost:1234"));
    ReplicationSiteImpl dest =
        new ReplicationSiteImpl("destId", "destination", new URL("https://otherhost:1234"));

    when(persistentStore.getSite("srcId")).thenReturn(Optional.of(src));
    when(persistentStore.getSite("destId")).thenReturn(Optional.of(dest));

    ReplicationStatus status = new ReplicationStatus("test");
    status.setPushCount(1L);
    status.setPushBytes(1024 * 1024 * 5L);
    status.setPullCount(2L);
    status.setPullBytes(1024 * 1024 * 10L);
    when(history.getReplicationEvents("test")).thenReturn(Collections.singletonList(status));
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId("id");
    config.setName("test");
    config.setCql("cql");
    config.setSource("srcId");
    config.setDestination("destId");
    config.setDirection(Direction.PUSH);
    when(configLoader.getAllConfigs()).thenReturn(Collections.singletonList(config));
    ReplicationField field = utils.getReplications().getList().get(0);
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(false));
    assertThat(field.itemsTransferred(), is(3));
    assertThat(field.dataTransferred(), is("15 MB"));
  }

  private AddressField getAddress(URL url) {
    AddressField address = new AddressField();
    address.url(url.toString());
    address.hostname(url.getHost());
    address.port(url.getPort());
    return address;
  }
}
