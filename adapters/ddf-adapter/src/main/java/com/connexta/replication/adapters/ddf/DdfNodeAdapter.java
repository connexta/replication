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
import com.connexta.replication.adapters.ddf.csw.MetacardMarshaller;
import com.connexta.replication.adapters.ddf.rest.DdfRestClient;
import com.connexta.replication.adapters.ddf.rest.DdfRestClientFactory;
import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.AdapterInterruptedException;
import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.Replication;
import com.connexta.replication.api.data.CreateRequest;
import com.connexta.replication.api.data.CreateStorageRequest;
import com.connexta.replication.api.data.DeleteRequest;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.QueryRequest;
import com.connexta.replication.api.data.QueryResponse;
import com.connexta.replication.api.data.Resource;
import com.connexta.replication.api.data.ResourceRequest;
import com.connexta.replication.api.data.ResourceResponse;
import com.connexta.replication.api.data.UpdateRequest;
import com.connexta.replication.api.data.UpdateStorageRequest;
import com.connexta.replication.data.QueryRequestImpl;
import com.connexta.replication.data.QueryResponseImpl;
import com.connexta.replication.data.ResourceImpl;
import com.connexta.replication.data.ResourceResponseImpl;
import com.google.common.annotations.VisibleForTesting;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
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
import net.opengis.cat.csw.v_2_0_2.CapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.ElementSetNameType;
import net.opengis.cat.csw.v_2_0_2.ElementSetType;
import net.opengis.cat.csw.v_2_0_2.GetCapabilitiesType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An adapter used for communicating with a DDF node. */
public class DdfNodeAdapter implements NodeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdfNodeAdapter.class);

  private static final String CONTACT_ERROR = "Error contacting CSW Server at {}";

  private final DdfRestClientFactory ddfRestClientFactory;

  private final URL hostUrl;

  private volatile String cachedSystemName;

  private final SecureCxfClientFactory<Csw> factory;

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
    try {
      final Csw csw = factory.getClient();
      final GetCapabilitiesType getCapabilitiesType = new GetCapabilitiesType();

      getCapabilitiesType.setService("CSW");
      final CapabilitiesType response = csw.getCapabilities(getCapabilitiesType);

      LOGGER.info(
          "Successfully contacted CSW server version {} at {}", response.getVersion(), hostUrl);
    } catch (InterruptedException | InterruptedIOException e) {
      Thread.currentThread().interrupt(); // propagate interruption
      return false;
    } catch (UndeclaredThrowableException e) {
      final Throwable t = e.getUndeclaredThrowable();

      if ((t instanceof InterruptedException) || (t instanceof InterruptedIOException)) {
        Thread.currentThread().interrupt(); // propagate interruption
      } else {
        LOGGER.debug(DdfNodeAdapter.CONTACT_ERROR, hostUrl, t);
        LOGGER.warn(DdfNodeAdapter.CONTACT_ERROR, hostUrl);
      }
      return false;
    } catch (Exception e) {
      LOGGER.debug(DdfNodeAdapter.CONTACT_ERROR, hostUrl, e);
      LOGGER.warn(DdfNodeAdapter.CONTACT_ERROR, hostUrl);
      return false;
    }
    return true;
  }

  private List<Metadata> getRecords(QueryRequest request) {
    try {
      LOGGER.trace(
          "============================ START CSW GetRecords ============================");
      LOGGER.trace("CSW cql query: {}", request.getCql());
      Csw csw = factory.getClient();

      GetRecordsType getRecordsType = new GetRecordsType();
      getRecordsType.setVersion(Constants.VERSION_2_0_2);
      getRecordsType.setService("CSW");
      getRecordsType.setResultType(ResultType.RESULTS);
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

      CswRecordCollection response = csw.getRecords(getRecordsType);
      LOGGER.debug("Csw query returned {} results", response.getCswRecords().size());

      return response.getCswRecords();
    } catch (RuntimeException e) { // let these bubble out and be handled by the caller
      throw e;
    } catch (Exception e) {
      throw wrapException("Error executing csw getRecords", e);
    } finally {
      LOGGER.trace("============================ END CSW GetRecords ============================");
    }
  }

  @Override
  public String getSystemName() {
    if (this.cachedSystemName != null) {
      return this.cachedSystemName;
    }

    String tagFilter = CqlBuilder.equalTo(Constants.METACARD_TAGS, Constants.REGISTRY_TAG);
    String identityFilter = CqlBuilder.negate(CqlBuilder.isNull(Constants.REGISTRY_IDENTITY_NODE));
    String filter = CqlBuilder.allOf(tagFilter, identityFilter);
    List<Metadata> results;

    try {
      results = getRecords(new QueryRequestImpl(filter, 1, 1));
    } catch (Exception e) {
      throw wrapException("Failed to retrieve remote system name", e);
    }

    String systemName;
    if (!results.isEmpty()) {
      systemName = ((DdfMetadata) results.get(0)).getAttributes().get("title").getValue();
    } else {
      throw new AdapterException(
          String.format(
              "No registry metadata available on remote node %s. Could not retrieve remote system name",
              hostUrl));
    }
    this.cachedSystemName = systemName;
    return systemName;
  }

  @Override
  public QueryResponse query(QueryRequest queryRequest) {
    // Failed items and new items are queried separately due to a bug in DDF that was only fixed
    // recently
    try {
      Iterable<Metadata> results =
          ResultIterable.resultIterable(
              this::getRecords,
              createDdfFailedItemQueryRequest(queryRequest),
              createDdfQueryRequest(queryRequest));

      return new QueryResponseImpl(results, e -> wrapException("Failed to query remote system", e));
    } catch (Exception e) {
      throw wrapException("Failed to query remote system", e);
    }
  }

  @Override
  public boolean exists(Metadata metadata) {
    try {
      return !getRecords(
              new QueryRequestImpl(
                  CqlBuilder.equalTo(Constants.METACARD_ID, metadata.getId()), 1, 1))
          .isEmpty();
    } catch (Exception e) {
      throw wrapException(
          String.format(
              "Error checking for the existence of metacard %s on %s",
              metadata.getId(), getSystemName()),
          e);
    }
  }

  @Override
  public boolean createRequest(CreateRequest createRequest) {
    try {
      List<Metadata> metadata = createRequest.getMetadata();
      DdfRestClient client = ddfRestClientFactory.create(hostUrl);
      metadata = metadata.stream().map(this::prepareMetadata).collect(Collectors.toList());
      return performRequestForEach(client::post, metadata);
    } catch (Exception e) {
      throw wrapException("Failed to create on remote system", e);
    }
  }

  @Override
  public boolean updateRequest(UpdateRequest updateRequest) {
    try {
      List<Metadata> metadata = updateRequest.getMetadata();
      DdfRestClient client = ddfRestClientFactory.create(hostUrl);
      metadata = metadata.stream().map(this::prepareMetadata).collect(Collectors.toList());
      return performRequestForEach(client::put, metadata);
    } catch (Exception e) {
      throw wrapException("Failed to update remote system", e);
    }
  }

  @Override
  public boolean deleteRequest(DeleteRequest deleteRequest) {
    try {
      List<String> ids =
          deleteRequest.getMetadata().stream().map(Metadata::getId).collect(Collectors.toList());
      DdfRestClient client = ddfRestClientFactory.create(hostUrl);
      return performRequestForEach(client::delete, ids);
    } catch (Exception e) {
      throw wrapException("Failed to delete on remote system", e);
    }
  }

  @Override
  public ResourceResponse readResource(ResourceRequest resourceRequest) {
    try {
      Metadata metadata = resourceRequest.getMetadata();
      DdfRestClient client = ddfRestClientFactory.create(hostUrl);
      Resource resource = client.get(metadata);
      return new ResourceResponseImpl(resource);
    } catch (Exception e) {
      throw wrapException("Failed to read resource from remote system", e);
    }
  }

  @Override
  public boolean createResource(CreateStorageRequest createStorageRequest) {
    try {
      Resource resource = createStorageRequest.getResource();
      DdfRestClient client = ddfRestClientFactory.createWithSubject(hostUrl);
      resource = cloneResourceWithPreparedMetadata(resource);
      return performRequestForEach(client::post, List.of(resource));
    } catch (Exception e) {
      throw wrapException("Failed to create resource on remote system", e);
    }
  }

  @Override
  public boolean updateResource(UpdateStorageRequest updateStorageRequest) {
    try {
      Resource resource = updateStorageRequest.getResource();
      DdfRestClient client = ddfRestClientFactory.createWithSubject(hostUrl);
      resource = cloneResourceWithPreparedMetadata(resource);
      return performRequestForEach(client::put, List.of(resource));
    } catch (Exception e) {
      throw wrapException("Failed to update resource on remote system", e);
    }
  }

  private Resource cloneResourceWithPreparedMetadata(Resource resource) {
    return new ResourceImpl(
        resource.getId(),
        resource.getName(),
        resource.getResourceUri(),
        resource.getQualifier(),
        resource.getInputStream(),
        resource.getMimeType(),
        resource.getSize(),
        prepareMetadata(resource.getMetadata()));
  }

  /**
   * Unmarshals the metadata containing metacard xml into a {@link DdfMetadata} which contains a map
   * which we can add tags and the origins attribute to.
   *
   * @param metadata The metadata to modify
   * @return A DdfMetadata with the metacard tags and origins
   */
  private DdfMetadata prepareMetadata(Metadata metadata) {
    if (metadata.getType() != String.class) {
      throw new AdapterException(
          "DDF adapter can't process raw metadata of type " + metadata.getType());
    }

    DdfMetadata ddfMetadata =
        MetacardMarshaller.unmarshal(
            (String) metadata.getRawMetadata(), MetacardMarshaller.metacardNamespaceMap());
    Map metadataMap = ddfMetadata.getAttributes();
    metadataMap.put(
        Constants.METACARD_TAGS,
        new MetacardAttribute(
            Constants.METACARD_TAGS,
            "string",
            new ArrayList(metadata.getTags()),
            Collections.emptyList()));
    metadataMap.put(
        Replication.ORIGINS,
        new MetacardAttribute(
            Replication.ORIGINS, "string", metadata.getLineage(), Collections.emptyList()));
    return ddfMetadata;
  }

  @Override
  public void close() {
    // nothing to close
  }

  private <T> boolean performRequestForEach(Function<T, Response> request, List<T> requestBodies) {
    for (T body : requestBodies) {
      Response response = request.apply(body);
      LOGGER.debug("Response received for request {}. Response: {}", body, response);
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

    LOGGER.debug("Final cql query filter: {}", finalFilter);
    return new QueryRequestImpl(
        finalFilter, queryRequest.getStartIndex(), queryRequest.getPageSize());
  }

  private AdapterException wrapException(String msg, Exception e) {
    Throwable t = e;

    if (t instanceof UndeclaredThrowableException) {
      t = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
    }
    if (t instanceof InterruptedIOException) {
      return new AdapterInterruptedException((InterruptedIOException) t);
    } else if (t instanceof InterruptedException) {
      return new AdapterInterruptedException((InterruptedException) t);
    } else if (t instanceof AdapterInterruptedException) {
      return (AdapterInterruptedException) t;
    } else if (t instanceof Error) {
      throw (Error) t;
    } else if (t instanceof AdapterException) {
      // re-write the exception with our message while preserving its stack trace
      final AdapterException ae = new AdapterException(msg, t.getCause());

      ae.setStackTrace(t.getStackTrace());
      throw ae;
    }
    return new AdapterException(msg, (Exception) t);
  }
}
