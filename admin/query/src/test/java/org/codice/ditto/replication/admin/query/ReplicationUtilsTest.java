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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.crypto.tink.subtle.Random;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ddf.admin.common.fields.common.HostField;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.codice.ditto.replication.admin.query.status.fields.ReplicationStats;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.Replicator;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.data.SyncRequestImpl;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.codice.junit.RestoreSystemProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@RestoreSystemProperties
public class ReplicationUtilsTest {

  private static final String SITE_ID = "siteId";

  private static final String REP_ID = "repId";

  private static final Date LAST_SUCCESS = new Date(Random.randInt(100));

  private static final Date START_TIME = new Date(Random.randInt(100));

  private static final Date LAST_RUN = new Date(Random.randInt(100));

  private ReplicationUtils utils;

  @Mock SiteManager siteManager;

  @Mock ReplicatorConfigManager configManager;

  @Mock ReplicatorHistory history;

  @Mock Replicator replicator;

  @Before
  public void setUp() throws Exception {
    when(siteManager.create()).thenReturn(new ReplicationSiteImpl());
    when(configManager.create()).thenReturn(new ReplicatorConfigImpl());
    utils = new ReplicationUtils(siteManager, configManager, history, replicator);
  }

  @Test
  public void createSite() throws Exception {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    site.setId(SITE_ID);
    site.setName("site");
    site.setUrl("https://localhost:1234");

    when(siteManager.createSite(anyString(), anyString()))
        .thenAnswer(
            invocationOnMock -> {
              site.setUrl(invocationOnMock.getArguments()[1].toString());
              return site;
            });
    ReplicationSiteField field =
        utils.createSite(site.getName(), getAddress(new URL(site.getUrl())), null, false);
    assertThat(field.id(), is(SITE_ID));
    assertThat(field.name(), is("site"));
    assertThat(field.address().url(), is("https://localhost:1234/services"));
    verify(siteManager).save(any(ReplicationSiteImpl.class));
  }

  @Test
  public void createSiteWithContext() throws Exception {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    site.setId(SITE_ID);
    site.setName("site");
    site.setUrl("https://localhost:1234/");

    when(siteManager.createSite(anyString(), anyString()))
        .thenAnswer(
            invocationOnMock -> {
              site.setUrl(invocationOnMock.getArguments()[1].toString());
              return site;
            });
    ReplicationSiteField field =
        utils.createSite(site.getName(), getAddress(new URL(site.getUrl())), "context", false);
    assertThat(field.id(), is(SITE_ID));
    assertThat(field.name(), is("site"));
    assertThat(field.address().url(), is("https://localhost:1234/context"));
    verify(siteManager).save(any(ReplicationSiteImpl.class));
  }

  @Test
  public void createSiteWithContextWithSlashes() throws Exception {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    site.setId(SITE_ID);
    site.setName("site");
    site.setUrl("https://localhost:1234/");

    when(siteManager.createSite(anyString(), anyString()))
        .thenAnswer(
            invocationOnMock -> {
              site.setUrl(invocationOnMock.getArguments()[1].toString());
              return site;
            });
    ReplicationSiteField field =
        utils.createSite(site.getName(), getAddress(new URL(site.getUrl())), " // context ", false);
    assertThat(field.id(), is(SITE_ID));
    assertThat(field.name(), is("site"));
    assertThat(field.address().url(), is("https://localhost:1234/context"));
    verify(siteManager).save(any(ReplicationSiteImpl.class));
  }

  @Test(expected = ReplicationException.class)
  public void getSiteFieldForSiteBadUrl() throws Exception {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    site.setId(SITE_ID);
    site.setName("site");
    site.setUrl("NotSoGood.URL");

    when(siteManager.createSite(anyString(), anyString())).thenReturn(site);
    AddressField address = new AddressField();
    address.url(site.getUrl());
    utils.createSite(site.getName(), address, null, false);
  }

  @Test
  public void createReplication() throws Exception {
    // setup
    ReplicationSiteImpl src = new ReplicationSiteImpl();
    src.setId("srcId");
    src.setName("source");
    src.setUrl("https://localhost:1234");

    ReplicationSiteImpl dest = new ReplicationSiteImpl();
    dest.setId("destId");
    dest.setName("destination");
    dest.setUrl("https://otherhost:1234");

    when(siteManager.get("srcId")).thenReturn(src);
    when(siteManager.get("destId")).thenReturn(dest);

    ReplicationStatus status = new ReplicationStatusImpl("test");
    status.setPushCount(1L);
    status.setPushBytes(1024 * 1024 * 5L);
    status.setPullCount(2L);
    status.setPullBytes(1024 * 1024 * 10L);
    status.setLastRun(LAST_RUN);
    status.setLastSuccess(LAST_SUCCESS);
    status.setStartTime(START_TIME);
    status.setDuration(9L);
    status.setPullFailCount(99L);
    status.setPushFailCount(100L);
    status.setStatus(Status.SUCCESS);
    when(history.getReplicationEvents("test")).thenReturn(Collections.singletonList(status));
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());

    // when
    ReplicationField field = utils.createReplication("test", "srcId", "destId", "cql", true);

    // then
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(true));
    assertThat(field.getStats().getStatus(), is(Status.SUCCESS.toString()));
    assertThat(field.getStats().getPushCount(), is(1L));
    assertThat(field.getStats().getPullCount(), is(2L));
    assertThat(field.getStats().getPushBytes(), is(5L));
    assertThat(field.getStats().getPullBytes(), is(10L));
    assertThat(field.getStats().getDuration(), is(9L));
    assertThat(field.getStats().getPullFailCount(), is(99L));
    assertThat(field.getStats().getPushFailCount(), is(100L));
    assertThat(field.getStats().getLastRun(), is(LAST_RUN.toInstant().toString()));
    assertThat(field.getStats().getLastSuccess(), is(LAST_SUCCESS.toInstant().toString()));
    assertThat(field.getStats().getStartTime(), is(START_TIME.toInstant().toString()));

    verify(configManager).save(any(ReplicatorConfig.class));
  }

  @Test
  public void createReplicationNoHistory() throws Exception {
    // setup
    ReplicationSiteImpl src = new ReplicationSiteImpl();
    src.setId("srcId");
    src.setName("source");
    src.setUrl("https://localhost:1234");

    ReplicationSiteImpl dest = new ReplicationSiteImpl();
    dest.setId("destId");
    dest.setName("destination");
    dest.setUrl("https://otherhost:1234");

    when(siteManager.get("srcId")).thenReturn(src);
    when(siteManager.get("destId")).thenReturn(dest);

    when(history.getReplicationEvents("test")).thenReturn(Collections.emptyList());
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());

    // when
    ReplicationField field = utils.createReplication("test", "srcId", "destId", "cql", true);

    // then
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(true));
    assertThat(field.getStats().getStatus(), is("NOT_RUN"));
    assertThat(field.getStats().getPushCount(), is(0L));
    assertThat(field.getStats().getPullCount(), is(0L));
    assertThat(field.getStats().getPushBytes(), is(0L));
    assertThat(field.getStats().getPullBytes(), is(0L));
    assertThat(field.getStats().getDuration(), is(0L));
    assertThat(field.getStats().getPullFailCount(), is(0L));
    assertThat(field.getStats().getPushFailCount(), is(0L));
    assertThat(field.getStats().getLastRun(), is(nullValue()));
    assertThat(field.getStats().getLastSuccess(), is(nullValue()));
    assertThat(field.getStats().getStartTime(), is(nullValue()));
    verify(configManager).save(any(ReplicatorConfig.class));
  }

  @Test
  public void updateReplication() throws Exception {
    // setup
    ReplicationSiteImpl src = new ReplicationSiteImpl();
    src.setId("srcId");
    src.setName("source");
    src.setUrl("https://localhost:1234");

    ReplicationSiteImpl dest = new ReplicationSiteImpl();
    dest.setId("destId");
    dest.setName("destination");
    dest.setUrl("https://otherhost:1234");

    when(siteManager.get("srcId")).thenReturn(src);
    when(siteManager.get("destId")).thenReturn(dest);
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId(REP_ID);
    config.setName("oldName");
    config.setFilter("oldCql");
    config.setFailureRetryCount(7);
    when(configManager.get(REP_ID)).thenReturn(config);
    ReplicationStatus status = new ReplicationStatusImpl("test");
    status.setPushCount(1L);
    status.setPushBytes(5L);
    status.setPullCount(2L);
    status.setPullBytes(10L);
    status.setLastRun(LAST_RUN);
    status.setLastSuccess(LAST_SUCCESS);
    status.setStartTime(START_TIME);
    status.setDuration(9L);
    status.setPullFailCount(99L);
    status.setPushFailCount(100L);
    status.setStatus(Status.SUCCESS);
    when(history.getReplicationEvents("test")).thenReturn(Collections.singletonList(status));
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());

    // when
    ReplicationField field =
        utils.updateReplication(REP_ID, "test", "srcId", "destId", "cql", true);

    // then
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(true));
    assertThat(field.getStats().getStatus(), is(Status.SUCCESS.toString()));
    assertThat(field.getStats().getPushCount(), is(1L));
    assertThat(field.getStats().getPullCount(), is(2L));
    assertThat(field.getStats().getPushBytes(), is(5L));
    assertThat(field.getStats().getPullBytes(), is(10L));
    assertThat(field.getStats().getDuration(), is(9L));
    assertThat(field.getStats().getPullFailCount(), is(99L));
    assertThat(field.getStats().getPushFailCount(), is(100L));
    assertThat(field.getStats().getLastRun(), is(LAST_RUN.toInstant().toString()));
    assertThat(field.getStats().getLastSuccess(), is(LAST_SUCCESS.toInstant().toString()));
    assertThat(field.getStats().getStartTime(), is(START_TIME.toInstant().toString()));

    verify(configManager).save(any(ReplicatorConfig.class));
  }

  @Test
  public void testGetSiteById() {
    final String siteId = "siteId";
    ReplicationSite site = mock(ReplicationSite.class);
    when(siteManager.get(siteId)).thenReturn(site);
    ReplicationSite fetchedSite = utils.getSite(siteId);
    assertThat(fetchedSite, is(site));
  }

  @Test
  public void testGetSiteByIdNoExistingSite() {
    when(siteManager.get(anyString())).thenThrow(NotFoundException.class);
    ReplicationSite site = utils.getSite("id");
    assertThat(site, is(nullValue()));
  }

  @Test
  public void testIsNotUpdatedSitesName() {
    final String siteId = "siteId";
    final String siteName = "siteName";
    ReplicationSite site = mock(ReplicationSite.class);
    when(site.getName()).thenReturn(siteName);
    when(siteManager.get(siteId)).thenReturn(site);
    boolean isNotUpdateSiteName = utils.isUpdatedSitesName(siteId, "notSiteName");
    assertThat(isNotUpdateSiteName, is(false));
  }

  @Test
  public void testIsUpdatedSitesName() {
    final String siteId = "siteId";
    final String siteName = "sItEnAmE"; // for ignores case checking
    ReplicationSite site = mock(ReplicationSite.class);
    when(site.getName()).thenReturn(siteName);
    when(siteManager.get(siteId)).thenReturn(site);
    boolean isNotUpdateSiteName = utils.isUpdatedSitesName(siteId, "siteName");
    assertThat(isNotUpdateSiteName, is(true));
  }

  @Test
  public void updateReplicationNullValues() {
    // setup
    ReplicationSiteImpl src = new ReplicationSiteImpl();
    src.setId("srcId");
    src.setName("source");
    src.setUrl("https://localhost:1234");

    ReplicationSiteImpl dest = new ReplicationSiteImpl();
    dest.setId("destId");
    dest.setName("destination");
    dest.setUrl("https://otherhost:1234");

    when(siteManager.get("srcId")).thenReturn(src);
    when(siteManager.get("destId")).thenReturn(dest);
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId(REP_ID);
    config.setName("test");
    config.setSource("srcId");
    config.setDestination("destId");
    config.setFilter("cql");
    config.setBidirectional(true);
    config.setFailureRetryCount(7);
    when(configManager.get(REP_ID)).thenReturn(config);
    ReplicationStatus status = new ReplicationStatusImpl("test");
    status.setPushCount(1L);
    status.setPushBytes(5L);
    status.setPullCount(2L);
    status.setPullBytes(1024 * 1024 * 10L);
    status.setLastRun(LAST_RUN);
    status.setLastSuccess(LAST_SUCCESS);
    status.setStartTime(START_TIME);
    status.setDuration(9L);
    status.setPullFailCount(99L);
    status.setPushFailCount(100L);
    status.setStatus(Status.SUCCESS);
    when(history.getReplicationEvents("test")).thenReturn(Collections.singletonList(status));
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());

    // when
    ReplicationField field = utils.updateReplication(REP_ID, null, null, null, null, null);

    // then
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(true));
    assertThat(field.getStats().getStatus(), is(Status.SUCCESS.toString()));
    assertThat(field.getStats().getPushCount(), is(1L));
    assertThat(field.getStats().getPullCount(), is(2L));
    assertThat(field.getStats().getPushBytes(), is(5L));
    assertThat(field.getStats().getPullBytes(), is(10L));
    assertThat(field.getStats().getDuration(), is(9L));
    assertThat(field.getStats().getPullFailCount(), is(99L));
    assertThat(field.getStats().getPushFailCount(), is(100L));
    assertThat(field.getStats().getLastRun(), is(LAST_RUN.toInstant().toString()));
    assertThat(field.getStats().getLastSuccess(), is(LAST_SUCCESS.toInstant().toString()));
    assertThat(field.getStats().getStartTime(), is(START_TIME.toInstant().toString()));
    verify(configManager).save(any(ReplicatorConfig.class));
  }

  @Test
  public void updateReplicationActiveSyncRequest() {
    // setup
    ReplicationSiteImpl src = new ReplicationSiteImpl();
    src.setId("srcId");
    src.setName("source");
    src.setUrl("https://localhost:1234");

    ReplicationSiteImpl dest = new ReplicationSiteImpl();
    dest.setId("destId");
    dest.setName("destination");
    dest.setUrl("https://otherhost:1234");

    when(siteManager.get("srcId")).thenReturn(src);
    when(siteManager.get("destId")).thenReturn(dest);
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId(REP_ID);
    config.setName("oldName");
    config.setFilter("oldCql");
    config.setFailureRetryCount(7);
    when(configManager.get(REP_ID)).thenReturn(config);
    ReplicationStatusImpl status = new ReplicationStatusImpl("test");
    status.setStatus(Status.PUSH_IN_PROGRESS);
    when(history.getReplicationEvents("test")).thenReturn(new ArrayList<>());
    SyncRequestImpl syncRequest = new SyncRequestImpl(config, src, dest, status);
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.singleton(syncRequest));

    // when
    ReplicationField field =
        utils.updateReplication(REP_ID, "test", "srcId", "destId", "cql", true);

    // then
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(true));
    assertThat(field.getStats().getStatus(), is(Status.PUSH_IN_PROGRESS.toString()));
    assertThat(field.getStats().getPushCount(), is(0L));
    assertThat(field.getStats().getPullCount(), is(0L));
    assertThat(field.getStats().getPushBytes(), is(0L));
    assertThat(field.getStats().getPullBytes(), is(0L));
    assertThat(field.getStats().getDuration(), is(-1L));
    assertThat(field.getStats().getPullFailCount(), is(0L));
    assertThat(field.getStats().getPushFailCount(), is(0L));
    assertThat(field.getStats().getLastRun(), is(nullValue()));
    assertThat(field.getStats().getLastSuccess(), is(nullValue()));
    assertThat(field.getStats().getStartTime(), is(nullValue()));
    verify(configManager).save(any(ReplicatorConfig.class));
  }

  @Test
  public void testMarkConfigDeleted() {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId("id");
    config.setName("name");

    when(configManager.get("id")).thenReturn(config);

    assertThat(utils.markConfigDeleted("id", true), is(true));
    assertThat(config.shouldDeleteData(), is(true));
    assertThat(config.isDeleted(), is(true));
    assertThat(config.isSuspended(), is(true));
    verify(replicator).cancelSyncRequest("id");
    verify(configManager).save(config);
  }

  @Test
  public void testMarkConfigDeletedNotFound() {
    doThrow(new NotFoundException()).when(configManager).get("id");
    assertThat(utils.markConfigDeleted("id", true), is(true));
  }

  @Test
  public void testMarkConfigDeletedErrorRetrievingConfig() {
    doThrow(new ReplicationPersistenceException("")).when(configManager).get("id");
    assertThat(utils.markConfigDeleted("id", true), is(false));
  }

  @Test
  public void getReplications() throws Exception {
    // setup
    ReplicationSiteImpl src = new ReplicationSiteImpl();
    src.setId("srcId");
    src.setName("source");
    src.setUrl("https://localhost:1234");

    ReplicationSiteImpl dest = new ReplicationSiteImpl();
    dest.setId("destId");
    dest.setName("destination");
    dest.setUrl("https://otherhost:1234");

    when(siteManager.get("srcId")).thenReturn(src);
    when(siteManager.get("destId")).thenReturn(dest);

    ReplicationStatus status = new ReplicationStatusImpl("test");
    status.setPushCount(1L);
    status.setPushBytes(1024 * 1024 * 5L);
    status.setPullCount(2L);
    status.setPullBytes(1024 * 1024 * 10L);
    status.setLastRun(LAST_RUN);
    status.setLastSuccess(LAST_SUCCESS);
    status.setStartTime(START_TIME);
    status.setDuration(9L);
    status.setPullFailCount(99L);
    status.setPushFailCount(100L);
    status.setStatus(Status.SUCCESS);
    when(history.getReplicationEvents("test")).thenReturn(Collections.singletonList(status));
    when(replicator.getActiveSyncRequests()).thenReturn(Collections.emptySet());
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId(REP_ID);
    config.setName("test");
    config.setFilter("cql");
    config.setSource("srcId");
    config.setDestination("destId");
    config.setBidirectional(false);
    when(configManager.objects()).thenReturn(Stream.of(config));

    // when
    ReplicationField field = utils.getReplications(false).getList().get(0);

    // then
    assertThat(field.name(), is("test"));
    assertThat(field.source().id(), is("srcId"));
    assertThat(field.destination().id(), is("destId"));
    assertThat(field.filter(), is("cql"));
    assertThat(field.biDirectional(), is(false));
    assertThat(field.getStats().getStatus(), is(Status.SUCCESS.toString()));
    assertThat(field.getStats().getPushCount(), is(1L));
    assertThat(field.getStats().getPullCount(), is(2L));
    assertThat(field.getStats().getPushBytes(), is(5L));
    assertThat(field.getStats().getPullBytes(), is(10L));
    assertThat(field.getStats().getDuration(), is(9L));
    assertThat(field.getStats().getPullFailCount(), is(99L));
    assertThat(field.getStats().getPushFailCount(), is(100L));
    assertThat(field.getStats().getLastRun(), is(LAST_RUN.toInstant().toString()));
    assertThat(field.getStats().getLastSuccess(), is(LAST_SUCCESS.toInstant().toString()));
    assertThat(field.getStats().getStartTime(), is(START_TIME.toInstant().toString()));
  }

  @Test
  public void testGetReplicationsFilterDeleted() {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setDeleted(true);

    when(configManager.objects()).thenReturn(Stream.of(config));
    assertThat(utils.getReplications(true).getList().size(), is(0));
  }

  @Test
  public void cancelConfig() {
    assertThat(utils.cancelConfig("test"), is(true));
    verify(replicator).cancelSyncRequest("test");
  }

  @Test
  public void suspendConfig() {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId("id");
    config.setName("test");
    config.setSuspended(false);
    when(configManager.get(anyString())).thenReturn(config);
    assertThat(utils.setConfigSuspended("id", true), is(true));
    ArgumentCaptor<ReplicatorConfig> captor = ArgumentCaptor.forClass(ReplicatorConfig.class);
    verify(configManager).save(captor.capture());
    assertThat(captor.getValue().isSuspended(), is(true));
    verify(replicator).cancelSyncRequest("id");
  }

  @Test
  public void suspendConfigAlreadySuspended() {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId("id");
    config.setName("test");
    config.setSuspended(true);
    when(configManager.get(anyString())).thenReturn(config);
    assertThat(utils.setConfigSuspended("id", true), is(false));
    verify(configManager, never()).save(any(ReplicatorConfig.class));
  }

  @Test
  public void enableConfig() {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setId("id");
    config.setName("test");
    config.setSuspended(true);
    when(configManager.get(anyString())).thenReturn(config);
    assertThat(utils.setConfigSuspended("id", false), is(true));
    ArgumentCaptor<ReplicatorConfig> captor = ArgumentCaptor.forClass(ReplicatorConfig.class);
    verify(configManager).save(captor.capture());
    assertThat(captor.getValue().isSuspended(), is(false));
    verify(replicator, never()).cancelSyncRequest("id");
  }

  @Test
  public void siteIdExists() {
    when(siteManager.exists(anyString())).thenReturn(true);
    assertThat(utils.siteIdExists("id"), is(true));
  }

  @Test
  public void siteIdDoesNotExist() {
    when(siteManager.get(anyString())).thenThrow(new NotFoundException());
    assertThat(utils.siteIdExists("id"), is(false));
  }

  @Test
  public void updateSiteName() {
    // setup
    final String newName = "newName";
    final String testUrl = "https://fakestreet:1234/context";
    ReplicationSite site = mock(ReplicationSite.class);
    when(site.getId()).thenReturn(SITE_ID);
    when(site.getUrl()).thenReturn(testUrl);
    when(site.getName()).thenReturn("prevName");

    when(siteManager.get(SITE_ID)).thenReturn(site);

    // when
    utils.updateSite(SITE_ID, newName, mockAddress(null, null), null);

    // then
    verify(site).setName(newName);
    verify(site).setUrl(testUrl);
    verify(siteManager).save(site);
  }

  @Test
  public void updateSiteHostname() {
    // setup
    ReplicationSite site = mock(ReplicationSite.class);
    when(site.getId()).thenReturn(SITE_ID);
    when(site.getUrl()).thenReturn("https://fakestreet:1234/context");

    when(siteManager.get(SITE_ID)).thenReturn(site);

    // when
    utils.updateSite(SITE_ID, null, mockAddress("updatedHost", null), null);

    // then
    verify(site, never()).setName(anyString());
    verify(site).setUrl("https://updatedHost:1234/context");
    verify(siteManager).save(site);
  }

  @Test
  public void updateSitePort() {
    // setup
    ReplicationSite site = mock(ReplicationSite.class);
    when(site.getId()).thenReturn(SITE_ID);
    when(site.getUrl()).thenReturn("https://fakestreet:1234/context");

    when(siteManager.get(SITE_ID)).thenReturn(site);

    // when
    utils.updateSite(SITE_ID, null, mockAddress(null, 4321), null);

    // then
    verify(site, never()).setName(anyString());
    verify(site).setUrl("https://fakestreet:4321/context");
    verify(siteManager).save(site);
  }

  @Test
  public void updateSiteRootContext() {
    // setup
    ReplicationSite site = mock(ReplicationSite.class);
    when(site.getId()).thenReturn(SITE_ID);
    when(site.getUrl()).thenReturn("https://fakestreet:1234/context");

    when(siteManager.get(SITE_ID)).thenReturn(site);

    // when
    utils.updateSite(SITE_ID, null, mockAddress(null, null), "updatedContext");

    // then
    verify(site, never()).setName(anyString());
    verify(site).setUrl("https://fakestreet:1234/updatedContext");
    verify(siteManager).save(site);
  }

  @Test
  public void getSites() {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    site.setId("id");
    site.setName("site");
    site.setUrl("https://localhost:1234");

    when(siteManager.objects()).thenReturn(Stream.of(site));
    ListField<ReplicationSiteField> sites = utils.getSites();
    ReplicationSiteField field = sites.getList().get(0);
    assertThat(field.id(), is(site.getId()));
    assertThat(field.name(), is(site.getName()));
    assertThat(field.address().url(), is(site.getUrl()));
  }

  @Test
  public void deleteSite() {
    assertThat(utils.deleteSite("id"), is(true));
    verify(siteManager).remove("id");
  }

  @Test
  public void deleteSiteFailed() {
    doThrow(new NotFoundException()).when(siteManager).remove(anyString());
    assertThat(utils.deleteSite("id"), is(false));
    verify(siteManager).remove("id");
  }

  @Test
  public void replicationConfigExists() {
    ReplicatorConfigImpl config = new ReplicatorConfigImpl();
    config.setName("test");
    when(configManager.objects()).thenReturn(Stream.of(config));
    assertThat(utils.replicationConfigExists("test"), is(true));
  }

  @Test
  public void replicationConfigDoesNotExist() {
    when(configManager.objects()).thenReturn(Stream.empty());
    assertThat(utils.replicationConfigExists("test"), is(false));
  }

  @Test
  public void siteExists() {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    site.setId("id");
    site.setName("site");
    site.setUrl("https://localhost:1234");

    when(siteManager.objects()).thenReturn(Stream.of(site));
    assertThat(utils.isDuplicateSiteName("site"), is(true));
  }

  @Test
  public void siteDoesNotExist() {
    ReplicationSiteImpl site = new ReplicationSiteImpl();
    site.setId("id");
    site.setName("site");
    site.setUrl("https://localhost:1234");

    when(siteManager.objects()).thenReturn(Stream.empty());
    assertThat(utils.isDuplicateSiteName("site"), is(false));
  }

  @Test
  public void testUpdateReplicationStats() {
    // setup
    final String repName = "repName";
    StringField repNameField = new StringField();
    repNameField.setValue(repName);

    ReplicationStats stats = new ReplicationStats();
    stats.setPid("pid");
    stats.setStartTime(START_TIME);
    stats.setLastRun(LAST_RUN);
    stats.setLastSuccess(LAST_SUCCESS);
    stats.setDuration(10L);
    stats.setStatus(Status.SUCCESS.name());
    stats.setPushCount(1L);
    stats.setPullCount(2L);
    stats.setPushFailCount(3L);
    stats.setPullFailCount(4L);
    stats.setPushBytes(5L);
    stats.setPullBytes(6L);

    final String statusId = "statusId";
    ReplicationStatus status = mock(ReplicationStatus.class);
    when(status.getId()).thenReturn(statusId);

    when(history.getReplicationEvents(repName)).thenReturn(Collections.singletonList(status));

    ArgumentCaptor<ReplicationStatus> repStatus = ArgumentCaptor.forClass(ReplicationStatus.class);

    // when
    boolean updated = utils.updateReplicationStats(repNameField, stats);

    // then
    assertThat(updated, is(true));
    verify(history).addReplicationEvent(repStatus.capture());

    ReplicationStatus savedStatus = repStatus.getValue();
    assertThat(savedStatus.getId(), is("pid"));
    assertThat(savedStatus.getStartTime(), is(START_TIME));
    assertThat(savedStatus.getLastRun(), is(LAST_RUN));
    assertThat(savedStatus.getLastSuccess(), is(LAST_SUCCESS));
    assertThat(savedStatus.getDuration(), is(10L));
    assertThat(savedStatus.getStatus(), is(Status.SUCCESS));
    assertThat(savedStatus.getPushCount(), is(1L));
    assertThat(savedStatus.getPullCount(), is(2L));
    assertThat(savedStatus.getPushFailCount(), is(3L));
    assertThat(savedStatus.getPullFailCount(), is(4L));
    assertThat(savedStatus.getPushBytes(), is(5L));
    assertThat(savedStatus.getPullBytes(), is(6L));
  }

  @Test
  public void testUpdateReplicationFailToDeleteExistingStats() {
    // setup
    final String repName = "repName";
    StringField repNameField = new StringField();
    repNameField.setValue(repName);

    ReplicationStats stats = new ReplicationStats();
    stats.setPid("pid");
    stats.setStartTime(START_TIME);
    stats.setLastRun(LAST_RUN);
    stats.setLastSuccess(LAST_SUCCESS);
    stats.setDuration(10L);
    stats.setStatus(Status.SUCCESS.name());
    stats.setPushCount(1L);
    stats.setPullCount(2L);
    stats.setPushFailCount(3L);
    stats.setPullFailCount(4L);
    stats.setPushBytes(5L);
    stats.setPullBytes(6L);

    final String statusId = "statusId";
    ReplicationStatus status = mock(ReplicationStatus.class);
    when(status.getId()).thenReturn(statusId);

    when(history.getReplicationEvents(repName)).thenReturn(Collections.singletonList(status));
    doThrow(ReplicationPersistenceException.class)
        .when(history)
        .removeReplicationEvents(Collections.singleton(statusId));

    // when
    boolean updated = utils.updateReplicationStats(repNameField, stats);

    // then
    assertThat(updated, is(false));
  }

  @Test
  public void testUpdateReplicationStatsFailToSave() {
    // setup
    final String repName = "repName";
    StringField repNameField = new StringField();
    repNameField.setValue(repName);

    ReplicationStats stats = new ReplicationStats();
    stats.setPid("pid");
    stats.setStartTime(START_TIME);
    stats.setLastRun(LAST_RUN);
    stats.setLastSuccess(LAST_SUCCESS);
    stats.setDuration(10L);
    stats.setStatus(Status.SUCCESS.name());
    stats.setPushCount(1L);
    stats.setPullCount(2L);
    stats.setPushFailCount(3L);
    stats.setPullFailCount(4L);
    stats.setPushBytes(5L);
    stats.setPullBytes(6L);

    final String statusId = "statusId";
    ReplicationStatus status = mock(ReplicationStatus.class);
    when(status.getId()).thenReturn(statusId);

    when(history.getReplicationEvents(repName)).thenReturn(Collections.singletonList(status));

    doThrow(ReplicationPersistenceException.class)
        .when(history)
        .addReplicationEvent(any(ReplicationStatus.class));

    // when
    boolean updated = utils.updateReplicationStats(repNameField, stats);

    // then
    assertThat(updated, is(false));
  }

  private AddressField getAddress(URL url) {
    AddressField address = new AddressField();
    address.url(url.toString());
    address.hostname(url.getHost());
    address.port(url.getPort());
    return address;
  }

  private AddressField mockAddress(String hostname, Integer port) {
    HostField hostField = mock(HostField.class);
    when(hostField.hostname()).thenReturn(hostname);
    when(hostField.port()).thenReturn(port);
    AddressField addressField = mock(AddressField.class);
    when(addressField.host()).thenReturn(hostField);
    return addressField;
  }
}
