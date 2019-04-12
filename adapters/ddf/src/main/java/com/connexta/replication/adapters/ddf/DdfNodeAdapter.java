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
package com.connexta.replication.adapters.ddf;

import com.connexta.replication.adapters.ddf.DdfRestClientFactory.DdfRestClient;
import com.connexta.replication.data.MetadataImpl;
import com.connexta.replication.data.QueryResponseImpl;
import com.connexta.replication.data.ResourceImpl;
import com.connexta.replication.data.ResourceResponseImpl;
import com.google.common.base.Splitter;
import com.google.common.io.ByteSource;
import com.thoughtworks.xstream.converters.Converter;
import ddf.catalog.Constants;
import ddf.catalog.content.data.ContentItem;
import ddf.catalog.content.data.impl.ContentItemImpl;
import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.types.Core;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.OperationTransaction.OperationType;
import ddf.catalog.operation.Query;
import ddf.catalog.operation.Response;
import ddf.catalog.operation.SourceResponse;
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
import ddf.catalog.source.UnsupportedQueryException;
import ddf.catalog.util.impl.ResultIterable;
import ddf.security.encryption.EncryptionService;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.spatial.ogc.csw.catalog.common.CswSourceConfiguration;
import org.codice.ddf.spatial.ogc.csw.catalog.common.source.AbstractCswStore;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.data.CreateRequest;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.DeleteRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.QueryResponse;
import org.codice.ditto.replication.api.data.ResourceRequest;
import org.codice.ditto.replication.api.data.ResourceResponse;
import org.codice.ditto.replication.api.data.UpdateRequest;
import org.codice.ditto.replication.api.data.UpdateStorageRequest;
import org.codice.ditto.replication.api.Replication;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.filter.Filter;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DdfNodeAdapter extends AbstractCswStore implements NodeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdfNodeAdapter.class);

  private static final String REGISTRY_TAG = "registry";

  private static final String REGISTRY_IDENTITY_NODE = "registry.local.registry-identity-node";

  private final DdfRestClientFactory ddfRestClientFactory;

  private final URL hostUrl;

  private String cachedSystemName;

  public DdfNodeAdapter(
      BundleContext context,
      CswSourceConfiguration cswSourceConfiguration,
      Converter provider,
      ClientFactoryFactory clientFactoryFactory,
      EncryptionService encryptionService,
      DdfRestClientFactory ddfRestClientFactory,
      URL hostUrl) {
    super(context, cswSourceConfiguration, provider, clientFactoryFactory, encryptionService);
    this.ddfRestClientFactory = ddfRestClientFactory;
    this.hostUrl = hostUrl;
  }

  @Override
  protected Map<String, Consumer<Object>> getAdditionalConsumers() {
    return Collections.emptyMap();
  }

  @Override
  public boolean isAvailable() {
    return getCapabilities() != null;
  }

  @Override
  public String getSystemName() {
    if (cachedSystemName != null) {
      return cachedSystemName;
    }
    Filter filter =
        filterBuilder.allOf(
            filterBuilder.attribute(Metacard.TAGS).is().equalTo().text(REGISTRY_TAG),
            filterBuilder.not(filterBuilder.attribute(REGISTRY_IDENTITY_NODE).empty()));

    Query newQuery = new QueryImpl(filter);
    ddf.catalog.operation.QueryRequest queryRequest =
        new QueryRequestImpl(newQuery, new HashMap<>());

    SourceResponse identityMetacard;
    try {
      identityMetacard = super.query(queryRequest);
    } catch (UnsupportedQueryException e) {
      throw new AdapterException("Could not get remote name from registry metacard", e);
    }
    if (!identityMetacard.getResults().isEmpty()) {
      cachedSystemName = identityMetacard.getResults().get(0).getMetacard().getTitle();
    } else {
      throw new AdapterException(
          "No registry metacard available on remote node. Could not retrieve remote system name");
    }
    return cachedSystemName;
  }

  @Override
  public QueryResponse query(QueryRequest queryRequest) {
    final String cql = queryRequest.getCql();
    Filter ecqlFilter;
    try {
      ecqlFilter = ECQL.toFilter(cql);
    } catch (CQLException e) {
      throw new AdapterException(String.format("Failed to create filter from cql %s", cql), e);
    }

    final List<Filter> filters = new ArrayList<>();

    for (String excludedNode : queryRequest.getExcludedNodes()) {
      filters.add(
          filterBuilder.not(
              filterBuilder.attribute(Replication.ORIGINS).is().equalTo().text(excludedNode)));
    }
    filters.add(
        filterBuilder.attribute(Core.METACARD_TAGS).is().equalTo().text(Metacard.DEFAULT_TAG));

    final List<Filter> failedItemFilters = new ArrayList<>();
    for (String itemId : queryRequest.getFailedItemIds()) {
      failedItemFilters.add(filterBuilder.attribute(Core.ID).is().equalTo().text(itemId));
    }

    Date modifiedAfter = queryRequest.getModifiedAfter();
    List<Filter> deletedFilters = new ArrayList<>();
    if (modifiedAfter != null) {
      modifiedAfter = new Date(modifiedAfter.getTime() - 1000);
      filters.add(filterBuilder.attribute(Core.METACARD_MODIFIED).is().after().date(modifiedAfter));

      deletedFilters.add(
          filterBuilder.attribute(MetacardVersion.VERSIONED_ON).after().date(modifiedAfter));
      deletedFilters.add(
          filterBuilder
              .attribute(Core.METACARD_TAGS)
              .is()
              .equalTo()
              .text(MetacardVersion.VERSION_TAG));
      deletedFilters.add(
          filterBuilder.attribute(MetacardVersion.ACTION).is().like().text("Deleted*"));
    }

    final Filter finalFilter =
        filterBuilder.anyOf(
            filterBuilder.allOf(
                ecqlFilter,
                filterBuilder.anyOf(
                    filterBuilder.allOf(filters), filterBuilder.allOf(deletedFilters))),
            filterBuilder.anyOf(failedItemFilters));

    Query query = new QueryImpl(finalFilter);
    ddf.catalog.operation.QueryRequest request = new QueryRequestImpl(query);

    ResultIterable results = ResultIterable.resultIterable(super::query, request);
    return new QueryResponseImpl(new ResultIterableWrapper(results));
  }

  @Override
  public boolean exists(Metadata metadata) {
    final String metacardId = metadata.getId();
    try {
      return this.query(
                  new QueryRequestImpl(
                      new QueryImpl(
                          filterBuilder.attribute(Core.ID).is().equalTo().text(metacardId))))
              .getHits()
          > 0;
    } catch (UnsupportedOperationException | UnsupportedQueryException e) {
      throw new AdapterException(
          String.format(
              "Error checking for the existence of metacard %s on %s",
              metacardId, getSystemName()));
    }
  }

  @Override
  public boolean createRequest(CreateRequest createRequest) {
    List<Metacard> metacards = getMetacards(createRequest.getMetadata());

    try {
      CreateResponse createResponse = super.create(new CreateRequestImpl(metacards));
      checkForProcessingErrors(createResponse, "CreateResponse");
    } catch (IngestException e) {
      LOGGER.debug("Failed to create metacards to remote store", e);
      return false;
    }

    return true;
  }

  @Override
  public boolean updateRequest(UpdateRequest updateRequest) {
    List<Metacard> metacards = getMetacards(updateRequest.getUpdatedMetadata());
    String[] ids = new String[metacards.size()];

    for (int i = 0; i < metacards.size(); i++) {
      ids[i] = metacards.get(i).getId();
    }

    ddf.catalog.operation.UpdateRequest ddfUpdate = new UpdateRequestImpl(ids, metacards);

    // todo: This previous state metacards is not correct, but it was this way before. Does this
    // have any effect?
    ddfUpdate
        .getProperties()
        .put(
            Constants.OPERATION_TRANSACTION_KEY,
            new OperationTransactionImpl(OperationType.UPDATE, metacards));

    try {
      UpdateResponse updateResponse = super.update(ddfUpdate);
      checkForProcessingErrors(updateResponse, "UpdateRequest");
    } catch (IngestException e) {
      LOGGER.debug("Failed to update metacards to remote store", e);
      return false;
    }

    return true;
  }

  @Override
  public boolean deleteRequest(DeleteRequest deleteRequest) {
    List<Metacard> metacards = getMetacards(deleteRequest.getMetadata());

    DeleteRequestImpl ddfDeleteRequest =
        new DeleteRequestImpl(metacards.stream().map(Metacard::getId).toArray(String[]::new));
    ddfDeleteRequest
        .getProperties()
        .put(
            Constants.OPERATION_TRANSACTION_KEY,
            new OperationTransactionImpl(OperationType.DELETE, metacards));

    try {
      DeleteResponse deleteResponse = super.delete(ddfDeleteRequest);
      checkForProcessingErrors(deleteResponse, "DeleteRequest");
    } catch (IngestException e) {
      LOGGER.debug("Failed to delete metacards to remote store", e);
      return false;
    }

    return true;
  }

  @Override
  public ResourceResponse readResource(ResourceRequest resourceRequest) {
    Metadata metadata = resourceRequest.getMetadata();

    Metacard metacard = (Metacard) metadata.getRawMetadata();
    String id = metacard.getId();
    URI uri = metacard.getResourceURI();

    Map<String, Serializable> properties = new HashMap<>();
    properties.put(Core.ID, id);

    Resource resource;
    try {
      resource = super.retrieveResource(uri, properties).getResource();
    } catch (IOException | ResourceNotFoundException | ResourceNotSupportedException e) {
      throw new AdapterException(
          String.format("Failed to retrieve resource %s", uri.toASCIIString()), e);
    }

    return new ResourceResponseImpl(
        new ResourceImpl(
            id,
            resource.getName(),
            uri,
            getQualifier(uri),
            resource.getInputStream(),
            resource.getMimeTypeValue(),
            resource.getSize(),
            metadata));
  }

  @Override
  public boolean createResource(CreateStorageRequest createStorageRequest) {
    List<ContentItem> contentItems =
        createStorageRequest
            .getResources()
            .stream()
            .map(this::toContentItem)
            .collect(Collectors.toList());

    DdfRestClient client = ddfRestClientFactory.create(hostUrl.toString());

    // todo: handle failures
    contentItems.forEach(client::post);

    return true;
  }

  @Override
  public boolean updateResource(UpdateStorageRequest updateStorageRequest) {
    List<ContentItem> contentItems =
        updateStorageRequest
            .getResources()
            .stream()
            .map(this::toContentItem)
            .collect(Collectors.toList());

    DdfRestClient client = ddfRestClientFactory.create(hostUrl.toString());

    // todo: handle failures
    contentItems.forEach(
        contentItem -> client.post(contentItem, contentItem.getMetacard().getId()));

    return true;
  }

  @Override
  public void close() {
    super.destroy(9001);
  }

  private List<Metacard> getMetacards(List<Metadata> metadata) {
    List<Metacard> metacards =
        metadata
            .stream()
            .filter(m -> Metacard.class.isAssignableFrom(m.getType()))
            .map(Metacard.class::cast)
            .collect(Collectors.toList());

    if (metadata.size() != metacards.size()) {
      LOGGER.debug(
          "Received unrecognized metadata in request and it will not be created on the remote store.");
    }

    return metacards;
  }

  private ContentItem toContentItem(org.codice.ditto.replication.api.data.Resource resource) {
    ByteSource byteSource =
        new ByteSource() {
          @Override
          public InputStream openStream() {
            return resource.getInputStream();
          }
        };

    return new ContentItemImpl(
        resource.getId(),
        resource.getQualifier(),
        byteSource,
        resource.getMimeType(),
        resource.getName(),
        resource.getSize(),
        (Metacard) resource.getMetadata());
  }

  private void checkForProcessingErrors(Response response, String requestType)
      throws IngestException {
    if (CollectionUtils.isNotEmpty(response.getProcessingErrors())) {
      throw new IngestException("Processing errors when submitting a " + requestType);
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

  private class ResultIterableWrapper implements Iterable<Metadata> {

    private final Iterator<Result> resultIterator;

    public ResultIterableWrapper(ResultIterable resultIterable) {
      resultIterator = resultIterable.iterator();
    }

    @Override
    public Iterator<Metadata> iterator() {
      return new Iterator<Metadata>() {
        @Override
        public boolean hasNext() {
          return resultIterator.hasNext();
        }

        @Override
        public Metadata next() {
          Metacard metacard = resultIterator.next().getMetacard();

          // Do not support transferring derived resources at this point so remove the derived
          // uri/urls
          metacard.setAttribute(
              new AttributeImpl(Metacard.DERIVED_RESOURCE_DOWNLOAD_URL, (Serializable) null));
          metacard.setAttribute(
              new AttributeImpl(Metacard.DERIVED_RESOURCE_URI, (Serializable) null));

          Metadata metadata =
              new MetadataImpl(
                  metacard,
                  Metacard.class,
                  (Date) metacard.getAttribute(Core.METACARD_MODIFIED).getValue());
          metadata.setResourceSize(Long.parseLong(metacard.getResourceSize()));
          metadata.setResourceModified(metacard.getModifiedDate());
          metadata.setResourceUri(metacard.getResourceURI());

          metacard
              .getAttribute(Replication.ORIGINS)
              .getValues()
              .stream()
              .filter(String.class::isInstance)
              .map(String.class::cast)
              .forEach(metadata::addLineage);

          metacard
              .getAttribute(Core.METACARD_TAGS)
              .getValues()
              .stream()
              .filter(String.class::isInstance)
              .map(String.class::cast)
              .forEach(metadata::addLineage);

          return metadata;
        }
      };
    }
  }
}
