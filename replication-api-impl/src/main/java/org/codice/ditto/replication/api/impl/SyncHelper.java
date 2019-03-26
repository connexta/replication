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

import com.google.common.base.Splitter;
import com.google.common.io.ByteSource;
import ddf.catalog.Constants;
import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.data.impl.ContentItemImpl;
import ddf.catalog.content.operation.impl.CreateStorageRequestImpl;
import ddf.catalog.content.operation.impl.UpdateStorageRequestImpl;
import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.OperationTransaction.OperationType;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.Response;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.operation.impl.CreateRequestImpl;
import ddf.catalog.operation.impl.DeleteRequestImpl;
import ddf.catalog.operation.impl.OperationTransactionImpl;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import ddf.catalog.resource.Resource;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.util.impl.ResultIterable;
import ddf.security.SubjectUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.ReplicationItem;
import org.codice.ditto.replication.api.ReplicationPersistentStore;
import org.codice.ditto.replication.api.ReplicationStatus;
import org.codice.ditto.replication.api.ReplicationStore;
import org.codice.ditto.replication.api.ReplicatorHistory;
import org.codice.ditto.replication.api.Status;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.mcard.Replication;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SyncHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncHelper.class);

  private final ReplicationStore source;

  private final String sourceName;

  private final ReplicationStore destination;

  private final String destinationName;

  private final ReplicatorConfig config;

  private final ReplicationPersistentStore persistentStore;

  private final ReplicatorHistory history;

  private final FilterBuilder builder;

  private Metacard mcard;

  private Optional<ReplicationItem> existingReplicationItem;

  private long syncCount;

  private long failCount;

  private long bytesTransferred;

  private boolean canceled = false;

  private ReplicationStatus status;

  public SyncHelper(
      ReplicationStore source,
      ReplicationStore destination,
      ReplicatorConfig config,
      ReplicationStatus status,
      ReplicationPersistentStore persistentStore,
      ReplicatorHistory history,
      FilterBuilder builder) {
    this.source = source;
    this.destination = destination;
    this.config = config;
    this.status = status;
    this.persistentStore = persistentStore;
    this.history = history;
    this.builder = builder;
    this.sourceName = source.getRemoteName();
    this.destinationName = destination.getRemoteName();
    syncCount = 0;
    failCount = 0;
    bytesTransferred = 0;
  }

  @SuppressWarnings("squid:S3655" /*isUpdatable performs the needed optional check*/)
  public SyncResponse sync() {
    for (Result metacardResult : getMetacardChangeSet()) {
      if (canceled) {
        break;
      }
      mcard = metacardResult.getMetacard();
      existingReplicationItem = persistentStore.getItem(mcard.getId(), sourceName, destinationName);

      try {
        if (isDeletedMetacard()) {
          processDeletedMetacard();
        } else if (isUpdatable()) {
          processUpdate(existingReplicationItem.get());
        } else {
          processCreate();
        }
      } catch (Exception e) {
        if (causedByConnectionLoss(e)) {
          logConnectionLoss();
          status.setStatus(Status.CONNECTION_LOST);
          return new SyncResponse(syncCount, failCount, bytesTransferred, status.getStatus());
        } else {
          recordItemFailure(e);
        }
      }
    }
    status.setStatus(canceled ? Status.CANCELED : Status.SUCCESS);
    return new SyncResponse(syncCount, failCount, bytesTransferred, status.getStatus());
  }

  /**
   * Cancels the currently running sync job. It will wait till the current processing item is
   * finished.
   */
  public void cancel() {
    this.canceled = true;
  }

  public boolean isCanceled() {
    return this.canceled;
  }

  private Iterable<Result> getMetacardChangeSet() {
    Filter filter = buildFilter();

    final QueryRequest request =
        new QueryRequestImpl(
            new QueryImpl(
                filter,
                1,
                100,
                new SortByImpl(Core.METACARD_MODIFIED, SortOrder.ASCENDING),
                false,
                0L));
    return ResultIterable.resultIterable(source::query, request);
  }

  private Filter buildFilter() {
    final Filter ecqlFilter;
    final List<Filter> filters = createBasicMetacardFilters();
    final List<Filter> failureFilters = createFailedMetacardFilters();
    final ReplicationStatus lastSuccessfulRun =
        history
            .getReplicationEvents(config.getName())
            .stream()
            .filter(s -> s.getStatus().equals(Status.SUCCESS))
            .findFirst()
            .orElse(null);
    Filter finalFilter;

    try {
      ecqlFilter = ECQL.toFilter(config.getFilter());
    } catch (CQLException e) {
      throw new ReplicationException("Error creating filter from cql: " + config.getFilter(), e);
    }

    if (lastSuccessfulRun != null) {
      long time = lastSuccessfulRun.getStartTime().getTime();
      if (lastSuccessfulRun.getLastSuccess() != null) {
        time = lastSuccessfulRun.getLastSuccess().getTime();
      }
      Date timeStamp = new Date(time - 1000);
      List<Filter> deletedFilters = createDeletedMetacardFilters(timeStamp);
      filters.add(builder.attribute(Core.METACARD_MODIFIED).is().after().date(timeStamp));
      finalFilter =
          builder.allOf(
              ecqlFilter, builder.anyOf(builder.allOf(filters), builder.allOf(deletedFilters)));
    } else {
      filters.add(ecqlFilter);
      finalFilter = builder.allOf(filters);
    }

    if (!failureFilters.isEmpty()) {
      finalFilter = builder.anyOf(finalFilter, builder.anyOf(failureFilters));
    }
    return finalFilter;
  }

  private List<Filter> createBasicMetacardFilters() {
    final List<Filter> filters = new ArrayList<>();
    filters.add(
        builder.not(builder.attribute(Replication.ORIGINS).is().equalTo().text(destinationName)));
    filters.add(builder.attribute(Core.METACARD_TAGS).is().equalTo().text(Metacard.DEFAULT_TAG));
    return filters;
  }

  private List<Filter> createFailedMetacardFilters() {
    final List<Filter> failureFilters = new ArrayList<>();
    List<String> failedIDs =
        persistentStore.getFailureList(config.getFailureRetryCount(), sourceName, destinationName);
    for (String id : failedIDs) {
      failureFilters.add(builder.attribute(Core.ID).is().equalTo().text(id));
    }
    return failureFilters;
  }

  private List<Filter> createDeletedMetacardFilters(Date timeStamp) {
    final List<Filter> deletedFilters = new ArrayList<>();
    deletedFilters.add(builder.attribute(MetacardVersion.VERSIONED_ON).after().date(timeStamp));
    deletedFilters.add(
        builder.attribute(Core.METACARD_TAGS).is().equalTo().text(MetacardVersion.VERSION_TAG));
    deletedFilters.add(builder.attribute(MetacardVersion.ACTION).is().like().text("Deleted*"));
    return deletedFilters;
  }

  private boolean isDeletedMetacard() {
    return Optional.ofNullable(mcard.getAttribute(MetacardVersion.ACTION))
        .filter(type -> type.getValue().toString().startsWith("Deleted"))
        .isPresent();
  }

  private void processDeletedMetacard() throws IngestException {
    String mcardId = (String) mcard.getAttribute(MetacardVersion.VERSION_OF_ID).getValue();
    existingReplicationItem = persistentStore.getItem(mcardId, sourceName, destinationName);

    if (existingReplicationItem.isPresent()) {
      final DeleteRequest deleteRequest = new DeleteRequestImpl(mcardId);

      // adding the operation transaction to avoid NPE when forming the response in AbstractCSWStore
      deleteRequest
          .getProperties()
          .put(
              Constants.OPERATION_TRANSACTION_KEY,
              new OperationTransactionImpl(OperationType.DELETE, Collections.singletonList(mcard)));
      final DeleteResponse deleteResponse = destination.delete(deleteRequest);
      checkForProcessingErrors(deleteResponse, "DeleteRequest");

      // remove oldReplicationItem from the store if the deleteRequest is successful
      persistentStore.deleteItem(mcardId, sourceName, destinationName);
      syncCount++;
      status.incrementCount();
    } else {
      LOGGER.trace(
          "No replication item for deleted metacard (id = {}). Not sending a delete request.",
          mcardId);
    }
  }

  private boolean isUpdatable() {
    return existingReplicationItem.isPresent() && metacardExists(mcard.getId(), destination);
  }

  private boolean metacardExists(String id, ReplicationStore store) {
    try {
      return store
              .query(
                  new QueryRequestImpl(
                      new QueryImpl(builder.attribute(Core.ID).is().equalTo().text(id))))
              .getHits()
          > 0;
    } catch (UnsupportedOperationException | UnsupportedQueryException e) {
      throw new ReplicationException(
          "Error checking for the existence of metacard " + id + " on " + store.getRemoteName());
    }
  }

  private void processUpdate(ReplicationItem replicationItem)
      throws IngestException, SourceUnavailableException {
    prepMetacard();
    if (resourceShouldBeUpdated(replicationItem)) {
      performResourceUpdate();
    } else if (metacardShouldBeUpdated(replicationItem)) {
      performMetacardUpdate();
    } else {
      logMetacardSkipped();
    }
  }

  private boolean resourceShouldBeUpdated(ReplicationItem replicationItem) {
    boolean hasResource = mcard.getResourceURI() != null;
    Date resourceModified = mcard.getModifiedDate();
    return hasResource
        && (resourceModified.after(replicationItem.getResourceModified())
            || replicationItem.getFailureCount() > 0);
  }

  private void performResourceUpdate() throws IngestException, SourceUnavailableException {
    final ContentItem contentItem = getResourceContentForMetacard();
    final UpdateResponse updateResponse =
        destination.update(
            new UpdateStorageRequestImpl(Collections.singletonList(contentItem), new HashMap<>()));
    checkForProcessingErrors(updateResponse, "UpdateStorageRequest");
    long bytes = Long.parseLong(mcard.getResourceSize());
    bytesTransferred += bytes;
    recordSuccessfulReplication();
    status.incrementBytesTransferred(bytes);
  }

  private boolean metacardShouldBeUpdated(ReplicationItem replicationItem) {
    Date metacardModified = (Date) mcard.getAttribute(Core.METACARD_MODIFIED).getValue();
    return metacardModified.after(replicationItem.getMetacardModified())
        || replicationItem.getFailureCount() > 0;
  }

  private void performMetacardUpdate() throws IngestException {
    final UpdateRequest updateRequest = new UpdateRequestImpl(mcard.getId(), mcard);

    // adding the operation transaction to avoid NPE when forming the response in AbstractCSWStore
    updateRequest
        .getProperties()
        .put(
            Constants.OPERATION_TRANSACTION_KEY,
            new OperationTransactionImpl(OperationType.UPDATE, Collections.singletonList(mcard)));
    final UpdateResponse updateResponse = destination.update(updateRequest);
    checkForProcessingErrors(updateResponse, "UpdateRequest");
    recordSuccessfulReplication();
  }

  private void logMetacardSkipped() {
    LOGGER.trace(
        "Not updating product (id = {}, hasResource = {}, metacard modified = {}, resource modified = {}, existing replication item: {})",
        mcard.getId(),
        mcard.getResourceURI() != null,
        mcard.getAttribute(Core.METACARD_MODIFIED).getValue(),
        mcard.getModifiedDate(),
        existingReplicationItem);
  }

  private void processCreate() throws IngestException, SourceUnavailableException {
    boolean hasResource = mcard.getResourceURI() != null;
    prepMetacard();

    if (hasResource) {
      performResourceCreate();
    } else {
      performMetacardCreate();
    }
    recordSuccessfulReplication();
  }

  private void performResourceCreate() throws IngestException, SourceUnavailableException {
    final ContentItem contentItem = getResourceContentForMetacard();
    final CreateResponse createResponse =
        destination.create(
            new CreateStorageRequestImpl(Collections.singletonList(contentItem), new HashMap<>()));
    checkForProcessingErrors(createResponse, "CreateStorageRequest");
    long bytes = Long.parseLong(mcard.getResourceSize());
    bytesTransferred += bytes;
    status.incrementBytesTransferred(bytes);
  }

  private void performMetacardCreate() throws IngestException {
    final CreateResponse createResponse = destination.create(new CreateRequestImpl(mcard));
    checkForProcessingErrors(createResponse, "CreateRequest");
  }

  private void prepMetacard() {
    List<Serializable> origins = new ArrayList<>();
    Attribute currentOrigins = mcard.getAttribute(Replication.ORIGINS);
    if (currentOrigins != null) {
      origins.addAll(currentOrigins.getValues());
    }

    origins.add(source.getRemoteName());
    mcard.setAttribute(new AttributeImpl(Replication.ORIGINS, origins));

    Set<Serializable> tags = new HashSet<>(mcard.getTags());
    tags.add(Replication.REPLICATED_TAG);
    List<Serializable> tagList = new ArrayList<>(tags);
    mcard.setAttribute(new AttributeImpl(Metacard.TAGS, tagList));
    mcard.setAttribute(
        new AttributeImpl(
            Metacard.POINT_OF_CONTACT, SubjectUtils.getEmailAddress(SecurityUtils.getSubject())));
    // We are not transferring derived resources at this point so remove the derived uri/urls
    mcard.setAttribute(
        new AttributeImpl(Metacard.DERIVED_RESOURCE_DOWNLOAD_URL, (Serializable) null));
    mcard.setAttribute(new AttributeImpl(Metacard.DERIVED_RESOURCE_URI, (Serializable) null));
  }

  private ContentItem getResourceContentForMetacard() throws IngestException {
    URI uri = mcard.getResourceURI();
    Resource resource = getResource(uri);
    ByteSource byteSource =
        new ByteSource() {
          @Override
          public InputStream openStream() {
            return resource.getInputStream();
          }
        };

    String qualifier = getQualifier(uri);
    ContentItem item =
        new ContentItemImpl(
            mcard.getId(),
            qualifier,
            byteSource,
            resource.getMimeTypeValue(),
            resource.getName() == null && qualifier != null
                ? qualifier + ".bin"
                : resource.getName(),
            resource.getSize(),
            mcard);
    if (qualifier != null) {
      addDerivedResourceUriToMetacard(item);
    }
    return item;
  }

  private Resource getResource(URI uri) throws IngestException {
    String mcardId = mcard.getId();
    Map<String, Serializable> properties = new HashMap<>();
    properties.put(Core.ID, mcardId);
    try {
      return source.retrieveResource(uri, properties).getResource();
    } catch (ResourceNotFoundException | ResourceNotSupportedException | IOException e) {
      throw new IngestException("Failed to retrieve resource for metacard " + mcardId, e);
    }
  }

  private String getQualifier(URI uri) {
    String qualifier = uri.getFragment();
    if (qualifier == null && uri.getQuery() != null) {
      Map<String, String> map =
          Splitter.on('&').trimResults().withKeyValueSeparator("=").split(uri.getQuery());
      qualifier = map.get("qualifier");
    }
    return qualifier;
  }

  private void addDerivedResourceUriToMetacard(ContentItem contentItem) {
    Attribute attribute = mcard.getAttribute(Core.DERIVED_RESOURCE_URI);
    if (attribute == null) {
      attribute = new AttributeImpl(Core.DERIVED_RESOURCE_URI, contentItem.getUri());
    } else {
      AttributeImpl newAttribute = new AttributeImpl(attribute);
      newAttribute.addValue(contentItem.getUri());
      attribute = newAttribute;
    }
    mcard.setAttribute(attribute);
  }

  private void recordSuccessfulReplication() {
    persistentStore.saveItem(createReplicationItem());
    syncCount++;
    status.incrementCount();
  }

  private void checkForProcessingErrors(Response response, String requestType)
      throws IngestException {
    if (CollectionUtils.isNotEmpty(response.getProcessingErrors())) {
      throw new IngestException("Processing errors when submitting a " + requestType);
    }
  }

  private boolean causedByConnectionLoss(Exception e) {
    return e instanceof SourceUnavailableException
        || !source.isAvailable()
        || !destination.isAvailable();
  }

  private void logConnectionLoss() {
    LOGGER.debug(
        "Encountered connection loss with source {}. Not continuing to process. Marking status as {}.",
        sourceName,
        Status.CONNECTION_LOST);
  }

  private void recordItemFailure(Exception e) {
    ReplicationItem newReplicationItem;

    LOGGER.debug("Exception processing record for metacard id {}", mcard.getId(), e);
    if (existingReplicationItem.isPresent()) {
      newReplicationItem = existingReplicationItem.get();
    } else {
      newReplicationItem = createReplicationItem();
    }
    newReplicationItem.incrementFailureCount();
    persistentStore.saveItem(newReplicationItem);
    failCount++;
    status.incrementFailure();
  }

  private ReplicationItem createReplicationItem() {
    String mcardId = mcard.getId();
    Date resourceModified = mcard.getModifiedDate();
    Date metacardModified = (Date) mcard.getAttribute(Core.METACARD_MODIFIED).getValue();

    return new ReplicationItemImpl(
        mcardId,
        resourceModified,
        metacardModified,
        sourceName,
        destinationName,
        existingReplicationItem.isPresent()
            ? existingReplicationItem.get().getConfigurationId()
            : config.getId());
  }
}
