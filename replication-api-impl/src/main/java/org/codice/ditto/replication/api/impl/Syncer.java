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

import com.connexta.replication.data.QueryRequestImpl;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.ws.rs.NotFoundException;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.Replication;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.ReplicationStatus;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.data.Resource;
import org.codice.ditto.replication.api.data.ResourceResponse;
import org.codice.ditto.replication.api.impl.data.CreateRequestImpl;
import org.codice.ditto.replication.api.impl.data.CreateStorageRequestImpl;
import org.codice.ditto.replication.api.impl.data.DeleteRequestImpl;
import org.codice.ditto.replication.api.impl.data.ResourceRequestImpl;
import org.codice.ditto.replication.api.impl.data.UpdateRequestImpl;
import org.codice.ditto.replication.api.impl.data.UpdateStorageRequestImpl;
import org.codice.ditto.replication.api.persistence.ReplicationItemManager;
import org.codice.ditto.replication.api.persistence.ReplicatorHistoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs creates, updates and deletes between source and destination {@link NodeAdapter}s based
 * on the given {@link ReplicatorConfig}.
 */
public class Syncer {

  private static final Logger LOGGER = LoggerFactory.getLogger(Syncer.class);

  private final ReplicationItemManager replicationItemManager;

  private final ReplicatorHistoryManager historyManager;

  public Syncer(
      ReplicationItemManager replicationItemManager, ReplicatorHistoryManager historyManager) {
    this.replicationItemManager = replicationItemManager;
    this.historyManager = historyManager;
  }

  /**
   * Create a new job for replicating between the given source and destination {@link NodeAdapter}s.
   * {@link Job#sync()} must be called to begin the syncing process.
   *
   * @param source the source {@link NodeAdapter}
   * @param destination the destination {@link NodeAdapter}
   * @param replicatorConfig a {@link ReplicatorConfig} defining the context of the sync
   * @param replicationStatus a new {@link ReplicationStatus}
   * @return a job ready for syncing
   */
  public Job create(
      NodeAdapter source,
      NodeAdapter destination,
      ReplicatorConfig replicatorConfig,
      ReplicationStatus replicationStatus) {
    return new Job(source, destination, replicatorConfig, replicationStatus);
  }

  class Job {

    private final Object lock = new Object();

    private final NodeAdapter source;

    private final NodeAdapter destination;

    private final String sourceName;

    private final String destinationName;

    private final ReplicatorConfig replicatorConfig;

    private final ReplicationStatus replicationStatus;

    private boolean canceled = false;

    private long bytesTransferred = 0;

    Job(
        NodeAdapter source,
        NodeAdapter destination,
        ReplicatorConfig replicatorConfig,
        ReplicationStatus replicationStatus) {
      this.source = source;
      this.destination = destination;
      this.replicatorConfig = replicatorConfig;
      this.replicationStatus = replicationStatus;

      this.sourceName = source.getSystemName();
      this.destinationName = destination.getSystemName();
    }

    /**
     * Blocking call that begins syncing between a source and destination {@link NodeAdapter}s.
     *
     * @return the result {@link Status} of the sync
     */
    SyncResponse sync() {
      Date modifiedAfter = getModifiedAfter();
      List<String> failedItemIds =
          replicationItemManager.getFailureList(
              replicatorConfig.getFailureRetryCount(), sourceName, destinationName);

      QueryRequest queryRequest =
          new QueryRequestImpl(
              replicatorConfig.getFilter(),
              Collections.singletonList(destinationName),
              failedItemIds,
              modifiedAfter);

      Iterable<Metadata> changeSet = source.query(queryRequest).getMetadata();

      for (Metadata metadata : changeSet) {
        synchronized (lock) {
          if (canceled) {
            break;
          }
        }

        Optional<ReplicationItem> replicationItem =
            replicationItemManager.getItem(metadata.getId(), sourceName, destinationName);

        try {
          if (metadata.isDeleted() && replicationItem.isPresent()) {
            doDelete(metadata, replicationItem.get());
          } else if (replicationItem.isPresent() && destination.exists(metadata)) {
            doUpdate(metadata, replicationItem.get());
          } else {
            doCreate(metadata, replicationItem);
          }
        } catch (VirtualMachineError e) {
          throw e;
        } catch (Exception e) {
          final boolean sourceAvailable = source.isAvailable();
          final boolean destinationAvailable = destination.isAvailable();
          if (!sourceAvailable || !destinationAvailable) {
            LOGGER.debug(
                "Lost connection to either source {} (available={}) or destination {} (available={}). Setting status to {}",
                sourceName,
                sourceAvailable,
                destinationName,
                destinationAvailable,
                Status.CONNECTION_LOST);
            replicationStatus.setStatus(Status.CONNECTION_LOST);
            return new SyncResponse(replicationStatus.getStatus());
          } else if (replicationItem.isPresent()) {
            ReplicationItem item = replicationItem.get();
            item.incrementFailureCount();
            replicationItemManager.saveItem(item);
            replicationStatus.incrementFailure();
          } else {
            ReplicationItem item = createReplicationItem(metadata, replicationItem);
            item.incrementFailureCount();
            replicationItemManager.saveItem(item);
            replicationStatus.incrementFailure();
          }
        }

        if (replicationStatus.getLastMetadataModified() == null
            || metadata.getMetadataModified().after(replicationStatus.getLastMetadataModified())) {
          replicationStatus.setLastMetadataModified(metadata.getMetadataModified());
        }
      }

      synchronized (lock) {
        replicationStatus.setStatus(canceled ? Status.CANCELED : Status.SUCCESS);
      }
      return new SyncResponse(replicationStatus.getStatus());
    }

    /**
     * Cancel this sync job. Any metadata/resource currently being replicated will first be finished
     * and recorded.
     */
    void cancel() {
      synchronized (lock) {
        this.canceled = true;
      }
    }

    private void doCreate(Metadata metadata, Optional<ReplicationItem> item) {
      addTagsAndLineage(metadata);
      boolean created;

      final String metadataId = metadata.getId();
      if (hasResource(metadata)) {
        ResourceResponse resourceResponse = source.readResource(new ResourceRequestImpl(metadata));
        List<Resource> resources = Collections.singletonList(resourceResponse.getResource());

        LOGGER.trace(
            "Sending create storage from {} to {} for metadata {}",
            sourceName,
            destinationName,
            metadataId);
        created = destination.createResource(new CreateStorageRequestImpl(resources));
        if (created) {
          bytesTransferred += metadata.getResourceSize();
          replicationStatus.incrementBytesTransferred(bytesTransferred);
        }
      } else {
        LOGGER.trace(
            "Sending create from {} to {} for metadata {}",
            sourceName,
            destinationName,
            metadataId);
        created =
            destination.createRequest(new CreateRequestImpl(Collections.singletonList(metadata)));
      }

      if (created) {
        ReplicationItem replicationItem = createReplicationItem(metadata, Optional.empty());
        replicationItemManager.saveItem(replicationItem);
        replicationStatus.incrementCount();
      } else {
        ReplicationItem replicationItem = createReplicationItem(metadata, item);
        replicationItem.incrementFailureCount();
        replicationItemManager.saveItem(replicationItem);
        replicationStatus.incrementFailure();

        LOGGER.debug(
            "Failed to create metadata {} from source {} to destination {}",
            metadata.getId(),
            sourceName,
            destinationName);
      }
    }

    private void doUpdate(Metadata metadata, ReplicationItem replicationItem) {
      addTagsAndLineage(metadata);

      boolean shouldUpdateMetadata =
          metadata.getMetadataModified().after(replicationItem.getMetadataModified())
              || replicationItem.getFailureCount() > 0;

      boolean shouldUpdateResource =
          hasResource(metadata)
              && (metadata.getResourceModified().after(replicationItem.getResourceModified())
                  || replicationItem.getFailureCount() > 0);

      final String metadataId = metadata.getId();
      boolean updated;
      if (shouldUpdateResource) {
        ResourceResponse resourceResponse = source.readResource(new ResourceRequestImpl(metadata));
        List<Resource> resources = Collections.singletonList(resourceResponse.getResource());

        LOGGER.trace(
            "Sending update storage from {} to {} for metadata {}",
            sourceName,
            destinationName,
            metadataId);
        updated = destination.updateResource(new UpdateStorageRequestImpl(resources));
        if (updated) {
          bytesTransferred += metadata.getResourceSize();
          replicationStatus.incrementBytesTransferred(bytesTransferred);
        }
      } else if (shouldUpdateMetadata) {
        LOGGER.trace(
            "Sending update from {} to {} for metadata {}",
            sourceName,
            destinationName,
            metadataId);

        updated =
            destination.updateRequest(new UpdateRequestImpl(Collections.singletonList(metadata)));
      } else {
        LOGGER.debug(
            "Skipping metadata {} update from source {} to destination {}",
            metadata.getId(),
            sourceName,
            destinationName);
        return;
      }

      if (updated) {
        ReplicationItem updateReplicationItem = createReplicationItem(metadata, Optional.empty());
        replicationItemManager.saveItem(updateReplicationItem);
        replicationStatus.incrementCount();
      } else {
        replicationItem.incrementFailureCount();
        replicationItemManager.saveItem(replicationItem);
        replicationStatus.incrementFailure();

        LOGGER.debug(
            "Failed to update metadata {} from source {} to destination {}",
            metadata.getId(),
            sourceName,
            destinationName);
      }
    }

    private void doDelete(Metadata metadata, ReplicationItem replicationItem) {
      LOGGER.trace(
          "Sending delete from {} to {} for metadata {}",
          sourceName,
          destinationName,
          metadata.getId());

      boolean deleted =
          destination.deleteRequest(new DeleteRequestImpl(Collections.singletonList(metadata)));
      String id = metadata.getId();

      if (deleted) {
        replicationItemManager.deleteItem(id, sourceName, destinationName);
        replicationStatus.incrementCount();
      } else {
        replicationItem.incrementFailureCount();
        replicationItemManager.saveItem(replicationItem);
        replicationStatus.incrementFailure();

        LOGGER.debug(
            "Failed to delete metadata {} from source {} to destination {}",
            id,
            sourceName,
            destinationName);
      }
    }

    private boolean hasResource(Metadata metadata) {
      return metadata.getResourceUri() != null;
    }

    private void addTagsAndLineage(Metadata metadata) {
      metadata.addLineage(sourceName);
      metadata.addTag(Replication.REPLICATED_TAG);
    }

    private ReplicationItem createReplicationItem(
        Metadata metadata, Optional<ReplicationItem> existingItem) {
      int failureCount = existingItem.isPresent() ? existingItem.get().getFailureCount() : 0;
      return new ReplicationItemImpl(
          metadata.getId(),
          metadata.getResourceModified(),
          metadata.getMetadataModified(),
          sourceName,
          destinationName,
          replicatorConfig.getId(),
          failureCount);
    }

    @Nullable
    private Date getModifiedAfter() {
      ReplicationStatus status;
      try {
        status = historyManager.getByReplicatorId(replicatorConfig.getId());
      } catch (NotFoundException e) {
        LOGGER.trace(
            "no history for replication config {} found. This config may not have completed a run yet.",
            replicatorConfig.getId());
        return null;
      }

      if (status.getLastMetadataModified() != null) {
        return status.getLastMetadataModified();
      } else {
        LOGGER.trace("no previous successful run for config {} found.", replicatorConfig.getId());
        return null;
      }
    }
  }
}
