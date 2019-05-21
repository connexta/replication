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
package com.connexta.replication.application;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigFileReaderTest {

  @Mock private ReplicatorConfigManager configManager;

  @Mock private SiteManager siteManager;

  @Mock private ReplicatorHistoryManager historyManager;

  private ConfigFileReader fileReader;

  @Before
  public void setup() {
    fileReader = new ConfigFileReader(configManager, siteManager, historyManager);
  }

  @Test
  public void readSites() throws Exception {
    when(siteManager.objects()).thenReturn(Stream.empty());
    ArgumentCaptor<ReplicationSite> siteCaptor = ArgumentCaptor.forClass(ReplicationSite.class);
    URI sitesUri = getClass().getClassLoader().getResource("sites.json").toURI();
    fileReader.readSites(Paths.get(sitesUri));
    verify(siteManager).save(siteCaptor.capture());
    ReplicationSite savedSite = siteCaptor.getValue();
    assertThat(savedSite.getName(), is("ditto-1"));
    assertThat(savedSite.getUrl(), is("https://ditto-1.phx.connexta.com:8993/services"));
  }

  @Test
  public void readSitesDeleteSite() throws Exception {
    ReplicationSite site = new ReplicationSiteImpl();
    site.setName("ditto-2");
    site.setId("id");
    when(siteManager.objects()).thenReturn(Stream.of(site));
    URI sitesUri = getClass().getClassLoader().getResource("sites.json").toURI();
    fileReader.readSites(Paths.get(sitesUri));
    verify(siteManager).remove(site.getId());
  }

  @Test
  public void readSitesSiteExists() throws Exception {
    ReplicationSite site = new ReplicationSiteImpl();
    site.setName("ditto-1");
    when(siteManager.objects()).thenReturn(Stream.of(site));
    URI sitesUri = getClass().getClassLoader().getResource("sites.json").toURI();
    fileReader.readSites(Paths.get(sitesUri));
    verify(siteManager, never()).save(any(ReplicationSite.class));
  }

  @Test
  public void readSitesFileNotFound() {
    fileReader.readSites(Paths.get("badPath"));
    verify(siteManager, never()).objects();
  }

  @Test
  public void readSitesSyntaxException() throws Exception {
    URI sitesUri = getClass().getClassLoader().getResource("bad.json").toURI();
    fileReader.readSites(Paths.get(sitesUri));
    verify(siteManager, never()).objects();
  }

  @Test
  public void readConfigs() throws Exception {
    ReplicationSite ditto1 = new ReplicationSiteImpl();
    ditto1.setName("ditto-1");
    ReplicationSite ditto2 = new ReplicationSiteImpl();
    ditto2.setName("ditto-2");
    when(configManager.objects()).thenReturn(Stream.empty());
    when(siteManager.objects()).thenReturn(Stream.of(ditto1, ditto2));
    ArgumentCaptor<ReplicatorConfig> configCaptor = ArgumentCaptor.forClass(ReplicatorConfig.class);
    URI configsUri = getClass().getClassLoader().getResource("configs.json").toURI();
    fileReader.readConfigs(Paths.get(configsUri));
    verify(configManager).save(configCaptor.capture());
    ReplicatorConfig savedConfig = configCaptor.getValue();
    assertThat(savedConfig.getName(), is("file6"));
    assertThat(savedConfig.isBidirectional(), is(false));
    assertThat(savedConfig.getSource(), is(ditto1.getId()));
    assertThat(savedConfig.getDestination(), is(ditto2.getId()));
    assertThat(savedConfig.getFilter(), is("title like 'file6'"));
  }

  @Test
  public void readConfigsDeleteConfig() throws Exception {
    ReplicatorConfig config = new ReplicatorConfigImpl();
    config.setName("markedConfig");
    config.setId("id");
    ReplicationStatus status = new ReplicationStatusImpl();
    status.setId("statusId");
    when(configManager.objects()).thenReturn(Stream.of(config));
    when(siteManager.objects()).thenReturn(Stream.empty());
    when(historyManager.getByReplicatorId(eq(config.getId()))).thenReturn(status);
    URI configsUri = getClass().getClassLoader().getResource("configs.json").toURI();
    fileReader.readConfigs(Paths.get(configsUri));
    verify(configManager).remove(config.getId());
    verify(historyManager).remove(status.getId());
  }

  @Test
  public void readConfigsConfigExists() throws Exception {
    ReplicatorConfig config = new ReplicatorConfigImpl();
    config.setName("file6");
    ReplicationSite ditto1 = new ReplicationSiteImpl();
    ditto1.setName("ditto-1");
    ReplicationSite ditto2 = new ReplicationSiteImpl();
    ditto2.setName("ditto-2");
    when(configManager.objects()).thenReturn(Stream.of(config));
    when(siteManager.objects()).thenReturn(Stream.of(ditto1, ditto2));
    URI configsUri = getClass().getClassLoader().getResource("configs.json").toURI();
    fileReader.readConfigs(Paths.get(configsUri));
    verify(configManager, never()).save(any(ReplicatorConfig.class));
  }

  @Test
  public void readConfigsSourceDoesNotExist() throws Exception {
    ReplicationSite ditto2 = new ReplicationSiteImpl();
    ditto2.setName("ditto-2");
    when(configManager.objects()).thenReturn(Stream.empty());
    when(siteManager.objects()).thenReturn(Stream.of(ditto2));
    URI configsUri = getClass().getClassLoader().getResource("configs.json").toURI();
    fileReader.readConfigs(Paths.get(configsUri));
    verify(siteManager).objects();
    verify(configManager, never()).save(any(ReplicatorConfig.class));
  }

  @Test
  public void readConfigsDestinationDoesNotExist() throws Exception {
    ReplicationSite ditto1 = new ReplicationSiteImpl();
    ditto1.setName("ditto-1");
    when(configManager.objects()).thenReturn(Stream.empty());
    when(siteManager.objects()).thenReturn(Stream.of(ditto1));
    URI configsUri = getClass().getClassLoader().getResource("configs.json").toURI();
    fileReader.readConfigs(Paths.get(configsUri));
    verify(siteManager).objects();
    verify(configManager, never()).save(any(ReplicatorConfig.class));
  }

  @Test
  public void readConfigsFileNotFound() {
    fileReader.readConfigs(Paths.get("badPath"));
    verify(configManager, never()).objects();
  }

  @Test
  public void readConfigsSyntaxException() throws Exception {
    URI sitesUri = getClass().getClassLoader().getResource("bad.json").toURI();
    fileReader.readConfigs(Paths.get(sitesUri));
    verify(configManager, never()).objects();
  }
}
