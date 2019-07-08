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

import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.Status;
import com.connexta.ion.replication.api.data.ReplicationStatus;
import com.connexta.ion.replication.api.impl.data.ReplicationStatusImpl;
import com.connexta.ion.replication.api.impl.spring.HistoryRepository;
import com.connexta.ion.replication.api.persistence.ReplicatorHistoryManager;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** A persistent store based implementation of the ReplicatorHistoryManager interface */
public class ReplicatorHistoryManagerImpl implements ReplicatorHistoryManager {

  private final HistoryRepository historyRepository;

  public ReplicatorHistoryManagerImpl(HistoryRepository historyRepository) {
    this.historyRepository = historyRepository;
  }

  @Override
  public ReplicationStatus create() {
    return new ReplicationStatusImpl();
  }

  @Override
  public ReplicationStatus get(String id) {
    return historyRepository.findById(id).orElseThrow(NotFoundException::new);
  }

  @Override
  public Stream<ReplicationStatus> objects() {
    return StreamSupport.stream(historyRepository.findAll().spliterator(), false)
        .map(ReplicationStatus.class::cast);
  }

  @Override
  public ReplicationStatus getByReplicatorId(String id) {
    return objects()
        .filter(status -> status.getReplicatorId().equals(id))
        .findFirst()
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Could not find a ReplicatonStatus for a replicator with ID: " + id));
  }

  @Override
  public void save(ReplicationStatus status) {
    if (!(status instanceof ReplicationStatusImpl)) {
      throw new IllegalArgumentException(
          "Expected a ReplicationStatusImpl but got a " + status.getClass().getSimpleName());
    }

    try {
      ReplicationStatus previous = getByReplicatorId(status.getReplicatorId());
      previous.addStats(status);
      historyRepository.save((ReplicationStatusImpl) previous);
    } catch (NotFoundException e) {
      status.setLastRun(status.getStartTime());
      if (status.getStatus().equals(Status.SUCCESS)) {
        status.setLastSuccess(status.getStartTime());
      }
      historyRepository.save((ReplicationStatusImpl) status);
    }
  }

  @Override
  public void remove(String id) {
    historyRepository.deleteById(id);
  }
}
