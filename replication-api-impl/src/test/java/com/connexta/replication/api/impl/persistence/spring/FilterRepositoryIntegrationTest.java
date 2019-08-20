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

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.connexta.replication.api.impl.persistence.EmbeddedSolrServerFactory;
import com.connexta.replication.api.impl.persistence.pojo.FilterPojo;
import java.util.Arrays;
import java.util.Optional;
import org.apache.solr.client.solrj.SolrClient;
import org.junit.After;
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
    basePackageClasses = FilterRepository.class) // to make sure we pickup the repositories
@RunWith(SpringRunner.class)
public class FilterRepositoryIntegrationTest {
  private static final int VERSION = 1;
  private static final int VERSION2 = 2;
  private static final int VERSION3 = 3;
  private static final String ID = "1234";
  private static final String ID2 = "1235";
  private static final String ID3 = "1236";
  private static final String SITE_ID = "site_id";
  private static final String SITE_ID2 = "site_id2";
  private static final String SITE_ID3 = "site_id3";
  private static final String FILTER = "filter";
  private static final String FILTER2 = "filter2";
  private static final String FILTER3 = "filter3";
  private static final String NAME = "name";
  private static final String NAME2 = "name2";
  private static final String NAME3 = "name3";
  private static final String DESCRIPTION = "description";
  private static final String DESCRIPTION2 = "description2";
  private static final String DESCRIPTION3 = "description3";
  private static final boolean SUSPENDED = true;
  private static final boolean SUSPENDED2 = false;
  private static final boolean SUSPENDED3 = true;
  private static final byte PRIORITY = 2;
  private static final byte PRIORITY2 = 3;
  private static final byte PRIORITY3 = 4;

  private static final FilterPojo POJO =
      new FilterPojo()
          .setId(ID)
          .setVersion(VERSION)
          .setSiteId(SITE_ID)
          .setFilter(FILTER)
          .setName(NAME)
          .setDescription(DESCRIPTION)
          .setSuspended(SUSPENDED)
          .setPriority(PRIORITY);
  private static final FilterPojo POJO2 =
      new FilterPojo()
          .setId(ID2)
          .setVersion(VERSION2)
          .setSiteId(SITE_ID2)
          .setFilter(FILTER2)
          .setName(NAME2)
          .setDescription(DESCRIPTION2)
          .setSuspended(SUSPENDED2)
          .setPriority(PRIORITY2);
  private static final FilterPojo POJO3 =
      new FilterPojo()
          .setId(ID3)
          .setVersion(VERSION3)
          .setSiteId(SITE_ID3)
          .setFilter(FILTER3)
          .setName(NAME3)
          .setDescription(DESCRIPTION3)
          .setSuspended(SUSPENDED3)
          .setPriority(PRIORITY3);

  @TestConfiguration
  @EnableSolrRepositories(basePackages = "com.connexta.replication")
  static class FilterRepositoryTestConfiguration {
    @Bean
    SolrClientFactory solrFactory() {
      return new EmbeddedSolrServerFactory("classpath:solr", FilterPojo.COLLECTION);
    }

    @Bean
    SolrClient solrClient(SolrClientFactory solrFactory) {
      return solrFactory.getSolrClient();
    }
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Autowired private FilterRepository repo;

  @After
  public void cleanup() {
    repo.deleteAll();
  }

  @Test
  public void testPojoPersistenceWhenIdIsNotDefined() {
    exception.expect(UncategorizedSolrException.class);
    exception.expectMessage(containsString("missing mandatory unique"));

    final FilterPojo pojo =
        new FilterPojo()
            .setVersion(VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(SUSPENDED)
            .setPriority(PRIORITY);

    repo.save(pojo);
  }

  @Test
  public void testPojoPersistenceWhenSiteIdIsNotDefined() {
    final FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(VERSION)
            .setFilter(FILTER)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(SUSPENDED)
            .setPriority(PRIORITY);

    final FilterPojo saved = repo.save(pojo);
    assertThat(saved, equalTo(pojo));

    final Optional<FilterPojo> loaded = repo.findById(ID);
    assertThat(loaded, isPresentAnd(equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenFilterIsNotDefined() {
    final FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(VERSION)
            .setSiteId(SITE_ID)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setSuspended(SUSPENDED)
            .setPriority(PRIORITY);

    final FilterPojo saved = repo.save(pojo);
    assertThat(saved, equalTo(pojo));

    final Optional<FilterPojo> loaded = repo.findById(ID);
    assertThat(loaded, isPresentAnd(equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenNameIsNotDefined() {
    final FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setDescription(DESCRIPTION)
            .setSuspended(SUSPENDED)
            .setPriority(PRIORITY);

    final FilterPojo saved = repo.save(pojo);
    assertThat(saved, equalTo(pojo));

    final Optional<FilterPojo> loaded = repo.findById(ID);
    assertThat(loaded, isPresentAnd(equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenDescriptionIsNotDefined() {
    final FilterPojo pojo =
        new FilterPojo()
            .setId(ID)
            .setVersion(VERSION)
            .setSiteId(SITE_ID)
            .setFilter(FILTER)
            .setName(NAME)
            .setSuspended(SUSPENDED)
            .setPriority(PRIORITY);

    final FilterPojo saved = repo.save(pojo);
    assertThat(saved, equalTo(pojo));

    final Optional<FilterPojo> loaded = repo.findById(ID);
    assertThat(loaded, isPresentAnd(equalTo(pojo)));
  }

  @Test
  public void testPojoPersistenceWhenAllFieldsAreDefined() {
    final FilterPojo saved = repo.save(POJO);
    assertThat(saved, equalTo(POJO));

    final Optional<FilterPojo> loaded = repo.findById(ID);
    assertThat(loaded, isPresentAnd(equalTo(POJO)));
  }

  @Test
  public void testSaveAll() throws Exception {
    final Iterable<FilterPojo> saved = repo.saveAll(Arrays.asList(POJO, POJO2));

    assertThat(saved, contains(POJO, POJO2));
    assertThat(repo.findById(ID), isPresentAnd(equalTo(POJO)));
    assertThat(repo.findById(ID2), isPresentAnd(equalTo(POJO2)));
  }

  @Test
  public void testFindById() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);

    assertThat(repo.findById(ID), isPresentAnd(equalTo(POJO)));
  }

  @Test
  public void testFindByIdWhenNotFound() throws Exception {
    repo.save(POJO);

    assertThat(repo.findById(ID2), isEmpty());
  }

  @Test
  public void testExistById() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);

    assertThat(repo.existsById(ID), equalTo(true));
  }

  @Test
  public void testExistByIdWhenNotFound() throws Exception {
    repo.save(POJO);

    assertThat(repo.existsById(ID2), not(equalTo(true)));
  }

  @Test
  public void testFindAll() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);

    assertThat(repo.findAll(), containsInAnyOrder(POJO, POJO2));
  }

  @Test
  public void testFindAllWhenNoneDefined() throws Exception {
    assertThat(repo.findAll(), emptyIterable());
  }

  @Test
  public void testFindAllById() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);
    repo.save(POJO3);

    assertThat(
        repo.findAllById(Arrays.asList(ID, ID2, "unknown-id")), containsInAnyOrder(POJO, POJO2));
  }

  @Test
  public void testCount() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);

    assertThat(repo.count(), equalTo(2L));
  }

  @Test
  public void testCountWhenNoneDefined() throws Exception {
    assertThat(repo.count(), equalTo(0L));
  }

  @Test
  public void testDeleteById() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);

    repo.deleteById(ID);
    assertThat(repo.findAll(), contains(POJO2));
  }

  @Test
  public void testDeleteByIdWhenNotFound() throws Exception {
    repo.save(POJO);

    repo.deleteById(ID2);
    assertThat(repo.findAll(), contains(POJO));
  }

  @Test
  public void testDelete() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);

    repo.delete(POJO);
    assertThat(repo.findAll(), contains(POJO2));
  }

  @Test
  public void testDeleteWhenNotFound() throws Exception {
    repo.save(POJO);

    repo.delete(POJO2);
    assertThat(repo.findAll(), contains(POJO));
  }

  @Test
  public void testDeleteAllByObj() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);
    repo.save(POJO3);

    repo.deleteAll(Arrays.asList(POJO, POJO3));
    assertThat(repo.findAll(), contains(POJO2));
  }

  @Test
  public void testDeleteAllByObjWhenOneNotDefined() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);

    repo.deleteAll(Arrays.asList(POJO, POJO3));
    assertThat(repo.findAll(), contains(POJO2));
  }

  @Test
  public void testDeleteAll() throws Exception {
    repo.save(POJO);
    repo.save(POJO2);
    repo.save(POJO3);

    assertThat(repo.count(), equalTo(3L));
    repo.deleteAll();
    assertThat(repo.count(), equalTo(0L));
  }

  @Test
  public void testFindBySiteId() {
    repo.save(POJO);
    FilterPojo pojo2WithSiteId1 =
        new FilterPojo()
            .setId(ID2)
            .setVersion(VERSION2)
            .setSiteId(SITE_ID)
            .setFilter(FILTER2)
            .setName(NAME2)
            .setDescription(DESCRIPTION2)
            .setSuspended(SUSPENDED2)
            .setPriority(PRIORITY2);
    repo.save(pojo2WithSiteId1);
    repo.save(POJO3);

    Iterable<FilterPojo> results = repo.findBySiteId(SITE_ID);
    assertThat(results, containsInAnyOrder(POJO, pojo2WithSiteId1));
    assertThat(results, not(contains(POJO3)));
  }
}
