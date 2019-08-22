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
package com.connexta.replication.api.impl.persistence.spring;

import com.connexta.replication.api.Action;
import com.connexta.replication.api.Status;
import com.connexta.replication.api.impl.persistence.EmbeddedSolrServerFactory;
import com.connexta.replication.api.impl.persistence.pojo.ItemPojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.solr.client.solrj.SolrClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.UncategorizedSolrException;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.SolrClientFactory;
import org.springframework.test.context.junit4.SpringRunner;

@ComponentScan(basePackageClasses = ItemRepository.class) // to make sure we pickup the repositories
@RunWith(SpringRunner.class)
public class ItemRepositoryIntegrationTest {
  private static final int VERSION = 1;
  private static final int VERSION2 = 2;
  private static final int VERSION3 = 3;
  private static final String ID = "1234";
  private static final String ID2 = "1235";
  private static final String ID3 = "1236";
  private static final String ID11 = "12341";
  private static final String ID12 = "12342";
  private static final String METADATA_ID = "m123";
  private static final String METADATA_ID2 = "m124";
  private static final String METADATA_ID3 = "m125";
  private static final Date RESOURCE_MODIFIED = new Date(1L);
  private static final Date RESOURCE_MODIFIED2 = new Date(12L);
  private static final Date RESOURCE_MODIFIED3 = new Date(13L);
  private static final Date METADATA_MODIFIED = new Date(2L);
  private static final Date METADATA_MODIFIED2 = new Date(22L);
  private static final Date METADATA_MODIFIED3 = new Date(23L);
  private static final Date DONE_TIME = new Date(3L);
  private static final Date DONE_TIME2 = new Date(32L);
  private static final Date DONE_TIME3 = new Date(33L);
  private static final Date DONE_TIME11 = new Date(3000L);
  private static final Date DONE_TIME12 = new Date(30000L);
  private static final String SOURCE = "source";
  private static final String SOURCE2 = "source2";
  private static final String SOURCE3 = "source3";
  private static final String DESTINATION = "destination";
  private static final String DESTINATION2 = "destination2";
  private static final String DESTINATION3 = "destination3";
  private static final String DESTINATION12 = "destination12";
  private static final String CONFIG_ID = "config.id";
  private static final String CONFIG_ID2 = "config.id2";
  private static final String CONFIG_ID3 = "config.id3";
  private static final long METADATA_SIZE = 123L;
  private static final long METADATA_SIZE2 = 1232L;
  private static final long METADATA_SIZE3 = 1233L;
  private static final long RESOURCE_SIZE = 234L;
  private static final long RESOURCE_SIZE2 = 2342L;
  private static final long RESOURCE_SIZE3 = 2343L;
  private static final Date START_TIME = new Date(4L);
  private static final Date START_TIME2 = new Date(42L);
  private static final Date START_TIME3 = new Date(43L);
  private static final String STATUS = Status.FAILURE.name();
  private static final String STATUS2 = Status.CONNECTION_LOST.name();
  private static final String STATUS3 = Status.SUCCESS.name();
  private static final String ACTION = Action.DELETE.name();
  private static final String ACTION2 = Action.CREATE.name();
  private static final String ACTION3 = Action.UPDATE.name();

  private static final ItemPojo POJO =
      new ItemPojo()
          .setVersion(ItemRepositoryIntegrationTest.VERSION)
          .setId(ItemRepositoryIntegrationTest.ID)
          .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
          .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
          .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
          .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
          .setSource(ItemRepositoryIntegrationTest.SOURCE)
          .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
          .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
          .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
          .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
          .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
          .setStatus(ItemRepositoryIntegrationTest.STATUS)
          .setAction(ItemRepositoryIntegrationTest.ACTION);
  private static final ItemPojo POJO2 =
      new ItemPojo()
          .setVersion(ItemRepositoryIntegrationTest.VERSION2)
          .setId(ItemRepositoryIntegrationTest.ID2)
          .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID2)
          .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED2)
          .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED2)
          .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME2)
          .setSource(ItemRepositoryIntegrationTest.SOURCE2)
          .setDestination(ItemRepositoryIntegrationTest.DESTINATION2)
          .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID2)
          .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE2)
          .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE2)
          .setStartTime(ItemRepositoryIntegrationTest.START_TIME2)
          .setStatus(ItemRepositoryIntegrationTest.STATUS2)
          .setAction(ItemRepositoryIntegrationTest.ACTION2);
  private static final ItemPojo POJO3 =
      new ItemPojo()
          .setVersion(ItemRepositoryIntegrationTest.VERSION3)
          .setId(ItemRepositoryIntegrationTest.ID3)
          .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID3)
          .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED3)
          .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED3)
          .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME3)
          .setSource(ItemRepositoryIntegrationTest.SOURCE3)
          .setDestination(ItemRepositoryIntegrationTest.DESTINATION3)
          .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID3)
          .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE3)
          .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE3)
          .setStartTime(ItemRepositoryIntegrationTest.START_TIME3)
          .setStatus(ItemRepositoryIntegrationTest.STATUS3)
          .setAction(ItemRepositoryIntegrationTest.ACTION3);
  private static final ItemPojo POJO11 =
      new ItemPojo()
          .setVersion(ItemRepositoryIntegrationTest.VERSION)
          .setId(ItemRepositoryIntegrationTest.ID11)
          .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
          .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
          .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
          .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME11)
          .setSource(ItemRepositoryIntegrationTest.SOURCE)
          .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
          .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
          .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
          .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
          .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
          .setStatus(ItemRepositoryIntegrationTest.STATUS)
          .setAction(ItemRepositoryIntegrationTest.ACTION);
  private static final ItemPojo POJO12 =
      new ItemPojo()
          .setVersion(ItemRepositoryIntegrationTest.VERSION)
          .setId(ItemRepositoryIntegrationTest.ID12)
          .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
          .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
          .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
          .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME12)
          .setSource(ItemRepositoryIntegrationTest.SOURCE)
          .setDestination(ItemRepositoryIntegrationTest.DESTINATION12)
          .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
          .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
          .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
          .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
          .setStatus(ItemRepositoryIntegrationTest.STATUS)
          .setAction(ItemRepositoryIntegrationTest.ACTION);

  @TestConfiguration
  @EnableSolrRepositories(basePackages = "com.connexta.replication")
  static class ReplicationItemRepositoryTestConfiguration {
    @Bean
    SolrClientFactory solrFactory() {
      return new EmbeddedSolrServerFactory("classpath:solr", ItemPojo.COLLECTION);
    }

    @Bean
    SolrClient solrClient(SolrClientFactory solrFactory) {
      return solrFactory.getSolrClient();
    }
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Autowired private ItemRepository repo;

  @After
  public void cleanup() {
    repo.deleteAll();
  }

  @Test
  public void testPojoPersistenceWhenIdIsNotDefined() throws Exception {
    exception.expect(UncategorizedSolrException.class);
    exception.expectMessage(Matchers.containsString("missing mandatory unique"));

    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);

    repo.save(pojo);
  }

  @Test
  public void testPojoPersistenceWhenMetadataIdIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenResourceModifiedIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenMetadataModifiedIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenDoneTimeIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenSourceIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenDestinationIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenConfigIdIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenMetadataSizeIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenResourceSizeIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenStartTimeIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStatus(ItemRepositoryIntegrationTest.STATUS)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenStatusIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setAction(ItemRepositoryIntegrationTest.ACTION);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenActionIsNotDefined() throws Exception {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemRepositoryIntegrationTest.VERSION)
            .setId(ItemRepositoryIntegrationTest.ID)
            .setMetadataId(ItemRepositoryIntegrationTest.METADATA_ID)
            .setResourceModified(ItemRepositoryIntegrationTest.RESOURCE_MODIFIED)
            .setMetadataModified(ItemRepositoryIntegrationTest.METADATA_MODIFIED)
            .setDoneTime(ItemRepositoryIntegrationTest.DONE_TIME)
            .setSource(ItemRepositoryIntegrationTest.SOURCE)
            .setDestination(ItemRepositoryIntegrationTest.DESTINATION)
            .setConfigId(ItemRepositoryIntegrationTest.CONFIG_ID)
            .setMetadataSize(ItemRepositoryIntegrationTest.METADATA_SIZE)
            .setResourceSize(ItemRepositoryIntegrationTest.RESOURCE_SIZE)
            .setStartTime(ItemRepositoryIntegrationTest.START_TIME)
            .setStatus(ItemRepositoryIntegrationTest.STATUS);
    final ItemPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ItemPojo> loaded = repo.findById(ItemRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenAllFieldsAreDefined() throws Exception {
    final ItemPojo saved = repo.save(ItemRepositoryIntegrationTest.POJO);

    Assert.assertThat(saved, Matchers.equalTo(ItemRepositoryIntegrationTest.POJO));
    Assert.assertThat(
        repo.findById(ItemRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ItemRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testSave() throws Exception {
    final ItemPojo saved = repo.save(ItemRepositoryIntegrationTest.POJO);

    Assert.assertThat(saved, Matchers.equalTo(ItemRepositoryIntegrationTest.POJO));
    Assert.assertThat(
        repo.findById(ItemRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ItemRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testSaveAll() throws Exception {
    final Iterable<ItemPojo> saved =
        repo.saveAll(
            Arrays.asList(ItemRepositoryIntegrationTest.POJO, ItemRepositoryIntegrationTest.POJO2));

    Assert.assertThat(
        saved,
        Matchers.contains(ItemRepositoryIntegrationTest.POJO, ItemRepositoryIntegrationTest.POJO2));
    Assert.assertThat(
        repo.findById(ItemRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ItemRepositoryIntegrationTest.POJO)));
    Assert.assertThat(
        repo.findById(ItemRepositoryIntegrationTest.ID2),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ItemRepositoryIntegrationTest.POJO2)));
  }

  @Test
  public void testFindById() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);

    Assert.assertThat(
        repo.findById(ItemRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ItemRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testFindByIdWhenNotFound() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);

    Assert.assertThat(repo.findById(ItemRepositoryIntegrationTest.ID2), OptionalMatchers.isEmpty());
  }

  @Test
  public void testExistById() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);

    Assert.assertThat(repo.existsById(ItemRepositoryIntegrationTest.ID), Matchers.equalTo(true));
  }

  @Test
  public void testExistByIdWhenNotFound() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);

    Assert.assertThat(
        repo.existsById(ItemRepositoryIntegrationTest.ID2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testFindAll() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);

    Assert.assertThat(
        repo.findAll(),
        Matchers.containsInAnyOrder(
            ItemRepositoryIntegrationTest.POJO, ItemRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testFindAllWhenNoneDefined() throws Exception {
    Assert.assertThat(repo.findAll(), Matchers.emptyIterable());
  }

  @Test
  public void testFindAllById() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);
    repo.save(ItemRepositoryIntegrationTest.POJO3);

    Assert.assertThat(
        repo.findAllById(
            Arrays.asList(
                ItemRepositoryIntegrationTest.ID, ItemRepositoryIntegrationTest.ID2, "unknown-id")),
        Matchers.containsInAnyOrder(
            ItemRepositoryIntegrationTest.POJO, ItemRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testCount() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);

    Assert.assertThat(repo.count(), Matchers.equalTo(2L));
  }

  @Test
  public void testCountWhenNoneDefined() throws Exception {
    Assert.assertThat(repo.count(), Matchers.equalTo(0L));
  }

  @Test
  public void testDeleteById() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);

    repo.deleteById(ItemRepositoryIntegrationTest.ID);
    Assert.assertThat(repo.findAll(), Matchers.contains(ItemRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteByIdWhenNotFound() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);

    repo.deleteById(ItemRepositoryIntegrationTest.ID2);
    Assert.assertThat(repo.findAll(), Matchers.contains(ItemRepositoryIntegrationTest.POJO));
  }

  @Test
  public void testDelete() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);

    repo.delete(ItemRepositoryIntegrationTest.POJO);
    Assert.assertThat(repo.findAll(), Matchers.contains(ItemRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteWhenNotFound() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);

    repo.delete(ItemRepositoryIntegrationTest.POJO2);
    Assert.assertThat(repo.findAll(), Matchers.contains(ItemRepositoryIntegrationTest.POJO));
  }

  @Test
  public void testDeleteAllByObj() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);
    repo.save(ItemRepositoryIntegrationTest.POJO3);

    repo.deleteAll(
        Arrays.asList(ItemRepositoryIntegrationTest.POJO, ItemRepositoryIntegrationTest.POJO3));
    Assert.assertThat(repo.findAll(), Matchers.contains(ItemRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteAllByObjWhenOneNotDefined() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);

    repo.deleteAll(
        Arrays.asList(ItemRepositoryIntegrationTest.POJO, ItemRepositoryIntegrationTest.POJO3));
    Assert.assertThat(repo.findAll(), Matchers.contains(ItemRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteAll() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);
    repo.save(ItemRepositoryIntegrationTest.POJO3);

    Assert.assertThat(repo.count(), Matchers.equalTo(3L));
    repo.deleteAll();
    Assert.assertThat(repo.count(), Matchers.equalTo(0L));
  }

  @Test
  public void testFindByConfigIdAndMetadataIdOrderByDoneTimeDesc() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO11);
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);
    repo.save(ItemRepositoryIntegrationTest.POJO3);
    repo.save(ItemRepositoryIntegrationTest.POJO12);

    final List<ItemPojo> page =
        repo.findByConfigIdAndMetadataIdOrderByDoneTimeDesc(
                ItemRepositoryIntegrationTest.CONFIG_ID,
                ItemRepositoryIntegrationTest.METADATA_ID,
                PageRequest.of(0, (int) repo.count()))
            .toList();

    Assert.assertThat(
        page,
        Matchers.contains(
            ItemRepositoryIntegrationTest.POJO12,
            ItemRepositoryIntegrationTest.POJO11,
            ItemRepositoryIntegrationTest.POJO));
  }

  @Test
  public void testFindByConfigId() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO11);
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);
    repo.save(ItemRepositoryIntegrationTest.POJO3);
    repo.save(ItemRepositoryIntegrationTest.POJO12);

    final List<ItemPojo> page =
        repo.findByConfigId(
                ItemRepositoryIntegrationTest.CONFIG_ID, PageRequest.of(0, (int) repo.count()))
            .toList();

    Assert.assertThat(
        page,
        Matchers.containsInAnyOrder(
            ItemRepositoryIntegrationTest.POJO,
            ItemRepositoryIntegrationTest.POJO11,
            ItemRepositoryIntegrationTest.POJO12));
  }

  @Test
  public void testDeleteByConfigId() throws Exception {
    repo.save(ItemRepositoryIntegrationTest.POJO11);
    repo.save(ItemRepositoryIntegrationTest.POJO);
    repo.save(ItemRepositoryIntegrationTest.POJO2);
    repo.save(ItemRepositoryIntegrationTest.POJO3);
    repo.save(ItemRepositoryIntegrationTest.POJO12);

    repo.deleteByConfigId(ItemRepositoryIntegrationTest.CONFIG_ID);

    Assert.assertThat(
        repo.findAll(),
        Matchers.containsInAnyOrder(
            ItemRepositoryIntegrationTest.POJO2, ItemRepositoryIntegrationTest.POJO3));
  }
}
