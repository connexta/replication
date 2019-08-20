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

import com.connexta.ion.replication.api.NodeAdapterType;
import com.connexta.replication.api.impl.persistence.EmbeddedSolrServerFactory;
import com.connexta.replication.api.impl.persistence.pojo.SitePojo;
import com.github.npathai.hamcrestopt.OptionalMatchers;
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

@ComponentScan(basePackageClasses = SiteRepository.class) // to make sure we pickup the repositories
@RunWith(SpringRunner.class)
public class SiteRepositoryIntegrationTest {
  private static final int VERSION = 1;
  private static final int VERSION2 = 2;
  private static final int VERSION3 = 3;
  private static final String ID = "1234";
  private static final String ID2 = "1235";
  private static final String ID3 = "1236";
  private static final boolean REMOTE_MANAGED = true;
  private static final boolean REMOTE_MANAGED2 = false;
  private static final boolean REMOTE_MANAGED3 = true;
  private static final String NAME = "site.name";
  private static final String NAME2 = "site.name2";
  private static final String NAME3 = "site.name3";
  private static final String URL = "http://localhost/service";
  private static final String URL2 = "http://localhost/service2";
  private static final String URL3 = "http://localhost/service3";
  private static final String TYPE = NodeAdapterType.DDF.name();
  private static final String TYPE2 = NodeAdapterType.ION.name();
  private static final String TYPE3 = NodeAdapterType.ION.name();

  private static final SitePojo POJO =
      new SitePojo()
          .setVersion(SiteRepositoryIntegrationTest.VERSION)
          .setId(SiteRepositoryIntegrationTest.ID)
          .setRemoteManaged(SiteRepositoryIntegrationTest.REMOTE_MANAGED)
          .setName(SiteRepositoryIntegrationTest.NAME)
          .setUrl(SiteRepositoryIntegrationTest.URL)
          .setType(SiteRepositoryIntegrationTest.TYPE);
  private static final SitePojo POJO2 =
      new SitePojo()
          .setVersion(SiteRepositoryIntegrationTest.VERSION2)
          .setId(SiteRepositoryIntegrationTest.ID2)
          .setRemoteManaged(SiteRepositoryIntegrationTest.REMOTE_MANAGED2)
          .setName(SiteRepositoryIntegrationTest.NAME2)
          .setUrl(SiteRepositoryIntegrationTest.URL2)
          .setType(SiteRepositoryIntegrationTest.TYPE2);
  private static final SitePojo POJO3 =
      new SitePojo()
          .setVersion(SiteRepositoryIntegrationTest.VERSION3)
          .setId(SiteRepositoryIntegrationTest.ID3)
          .setRemoteManaged(SiteRepositoryIntegrationTest.REMOTE_MANAGED3)
          .setName(SiteRepositoryIntegrationTest.NAME3)
          .setUrl(SiteRepositoryIntegrationTest.URL3)
          .setType(SiteRepositoryIntegrationTest.TYPE3);

  @TestConfiguration
  @EnableSolrRepositories(basePackages = "com.connexta.replication")
  static class SiteRepositoryTestConfiguration {
    @Bean
    SolrClientFactory solrFactory() {
      return new EmbeddedSolrServerFactory("classpath:solr", SitePojo.COLLECTION);
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

  @Autowired private SiteRepository repo;

  @Test
  public void testPojoPersistenceWhenIdIsNotDefined() throws Exception {
    exception.expect(UncategorizedSolrException.class);
    exception.expectMessage(Matchers.containsString("missing mandatory unique"));

    final SitePojo pojo =
        new SitePojo()
            .setVersion(SiteRepositoryIntegrationTest.VERSION)
            .setRemoteManaged(SiteRepositoryIntegrationTest.REMOTE_MANAGED)
            .setUrl(SiteRepositoryIntegrationTest.URL)
            .setName(SiteRepositoryIntegrationTest.NAME)
            .setType(SiteRepositoryIntegrationTest.TYPE);

    repo.save(pojo);
  }

  @Test
  public void testPojoPersistenceWhenNameIsNotDefined() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setVersion(SiteRepositoryIntegrationTest.VERSION)
            .setId(SiteRepositoryIntegrationTest.ID)
            .setRemoteManaged(SiteRepositoryIntegrationTest.REMOTE_MANAGED)
            .setUrl(SiteRepositoryIntegrationTest.URL)
            .setType(SiteRepositoryIntegrationTest.TYPE);
    final SitePojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<SitePojo> loaded = repo.findById(SiteRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenUrlIsNotDefined() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setVersion(SiteRepositoryIntegrationTest.VERSION)
            .setId(SiteRepositoryIntegrationTest.ID)
            .setRemoteManaged(SiteRepositoryIntegrationTest.REMOTE_MANAGED)
            .setName(SiteRepositoryIntegrationTest.NAME)
            .setType(SiteRepositoryIntegrationTest.TYPE);
    final SitePojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    final Optional<SitePojo> loaded = repo.findById(SiteRepositoryIntegrationTest.ID);

    Assert.assertThat(loaded, OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenTypeIsNotDefined() throws Exception {
    final SitePojo pojo =
        new SitePojo()
            .setVersion(SiteRepositoryIntegrationTest.VERSION)
            .setId(SiteRepositoryIntegrationTest.ID)
            .setRemoteManaged(SiteRepositoryIntegrationTest.REMOTE_MANAGED)
            .setName(SiteRepositoryIntegrationTest.NAME)
            .setUrl(SiteRepositoryIntegrationTest.URL);
    final SitePojo saved = repo.save(pojo);

    Assert.assertThat(saved, Matchers.equalTo(pojo));
    Assert.assertThat(
        repo.findById(SiteRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenAllFieldsAreDefined() throws Exception {
    final SitePojo saved = repo.save(SiteRepositoryIntegrationTest.POJO);

    Assert.assertThat(saved, Matchers.equalTo(SiteRepositoryIntegrationTest.POJO));
    Assert.assertThat(
        repo.findById(SiteRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(SiteRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testSave() throws Exception {
    final SitePojo saved = repo.save(SiteRepositoryIntegrationTest.POJO);

    Assert.assertThat(saved, Matchers.equalTo(SiteRepositoryIntegrationTest.POJO));
    Assert.assertThat(
        repo.findById(SiteRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(SiteRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testSaveAll() throws Exception {
    final Iterable<SitePojo> saved =
        repo.saveAll(
            Arrays.asList(SiteRepositoryIntegrationTest.POJO, SiteRepositoryIntegrationTest.POJO2));

    Assert.assertThat(
        saved,
        Matchers.contains(SiteRepositoryIntegrationTest.POJO, SiteRepositoryIntegrationTest.POJO2));
    Assert.assertThat(
        repo.findById(SiteRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(SiteRepositoryIntegrationTest.POJO)));
    Assert.assertThat(
        repo.findById(SiteRepositoryIntegrationTest.ID2),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(SiteRepositoryIntegrationTest.POJO2)));
  }

  @Test
  public void testFindById() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);

    Assert.assertThat(
        repo.findById(SiteRepositoryIntegrationTest.ID),
        OptionalMatchers.isPresentAnd(Matchers.equalTo(SiteRepositoryIntegrationTest.POJO)));
  }

  @Test
  public void testFindByIdWhenNotFound() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);

    Assert.assertThat(repo.findById(SiteRepositoryIntegrationTest.ID2), OptionalMatchers.isEmpty());
  }

  @Test
  public void testExistById() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);

    Assert.assertThat(repo.existsById(SiteRepositoryIntegrationTest.ID), Matchers.equalTo(true));
  }

  @Test
  public void testExistByIdWhenNotFound() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);

    Assert.assertThat(
        repo.existsById(SiteRepositoryIntegrationTest.ID2), Matchers.not(Matchers.equalTo(true)));
  }

  @Test
  public void testFindAll() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);

    Assert.assertThat(
        repo.findAll(),
        Matchers.containsInAnyOrder(
            SiteRepositoryIntegrationTest.POJO, SiteRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testFindAllWhenNoneDefined() throws Exception {
    Assert.assertThat(repo.findAll(), Matchers.emptyIterable());
  }

  @Test
  public void testFindAllById() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);
    repo.save(SiteRepositoryIntegrationTest.POJO3);

    Assert.assertThat(
        repo.findAllById(
            Arrays.asList(
                SiteRepositoryIntegrationTest.ID, SiteRepositoryIntegrationTest.ID2, "unknown-id")),
        Matchers.containsInAnyOrder(
            SiteRepositoryIntegrationTest.POJO, SiteRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testCount() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);

    Assert.assertThat(repo.count(), Matchers.equalTo(2L));
  }

  @Test
  public void testCountWhenNoneDefined() throws Exception {
    Assert.assertThat(repo.count(), Matchers.equalTo(0L));
  }

  @Test
  public void testDeleteById() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);

    repo.deleteById(SiteRepositoryIntegrationTest.ID);
    Assert.assertThat(repo.findAll(), Matchers.contains(SiteRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteByIdWhenNotFound() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);

    repo.deleteById(SiteRepositoryIntegrationTest.ID2);
    Assert.assertThat(repo.findAll(), Matchers.contains(SiteRepositoryIntegrationTest.POJO));
  }

  @Test
  public void testDelete() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);

    repo.delete(SiteRepositoryIntegrationTest.POJO);
    Assert.assertThat(repo.findAll(), Matchers.contains(SiteRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteWhenNotFound() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);

    repo.delete(SiteRepositoryIntegrationTest.POJO2);
    Assert.assertThat(repo.findAll(), Matchers.contains(SiteRepositoryIntegrationTest.POJO));
  }

  @Test
  public void testDeleteAllByObj() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);
    repo.save(SiteRepositoryIntegrationTest.POJO3);

    repo.deleteAll(
        Arrays.asList(SiteRepositoryIntegrationTest.POJO, SiteRepositoryIntegrationTest.POJO3));
    Assert.assertThat(repo.findAll(), Matchers.contains(SiteRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteAllByObjWhenOneNotDefined() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);

    repo.deleteAll(
        Arrays.asList(SiteRepositoryIntegrationTest.POJO, SiteRepositoryIntegrationTest.POJO3));
    Assert.assertThat(repo.findAll(), Matchers.contains(SiteRepositoryIntegrationTest.POJO2));
  }

  @Test
  public void testDeleteAll() throws Exception {
    repo.save(SiteRepositoryIntegrationTest.POJO);
    repo.save(SiteRepositoryIntegrationTest.POJO2);
    repo.save(SiteRepositoryIntegrationTest.POJO3);

    Assert.assertThat(repo.count(), Matchers.equalTo(3L));
    repo.deleteAll();
    Assert.assertThat(repo.count(), Matchers.equalTo(0L));
  }
}
