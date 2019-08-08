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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.ion.replication.api.ReplicationItem;
import com.connexta.ion.replication.api.Status;
import com.connexta.ion.replication.api.impl.data.ReplicationItemImpl;
import com.connexta.ion.replication.api.impl.spring.ItemRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    ReplicationItemImpl item = mock(ReplicationItemImpl.class);
    List<ReplicationItem> items = List.of(item);
    Page page = mock(Page.class);
    when(page.stream()).thenReturn(items.stream());
    when(itemRepository.findByConfigIdAndMetadataIdOrderByDoneTimeDesc(
            anyString(), anyString(), any(Pageable.class)))
        .thenReturn(page);
    assertThat(itemManager.getLatestItem("configId", "id").get(), is(item));
  }

  @Test
  public void deleteAllItems() {
    itemManager.deleteAllItems();
    verify(itemRepository).deleteAll();
  }

  @Test
  public void getItemsForConfig() {
    List<ReplicationItem> items = Collections.singletonList(new ReplicationItemImpl());
    when(itemRepository.findByConfigId(anyString(), any(PageRequest.class)))
        .thenReturn(
            new PageImpl(
                items.stream().map(ReplicationItemImpl.class::cast).collect(Collectors.toList())));
    assertThat(itemManager.getItemsForConfig("configId", 0, 1), is(items));
  }

  @Test
  public void saveItem() {
    ReplicationItemImpl item = new ReplicationItemImpl();
    itemManager.saveItem(item);
    verify(itemRepository).save(eq(item));
  }

  @Test
  public void deleteItem() {
    itemManager.deleteItem("id", "source", "destination");
    verify(itemRepository)
        .deleteByIdAndSourceAndDestination(eq("id"), eq("source"), eq("destination"));
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
  public void deleteItemsForConfig() {
    itemManager.deleteItemsForConfig("configId");
    verify(itemRepository).deleteByConfigId("configId");
  }

  private GroupPage mockGroupPage(int num, int failItemIndex, String failMetadataId) {
    List<GroupEntry> entries = new ArrayList<>();
    for (int i = 0; i < num; i++) {
      ReplicationItemImpl item = mock(ReplicationItemImpl.class);

      if (i == failItemIndex) {
        when(item.getMetadataId()).thenReturn(failMetadataId);
        when(item.getStatus()).thenReturn(Status.FAILURE);
      } else {
        when(item.getStatus()).thenReturn(Status.SUCCESS);
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
