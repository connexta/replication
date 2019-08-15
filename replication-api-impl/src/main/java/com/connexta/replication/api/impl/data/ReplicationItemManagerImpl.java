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

import com.connexta.ion.replication.api.NonTransientReplicationPersistenceException;
import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.RecoverableReplicationPersistenceException;
import com.connexta.ion.replication.api.Status;
import com.connexta.ion.replication.api.TransientReplicationPersistenceException;
import com.connexta.replication.api.data.ReplicationItem;
import com.connexta.replication.api.impl.persistence.pojo.ItemPojo;
import com.connexta.replication.api.impl.persistence.spring.ItemRepository;
import com.connexta.replication.api.persistence.ReplicationItemManager;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Resource;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Crotch;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;

/** Provides operations for managing {@link ItemPojo}s. */
public class ReplicationItemManagerImpl implements ReplicationItemManager {

  private static final int PAGE_SIZE = 50;

  private final ItemRepository itemRepository;

  @Resource private final SolrTemplate solrTemplate;

  /**
   * Creates a new ReplicationItemManager.
   *
   * @param itemRepository solr repository for {@link ItemPojo}s
   * @param solrTemplate solr operations implementation for querying solr
   */
  public ReplicationItemManagerImpl(ItemRepository itemRepository, SolrTemplate solrTemplate) {
    this.itemRepository = itemRepository;
    this.solrTemplate = solrTemplate;
  }

  @Override
  public ReplicationItem get(String id) {
    try {
      return itemRepository
          .findById(id)
          .map(ReplicationItemImpl::new)
          .orElseThrow(NotFoundException::new);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public Stream<ReplicationItem> objects() {
    try {
      return StreamSupport.stream(itemRepository.findAll().spliterator(), false)
          .map(ReplicationItemImpl::new)
          .map(ReplicationItem.class::cast);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void save(ReplicationItem replicationItem) {
    if (!(replicationItem instanceof ReplicationItemImpl)) {
      throw new IllegalArgumentException(
          "Expected a ReplicationItemImpl but got a " + replicationItem.getClass().getSimpleName());
    }
    try {
      itemRepository.save(((ReplicationItemImpl) replicationItem).writeTo(new ItemPojo()));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void remove(String id) {
    try {
      itemRepository.deleteById(id);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public Optional<ReplicationItem> getLatest(String configId, String metadataId) {
    try {
      return itemRepository
          .findByConfigIdAndMetadataIdOrderByDoneTimeDesc(
              configId, metadataId, PageRequest.of(0, 1))
          .stream()
          .map(ReplicationItemImpl::new)
          .map(ReplicationItem.class::cast)
          .findFirst();
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public List<ReplicationItem> getAllForConfig(String configId, int startIndex, int pageSize) {
    try {
      return itemRepository
          .findByConfigId(configId, PageRequest.of(startIndex, pageSize))
          .map(ReplicationItemImpl::new)
          .map(ReplicationItem.class::cast)
          .getContent();
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public List<String> getFailureList(String configId) {
    try {
      long pageTotal;
      int pageNum = 0;
      PageRequest pageRequest = PageRequest.of(pageNum, PAGE_SIZE);

      List<String> failureList = new LinkedList<>();
      do {
        GroupPage<ItemPojo> groupPage = doSolrQuery(configId, pageRequest);
        GroupResult<ItemPojo> groupResult = groupPage.getGroupResult("metadata_id");
        Page<GroupEntry<ItemPojo>> page = groupResult.getGroupEntries();
        pageTotal = page.getTotalElements();
        for (GroupEntry<ItemPojo> entry : page.getContent()) {
          entry.getResult().stream()
              .findFirst()
              .ifPresent(
                  item -> {
                    if (!Status.SUCCESS.name().equals(item.getStatus())) {
                      failureList.add(item.getMetadataId());
                    }
                  });
        }

        pageRequest = PageRequest.of(++pageNum, PAGE_SIZE);
      } while (pageTotal == PAGE_SIZE);
      return failureList;
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  private GroupPage<ItemPojo> doSolrQuery(String configId, Pageable pageable) {
    Criteria queryCriteria = Crotch.where("config_id").is(configId);
    Sort doneTimeDescSort = Sort.by(Direction.DESC, "done_time");
    SimpleQuery groupQuery =
        new SimpleQuery(queryCriteria).addSort(doneTimeDescSort).setPageRequest(pageable);
    GroupOptions options =
        new GroupOptions().addGroupByField("metadata_id").setLimit(1).addSort(doneTimeDescSort);
    groupQuery.setGroupOptions(options);

    return solrTemplate.queryForGroupPage(ItemPojo.COLLECTION, groupQuery, ItemPojo.class);
  }

  @Override
  public void removeAllForConfig(String configId) {
    try {
      itemRepository.deleteByConfigId(configId);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }
}
