package org.codice.ditto.replication.api.impl;

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
import org.codice.ditto.replication.api.impl.data.QueryRequestImpl;
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

  public Job create(
      NodeAdapter source,
      NodeAdapter destination,
      ReplicatorConfig replicatorConfig,
      ReplicationStatus replicationStatus) {
    return new Job(source, destination, replicatorConfig, replicationStatus);
  }

  public class Job {

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

    public SyncResponse sync() {
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

      Date modifiedOfLastMetadata = null;
      for (Metadata metadata : changeSet) {
        if (canceled) {
          break;
        }

        Optional<ReplicationItem> replicationItem =
            replicationItemManager.getItem(metadata.getId(), sourceName, destinationName);

        if (!replicationItem.isPresent()) {
          doCreate(metadata);
        } else if (source.exists(metadata)) {
          doUpdate(metadata, replicationItem.get());
        } else {
          doDelete(metadata, replicationItem.get());
        }

        modifiedOfLastMetadata = metadata.getMetadataModified();
      }

      if (modifiedOfLastMetadata != null) {
        replicationStatus.setLastSuccess(modifiedOfLastMetadata);
      }

      replicationStatus.setStatus(canceled ? Status.CANCELED : Status.SUCCESS);
      return new SyncResponse(replicationStatus.getStatus());
    }

    /**
     * Cancel this sync job. Any metadata/resource currently being replicated will first be finished
     * and recorded.
     */
    public void cancel() {
      this.canceled = true;
    }

    private void doCreate(Metadata metadata) {
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
        ReplicationItem replicationItem = createReplicationItem(metadata);
        replicationItemManager.saveItem(replicationItem);
        replicationStatus.incrementCount();
      } else {
        ReplicationItem replicationItem = createReplicationItem(metadata);
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
          metadata.getMetadataModified().after(replicationItem.getMetacardModified())
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
            destination,
            metadataId);
        updated = destination.updateResource(new UpdateStorageRequestImpl(resources));
        if (updated) {
          bytesTransferred += metadata.getResourceSize();
          replicationStatus.incrementBytesTransferred(bytesTransferred);
        }
      } else if (shouldUpdateMetadata) {
        LOGGER.trace(
            "Sending update from {} to {} for metadata {}", sourceName, destination, metadataId);
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
        ReplicationItem updateReplicationItem = createReplicationItem(metadata);
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
      boolean deleted =
          destination.deleteRequest(new DeleteRequestImpl(Collections.singletonList(metadata)));
      String id = metadata.getId();

      if (deleted) {
        LOGGER.trace(
            "Sending delete from {} to {} for metadata {}",
            sourceName,
            destinationName,
            metadata.getId());
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
      // todo: Will this add duplicate lineage?
      metadata.addLineage(sourceName);
      metadata.addTag(Replication.REPLICATED_TAG);
    }

    private ReplicationItem createReplicationItem(Metadata metadata) {
      return new ReplicationItemImpl(
          metadata.getId(),
          metadata.getResourceModified(),
          metadata.getMetadataModified(),
          sourceName,
          destinationName,
          replicatorConfig.getId());
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

      if (status.getLastSuccess() != null) {
        return new Date(status.getLastSuccess().getTime());
      } else {
        LOGGER.trace("no previous successful run for config {} found.", replicatorConfig.getId());
        return null;
      }
    }
  }
}
