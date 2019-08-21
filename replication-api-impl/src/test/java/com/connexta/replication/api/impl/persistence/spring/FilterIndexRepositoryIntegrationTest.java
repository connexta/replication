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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;

import com.connexta.replication.api.impl.persistence.EmbeddedSolrServerFactory;
import com.connexta.replication.api.impl.persistence.pojo.FilterIndexPojo;
import java.time.Instant;
import java.util.List;
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

@ComponentScan(basePackageClasses = FilterIndexRepository.class)
@RunWith(SpringRunner.class)
public class FilterIndexRepositoryIntegrationTest {
  private static final String ID1 = "id";
  private static final Instant MODIFIED_SINCE1 = Instant.ofEpochSecond(100);
  private static final String FILTER_ID1 = "filterId";
  private static final FilterIndexPojo POJO1 =
      new FilterIndexPojo().setId(ID1).setModifiedSince(MODIFIED_SINCE1).setFilterId(FILTER_ID1);

  private static final String ID2 = "id2";
  private static final Instant MODIFIED_SINCE2 = Instant.ofEpochSecond(200);
  private static final String FILTER_ID2 = "filterId2";
  private static final FilterIndexPojo POJO2 =
      new FilterIndexPojo().setId(ID2).setModifiedSince(MODIFIED_SINCE2).setFilterId(FILTER_ID2);

  private static final String ID3 = "id3";
  private static final Instant MODIFIED_SINCE3 = Instant.ofEpochSecond(300);
  private static final String FILTER_ID3 = "filterId3";
  private static final FilterIndexPojo POJO3 =
      new FilterIndexPojo().setId(ID3).setModifiedSince(MODIFIED_SINCE3).setFilterId(FILTER_ID3);

  @TestConfiguration
  @EnableSolrRepositories(basePackages = "com.connexta.replication")
  static class ReplicationFilterIndexRepositoryTestConfiguration {
    @Bean
    SolrClientFactory solrFactory() {
      return new EmbeddedSolrServerFactory("classpath:solr", FilterIndexPojo.COLLECTION);
    }

    @Bean
    SolrClient solrClient(SolrClientFactory solrFactory) {
      return solrFactory.getSolrClient();
    }
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Autowired private FilterIndexRepository repo;

  @After
  public void cleanup() {
    repo.deleteAll();
  }

  @Test
  public void testPojoPersistenceWhenIdIsNotDefined() {
    exception.expect(UncategorizedSolrException.class);
    exception.expectMessage("Document is missing mandatory uniqueKey field: id");

    final FilterIndexPojo pojo =
        new FilterIndexPojo().setModifiedSince(MODIFIED_SINCE1).setFilterId(FILTER_ID1);

    repo.save(pojo);
  }

  @Test
  public void testPojoPersistenceWhenAllFieldsDefined() {
    final FilterIndexPojo pojo =
        new FilterIndexPojo().setId(ID1).setModifiedSince(MODIFIED_SINCE1).setFilterId(FILTER_ID1);

    FilterIndexPojo saved = repo.save(pojo);
    assertThat(saved, is(pojo));

    final Optional<FilterIndexPojo> loaded = repo.findById(ID1);
    assertThat(loaded, isPresentAnd(is(pojo)));
  }

  // TODO: This should fail when this field can be set to required in the schema
  @Test
  public void testPojoPersistenceWhenModifiedSinceNotDefined() {
    final FilterIndexPojo pojo = new FilterIndexPojo().setId(ID1).setFilterId(FILTER_ID1);

    FilterIndexPojo saved = repo.save(pojo);
    assertThat(saved, is(pojo));

    final Optional<FilterIndexPojo> loaded = repo.findById(ID1);
    assertThat(loaded, isPresentAnd(is(pojo)));
  }

  // TODO: This should fail when this field can be set to required in the schema
  @Test
  public void testPojoPersistenceWhenFilterIdIsNotDefined() {
    final FilterIndexPojo pojo = new FilterIndexPojo().setId(ID1).setModifiedSince(MODIFIED_SINCE1);

    FilterIndexPojo saved = repo.save(pojo);
    assertThat(saved, is(pojo));

    final Optional<FilterIndexPojo> loaded = repo.findById(ID1);
    assertThat(loaded, isPresentAnd(is(pojo)));
  }

  @Test
  public void testSave() {
    final FilterIndexPojo saved = repo.save(POJO1);
    assertThat(saved, is(POJO1));

    final Optional<FilterIndexPojo> loaded = repo.findById(ID1);
    assertThat(loaded, isPresentAnd(is(POJO1)));
  }

  @Test
  public void testSaveAll() {
    final Iterable<FilterIndexPojo> saved = repo.saveAll(List.of(POJO1, POJO2));
    assertThat(saved, contains(POJO1, POJO2));

    final Iterable<FilterIndexPojo> loaded = repo.findAllById(List.of(ID1, ID2));
    assertThat(loaded, containsInAnyOrder(POJO1, POJO2));
  }

  @Test
  public void testFindById() {
    repo.save(POJO1);
    repo.save(POJO2);

    final Optional<FilterIndexPojo> loaded = repo.findById(ID1);
    assertThat(loaded, isPresentAnd(is(POJO1)));
  }

  @Test
  public void testFindByIdWhenNotFound() {
    repo.save(POJO1);

    final Optional<FilterIndexPojo> loaded = repo.findById("doesntexist");
    assertThat(loaded, isEmpty());
  }

  @Test
  public void testExistById() {
    repo.save(POJO1);
    assertThat(repo.existsById(ID1), is(true));
  }

  @Test
  public void testExistByIdWhenNotFound() {
    repo.save(POJO1);
    assertThat(repo.existsById("doesntexist"), is(false));
  }

  @Test
  public void testFindAll() {
    repo.save(POJO1);
    repo.save(POJO2);

    final Iterable<FilterIndexPojo> loaded = repo.findAll();
    assertThat(loaded, containsInAnyOrder(POJO1, POJO2));
  }

  @Test
  public void testFindAllWhenNoneDefined() {
    final Iterable<FilterIndexPojo> loaded = repo.findAll();
    assertThat(loaded, emptyIterable());
  }

  @Test
  public void testFindAllByIdWithUnknownId() {
    repo.saveAll(List.of(POJO1, POJO2, POJO3));

    final Iterable<FilterIndexPojo> loaded = repo.findAllById(List.of(ID1, ID2, "doesntexist"));
    assertThat(loaded, iterableWithSize(2));
    assertThat(loaded, containsInAnyOrder(POJO1, POJO2));
  }

  @Test
  public void testCount() {
    repo.saveAll(List.of(POJO1, POJO2, POJO3));
    assertThat(repo.count(), is(3L));
  }

  @Test
  public void testCountWhenNoneDefined() {
    assertThat(repo.count(), is(0L));
  }

  @Test
  public void testDeleteById() {
    repo.saveAll(List.of(POJO1, POJO2));
    repo.deleteById(ID1);
    assertThat(repo.findAll(), contains(POJO2));
  }

  @Test
  public void testDeleteByIdWhenNotFound() {
    repo.save(POJO1);
    repo.deleteById("doesntexist");
    assertThat(repo.findAll(), contains(POJO1));
  }

  @Test
  public void testDelete() {
    repo.saveAll(List.of(POJO1, POJO2));
    repo.delete(POJO1);
    assertThat(repo.findAll(), contains(POJO2));
  }

  @Test
  public void testDeleteWhenNotFound() {
    repo.save(POJO1);
    repo.delete(POJO2);
    assertThat(repo.findAll(), contains(POJO1));
  }

  @Test
  public void testDeleteAllByObj() {
    repo.saveAll(List.of(POJO1, POJO2, POJO3));
    repo.deleteAll(List.of(POJO1, POJO2));
    assertThat(repo.findAll(), contains(POJO3));
  }

  @Test
  public void testDeleteAllByObjWhenOneNotDefined() {
    repo.saveAll(List.of(POJO1, POJO2));
    repo.deleteAll(List.of(POJO1, POJO3));
    assertThat(repo.findAll(), contains(POJO2));
  }

  @Test
  public void testDeleteAll() {
    repo.saveAll(List.of(POJO1, POJO2));
    assertThat(repo.count(), is(2L));
    repo.deleteAll();
    assertThat(repo.count(), is(0L));
  }

  @Test
  public void testFindByFilterId() {
    repo.saveAll(List.of(POJO1, POJO2));

    final Optional<FilterIndexPojo> loaded = repo.findByFilterId(FILTER_ID1);
    assertThat(loaded, isPresentAnd(is(POJO1)));
  }

  @Test
  public void testModifiedTimeUpdated() {
    final FilterIndexPojo pojo =
        new FilterIndexPojo().setId(ID1).setModifiedSince(MODIFIED_SINCE1).setFilterId(FILTER_ID1);
    repo.save(pojo);
    FilterIndexPojo loaded = repo.findById(ID1).get();
    assertThat(loaded.getModifiedSince(), is(MODIFIED_SINCE1));

    final Instant updatedModifiedSince = Instant.ofEpochSecond(678);
    loaded.setModifiedSince(updatedModifiedSince);
    repo.save(loaded);

    FilterIndexPojo updated = repo.findById(ID1).get();
    assertThat(updated.getModifiedSince(), is(updatedModifiedSince));
    assertThat(repo.count(), is(1L));
  }

  @Test
  public void testFindByFilterIdWhenNotFound() {
    repo.save(POJO1);
    final Optional<FilterIndexPojo> loaded = repo.findByFilterId("doesntexist");
    assertThat(loaded, isEmpty());
  }
}
