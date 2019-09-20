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
package com.connexta.replication.api.impl.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.NodeAdapterFactory;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.data.SiteType;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.Queue;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkerThreadPoolTest {

  private static final int INITIAL_SIZE = 3;
  private static final SiteType DDF_TYPE = SiteType.DDF;
  private static final URL SITE_URL;

  private Queue queue;

  private Site localSite;

  private SiteManager siteManager;

  private NodeAdapters nodeAdapters;

  private WorkerThreadPool threadPool;

  static {
    try {
      SITE_URL = new URL("http://test:1234");
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<Worker> workers;

  @BeforeEach
  void before() {
    workers = new ArrayList<>();
    queue = mock(Queue.class);
    siteManager = mock(SiteManager.class);

    localSite = mock(Site.class);
    when(localSite.getType()).thenReturn(DDF_TYPE);
    when(localSite.getUrl()).thenReturn(SITE_URL);

    NodeAdapter localAdapter = mock(NodeAdapter.class);
    NodeAdapterFactory factory = mock(NodeAdapterFactory.class);
    when(factory.create(any(URL.class))).thenReturn(localAdapter);

    nodeAdapters = mock(NodeAdapters.class);
    when(nodeAdapters.factoryFor(DDF_TYPE)).thenReturn(factory);

    threadPool =
        new TestWorkerThreadPool(queue, INITIAL_SIZE, localSite, siteManager, nodeAdapters);
  }

  @Test
  void testSpinDownWorkers() {
    assertEquals(INITIAL_SIZE, threadPool.getSize());

    Worker interruptedWorker = workers.get(0);

    threadPool.setSize(1);
    assertEquals(1, threadPool.getSize());

    verify(interruptedWorker).interrupt();
  }

  @Test
  void testSpinUpWorkers() {
    threadPool = new TestWorkerThreadPool(queue, 0, localSite, siteManager, nodeAdapters);
    threadPool.setSize(5);
    assertEquals(5, threadPool.getSize());
    workers.forEach(worker -> verify(worker).start());
  }

  @Test
  void testSetSizeEqualToLastSize() {
    assertEquals(INITIAL_SIZE, threadPool.getSize());
    workers.clear();

    threadPool.setSize(INITIAL_SIZE);
    assertEquals(INITIAL_SIZE, threadPool.getSize());

    // should be 0 since we cleared after initially adding and no new workers were added on setSize
    assertEquals(workers.size(), 0);
  }

  @Test
  void testGetSize() {
    assertEquals(INITIAL_SIZE, threadPool.getSize());
  }

  @Test
  void testShutdown() {
    threadPool.shutdown();
    workers.forEach(worker -> verify(worker).interrupt());
  }

  @Test
  void testSetSizeAfterShutdownIsNoop() {
    threadPool.shutdown();
    threadPool.setSize(100);
    assertEquals(0, threadPool.getSize());
  }

  // extends WorkerThreadPool to return mock workers
  class TestWorkerThreadPool extends WorkerThreadPool {

    boolean cancelled = false;

    TestWorkerThreadPool(
        Queue queue, int size, Site localSite, SiteManager siteManager, NodeAdapters nodeAdapters) {
      super(queue, size, localSite, siteManager, nodeAdapters);
    }

    @Override
    Worker newWorker(NodeAdapter localAdapter) {
      Worker worker = mock(Worker.class);
      when(worker.cancelIfNotProcessing()).thenReturn(cancelled);
      cancelled = !cancelled;
      workers.add(worker);
      return worker;
    }
  }
}
