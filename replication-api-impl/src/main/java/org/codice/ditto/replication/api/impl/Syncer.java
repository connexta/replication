package org.codice.ditto.replication.api.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.ReplicationPersistentStore;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.CreateRequestImpl;
import org.codice.ditto.replication.api.impl.data.CreateStorageRequestImpl;
import org.codice.ditto.replication.api.impl.data.DeleteRequestImpl;
import org.codice.ditto.replication.api.impl.data.QueryRequestImpl;
import org.codice.ditto.replication.api.impl.data.ResourceRequestImpl;
import org.codice.ditto.replication.api.impl.data.UpdateRequestImpl;
import org.codice.ditto.replication.api.impl.data.UpdateStorageRequestImpl;
import org.codice.ditto.replication.api.mcard.Replication;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.Resource;
import org.codice.ditto.replication.api.data.ResourceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs creates, updates and deletes between source and destination {@link NodeAdapter}s based
 * on the given {@link ReplicatorConfig}.
 */
public class Syncer {

  private static final Logger LOGGER = LoggerFactory.getLogger(Syncer.class);

  private final ReplicationPersistentStore replicationPersistentStore;

  private final ReplicatorHistory replicatorHistory;

  public Syncer(
      ReplicationPersistentStore replicationPersistentStore, ReplicatorHistory replicatorHistory) {
    this.replicationPersistentStore = replicationPersistentStore;
    this.replicatorHistory = replicatorHistory;
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
          replicationPersistentStore.getFailureList(
              replicatorConfig.getFailureRetryCount(), sourceName, destinationName);

      QueryRequest queryRequest =
          new QueryRequestImpl(
              replicatorConfig.getFilter(),
              Collections.singletonList(destinationName),
              failedItemIds,
              modifiedAfter);

      Iterable<Metadata> changeSet = source.query(queryRequest).getMetadata();

      for (Metadata metadata : changeSet) {
        if (canceled) {
          break;
        }

        Optional<ReplicationItem> replicationItem =
            replicationPersistentStore.getItem(metadata.getId(), sourceName, destinationName);

        // todo: handle failures
        if (!replicationItem.isPresent()) {
          doCreate(metadata);
        } else if (destination.exists(metadata)) {
          doUpdate(metadata, replicationItem.get());
        } else {
          doDelete(metadata);
        }
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

      if (hasResource(metadata)) {
        ResourceResponse resourceResponse = source.readResource(new ResourceRequestImpl(metadata));
        List<Resource> resources = Collections.singletonList(resourceResponse.getResource());

        created = destination.createResource(new CreateStorageRequestImpl(resources));
        if (created) {
          bytesTransferred += metadata.getResourceSize();
          replicationStatus.incrementBytesTransferred(bytesTransferred);
        }
      } else {
        created =
            destination.createRequest(new CreateRequestImpl(Collections.singletonList(metadata)));
      }

      if (created) {
        ReplicationItem replicationItem = createReplicationItem(metadata);
        replicationPersistentStore.saveItem(replicationItem);
        replicationStatus.incrementCount();
      } else {
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
          metadata.getResourceModified().after(replicationItem.getResourceModified())
              || replicationItem.getFailureCount() > 0;

      boolean shouldUpdateResource = hasResource(metadata) && shouldUpdateMetadata;

      boolean updated = false;
      if (shouldUpdateResource) {
        ResourceResponse resourceResponse = source.readResource(new ResourceRequestImpl(metadata));
        List<Resource> resources = Collections.singletonList(resourceResponse.getResource());

        updated = destination.updateResource(new UpdateStorageRequestImpl(resources));
        if (updated) {
          bytesTransferred += metadata.getResourceSize();
          replicationStatus.incrementBytesTransferred(bytesTransferred);
        }
      } else if (shouldUpdateMetadata) {
        updated =
            destination.updateRequest(new UpdateRequestImpl(Collections.singletonList(metadata)));
      } else {
        LOGGER.debug(
            "Skipping metadata {} update from source {} to destination {}",
            metadata.getId(),
            sourceName,
            destinationName);
      }

      if (updated) {
        ReplicationItem updateReplicationItem = createReplicationItem(metadata);
        replicationPersistentStore.saveItem(updateReplicationItem);
        replicationStatus.incrementCount();
      } else {
        LOGGER.debug(
            "Failed to update metadata {} from source {} to destination {}",
            metadata.getId(),
            sourceName,
            destinationName);
      }
    }

    private void doDelete(Metadata metadata) {
      boolean deleted =
          destination.deleteRequest(new DeleteRequestImpl(Collections.singletonList(metadata)));
      String id = metadata.getId();

      if (deleted) {
        replicationPersistentStore.deleteItem(id, sourceName, destinationName);
        replicationStatus.incrementCount();
      } else {
        LOGGER.debug(
            "Failed to ddelete metadata {} from source {} to destination {}",
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
          metadata.getResourceModified(),
          sourceName,
          destinationName,
          replicatorConfig.getId());
    }

    @Nullable
    private Date getModifiedAfter() {
      final ReplicationStatus lastSuccessfulRun =
          replicatorHistory
              .getReplicationEvents(replicatorConfig.getName())
              .stream()
              .filter(s -> s.getStatus().equals(Status.SUCCESS))
              .findFirst()
              .orElse(null);

      if (lastSuccessfulRun != null) {
        long time = lastSuccessfulRun.getStartTime().getTime();
        if (lastSuccessfulRun.getLastSuccess() != null) {
          time = lastSuccessfulRun.getLastSuccess().getTime();
        }

        return new Date(time);
      } else {
        return null;
      }
    }
  }
}
