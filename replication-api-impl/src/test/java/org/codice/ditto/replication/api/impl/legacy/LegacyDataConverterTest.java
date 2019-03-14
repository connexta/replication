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
package org.codice.ditto.replication.api.impl.legacy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import ddf.catalog.CatalogFramework;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.proxy.builder.GeotoolsFilterBuilder;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.security.service.SecurityServiceException;
import java.io.Serializable;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.Direction;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.MetacardHelper;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.mcard.ReplicationConfigAttributes;
import org.codice.ditto.replication.api.impl.persistence.ReplicationPersistentStore;
import org.codice.ditto.replication.api.mcard.ReplicationConfig;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.codice.junit.rules.RestoreSystemProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opengis.filter.Filter;

@RunWith(MockitoJUnitRunner.class)
public class LegacyDataConverterTest {

  // @Rule public Timeout globalTimeout = Timeout.seconds(10);

  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  LegacyDataConverter converter;

  @Mock CatalogFramework framework;

  @Mock MetacardHelper helper;

  @Mock SiteManager siteManager;

  @Mock ReplicatorConfigManager newConfigManager;

  @Mock ReplicationPersistentStore persistentStore;

  @Mock Security security;

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
    when(security.runWithSubjectOrElevate(any(Callable.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, Callable.class).call());
    when(security.runAsAdmin(any(PrivilegedAction.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, PrivilegedAction.class).run());

    OldSite oldSite = new OldSite();
    oldSite.setId("oldSiteId");
    oldSite.setName("oldSiteName");
    oldSite.setUrl("https://oldurl:8080");
    when(persistentStore.objects(eq(OldSite.class)))
        .thenAnswer(
            new Answer<Stream<OldSite>>() {
              @Override
              public Stream<OldSite> answer(InvocationOnMock invocation) throws Throwable {
                return Stream.of(oldSite);
              }
            });
    when(siteManager.createSite(anyString(), anyString())).thenReturn(new ReplicationSiteImpl());
    converter =
        new LegacyDataConverter(
            framework, builder, helper, siteManager, newConfigManager, persistentStore, security);
    System.setProperty(SystemBaseUrl.INTERNAL_HOST, "localhost");
    System.setProperty(SystemBaseUrl.INTERNAL_HTTPS_PORT, "5678");
  }

  @Test
  public void init() throws Exception {
    ReplicatorConfig config =
        newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql");

    when(siteManager.objects()).thenReturn(Stream.empty());
    when(newConfigManager.objects()).thenReturn(Stream.empty());
    when(helper.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.singletonList(config));
    converter.init();
    verify(siteManager).save(any(ReplicationSite.class));
    verify(persistentStore).delete(OldSite.class, "oldSiteId");
    verify(newConfigManager).save(config);
    verify(framework).delete(any(DeleteRequest.class));
  }

  @Test
  public void initSiteAlreadyConverted() throws Exception {
    OldSite oldSite = new OldSite();
    oldSite.setId("oldSiteId");
    oldSite.setName("oldSiteName");
    oldSite.setUrl("https://oldurl:8080");

    when(siteManager.objects()).thenReturn(Stream.of(oldSite));
    when(newConfigManager.objects()).thenReturn(Stream.empty());
    when(helper.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.emptyList());
    converter.init();
    verify(siteManager, never()).save(any(ReplicationSite.class));
    verify(persistentStore).delete(OldSite.class, "oldSiteId");
  }

  @Test
  public void initThrowsNotFoundException() throws Exception {
    when(siteManager.objects()).thenReturn(Stream.empty());
    when(newConfigManager.objects()).thenReturn(Stream.empty());
    when(helper.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.emptyList());
    doThrow(new NotFoundException())
        .when(persistentStore)
        .delete(eq(OldSite.class), eq("oldSiteId"));
    converter.init();
    verify(persistentStore).delete(OldSite.class, "oldSiteId");
  }

  @Test
  public void initConfigAlreadyConverted() throws Exception {
    ReplicatorConfig config =
        newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql");

    when(siteManager.objects()).thenReturn(Stream.empty());
    when(newConfigManager.objects()).thenReturn(Stream.of(config));
    when(helper.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.singletonList(config));
    converter.init();
    verify(siteManager).save(any(ReplicationSite.class));
    verify(persistentStore).delete(OldSite.class, "oldSiteId");
    verify(newConfigManager, never()).save(config);
    verify(framework).delete(any(DeleteRequest.class));
  }

  @Test
  public void initThrowsSecurityServiceException() throws Exception {
    when(siteManager.objects()).thenReturn(Stream.empty());
    reset(security);
    when(security.runAsAdmin(any(PrivilegedAction.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, PrivilegedAction.class).run());
    when(security.runWithSubjectOrElevate(any(Callable.class)))
        .thenThrow(new SecurityServiceException());
    converter.init();
    verify(newConfigManager, never()).save(any(ReplicatorConfig.class));
  }

  // this test will throw an exception which should cause failsafe to retry.
  @Test
  public void initThrowsRetryException() throws Exception {
    ReplicatorConfig config =
        newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql");
    when(siteManager.objects()).thenReturn(Stream.empty(), Stream.empty());
    when(newConfigManager.objects())
        .thenThrow(new ReplicationPersistenceException())
        .thenReturn(Stream.empty());
    when(helper.getTypeForFilter(any(Filter.class), any(Function.class)))
        .thenReturn(Collections.singletonList(config));
    converter.init();
    verify(siteManager, times(2)).save(any(ReplicationSite.class));
    verify(persistentStore, times(2)).delete(OldSite.class, "oldSiteId");
    verify(newConfigManager).save(config);
    verify(framework).delete(any(DeleteRequest.class));
  }

  @Test
  public void removeConfig() throws Exception {
    converter.removeConfig(newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql"));
    ArgumentCaptor<DeleteRequest> captor = ArgumentCaptor.forClass(DeleteRequest.class);
    verify(framework).delete(captor.capture());
    assertThat(captor.getValue().getAttributeValues().get(0), is("id"));
  }

  @Test(expected = ReplicationException.class)
  public void removeConfigSourceUnavailableException() throws Exception {
    when(framework.delete(any(DeleteRequest.class))).thenThrow(new SourceUnavailableException());
    converter.removeConfig(newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql"));
  }

  @Test
  public void removeConfigIngestException() throws Exception {
    when(framework.delete(any(DeleteRequest.class))).thenThrow(new IngestException());
    converter.removeConfig(newReplicatorConfig("test", "id", "src", "dest", Direction.PUSH, "cql"));
    verify(framework).delete(any(DeleteRequest.class)); // just confirm the exception is handled
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
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config.getId(), is("id"));
    assertThat(config.getName(), is("name"));
    assertThat(config.getSource(), is("src"));
    assertThat(config.getDestination(), is("dest"));
    assertThat(config.isBidirectional(), is(false));
    assertThat(config.getFilter(), is("cql"));
  }

  @Test
  public void getConfigFromMetacardDirectionIsBoth() {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.SOURCE, "src");
    mcard.setAttribute(ReplicationConfig.DESTINATION, "dest");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "BOTH");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config.getId(), is("id"));
    assertThat(config.getName(), is("name"));
    assertThat(config.getSource(), is("src"));
    assertThat(config.getDestination(), is("dest"));
    assertThat(config.isBidirectional(), is(true));
    assertThat(config.getFilter(), is("cql"));
  }

  @Test(expected = ReplicationPersistenceException.class)
  public void getConfigFromMetacardPersistenceExceptionOccurs() {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.URL, "https://host:1234");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PUSH");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");
    when(siteManager.objects()).thenThrow(new ReplicationPersistenceException(""));
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config, is(nullValue()));
  }

  @Test
  public void getConfigNullSource() {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.DESTINATION, "dest");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PUSH");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config, is(nullValue()));
  }

  @Test
  public void getConfigNullDestination() {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.SOURCE, "src");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PUSH");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config, is(nullValue()));
  }

  @Test
  public void getConfigNullName() {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.SOURCE, "src");
    mcard.setAttribute(ReplicationConfig.DESTINATION, "dest");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PUSH");
    mcard.setAttribute(ReplicationConfig.CQL, "cql");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config, is(nullValue()));
  }

  @Test
  public void getConfigNullFilter() {
    MetacardImpl mcard = new MetacardImpl(type);
    mcard.setId("id");
    mcard.setAttribute(ReplicationConfig.NAME, "name");
    mcard.setAttribute(ReplicationConfig.SOURCE, "src");
    mcard.setAttribute(ReplicationConfig.DESTINATION, "dest");
    mcard.setAttribute(ReplicationConfig.DIRECTION, "PUSH");
    mcard.setAttribute(ReplicationConfig.TYPE, "RESOURCE");
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
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
    mcard.setAttribute(ReplicationConfig.SUSPEND, true);

    ReplicationSite site1 = new ReplicationSiteImpl();
    site1.setId("siteid");
    site1.setName("mysite");
    site1.setUrl("https://host:1234");
    ReplicationSite site2 = new ReplicationSiteImpl();
    site2.setId("localid");
    site2.setName("anothersite");
    site2.setUrl("https://localhost:5678");

    when(siteManager.objects())
        .thenAnswer(
            new Answer() {
              public Stream<ReplicationSite> answer(InvocationOnMock invocation) {
                return Stream.of(site1, site2);
              }
            });
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config.getSource(), is("localid"));
    assertThat(config.getDestination(), is("siteid"));
    assertThat(config.isBidirectional(), is(false));
    assertThat(config.getVersion(), is(1));
    assertThat(config.isSuspended(), is(true));
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

    ReplicationSite site1 = new ReplicationSiteImpl();
    site1.setId("siteid");
    site1.setName("mysite");
    site1.setUrl("https://host:1234");
    ReplicationSite site2 = new ReplicationSiteImpl();
    site2.setId("localid");
    site2.setName("anothersite");
    site2.setUrl("https://localhost:5678");

    when(siteManager.objects())
        .thenAnswer(
            new Answer() {
              public Stream<ReplicationSite> answer(InvocationOnMock invocation) {
                return Stream.of(site1, site2);
              }
            });
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config.getSource(), is("siteid"));
    assertThat(config.getDestination(), is("localid"));
    assertThat(config.isBidirectional(), is(false));
    assertThat(config.getVersion(), is(1));
    assertThat(config.isSuspended(), is(false));
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

    when(siteManager.objects()).thenReturn(Stream.empty());
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
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

    ReplicationSite site = new ReplicationSiteImpl();
    site.setId("localid");
    site.setName("anothersite");
    site.setUrl("https://localhost:5678");

    ReplicationSite newSite = new ReplicationSiteImpl();
    newSite.setId("newid");
    newSite.setName("host");
    newSite.setUrl("https://host:1234");

    when(siteManager.objects())
        .thenAnswer(
            new Answer() {
              public Stream<ReplicationSite> answer(InvocationOnMock invocation) {
                return Stream.of(site);
              }
            });
    when(siteManager.createSite(anyString(), anyString())).thenReturn(newSite);
    ReplicatorConfig config = converter.getConfigFromMetacard(mcard);
    assertThat(config.getSource(), is("newid"));
    assertThat(config.getDestination(), is("localid"));
  }

  private ReplicatorConfig newReplicatorConfig(String name) {
    return newReplicatorConfig(name, null, "source", "destination", Direction.PUSH, "empty");
  }

  private ReplicatorConfigImpl newReplicatorConfig(
      String name, String id, String src, String dest, Direction dir, String cql) {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setName(name);
    config.setId(id);
    config.setSource(src);
    config.setDestination(dest);
    config.setBidirectional(dir == Direction.BOTH);
    config.setFilter(cql);
    config.setDescription("description");
    config.setFailureRetryCount(5);
    return config;
  }
}
