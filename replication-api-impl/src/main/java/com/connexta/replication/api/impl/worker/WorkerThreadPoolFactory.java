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

import com.connexta.replication.api.data.Site;
import com.connexta.replication.api.impl.NodeAdapters;
import com.connexta.replication.api.persistence.SiteManager;
import com.connexta.replication.api.queue.QueueBroker;

/** Factory for creating new worker thread pools. Primarily to facilitate testing. */
public class WorkerThreadPoolFactory {

  private QueueBroker broker;

  private Site localSite;

  private SiteManager siteManager;

  private NodeAdapters nodeAdapters;

  /**
   * Creates a new WorkerThreadPoolFactory.
   *
   * @param broker the queue broker for retrieving queues
   * @param localSite this system's local site
   * @param siteManager the site data access manager
   * @param nodeAdapters node adapters factory
   */
  public WorkerThreadPoolFactory(
      QueueBroker broker, Site localSite, SiteManager siteManager, NodeAdapters nodeAdapters) {
    this.broker = broker;
    this.localSite = localSite;
    this.siteManager = siteManager;
    this.nodeAdapters = nodeAdapters;
  }

  /**
   * Creates a new WorkerThreadPool.
   *
   * @param siteId the siteId for the queue
   * @param poolSize the initial size of this thread pool
   * @return the new worker thread pool
   */
  public WorkerThreadPool create(String siteId, int poolSize) {
    return new WorkerThreadPool(
        broker.getQueue(siteId), poolSize, localSite, siteManager, nodeAdapters);
  }
}
