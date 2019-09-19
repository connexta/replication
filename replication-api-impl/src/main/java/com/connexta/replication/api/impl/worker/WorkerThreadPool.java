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

import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.Queue;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Manages a site's set of workers which in turn run workers. */
public class WorkerThreadPool {

  private volatile boolean shutdown = false;

  private final List<Worker> workers = new CopyOnWriteArrayList<>();

  private final NodeAdapters nodeAdapters;

  private final Site localSite;

  private final SiteManager siteManager;

  private final Queue queue;

  /**
   * Creates a new worker thread pool for the given site and spins up a number of workers equal to
   * the size passed in.
   *
   * @param queue queue which workers will poll tasks from
   * @param size the initialize size of the pool
   * @param localSite the local site
   * @param siteManager site manager for accessing sites
   * @param nodeAdapters factory for creating node adapters
   */
  WorkerThreadPool(
      Queue queue, int size, Site localSite, SiteManager siteManager, NodeAdapters nodeAdapters) {
    this.queue = queue;
    this.localSite = localSite;
    this.siteManager = siteManager;
    this.nodeAdapters = nodeAdapters;
    setSize(size);
  }

  /**
   * Sets the size of this thread pool. If the current size exceeds the new size, then workers will
   * be interrupted until the new size is achieved. Workers that are currently not processing will
   * be prioritized for cleanup before workers that are processing. If all workers are processing or
   * there is a remaining amount that needs to be interrupted, then they will be stopped in the
   * order they are encountered until the amount of workers un-interrupted is equal to the new size.
   *
   * <p>If the new size is greater than the current size, then {@code newSize-currentSize} workers
   * will be spun up.
   *
   * @param size the size of this thread pool, or noop if shutdown
   */
  public final void setSize(int size) {
    if (shutdown) {
      return;
    }

    final int currentSize = this.getSize();
    if (size == currentSize) {
      return;
    }

    if (size > currentSize) {
      final int delta = size - currentSize;
      spinUpWorkers(delta);
      return;
    }

    final int delta = currentSize - size;
    spinDownWorkers(delta);
  }

  private void spinDownWorkers(int target) {
    int cancelled = 0;
    // try to cancel non processing workers first

    for (Worker worker : workers) {
      if (worker.cancelIfNotProcessing()) {
        workers.remove(worker);
        if (++cancelled == target) {
          return;
        }
      }
    }

    // begin cancelling processing workers in order until target is reached
    for (Worker worker : workers) {
      worker.interrupt();
      workers.remove(worker);
      if (++cancelled == target) {
        return;
      }
    }
  }

  private void spinUpWorkers(int num) {
    for (int i = 0; i < num; i++) {
      NodeAdapter localAdapter =
          nodeAdapters.factoryFor(localSite.getType()).create(localSite.getUrl());

      Worker worker = newWorker(localAdapter);
      worker.start();
      workers.add(worker);
    }
  }

  /** @return the current size of this thread pool */
  public int getSize() {
    return this.workers.size();
  }

  /**
   * Shutdown this thread pool, interrupting every worker and forbidding new workers from being
   * added.
   */
  void shutdown() {
    shutdown = true;
    workers.forEach(
        worker -> {
          worker.interrupt();
          workers.remove(worker);
        });
  }

  // Simple factory method for mocking out workers
  @VisibleForTesting
  Worker newWorker(NodeAdapter localAdapter) {
    return new Worker(queue, localAdapter, siteManager, nodeAdapters);
  }
}
