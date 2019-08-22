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

import com.connexta.ion.replication.api.InvalidFieldException;
import com.connexta.ion.replication.api.NonTransientReplicationPersistenceException;
import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.RecoverableReplicationPersistenceException;
import com.connexta.ion.replication.api.TransientReplicationPersistenceException;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteKind;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.connexta.replication.api.impl.persistence.spring.SiteRepository;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;

public class SiteManagerImplTest {
  private static final int VERSION = 1;
  private static final int VERSION2 = 2;
  private static final int VERSION3 = 3;
  private static final String ID = "1234";
  private static final String ID2 = "1235";
  private static final String ID3 = "1236";
  private static final String NAME = "site.name";
  private static final String NAME2 = "site.name2";
  private static final String NAME3 = "site.name3";
  private static final String DESCRIPTION = "site.description";
  private static final String DESCRIPTION2 = "site.description2";
  private static final String DESCRIPTION3 = "site.description3";
  private static final URL URL;
  private static final URL URL2;
  private static final URL URL3;
  private static final SiteType TYPE = SiteType.DDF;
  private static final SiteType TYPE2 = SiteType.ION;
  private static final SiteType TYPE3 = SiteType.UNKNOWN;
  private static final SiteKind KIND = SiteKind.REGIONAL;
  private static final SiteKind KIND2 = SiteKind.TACTICAL;
  private static final SiteKind KIND3 = SiteKind.UNKNOWN;
  private static final Duration POLLING_PERIOD = Duration.ofMinutes(10L);
  private static final Duration POLLING_PERIOD2 = null;
  private static final Duration POLLING_PERIOD3 = Duration.ofMinutes(30L);
  private static final long POLLING_PERIOD_MILLIS = SiteManagerImplTest.POLLING_PERIOD.toMillis();
  private static final long POLLING_PERIOD_MILLIS2 = 0L;
  private static final long POLLING_PERIOD_MILLIS3 = SiteManagerImplTest.POLLING_PERIOD3.toMillis();
  private static final int PARALLELISM_FACTOR = 1;
  private static final int PARALLELISM_FACTOR2 = 0;
  private static final int PARALLELISM_FACTOR3 = 3;

  static {
    try {
      URL = new URL("http://localhost/service");
      URL2 = new URL("http://localhost/service2");
      URL3 = new URL("http://localhost/service3");
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  private final SiteImpl persistable = new SiteImpl();
  private final SiteImpl persistable2 = new SiteImpl();
  private final SiteImpl persistable3 = new SiteImpl();

  {
    persistable.setId(SiteManagerImplTest.ID);
    persistable.setName(SiteManagerImplTest.NAME);
    persistable.setDescription(SiteManagerImplTest.DESCRIPTION);
    persistable.setUrl(SiteManagerImplTest.URL);
    persistable.setType(SiteManagerImplTest.TYPE);
    persistable.setKind(SiteManagerImplTest.KIND);
    persistable.setPollingPeriod(SiteManagerImplTest.POLLING_PERIOD);
    persistable.setParallelismFactor(SiteManagerImplTest.PARALLELISM_FACTOR);

    persistable2.setId(SiteManagerImplTest.ID2);
    persistable2.setName(SiteManagerImplTest.NAME2);
    persistable2.setDescription(SiteManagerImplTest.DESCRIPTION2);
    persistable2.setUrl(SiteManagerImplTest.URL2);
    persistable2.setType(SiteManagerImplTest.TYPE2);
    persistable2.setKind(SiteManagerImplTest.KIND2);
    persistable2.setPollingPeriod(SiteManagerImplTest.POLLING_PERIOD2);
    persistable2.setParallelismFactor(SiteManagerImplTest.PARALLELISM_FACTOR2);

    persistable3.setId(SiteManagerImplTest.ID3);
    persistable3.setName(SiteManagerImplTest.NAME3);
    persistable3.setDescription(SiteManagerImplTest.DESCRIPTION3);
    persistable3.setUrl(SiteManagerImplTest.URL3);
    persistable3.setType(SiteManagerImplTest.TYPE3);
    persistable3.setKind(SiteManagerImplTest.KIND3);
    persistable3.setPollingPeriod(SiteManagerImplTest.POLLING_PERIOD3);
    persistable3.setParallelismFactor(SiteManagerImplTest.PARALLELISM_FACTOR3);
  }

  private final SitePojo pojo =
      new SitePojo()
          .setVersion(SiteManagerImplTest.VERSION)
          .setId(SiteManagerImplTest.ID)
          .setName(SiteManagerImplTest.NAME)
          .setDescription(SiteManagerImplTest.DESCRIPTION)
          .setUrl(SiteManagerImplTest.URL.toString())
          .setType(SiteManagerImplTest.TYPE.name())
          .setKind(SiteManagerImplTest.KIND.name())
          .setPollingPeriod(SiteManagerImplTest.POLLING_PERIOD_MILLIS)
          .setParallelismFactor(SiteManagerImplTest.PARALLELISM_FACTOR);
  private final SitePojo pojo2 =
      new SitePojo()
          .setVersion(SiteManagerImplTest.VERSION2)
          .setId(SiteManagerImplTest.ID2)
          .setName(SiteManagerImplTest.NAME2)
          .setDescription(SiteManagerImplTest.DESCRIPTION2)
          .setUrl(SiteManagerImplTest.URL2.toString())
          .setType(SiteManagerImplTest.TYPE2.name())
          .setKind(SiteManagerImplTest.KIND2.name())
          .setPollingPeriod(SiteManagerImplTest.POLLING_PERIOD_MILLIS2)
          .setParallelismFactor(SiteManagerImplTest.PARALLELISM_FACTOR2);
  private final SitePojo pojo3 =
      new SitePojo()
          .setVersion(SiteManagerImplTest.VERSION3)
          .setId(SiteManagerImplTest.ID3)
          .setName(SiteManagerImplTest.NAME3)
          .setDescription(SiteManagerImplTest.DESCRIPTION3)
          .setUrl(SiteManagerImplTest.URL3.toString())
          .setType(SiteManagerImplTest.TYPE3.name())
          .setKind(SiteManagerImplTest.KIND3.name())
          .setPollingPeriod(SiteManagerImplTest.POLLING_PERIOD_MILLIS3)
          .setParallelismFactor(SiteManagerImplTest.PARALLELISM_FACTOR3);

  private final SiteRepository repo = Mockito.mock(SiteRepository.class);
  private final SiteManagerImpl mgr = new SiteManagerImpl(repo);

  @Test
  public void testGet() throws Exception {
    Mockito.when(repo.findById(SiteManagerImplTest.ID)).thenReturn(Optional.of(pojo));

    Assert.assertThat(mgr.get(SiteManagerImplTest.ID), Matchers.equalTo(persistable));
  }

  @Test(expected = NotFoundException.class)
  public void testGetWhenNoneFound() throws Exception {
    Mockito.when(repo.findById(SiteManagerImplTest.ID)).thenReturn(Optional.empty());

    mgr.get(SiteManagerImplTest.ID);
  }

  @Test(expected = InvalidFieldException.class)
  public void testGetWhenFailedPojoIsInvalid() throws Exception {
    pojo.setId(null);

    Mockito.when(repo.findById(SiteManagerImplTest.ID)).thenReturn(Optional.of(pojo));

    mgr.get(SiteManagerImplTest.ID);
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testGetWithNonTransientFailure() throws Exception {
    Mockito.when(repo.findById(SiteManagerImplTest.ID))
        .thenThrow(new NonTransientDataAccessResourceException("testing"));

    mgr.get(SiteManagerImplTest.ID);
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testGetWithTransientFailure() throws Exception {
    Mockito.when(repo.findById(SiteManagerImplTest.ID))
        .thenThrow(new TransientDataAccessResourceException("testing"));

    mgr.get(SiteManagerImplTest.ID);
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testGetWithRecoverableFailure() throws Exception {
    Mockito.when(repo.findById(SiteManagerImplTest.ID))
        .thenThrow(new RecoverableDataAccessException("testing"));

    mgr.get(SiteManagerImplTest.ID);
  }

  @Test
  public void testObjects() {
    Mockito.when(repo.findAll()).thenReturn(Arrays.asList(pojo, pojo2, pojo3));

    Assert.assertThat(
        mgr.objects().collect(Collectors.toList()),
        Matchers.containsInAnyOrder(persistable, persistable2, persistable3));
  }

  @Test
  public void testObjectsWhenNoneFound() {
    Mockito.when(repo.findAll()).thenReturn(Collections.emptyList());

    Assert.assertThat(mgr.objects().collect(Collectors.toList()), Matchers.emptyIterable());
  }

  @Test(expected = InvalidFieldException.class)
  public void testObjectsWhenPojoIsInvalid() {
    pojo2.setId(null);

    Mockito.when(repo.findAll()).thenReturn(Arrays.asList(pojo, pojo2, pojo3));

    mgr.objects().collect(Collectors.toList());
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testObjectsWithNonTransientFailure() {
    Mockito.when(repo.findAll()).thenThrow(new NonTransientDataAccessResourceException("testing"));

    mgr.objects().count();
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testObjectsWithTransientFailure() {
    Mockito.when(repo.findAll()).thenThrow(new TransientDataAccessResourceException("testing"));

    mgr.objects().count();
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testObjectsWithRecoverableFailure() {
    Mockito.when(repo.findAll()).thenThrow(new RecoverableDataAccessException("testing"));

    mgr.objects().collect(Collectors.toList());
  }

  @Test
  public void testSave() throws Exception {
    mgr.save(persistable);

    final ArgumentCaptor<SitePojo> saved = ArgumentCaptor.forClass(SitePojo.class);

    Mockito.verify(repo).save(saved.capture());

    Assert.assertThat(saved.getValue(), Matchers.equalTo(pojo));
  }

  @Test(expected = InvalidFieldException.class)
  public void testSaveWhenPersistableIsInvalid() throws Exception {
    persistable.setId(null);

    mgr.save(persistable);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSaveWhenNotTheRightClass() {
    final Site site = Mockito.mock(Site.class);

    mgr.save(site);
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testSaveWithNonTransientFailure() throws Exception {
    Mockito.doThrow(new NonTransientDataAccessResourceException("testing"))
        .when(repo)
        .save(Mockito.any());

    mgr.save(persistable);
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testSaveWithTransientFailure() throws Exception {
    Mockito.doThrow(new TransientDataAccessResourceException("testing"))
        .when(repo)
        .save(Mockito.any());

    mgr.save(persistable);
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testSaveWithRecoverableFailure() throws Exception {
    Mockito.doThrow(new RecoverableDataAccessException("testing")).when(repo).save(Mockito.any());

    mgr.save(persistable);
  }

  @Test
  public void testRemove() {
    mgr.remove(SiteManagerImplTest.ID);

    Mockito.verify(repo).deleteById(SiteManagerImplTest.ID);
  }

  @Test(expected = NonTransientReplicationPersistenceException.class)
  public void testRemoveWithNonTransientFailure() throws Exception {
    Mockito.doThrow(new NonTransientDataAccessResourceException("testing"))
        .when(repo)
        .deleteById(SiteManagerImplTest.ID);

    mgr.remove(SiteManagerImplTest.ID);
  }

  @Test(expected = TransientReplicationPersistenceException.class)
  public void testRemoveWithTransientFailure() throws Exception {
    Mockito.doThrow(new TransientDataAccessResourceException("testing"))
        .when(repo)
        .deleteById(SiteManagerImplTest.ID);

    mgr.remove(SiteManagerImplTest.ID);
  }

  @Test(expected = RecoverableReplicationPersistenceException.class)
  public void testRemoveWithRecoverableFailure() throws Exception {
    Mockito.doThrow(new RecoverableDataAccessException("testing"))
        .when(repo)
        .deleteById(SiteManagerImplTest.ID);

    mgr.remove(SiteManagerImplTest.ID);
  }
}
