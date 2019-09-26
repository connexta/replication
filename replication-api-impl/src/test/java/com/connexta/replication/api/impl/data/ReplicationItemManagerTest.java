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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.Action;
import com.connexta.replication.api.Status;
import com.connexta.replication.api.data.ReplicationItem;
import com.connexta.replication.api.impl.persistence.pojo.ItemPojo;
import com.connexta.replication.api.impl.persistence.spring.ItemRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationItemManagerTest {

  @Mock private ItemRepository itemRepository;

  @Mock private SolrTemplate solrTemplate;

  private ReplicationItemManagerImpl itemManager;

  @Before
  public void setup() throws Exception {
    itemManager = new ReplicationItemManagerImpl(itemRepository, solrTemplate);
  }

  @Test
  public void getItemLatestItem() {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemPojo.CURRENT_VERSION)
            .setId("12345")
            .setMetadataId("m123")
            .setFilterId("c123")
            .setSource("sourceA")
            .setDestination("destinationB")
            .setStartTime(new Date())
            .setDoneTime(new Date())
            .setAction(Action.DELETE.name())
            .setStatus(Status.CONNECTION_LOST.name());
    List<ItemPojo> items = List.of(pojo);
    Page page = mock(Page.class);

    when(page.stream()).thenReturn(items.stream());
    when(itemRepository.findByFilterIdAndMetadataIdOrderByDoneTimeDesc(
            anyString(), anyString(), any(Pageable.class)))
        .thenReturn(page);

    final ReplicationItem item = itemManager.getLatest("filterId", "id").get();

    assertThat(item.getId(), is(pojo.getId()));
    assertThat(item.getMetadataId(), is(pojo.getMetadataId()));
    assertThat(item.getFilterId(), is(pojo.getFilterId()));
    assertThat(item.getSource(), is(pojo.getSource()));
    assertThat(item.getDestination(), is(pojo.getDestination()));
    assertThat(item.getStartTime(), is(pojo.getStartTime()));
    assertThat(item.getDoneTime(), is(pojo.getDoneTime()));
    assertThat(item.getAction().name(), is(pojo.getAction()));
    assertThat(item.getStatus().name(), is(pojo.getStatus()));
  }

  @Test
  public void getItemsForFilter() {
    final ItemPojo pojo =
        new ItemPojo()
            .setVersion(ItemPojo.CURRENT_VERSION)
            .setId("12345")
            .setMetadataId("m123")
            .setFilterId("c123")
            .setSource("sourceA")
            .setDestination("destinationB")
            .setStartTime(new Date())
            .setDoneTime(new Date())
            .setAction(Action.DELETE.name())
            .setStatus(Status.CONNECTION_LOST.name());
    final List<ItemPojo> pojos = Collections.singletonList(pojo);

    when(itemRepository.findByFilterId(anyString(), any(PageRequest.class)))
        .thenReturn(
            new PageImpl(pojos.stream().map(ItemPojo.class::cast).collect(Collectors.toList())));
    final List<ReplicationItem> items = itemManager.getAllForFilter("filterId", 0, 1);

    assertThat(items.size(), is(1));
    final ReplicationItem item = items.get(0);

    assertThat(item.getId(), is(pojo.getId()));
    assertThat(item.getMetadataId(), is(pojo.getMetadataId()));
    assertThat(item.getFilterId(), is(pojo.getFilterId()));
    assertThat(item.getSource(), is(pojo.getSource()));
    assertThat(item.getDestination(), is(pojo.getDestination()));
    assertThat(item.getStartTime(), is(pojo.getStartTime()));
    assertThat(item.getDoneTime(), is(pojo.getDoneTime()));
    assertThat(item.getAction().name(), is(pojo.getAction()));
    assertThat(item.getStatus().name(), is(pojo.getStatus()));
  }

  @Test
  public void saveItem() {
    final ReplicationItemImpl item = new ReplicationItemImpl();

    item.setId("12345");
    item.setMetadataId("m123");
    item.setFilterId("c123");
    item.setSource("sourceA");
    item.setDestination("destinationB");
    item.setStartTime(new Date());
    item.setDoneTime(new Date());
    item.setAction(Action.CREATE);
    item.setStatus(Status.FAILURE);

    itemManager.save(item);

    final ArgumentCaptor<ItemPojo> pojo = ArgumentCaptor.forClass(ItemPojo.class);

    verify(itemRepository).save(pojo.capture());

    assertThat(pojo.getValue().getId(), is(item.getId()));
    assertThat(pojo.getValue().getMetadataId(), is(item.getMetadataId()));
    assertThat(pojo.getValue().getFilterId(), is(item.getFilterId()));
    assertThat(pojo.getValue().getSource(), is(item.getSource()));
    assertThat(pojo.getValue().getDestination(), is(item.getDestination()));
    assertThat(pojo.getValue().getStartTime(), is(item.getStartTime()));
    assertThat(pojo.getValue().getDoneTime(), is(item.getDoneTime()));
    assertThat(pojo.getValue().getAction(), is(item.getAction().name()));
    assertThat(pojo.getValue().getStatus(), is(item.getStatus().name()));
  }

  @Test
  public void getFailureListWithPaging() {
    final String failureId1 = "failureId1";
    final String failureId2 = "failureId2";
    GroupPage page1 = mockGroupPage(50, 1, failureId1);
    GroupPage page2 = mockGroupPage(1, 0, failureId2);
    when(solrTemplate.queryForGroupPage(anyString(), any(), any()))
        .thenReturn(page1)
        .thenReturn(page2);

    List<String> failureIds = itemManager.getFailureList("id");
    assertThat(failureIds.size(), is(2));
    assertThat(failureIds, contains(failureId1, failureId2));
  }

  @Test
  public void deleteItemsForFilter() {
    itemManager.removeAllForFilter("filterId");
    verify(itemRepository).deleteByFilterId("filterId");
  }

  private GroupPage mockGroupPage(int num, int failItemIndex, String failMetadataId) {
    List<GroupEntry> entries = new ArrayList<>();
    for (int i = 0; i < num; i++) {
      final ItemPojo item = mock(ItemPojo.class);

      if (i == failItemIndex) {
        when(item.getMetadataId()).thenReturn(failMetadataId);
        when(item.getStatus()).thenReturn(Status.FAILURE.name());
      } else {
        when(item.getStatus()).thenReturn(Status.SUCCESS.name());
      }

      Page page = mock(Page.class);
      Stream stream = Stream.of(item);
      when(page.stream()).thenReturn(stream);

      GroupEntry entry = mock(GroupEntry.class);
      when(entry.getResult()).thenReturn(page);
      entries.add(entry);
    }

    Page pageFromGroup = mock(Page.class);
    when(pageFromGroup.getContent()).thenReturn(entries);
    when(pageFromGroup.getTotalElements()).thenReturn((long) num);

    GroupResult groupResult = mock(GroupResult.class);
    when(groupResult.getGroupEntries()).thenReturn(pageFromGroup);

    GroupPage groupPage = mock(GroupPage.class);
    when(groupPage.getGroupResult("metadata_id")).thenReturn(groupResult);
    return groupPage;
  }
}
