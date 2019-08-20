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

import com.connexta.replication.api.impl.persistence.EmbeddedSolrServerFactory;
import com.connexta.replication.api.impl.persistence.pojo.ConfigPojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.time.Instant;
import java.util.Arrays;
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
import org.springframework.data.solr.UncategorizedSolrException;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.SolrClientFactory;
import org.springframework.test.context.junit4.SpringRunner;

@ComponentScan(
    basePackageClasses = ConfigRepository.class) // to make sure we pickup the repositories
@RunWith(SpringRunner.class)
public class ConfigRepositoryIntegrationTest {
  private static final int VERSION = 1;
  private static final int VERSION2 = 2;
  private static final int VERSION3 = 3;
  private static final String ID = "1234";
  private static final String ID2 = "1235";
  private static final String ID3 = "1236";
  private static final String NAME = "site.name";
  private static final String NAME2 = "site.name2";
  private static final String NAME3 = "site.name3";
  private static final boolean BIDIRECTIONAL = true;
  private static final boolean BIDIRECTIONAL2 = false;
  private static final boolean BIDIRECTIONAL3 = true;
  private static final String SOURCE = "source";
  private static final String SOURCE2 = "source2";
  private static final String SOURCE3 = "source3";
  private static final String DESTINATION = "destination";
  private static final String DESTINATION2 = "destination2";
  private static final String DESTINATION3 = "destination3";
  private static final String FILTER = "filter";
  private static final String FILTER2 = "filter2";
  private static final String FILTER3 = "filter3";
  private static final String DESCRIPTION = "description";
  private static final String DESCRIPTION2 = "description2";
  private static final String DESCRIPTION3 = "description3";
  private static final boolean SUSPENDED = false;
  private static final boolean SUSPENDED2 = true;
  private static final boolean SUSPENDED3 = false;
  private static final Instant LAST_METADATA_MODIFIED = Instant.ofEpochMilli(1L);
  private static final Instant LAST_METADATA_MODIFIED2 = Instant.ofEpochMilli(2L);
  private static final Instant LAST_METADATA_MODIFIED3 = Instant.ofEpochMilli(3L);

  private static final ConfigPojo POJO =
      new ConfigPojo()
          .setVersion(ConfigRepositoryIntegrationTest.VERSION)
          .setId(ConfigRepositoryIntegrationTest.ID)
          .setName(ConfigRepositoryIntegrationTest.NAME)
          .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL)
          .setSource(ConfigRepositoryIntegrationTest.SOURCE)
          .setDestination(ConfigRepositoryIntegrationTest.DESTINATION)
          .setFilter(ConfigRepositoryIntegrationTest.FILTER)
          .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION)
          .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED)
          .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED);
  private static final ConfigPojo POJO2 =
      new ConfigPojo()
          .setVersion(ConfigRepositoryIntegrationTest.VERSION2)
          .setId(ConfigRepositoryIntegrationTest.ID2)
          .setName(ConfigRepositoryIntegrationTest.NAME2)
          .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL2)
          .setSource(ConfigRepositoryIntegrationTest.SOURCE2)
          .setDestination(ConfigRepositoryIntegrationTest.DESTINATION2)
          .setFilter(ConfigRepositoryIntegrationTest.FILTER2)
          .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION2)
          .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED2)
          .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED2);
  private static final ConfigPojo POJO3 =
      new ConfigPojo()
          .setVersion(ConfigRepositoryIntegrationTest.VERSION3)
          .setId(ConfigRepositoryIntegrationTest.ID3)
          .setName(ConfigRepositoryIntegrationTest.NAME3)
          .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL3)
          .setSource(ConfigRepositoryIntegrationTest.SOURCE3)
          .setDestination(ConfigRepositoryIntegrationTest.DESTINATION3)
          .setFilter(ConfigRepositoryIntegrationTest.FILTER3)
          .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION3)
          .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED3)
          .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED3);

  @TestConfiguration
  @EnableSolrRepositories(basePackages = "com.connexta.replication")
  static class ConfigRepositoryTestConfiguration {
    @Bean
    SolrClientFactory solrFactory() {
      return new EmbeddedSolrServerFactory("classpath:solr", ConfigPojo.COLLECTION);
    }

    @Bean
    SolrClient solrClient(SolrClientFactory solrFactory) {
      return solrFactory.getSolrClient();
    }
  }

  @After
  public void cleanup() {
    repo.deleteAll();
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Autowired private ConfigRepository repo;

  @Test
  public void testPojoPersistenceWhenIdIsNotDefined() throws Exception {
    exception.expect(UncategorizedSolrException.class);
    exception.expectMessage(Matchers.containsString("missing mandatory unique"));

    final ConfigPojo pojo =
        new ConfigPojo()
            .setVersion(ConfigRepositoryIntegrationTest.VERSION)
            .setName(ConfigRepositoryIntegrationTest.NAME)
            .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL)
            .setSource(ConfigRepositoryIntegrationTest.SOURCE)
            .setDestination(ConfigRepositoryIntegrationTest.DESTINATION)
            .setFilter(ConfigRepositoryIntegrationTest.FILTER)
            .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION)
            .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED)
            .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED);

    repo.save(pojo);
  }

  @Test
  public void testPojoPersistenceWhenNameIsNotDefined() throws Exception {
    final ConfigPojo pojo =
        new ConfigPojo()
            .setVersion(ConfigRepositoryIntegrationTest.VERSION)
            .setId(ConfigRepositoryIntegrationTest.ID)
            .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL)
            .setSource(ConfigRepositoryIntegrationTest.SOURCE)
            .setDestination(ConfigRepositoryIntegrationTest.DESTINATION)
            .setFilter(ConfigRepositoryIntegrationTest.FILTER)
            .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION)
            .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED)
            .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED);
    final ConfigPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ConfigPojo> loaded = repo.findById(ConfigRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenSourceIsNotDefined() throws Exception {
    final ConfigPojo pojo =
        new ConfigPojo()
            .setVersion(ConfigRepositoryIntegrationTest.VERSION)
            .setId(ConfigRepositoryIntegrationTest.ID)
            .setName(ConfigRepositoryIntegrationTest.NAME)
            .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL)
            .setDestination(ConfigRepositoryIntegrationTest.DESTINATION)
            .setFilter(ConfigRepositoryIntegrationTest.FILTER)
            .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION)
            .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED)
            .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED);
    final ConfigPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ConfigPojo> loaded = repo.findById(ConfigRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenDestinationIsNotDefined() throws Exception {
    final ConfigPojo pojo =
        new ConfigPojo()
            .setVersion(ConfigRepositoryIntegrationTest.VERSION)
            .setId(ConfigRepositoryIntegrationTest.ID)
            .setName(ConfigRepositoryIntegrationTest.NAME)
            .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL)
            .setSource(ConfigRepositoryIntegrationTest.SOURCE)
            .setFilter(ConfigRepositoryIntegrationTest.FILTER)
            .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION)
            .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED)
            .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED);
    final ConfigPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ConfigPojo> loaded = repo.findById(ConfigRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenFilterIsNotDefined() throws Exception {
    final ConfigPojo pojo =
        new ConfigPojo()
            .setVersion(ConfigRepositoryIntegrationTest.VERSION)
            .setId(ConfigRepositoryIntegrationTest.ID)
            .setName(ConfigRepositoryIntegrationTest.NAME)
            .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL)
            .setSource(ConfigRepositoryIntegrationTest.SOURCE)
            .setDestination(ConfigRepositoryIntegrationTest.DESTINATION)
            .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION)
            .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED)
            .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED);
    final ConfigPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ConfigPojo> loaded = repo.findById(ConfigRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenDescriptionsNotDefined() throws Exception {
    final ConfigPojo pojo =
        new ConfigPojo()
            .setVersion(ConfigRepositoryIntegrationTest.VERSION)
            .setId(ConfigRepositoryIntegrationTest.ID)
            .setName(ConfigRepositoryIntegrationTest.NAME)
            .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL)
            .setSource(ConfigRepositoryIntegrationTest.SOURCE)
            .setDestination(ConfigRepositoryIntegrationTest.DESTINATION)
            .setFilter(ConfigRepositoryIntegrationTest.FILTER)
            .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED)
            .setLastMetadataModified(ConfigRepositoryIntegrationTest.LAST_METADATA_MODIFIED);
    final ConfigPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ConfigPojo> loaded = repo.findById(ConfigRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenLastMetadataModifiedIsNotDefined() throws Exception {
    final ConfigPojo pojo =
        new ConfigPojo()
            .setVersion(ConfigRepositoryIntegrationTest.VERSION)
            .setId(ConfigRepositoryIntegrationTest.ID)
            .setName(ConfigRepositoryIntegrationTest.NAME)
            .setBidirectional(ConfigRepositoryIntegrationTest.BIDIRECTIONAL)
            .setSource(ConfigRepositoryIntegrationTest.SOURCE)
            .setDestination(ConfigRepositoryIntegrationTest.DESTINATION)
            .setFilter(ConfigRepositoryIntegrationTest.FILTER)
            .setDescription(ConfigRepositoryIntegrationTest.DESCRIPTION)
            .setSuspended(ConfigRepositoryIntegrationTest.SUSPENDED);
    final ConfigPojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<ConfigPojo> loaded = repo.findById(ConfigRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenAllFieldsAreDefined() throws Exception {
    final ConfigPojo saved = repo.save(ConfigRepositoryIntegrationTest.POJO);

    Assert.assertThat(saved, Matchers.equalTo(ConfigRepositoryIntegrationTest.POJO));
    Assert.assertThat(
        repo.findById(ConfigRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ConfigRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testSave() throws Exception {
    final ConfigPojo saved = repo.save(ConfigRepositoryIntegrationTest.POJO);

    Assert.assertThat(saved, Matchers.equalTo(ConfigRepositoryIntegrationTest.POJO));
    Assert.assertThat(
        repo.findById(ConfigRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ConfigRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testSaveAll() throws Exception {
    final Iterable<ConfigPojo> saved =
        repo.saveAll(
            Arrays.asList(
                ConfigRepositoryIntegrationTest.POJO, ConfigRepositoryIntegrationTest.POJO2));

    Assert.assertThat(
        saved,
        Matchers.contains(
            ConfigRepositoryIntegrationTest.POJO, ConfigRepositoryIntegrationTest.POJO2));
    Assert.assertThat(
        repo.findById(ConfigRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ConfigRepositoryIntegrationTest.POJO)));
    Assert.assertThat(
        repo.findById(ConfigRepositoryIntegrationTest.ID2),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ConfigRepositoryIntegrationTest.POJO2)));
  }

  @Test
  public void testFindById() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);

    Assert.assertThat(
        repo.findById(ConfigRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(ConfigRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testFindByIdWhenNotFound() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);

    Assert.assertThat(
        repo.findById(ConfigRepositoryIntegrationTest.ID2), OptionalMatchers.isEmpty());
  }

  @Test
  public void testExistById() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);

    Assert.assertThat(repo.existsById(ConfigRepositoryIntegrationTest.ID), Matchers.equalTo(true));
  }

  @Test
  public void testExistByIdWhenNotFound() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);

    Assert.assertThat(
        repo.existsById(ConfigRepositoryIntegrationTest.ID2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testFindAll() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);

    Assert.assertThat(
        repo.findAll(),
        Matchers.containsInAnyOrder(
            ConfigRepositoryIntegrationTest.POJO, ConfigRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testFindAllWhenNoneDefined() throws Exception {
    Assert.assertThat(repo.findAll(), Matchers.emptyIterable());
  }

  @Test
  public void testFindAllById() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);
    repo.save(ConfigRepositoryIntegrationTest.POJO3);

    Assert.assertThat(
        repo.findAllById(
            Arrays.asList(
                ConfigRepositoryIntegrationTest.ID,
                ConfigRepositoryIntegrationTest.ID2,
                "unknown-id")),
        Matchers.containsInAnyOrder(
            ConfigRepositoryIntegrationTest.POJO, ConfigRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testCount() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);

    Assert.assertThat(repo.count(), Matchers.equalTo(2L));
  }

  @Test
  public void testCountWhenNoneDefined() throws Exception {
    Assert.assertThat(repo.count(), Matchers.equalTo(0L));
  }

  @Test
  public void testDeleteById() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);

    repo.deleteById(ConfigRepositoryIntegrationTest.ID);
    Assert.assertThat(repo.findAll(), Matchers.contains(ConfigRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteByIdWhenNotFound() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);

    repo.deleteById(ConfigRepositoryIntegrationTest.ID2);
    Assert.assertThat(repo.findAll(), Matchers.contains(ConfigRepositoryIntegrationTest.POJO));
  }

  @Test
  public void testDelete() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);

    repo.delete(ConfigRepositoryIntegrationTest.POJO);
    Assert.assertThat(repo.findAll(), Matchers.contains(ConfigRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteWhenNotFound() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);

    repo.delete(ConfigRepositoryIntegrationTest.POJO2);
    Assert.assertThat(repo.findAll(), Matchers.contains(ConfigRepositoryIntegrationTest.POJO));
  }

  @Test
  public void testDeleteAllByObj() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);
    repo.save(ConfigRepositoryIntegrationTest.POJO3);

    repo.deleteAll(
        Arrays.asList(ConfigRepositoryIntegrationTest.POJO, ConfigRepositoryIntegrationTest.POJO3));
    Assert.assertThat(repo.findAll(), Matchers.contains(ConfigRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteAllByObjWhenOneNotDefined() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);

    repo.deleteAll(
        Arrays.asList(ConfigRepositoryIntegrationTest.POJO, ConfigRepositoryIntegrationTest.POJO3));
    Assert.assertThat(repo.findAll(), Matchers.contains(ConfigRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteAll() throws Exception {
    repo.save(ConfigRepositoryIntegrationTest.POJO);
    repo.save(ConfigRepositoryIntegrationTest.POJO2);
    repo.save(ConfigRepositoryIntegrationTest.POJO3);

    Assert.assertThat(repo.count(), Matchers.equalTo(3L));
    repo.deleteAll();
    Assert.assertThat(repo.count(), Matchers.equalTo(0L));
  }
}
