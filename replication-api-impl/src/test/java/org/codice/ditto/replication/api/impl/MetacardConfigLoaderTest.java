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
package org.codice.ditto.replication.api.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.source.IngestException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationType;
import org.codice.ditto.replication.api.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.mcard.ReplicationConfigAttributes;
import org.codice.ditto.replication.api.impl.modern.ReplicationSiteImpl;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.codice.ditto.replication.api.modern.ReplicationSite;
import org.codice.ditto.replication.api.modern.ReplicationSitePersistentStore;
import org.codice.junit.rules.RestoreSystemProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengis.filter.Filter;

@RunWith(MockitoJUnitRunner.class)
public class MetacardConfigLoaderTest {

  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  MetacardConfigLoader configLoader;

  @Mock CatalogFramework framework;

  @Mock MetacardHelper helper;

  @Mock ReplicationSitePersistentStore siteStore;

  MetacardType type;

  @Before
  public void setUp() throws Exception {
    FilterBuilder builder = new GeotoolsFilterBuilder();
    List<MetacardType> types = new ArrayList<>();
    types.add(new CoreAttributes());
    types.add(new ReplicationConfigAttributes());
    type = new MetacardTypeImpl("replication-config", types);
    doCallRealMethod()
        .when(helper)
        .setIfPresent(any(Metacard.class), any(String.class), any(Serializable.class));
    doCallRealMethod()
        .when(helper)
        .setIfPresentOrDefault(
            any(Metacard.class),
            any(String.class),
            any(Serializable.class),
            any(Serializable.class));
    when(helper.getAttributeValueOrDefault(any(Metacard.class), anyString(), any(Object.class)))
        .thenCallRealMethod();
    configLoader = new MetacardConfigLoader(framework, builder, helper, type, siteStore);
    System.setProperty(SystemBaseUrl.INTERNAL_HOST, "localhost");
    System.setProperty(SystemBaseUrl.INTERNAL_HTTPS_PORT, "5678");
  }

  @Test
  public void getConfig() {
    when(helper.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.singletonList(newReplicatorConfig("test")));
    Optional<ReplicatorConfig> config = configLoader.getConfig("test");
    assertThat(config.isPresent(), is(true));
  }

  @Test
  public void getConfigNoConfigPresent() {
    when(helper.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.emptyList());
    Optional<ReplicatorConfig> config = configLoader.getConfig("test");
    assertThat(config.isPresent(), is(false));
  }

  @Test(expected = IllegalStateException.class)
  public void getConfigMultipleEntries() {
    List<ReplicatorConfig> configs = new ArrayList<>();
    configs.add(newReplicatorConfig("config1"));
    configs.add(newReplicatorConfig("config2"));
    when(helper.getTypeForFilter(any(Filter.class), any(Function.class))).thenReturn(configs);
    configLoader.getConfig("test");
  }

  @Test
  public void saveConfig() throws Exception {
    ReplicatorConfig config =
        newReplicatorConfig("test", null, "src", "dest", Direction.PUSH, "cql");
    configLoader.saveConfig(config);
    ArgumentCaptor<CreateRequest> captor = ArgumentCaptor.forClass(CreateRequest.class);
    verify(framework).create(captor.capture());
    Metacard mcard = captor.getValue().getMetacards().get(0);
    assertThat(mcard.getId(), is(nullValue()));
    assertThat(mcard.getAttribute(ReplicationConfig.DESTINATION).getValue(), is("dest"));
    assertThat(mcard.getAttribute(ReplicationConfig.SOURCE).getValue(), is("src"));
    assertThat(mcard.getAttribute(ReplicationConfig.NAME).getValue(), is("test"));
    assertThat(mcard.getAttribute(ReplicationConfig.DIRECTION).getValue(), is("PUSH"));
    assertThat(mcard.getAttribute(ReplicationConfig.CQL).getValue(), is("cql"));
  }

  @Test(expected = ReplicationException.class)
  public void saveConfigCatalogError() throws Exception {
    ReplicatorConfig config =
        newReplicatorConfig("test", null, "src", "dest", Direction.PUSH, "cql");
    when(framework.create(any(CreateRequest.class))).thenThrow(new IngestException());
    configLoader.saveConfig(config);
  }

  @Test
  public void saveExistingConfig() throws Exception {
    ReplicatorConfig config =
        newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql");
    MetacardImpl orig = new MetacardImpl(type);
    orig.setId("id");
    orig.setAttribute(ReplicationConfig.NAME, "oldName");
    when(helper.getMetacardById(any(String.class))).thenReturn(orig);
    configLoader.saveConfig(config);
    ArgumentCaptor<UpdateRequest> captor = ArgumentCaptor.forClass(UpdateRequest.class);
    verify(framework).update(captor.capture());
    Metacard mcard = captor.getValue().getUpdates().get(0).getValue();
    assertThat(mcard.getId(), is("id"));
    assertThat(mcard.getAttribute(ReplicationConfig.NAME).getValue(), is("test"));
  }

  @Test(expected = ReplicationException.class)
  public void saveExistingConfigCatalogError() throws Exception {
    ReplicatorConfig config =
        newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql");
    MetacardImpl orig = new MetacardImpl(type);
    orig.setId("id");
    orig.setAttribute(ReplicationConfig.NAME, "oldName");
    when(helper.getMetacardById(any(String.class))).thenReturn(orig);
    when(framework.update(any(UpdateRequest.class))).thenThrow(new IngestException());
    configLoader.saveConfig(config);
  }

  @Test(expected = ReplicationException.class)
  public void saveBadConfig() throws Exception {
    ReplicatorConfig config =
        newReplicatorConfig("test", null, null, "dest", Direction.PUSH, "cql");
    configLoader.saveConfig(config);
  }

  @Test
  public void removeConfig() throws Exception {
    configLoader.removeConfig(
        newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql"));
    ArgumentCaptor<DeleteRequest> captor = ArgumentCaptor.forClass(DeleteRequest.class);
    verify(framework).delete(captor.capture());
    assertThat(captor.getValue().getAttributeValues().get(0), is("id"));
  }

  @Test(expected = ReplicationException.class)
  public void removeConfigError() throws Exception {
    when(framework.delete(any(DeleteRequest.class))).thenThrow(new IngestException());
    configLoader.removeConfig(
        newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql"));
  }

  @Test
  public void getConfigFromMetacard() {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.SOURCE, "src");
    mcard.setAttribute(ReplicationConfig.DESTINATION, "dest");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PUSH");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");
    ReplicatorConfig config = configLoader.getConfigFromMetacard(mcard);
    assertThat(config.getId(), is("id"));
    assertThat(config.getName(), is("name"));
    assertThat(config.getSource(), is("src"));
    assertThat(config.getDestination(), is("dest"));
    assertThat(config.getDirection(), is(Direction.PUSH));
    assertThat(config.getReplicationType(), is(ReplicationType.RESOURCE));
    assertThat(config.getCql(), is("cql"));
  }

  @Test
  public void getConfigFromBadMetacard() {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.DESTINATION, "dest");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PUSH");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");
    ReplicatorConfig config = configLoader.getConfigFromMetacard(mcard);
    assertThat(config, is(nullValue()));
  }

  @Test
  public void getConfigFromOldMetacard() throws Exception {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.URL, "https://host:1234");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PUSH");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");

    Set<ReplicationSite> sites = new HashSet<>();
    sites.add(new ReplicationSiteImpl("siteid", "mysite", new URL("https://host:1234")));
    sites.add(new ReplicationSiteImpl("localid", "anothersite", new URL("https://localhost:5678")));

    when(siteStore.getSites()).thenReturn(sites);
    ReplicatorConfig config = configLoader.getConfigFromMetacard(mcard);
    assertThat(config.getSource(), is("localid"));
    assertThat(config.getDestination(), is("siteid"));
    assertThat(config.getDirection(), is(Direction.PUSH));
  }

  @Test
  public void getConfigFromOldMetacardPull() throws Exception {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.URL, "https://host:1234");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PULL");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");

    Set<ReplicationSite> sites = new HashSet<>();
    sites.add(new ReplicationSiteImpl("siteid", "mysite", new URL("https://host:1234")));
    sites.add(new ReplicationSiteImpl("localid", "anothersite", new URL("https://localhost:5678")));

    when(siteStore.getSites()).thenReturn(sites);
    ReplicatorConfig config = configLoader.getConfigFromMetacard(mcard);
    assertThat(config.getSource(), is("siteid"));
    assertThat(config.getDestination(), is("localid"));
    assertThat(config.getDirection(), is(Direction.PUSH));
  }

  @Test
  public void getConfigFromOldMetacardBadUrl() throws Exception {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.URL, "badurl");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PULL");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");

    when(siteStore.getSites()).thenReturn(new HashSet<>());
    ReplicatorConfig config = configLoader.getConfigFromMetacard(mcard);
    assertThat(config, is(nullValue()));
  }

  @Test
  public void getConfigFromOldMetacardCreateNewSite() throws Exception {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.URL, "https://host:1234");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PULL");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");

    Set<ReplicationSite> sites = new HashSet<>();
    sites.add(new ReplicationSiteImpl("localid", "anothersite", new URL("https://localhost:5678")));

    when(siteStore.getSites()).thenReturn(sites);
    when(siteStore.saveSite(anyString(), anyString()))
        .thenReturn(new ReplicationSiteImpl("newid", "host", new URL("https://host:1234")));
    ReplicatorConfig config = configLoader.getConfigFromMetacard(mcard);
    assertThat(config.getSource(), is("newid"));
    assertThat(config.getDestination(), is("localid"));
  }

  private ReplicatorConfig newReplicatorConfig(String name) {
    return newReplicatorConfig(name, null, "source", "destination", Direction.PUSH, "empty");
  }

  private ReplicatorConfig newReplicatorConfig(
      String name, String id, String src, String dest, Direction dir, String cql) {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setName(name);
    config.setId(id);
    config.setSource(src);
    config.setDestination(dest);
    config.setDirection(dir);
    config.setCql(cql);
    config.setReplicationType(ReplicationType.RESOURCE);
    config.setDescription("description");
    config.setFailureRetryCount(5);
    return config;
  }
}
