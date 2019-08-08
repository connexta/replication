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
package com.connexta.ion.replication.api.impl.persistence;

import com.connexta.ion.replication.api.ReplicationItem;
import com.connexta.ion.replication.api.Status;
import com.connexta.ion.replication.api.impl.data.ReplicationItemImpl;
import com.connexta.ion.replication.api.impl.spring.ItemRepository;
import com.connexta.ion.replication.api.persistence.ReplicationItemManager;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
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

/** Provides operations for managing {@link ReplicationItem}s. */
public class ReplicationItemManagerImpl implements ReplicationItemManager {

  private static final int PAGE_SIZE = 50;

  private final ItemRepository itemRepository;

  @Resource private final SolrTemplate solrTemplate;

  /**
   * Creates a new ReplicationItemManager.
   *
   * @param itemRepository solr repository for {@link ReplicationItem}s
   * @param solrTemplate solr operations implementation for querying solr
   */
  public ReplicationItemManagerImpl(ItemRepository itemRepository, SolrTemplate solrTemplate) {
    this.itemRepository = itemRepository;
    this.solrTemplate = solrTemplate;
  }

  @Override
  public Optional<ReplicationItem> getLatestItem(String configId, String metadataId) {
    return itemRepository
        .findByConfigIdAndMetadataIdOrderByDoneTimeDesc(configId, metadataId, PageRequest.of(0, 1))
        .stream()
        .map(ReplicationItem.class::cast)
        .findFirst();
  }

  @Override
  public void deleteAllItems() {
    itemRepository.deleteAll();
  }

  @Override
  public List<ReplicationItem> getItemsForConfig(String configId, int startIndex, int pageSize) {
    return itemRepository
        .findByConfigId(configId, PageRequest.of(startIndex, pageSize))
        .map(ReplicationItem.class::cast)
        .getContent();
  }

  @Override
  public void saveItem(ReplicationItem replicationItem) {
    if (replicationItem instanceof ReplicationItemImpl) {
      itemRepository.save((ReplicationItemImpl) replicationItem);
    } else {
      throw new IllegalArgumentException(
          "Expected a ReplicationItemImpl but got a " + replicationItem.getClass().getSimpleName());
    }
  }

  @Override
  public void deleteItem(String metadataId, String source, String destination) {
    itemRepository.deleteByIdAndSourceAndDestination(metadataId, source, destination);
  }

  @Override
  public List<String> getFailureList(String configId) {
    long pageTotal;
    int pageNum = 0;
    PageRequest pageRequest = PageRequest.of(pageNum, PAGE_SIZE);

    List<String> failureList = new LinkedList<>();
    do {
      GroupPage<ReplicationItemImpl> groupPage = doSolrQuery(configId, pageRequest);
      GroupResult<ReplicationItemImpl> groupResult = groupPage.getGroupResult("metadata_id");
      Page<GroupEntry<ReplicationItemImpl>> page = groupResult.getGroupEntries();
      pageTotal = page.getTotalElements();
      for (GroupEntry<ReplicationItemImpl> entry : page.getContent()) {
        entry
            .getResult()
            .stream()
            .findFirst()
            .ifPresent(
                item -> {
                  if (item.getStatus() != Status.SUCCESS) {
                    failureList.add(item.getMetadataId());
                  }
                });
      }

      pageRequest = PageRequest.of(++pageNum, PAGE_SIZE);
    } while (pageTotal == PAGE_SIZE);
    return failureList;
  }

  private GroupPage<ReplicationItemImpl> doSolrQuery(String configId, Pageable pageable) {
    Criteria queryCriteria = Crotch.where("config_id").is(configId);
    Sort doneTimeDescSort = new Sort(Direction.DESC, "done_time");
    SimpleQuery groupQuery =
        new SimpleQuery(queryCriteria).addSort(doneTimeDescSort).setPageRequest(pageable);
    GroupOptions options =
        new GroupOptions().addGroupByField("metadata_id").setLimit(1).addSort(doneTimeDescSort);
    groupQuery.setGroupOptions(options);

    return solrTemplate.queryForGroupPage(
        "replication_item", groupQuery, ReplicationItemImpl.class);
  }

  @Override
  public void deleteItemsForConfig(String configId) {
    itemRepository.deleteByConfigId(configId);
  }
}
