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

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReplicationItemManagerTest {

  //  @Mock private ItemRepository itemRepository;
  //
  //  private ReplicationItemManagerImpl itemManager;
  //
  //  @Before
  //  public void setup() throws Exception {
  //    itemManager = new ReplicationItemManagerImpl(itemRepository);
  //  }
  //
  //  @Test
  //  public void getItem() {
  //    Optional<ReplicationItem> item = Optional.of(new ReplicationItemImpl());
  //    when(itemRepository.findByIdAndSourceAndDestination(anyString(), anyString(), anyString()))
  //        .thenReturn(item.map(ReplicationItemImpl.class::cast));
  //    assertThat(itemManager.getItem("id", "source", "destination"), is(item));
  //  }
  //
  //  @Test
  //  public void deleteAllItems() {
  //    itemManager.deleteAllItems();
  //    verify(itemRepository).deleteAll();
  //  }
  //
  //  @Test
  //  public void getItemsForConfig() {
  //    List<ReplicationItem> items = Collections.singletonList(new ReplicationItemImpl());
  //    when(itemRepository.findByConfigId(anyString(), any(PageRequest.class)))
  //        .thenReturn(
  //            new PageImpl(
  //
  // items.stream().map(ReplicationItemImpl.class::cast).collect(Collectors.toList())));
  //    assertThat(itemManager.getItemsForConfig("configId", 0, 1), is(items));
  //  }
  //
  //  @Test
  //  public void saveItem() {
  //    ReplicationItemImpl item = new ReplicationItemImpl();
  //    itemManager.saveItem(item);
  //    verify(itemRepository).save(eq(item));
  //  }
  //
  //  @Test
  //  public void deleteItem() {
  //    itemManager.deleteItem("id", "source", "destination");
  //    verify(itemRepository)
  //        .deleteByIdAndSourceAndDestination(eq("id"), eq("source"), eq("destination"));
  //  }
  //
  //  @Test
  //  public void getFailureList() {
  //    ReplicationItemImpl item =
  //        new ReplicationItemImpl("id", new Date(), new Date(), "source", "destination",
  // "configId");
  //    List<ReplicationItem> items = Collections.singletonList(item);
  //    when(itemRepository.findByFailureCountBetweenAndSourceAndDestination(
  //            anyInt(), anyInt(), anyString(), anyString(), any(Pageable.class)))
  //        .thenReturn(
  //            new PageImpl(
  //
  // items.stream().map(ReplicationItemImpl.class::cast).collect(Collectors.toList())));
  //    assertThat(
  //        itemManager.getFailureList(5, "source", "destination"),
  //        is(items.stream().map(ReplicationItem::getId).collect(Collectors.toList())));
  //  }
  //
  //  @Test
  //  public void deleteItemsForConfig() {
  //    itemManager.deleteItemsForConfig("configId");
  //    verify(itemRepository).deleteByConfigId("configId");
  //  }
}
