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
package org.codice.ditto.replication.api.impl;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.concurrent.ExecutorService;
import org.codice.ddf.security.common.Security;
import org.codice.ditto.replication.api.Heartbeater;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.Verifier;
import org.codice.ditto.replication.api.data.ReplicationSite;
import org.codice.ditto.replication.api.persistence.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an implementation for both the {@link Verifier} and {@link Heartbeater} services capable
 * of communicating with either Ion or DDF systems.
 */
public class HeartbeaterAndVerifierImpl implements Verifier, Heartbeater {
  private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeaterAndVerifierImpl.class);

  @SuppressWarnings("unused")
  private final ReplicatorHistory history;

  private final SiteManager siteManager;

  private final ExecutorService executor;

  private final Security security;

  public HeartbeaterAndVerifierImpl(
      ReplicatorHistory history, SiteManager siteManager, ExecutorService executor) {
    this(history, siteManager, executor, Security.getInstance());
  }

  public HeartbeaterAndVerifierImpl(
      ReplicatorHistory history,
      SiteManager siteManager,
      ExecutorService executor,
      Security security) {
    this.history = notNull(history);
    this.executor = notNull(executor);
    this.siteManager = notNull(siteManager);
    this.security = security;
  }

  /** Initializes the heartbeat and verification service. */
  public void init() {
    LOGGER.trace("Initializing the heartbeater and verifier");
  }

  /** Cleanups the heartbeat and verification service. */
  public void cleanUp() {
    LOGGER.trace("Cleaning the heartbeater and verifier");
    LOGGER.trace(
        "Shutting down now the single-thread scheduler that executes sync requests from the queue");
    executor.shutdownNow();
    LOGGER.trace("Successfully shut down heartbeater thread pool and scheduler");
  }

  @Override
  public void verify(ReplicationSite site) {
    final String url = site.getUrl();

    // For now, fake the verification until implemented
    site.setVerifiedUrl(url);
    siteManager.save(site);
  }

  @Override
  public void heartbeat(ReplicationSite site) throws InterruptedException {
    // do nothing for now
    // schedule the heartbeat to occur on another thread
    // if the heartbeat fails, get all configurations that involves that site
    //   and mark their status to Status.CONNECTION_LOST if the current status was XXXX_IN_PROGRESS
    //   and to Status.CONNECTION_UNAVAILABLE for any other status
    // if the heartbeat succeeds, get all configurations that involves that site
    //   and if their current status is Status.CONNECTION_LOST or Status.CONNECTION_UNAVAILABLE,
    //   mark their status to Status.PENDING indicating the remote end should restart soon to
    //   replicate. For all other status, leave untouched
  }
}
