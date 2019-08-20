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
package com.connexta.ion.replication.api.impl;

import com.connexta.ion.replication.api.Action;
import com.connexta.ion.replication.api.NodeAdapter;
import com.connexta.ion.replication.api.Replication;
import com.connexta.ion.replication.api.Status;
import com.connexta.ion.replication.api.data.Metadata;
import com.connexta.ion.replication.api.data.QueryRequest;
import com.connexta.ion.replication.api.data.Resource;
import com.connexta.ion.replication.api.data.ResourceResponse;
import com.connexta.ion.replication.api.impl.data.CreateRequestImpl;
import com.connexta.ion.replication.api.impl.data.CreateStorageRequestImpl;
import com.connexta.ion.replication.api.impl.data.DeleteRequestImpl;
import com.connexta.ion.replication.api.impl.data.ResourceRequestImpl;
import com.connexta.ion.replication.api.impl.data.UpdateRequestImpl;
import com.connexta.ion.replication.api.impl.data.UpdateStorageRequestImpl;
import com.connexta.ion.replication.data.QueryRequestImpl;
import com.connexta.replication.api.data.ReplicationItem;
import com.connexta.replication.api.data.ReplicatorConfig;
import com.connexta.replication.api.impl.data.ReplicationItemImpl;
import com.connexta.replication.api.impl.persistence.ReplicationItemManager;
import com.connexta.replication.api.impl.persistence.ReplicatorConfigManager;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs creates, updates and deletes between source and destination {@link NodeAdapter}s based
 * on the given {@link ReplicatorConfig}.
 */
public class Syncer {

  private static final Logger LOGGER = LoggerFactory.getLogger(Syncer.class);

  private final ReplicationItemManager replicationItemManager;

  private final ReplicatorConfigManager configs;

  /**
   * Creates a new {@code Syncer}.
   *
   * @param replicationItemManager bean for operating on {@link ReplicationItem}s
   * @param configs bean for operating on {@link ReplicatorConfig}s
   */
  public Syncer(ReplicationItemManager replicationItemManager, ReplicatorConfigManager configs) {
    this.replicationItemManager = replicationItemManager;
    this.configs = configs;
  }

  /**
   * Create a new job for replicating between the given source and destination {@link NodeAdapter}s.
   * {@link Job#sync()} must be called to begin the syncing process.
   *
   * @param source the source {@link NodeAdapter}
   * @param destination the destination {@link NodeAdapter}
   * @param replicatorConfig a {@link ReplicatorConfig} defining the context of the sync
   * @return a job ready for syncing
   */
  Job create(
      NodeAdapter source,
      NodeAdapter destination,
      ReplicatorConfig replicatorConfig,
      Set<Consumer<ReplicationItem>> callbacks) {
    return new Job(source, destination, replicatorConfig, callbacks);
  }

  /**
   * Class which will handle transferring resources and metadata between a source and destination
   * {@link NodeAdapter}.
   */
  class Job {

    private final NodeAdapter source;

    private final NodeAdapter destination;

    private final String sourceName;

    private final String destinationName;

    private final ReplicatorConfig replicatorConfig;

    private final Set<Consumer<ReplicationItem>> callbacks;

    Job(
        NodeAdapter source,
        NodeAdapter destination,
        ReplicatorConfig replicatorConfig,
        Set<Consumer<ReplicationItem>> callbacks) {
      this.source = source;
      this.destination = destination;
      this.replicatorConfig = replicatorConfig;
      this.callbacks = callbacks;

      this.sourceName = source.getSystemName();
      this.destinationName = destination.getSystemName();
    }

    /** Blocking call that begins syncing between a source and destination {@link NodeAdapter}s. */
    void sync() {
      Date modifiedAfter = getModifiedAfter();
      List<String> failedItemIds = replicationItemManager.getFailureList(replicatorConfig.getId());

      QueryRequest queryRequest =
          new QueryRequestImpl(
              replicatorConfig.getFilter(),
              Collections.singletonList(destinationName),
              failedItemIds,
              modifiedAfter);

      Iterable<Metadata> changeSet = source.query(queryRequest).getMetadata();

      for (Metadata metadata : changeSet) {
        ReplicationItem existingItem =
            replicationItemManager
                .getLatest(replicatorConfig.getId(), metadata.getId())
                .orElse(null);

        Status status;
        ReplicationItemImpl.Builder builder = createReplicationItem(metadata);
        builder.markStartTime();
        try {
          if (metadata.isDeleted() && existingItem != null) {
            builder.action(Action.DELETE);
            status = doDelete(metadata);
          } else if (destination.exists(metadata) && existingItem != null) {
            builder.action(Action.UPDATE);
            status = doUpdate(metadata, existingItem);
          } else {
            builder.action(Action.CREATE);
            status = doCreate(metadata);
          }
        } catch (VirtualMachineError e) {
          throw e;
        } catch (Exception e) {
          final boolean sourceAvailable = source.isAvailable();
          final boolean destinationAvailable = destination.isAvailable();
          if (!sourceAvailable || !destinationAvailable) {
            LOGGER.debug(
                "Lost connection to either source {} (available={}) or destination {} (available={}).",
                sourceName,
                sourceAvailable,
                destinationName,
                destinationAvailable);
            status = Status.CONNECTION_LOST;
          } else {
            status = Status.FAILURE;
          }
        }

        builder.markDoneTime();

        if (status != null) {
          ReplicationItem item = builder.status(status).build();
          replicationItemManager.save(item);
          callbacks.forEach(callback -> callback.accept(item));
        }
        final Instant lastModified = replicatorConfig.getLastMetadataModified();

        if (lastModified == null || metadata.getMetadataModified().after(Date.from(lastModified))) {
          replicatorConfig.setLastMetadataModified(metadata.getMetadataModified().toInstant());
          configs.save(replicatorConfig);
        }
      }
    }

    private Status doCreate(Metadata metadata) {
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
      } else {
        LOGGER.trace(
            "Sending create from {} to {} for metadata {}",
            sourceName,
            destinationName,
            metadataId);
        created =
            destination.createRequest(new CreateRequestImpl(Collections.singletonList(metadata)));
      }

      return created ? Status.SUCCESS : Status.FAILURE;
    }

    @Nullable
    private Status doUpdate(Metadata metadata, ReplicationItem replicationItem) {
      addTagsAndLineage(metadata);

      boolean shouldUpdateMetadata =
          metadata.getMetadataModified().after(replicationItem.getMetadataModified())
              || replicationItem.getStatus() != Status.SUCCESS;

      boolean shouldUpdateResource =
          hasResource(metadata)
              && (metadata.getResourceModified().after(replicationItem.getResourceModified())
                  || replicationItem.getStatus() != Status.SUCCESS);

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
        return null;
      }

      return updated ? Status.SUCCESS : Status.FAILURE;
    }

    private Status doDelete(Metadata metadata) {
      LOGGER.trace(
          "Sending delete from {} to {} for metadata {}",
          sourceName,
          destinationName,
          metadata.getId());

      boolean deleted =
          destination.deleteRequest(new DeleteRequestImpl(Collections.singletonList(metadata)));
      return deleted ? Status.SUCCESS : Status.FAILURE;
    }

    private boolean hasResource(Metadata metadata) {
      return metadata.getResourceUri() != null;
    }

    private void addTagsAndLineage(Metadata metadata) {
      metadata.addLineage(sourceName);
      metadata.addTag(Replication.REPLICATED_TAG);
    }

    private ReplicationItemImpl.Builder createReplicationItem(Metadata metadata) {
      return new ReplicationItemImpl.Builder(
              metadata.getId(), replicatorConfig.getId(), sourceName, destinationName)
          .resourceModified(metadata.getResourceModified())
          .metadataModified(metadata.getMetadataModified())
          .resourceSize(metadata.getResourceSize())
          .metadataSize(metadata.getMetadataSize());
    }

    @Nullable
    private Date getModifiedAfter() {
      final Instant lastModified = replicatorConfig.getLastMetadataModified();

      if (lastModified != null) {
        return Date.from(lastModified);
      } else {
        LOGGER.trace("no previous successful run for config {} found.", replicatorConfig.getName());
        return null;
      }
    }
  }
}
