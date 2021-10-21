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

import com.connexta.replication.adapters.ddf.csw.Constants;
import com.connexta.replication.adapters.ddf.csw.Csw;
import com.connexta.replication.adapters.ddf.csw.CswRecordCollection;
import com.connexta.replication.adapters.ddf.rest.DdfRestClient;
import com.connexta.replication.adapters.ddf.rest.DdfRestClientFactory;
import com.connexta.replication.data.MetadataAttribute;
import com.connexta.replication.data.QueryRequestImpl;
import com.connexta.replication.data.QueryResponseImpl;
import com.connexta.replication.data.ResourceResponseImpl;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.xml.namespace.QName;
import net.opengis.cat.csw.v_2_0_2.ElementSetNameType;
import net.opengis.cat.csw.v_2_0_2.ElementSetType;
import net.opengis.cat.csw.v_2_0_2.GetRecordsType;
import net.opengis.cat.csw.v_2_0_2.ObjectFactory;
import net.opengis.cat.csw.v_2_0_2.QueryConstraintType;
import net.opengis.cat.csw.v_2_0_2.QueryType;
import net.opengis.cat.csw.v_2_0_2.ResultType;
import net.opengis.filter.v_1_1_0.PropertyNameType;
import net.opengis.filter.v_1_1_0.SortByType;
import net.opengis.filter.v_1_1_0.SortOrderType;
import net.opengis.filter.v_1_1_0.SortPropertyType;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.Replication;
import org.codice.ditto.replication.api.data.CreateRequest;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.DeleteRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.QueryResponse;
import org.codice.ditto.replication.api.data.Resource;
import org.codice.ditto.replication.api.data.ResourceRequest;
import org.codice.ditto.replication.api.data.ResourceResponse;
import org.codice.ditto.replication.api.data.UpdateRequest;
import org.codice.ditto.replication.api.data.UpdateStorageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An adapter used for communicating with a DDF node. */
public class DdfNodeAdapter implements NodeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdfNodeAdapter.class);

  private final DdfRestClientFactory ddfRestClientFactory;

  private final URL hostUrl;

  private String cachedSystemName;

  private SecureCxfClientFactory<Csw> factory;

  /**
   * Creates a DdfNodeAdapter.
   *
   * @param ddfRestClientFactory a factory for creating rest clients
   * @param ddfCswClientFactory a factory for creating csw clients
   * @param hostUrl the url of the DDF to create clients for
   */
  public DdfNodeAdapter(
      DdfRestClientFactory ddfRestClientFactory,
      SecureCxfClientFactory<Csw> ddfCswClientFactory,
      URL hostUrl) {
    this.ddfRestClientFactory = ddfRestClientFactory;
    this.hostUrl = hostUrl;
    this.factory = ddfCswClientFactory;
  }

  @Override
  public boolean isAvailable() {
    DdfRestClient client = ddfRestClientFactory.create(hostUrl);

    boolean available = false;
    try {
      available = client.ping();
      LOGGER.debug("Server at {} available? {}", hostUrl, available);
    } catch (Exception e) {
      LOGGER.warn("Error contacting server at {}", hostUrl, e);
      available = false;
    }
    return available;
  }

  private List<Metadata> getRecords(QueryRequest request) {
    try {
      LOGGER.trace(
          "============================ START CSW GetRecords ============================");
      LOGGER.trace("CSW cql query: {}", request.getCql());
      Csw csw = factory.getClient();
      GetRecordsType getRecordsType = getCswQuery(request, ResultType.RESULTS);

      CswRecordCollection response = csw.getRecords(getRecordsType);
      LOGGER.debug("Csw query returned {} results", response.getCswRecords().size());
      LOGGER.trace("============================ END CSW GetRecords ============================");

      return response.getCswRecords();
    } catch (Exception e) {
      throw new AdapterException("Error executing csw getRecords", e);
    }
  }

  public long checkForDatasets(QueryRequest request) {
    Csw csw = factory.getClient();

    GetRecordsType getRecordsType = getCswQuery(request, ResultType.HITS);
    try {
      return csw.getRecords(getRecordsType).getNumberOfRecordsMatched();
    } catch (Exception e) {
      LOGGER.debug("Error getting hit count.", e);
      return -1;
    }
  }

  private GetRecordsType getCswQuery(QueryRequest request, ResultType type) {
    GetRecordsType getRecordsType = new GetRecordsType();
    getRecordsType.setVersion(Constants.VERSION_2_0_2);
    getRecordsType.setService("CSW");
    getRecordsType.setResultType(type);
    getRecordsType.setStartPosition(BigInteger.valueOf(request.getStartIndex()));
    getRecordsType.setMaxRecords(BigInteger.valueOf(request.getPageSize()));
    getRecordsType.setOutputFormat(MediaType.APPLICATION_XML);
    getRecordsType.setOutputSchema(Constants.METACARD_SCHEMA);
    QueryType queryType = new QueryType();
    queryType.setTypeNames(
        Arrays.asList(
            new QName(Constants.CSW_OUTPUT_SCHEMA, "Record", Constants.CSW_NAMESPACE_PREFIX)));

    ElementSetNameType elementSetNameType = new ElementSetNameType();
    elementSetNameType.setValue(ElementSetType.FULL);
    queryType.setElementSetName(elementSetNameType);
    SortByType cswSortBy = new SortByType();
    SortPropertyType sortProperty = new SortPropertyType();
    PropertyNameType propertyName = new PropertyNameType();
    propertyName.setContent(Arrays.asList(Constants.METACARD_MODIFIED));
    sortProperty.setPropertyName(propertyName);
    sortProperty.setSortOrder(SortOrderType.ASC);
    cswSortBy.getSortProperty().add(sortProperty);
    queryType.setSortBy(cswSortBy);
    QueryConstraintType queryConstraintType = new QueryConstraintType();
    queryConstraintType.setVersion(Constants.CONSTRAINT_VERSION);
    queryConstraintType.setCqlText(request.getCql());
    queryType.setConstraint(queryConstraintType);
    ObjectFactory objectFactory = new ObjectFactory();
    getRecordsType.setAbstractQuery(objectFactory.createQuery(queryType));
    return getRecordsType;
  }

  @Override
  public String getSystemName() {
    if (this.cachedSystemName != null) {
      return this.cachedSystemName;
    }

    // get any one metacard from the system
    String modifiedFilter = CqlBuilder.negate(CqlBuilder.isNull(Constants.METACARD_MODIFIED));
    String tagFilter = CqlBuilder.like(Constants.METACARD_TAGS, "*");
    String noTagFilter = CqlBuilder.negate(CqlBuilder.isNull(Constants.METACARD_TAGS));
    String filter = CqlBuilder.anyOf(modifiedFilter, tagFilter, noTagFilter);
    List<Metadata> results;

    try {
      results = getRecords(new QueryRequestImpl(filter, 1, 1));
    } catch (Exception e) {
      throw new AdapterException("Failed to retrieve remote system name", e);
    }

    if (results.isEmpty() || results.get(0).getSource() == null) {
      throw new AdapterException(
          String.format("Failed to retrieve remote system name from %s.", hostUrl));
    }
    this.cachedSystemName = results.get(0).getSource();
    return this.cachedSystemName;
  }

  @Override
  public QueryResponse query(QueryRequest queryRequest) {
    // Failed items and new items are queried separately due to a bug in DDF that was only fixed
    // recently
    Iterable<Metadata> results =
        ResultIterable.resultIterable(
            this::getRecords,
            createDdfFailedItemQueryRequest(queryRequest),
            createDdfQueryRequest(queryRequest));

    return new QueryResponseImpl(results);
  }

  @Override
  public boolean exists(Metadata metadata) {
    try {
      return checkForDatasets(
              new QueryRequestImpl(
                  CqlBuilder.equalTo(Constants.METACARD_ID, metadata.getId()), 1, 1))
          > 0;
    } catch (Exception e) {
      throw new AdapterException(
          String.format(
              "Error checking for the existence of metacard %s on %s",
              metadata.getId(), getSystemName()),
          e);
    }
  }

  @Override
  public boolean createRequest(CreateRequest createRequest) {
    List<Metadata> metadata = createRequest.getMetadata();
    DdfRestClient client = ddfRestClientFactory.create(hostUrl);
    metadata.forEach(this::prepareMetadata);
    return performRequestForEach(client::post, metadata);
  }

  @Override
  public boolean updateRequest(UpdateRequest updateRequest) {
    List<Metadata> metadata = updateRequest.getMetadata();
    DdfRestClient client = ddfRestClientFactory.create(hostUrl);
    metadata.forEach(this::prepareMetadata);
    return performRequestForEach(client::put, metadata);
  }

  @Override
  public boolean deleteRequest(DeleteRequest deleteRequest) {
    List<String> ids =
        deleteRequest.getMetadata().stream().map(Metadata::getId).collect(Collectors.toList());
    DdfRestClient client = ddfRestClientFactory.create(hostUrl);
    return performRequestForEach(client::delete, ids);
  }

  @Override
  public ResourceResponse readResource(ResourceRequest resourceRequest) {
    Metadata metadata = resourceRequest.getMetadata();
    DdfRestClient client = ddfRestClientFactory.create(hostUrl);
    Resource resource = client.get(metadata);
    return new ResourceResponseImpl(resource);
  }

  @Override
  public boolean createResource(CreateStorageRequest createStorageRequest) {
    List<Resource> resources = createStorageRequest.getResources();
    DdfRestClient client = ddfRestClientFactory.createWithSubject(hostUrl);
    resources.forEach(e -> prepareMetadata(e.getMetadata()));
    return performRequestForEach(client::post, resources);
  }

  @Override
  public boolean updateResource(UpdateStorageRequest updateStorageRequest) {
    List<Resource> resources = updateStorageRequest.getResources();
    DdfRestClient client = ddfRestClientFactory.createWithSubject(hostUrl);
    resources.forEach(e -> prepareMetadata(e.getMetadata()));
    return performRequestForEach(client::put, resources);
  }

  private void prepareMetadata(Metadata metadata) {

    if (!(metadata.getRawMetadata() instanceof Map)) {
      throw new AdapterException(
          "DDF adapter can't process raw metadata of type " + metadata.getRawMetadata().getClass());
    }
    Map metadataMap = (Map) metadata.getRawMetadata();
    metadataMap.put(
        Constants.METACARD_TAGS,
        new MetadataAttribute(
            Constants.METACARD_TAGS,
            "string",
            new ArrayList(metadata.getTags()),
            Collections.emptyList()));
    metadataMap.put(
        Replication.ORIGINS,
        new MetadataAttribute(
            Replication.ORIGINS, "string", metadata.getLineage(), Collections.emptyList()));
  }

  @Override
  public void close() throws IOException {
    // nothing to close
  }

  private <T> boolean performRequestForEach(Function<T, Response> request, List<T> requestBodies) {
    for (T body : requestBodies) {
      Response response = request.apply(body);
      if (response == null || !response.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
        return false;
      }
    }

    return true;
  }

  @VisibleForTesting
  @Nullable
  QueryRequest createDdfFailedItemQueryRequest(QueryRequest queryRequest) {
    if (queryRequest.getFailedItemIds().isEmpty()) {
      return null;
    }

    final List<String> failedItemFilters = new ArrayList<>();
    for (String itemId : queryRequest.getFailedItemIds()) {
      failedItemFilters.add(CqlBuilder.equalTo(Constants.METACARD_ID, itemId));
    }

    String filter = CqlBuilder.anyOf(failedItemFilters);
    LOGGER.debug("Failed items filter: {}", filter);
    return new QueryRequestImpl(filter, queryRequest.getStartIndex(), queryRequest.getPageSize());
  }

  @VisibleForTesting
  QueryRequest createDdfQueryRequest(QueryRequest queryRequest) {
    String cql = queryRequest.getCql();
    if (!cql.trim().startsWith("[")) {
      cql = "[ " + cql + " ]";
    }
    LOGGER.debug("Base query filter: {}", cql);
    final List<String> filters = new ArrayList<>();

    for (String excludedNode : queryRequest.getExcludedNodes()) {
      filters.add(CqlBuilder.negate(CqlBuilder.equalTo(Replication.ORIGINS, excludedNode)));
    }
    filters.add(CqlBuilder.equalTo(Constants.METACARD_TAGS, Constants.DEFAULT_TAG));

    final List<String> deletedFailedItemFilters = new ArrayList<>();
    for (String itemId : queryRequest.getFailedItemIds()) {
      // filter for items that failed to replicate and were deleted since the last attempt
      deletedFailedItemFilters.add(
          CqlBuilder.allOf(
              CqlBuilder.equalTo(Constants.VERSION_OF_ID, itemId),
              CqlBuilder.like(Constants.ACTION, "Deleted*")));
    }

    String finalFilter;

    Date modifiedAfter = queryRequest.getModifiedAfter();
    List<String> deletedFilters = new ArrayList<>();
    if (modifiedAfter != null) {
      filters.add(CqlBuilder.after(Constants.METACARD_MODIFIED, modifiedAfter));

      deletedFilters.add(CqlBuilder.after(Constants.VERSIONED_ON, modifiedAfter));
      deletedFilters.add(CqlBuilder.equalTo(Constants.METACARD_TAGS, Constants.VERSION_TAG));
      deletedFilters.add(CqlBuilder.like(Constants.ACTION, "Deleted*"));

      String timeTypeFilter = CqlBuilder.allOf(filters);
      String deletedItemsFilter = CqlBuilder.allOf(deletedFilters);
      LOGGER.debug("Time and type filter: {}", timeTypeFilter);
      LOGGER.debug("Deleted items filter: {}", deletedItemsFilter);
      finalFilter = CqlBuilder.allOf(cql, CqlBuilder.anyOf(timeTypeFilter, deletedItemsFilter));
    } else {
      filters.add(cql);
      finalFilter = CqlBuilder.allOf(filters);
    }

    if (!deletedFailedItemFilters.isEmpty()) {
      String deletedFailedItemsFilter = CqlBuilder.anyOf(deletedFailedItemFilters);
      LOGGER.debug("Deleted failed items filter: {}", CqlBuilder.anyOf(deletedFailedItemFilters));
      finalFilter = CqlBuilder.anyOf(finalFilter, deletedFailedItemsFilter);
    }
    LOGGER.debug("Final cql query filter: {}", finalFilter);
    return new QueryRequestImpl(
        finalFilter, queryRequest.getStartIndex(), queryRequest.getPageSize());
  }
}
