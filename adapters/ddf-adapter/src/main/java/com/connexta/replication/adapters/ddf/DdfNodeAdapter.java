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

import static com.connexta.replication.adapters.ddf.CqlBuilder.after;
import static com.connexta.replication.adapters.ddf.CqlBuilder.allOf;
import static com.connexta.replication.adapters.ddf.CqlBuilder.anyOf;
import static com.connexta.replication.adapters.ddf.CqlBuilder.equalTo;
import static com.connexta.replication.adapters.ddf.CqlBuilder.like;
import static com.connexta.replication.adapters.ddf.CqlBuilder.negate;
import static com.connexta.replication.adapters.ddf.csw.Constants.ACTION;
import static com.connexta.replication.adapters.ddf.csw.Constants.DEFAULT_TAG;
import static com.connexta.replication.adapters.ddf.csw.Constants.METACARD_ID;
import static com.connexta.replication.adapters.ddf.csw.Constants.METACARD_MODIFIED;
import static com.connexta.replication.adapters.ddf.csw.Constants.METACARD_TAGS;
import static com.connexta.replication.adapters.ddf.csw.Constants.REGISTRY_IDENTITY_NODE;
import static com.connexta.replication.adapters.ddf.csw.Constants.REGISTRY_TAG;
import static com.connexta.replication.adapters.ddf.csw.Constants.VERSIONED_ON;
import static com.connexta.replication.adapters.ddf.csw.Constants.VERSION_TAG;

import com.connexta.replication.adapters.ddf.csw.Constants;
import com.connexta.replication.adapters.ddf.csw.Csw;
import com.connexta.replication.adapters.ddf.csw.CswJAXBElementProvider;
import com.connexta.replication.adapters.ddf.csw.CswRecordCollection;
import com.connexta.replication.adapters.ddf.csw.CswResponseExceptionMapper;
import com.connexta.replication.adapters.ddf.csw.GetRecordsMessageBodyReader;
import com.connexta.replication.adapters.ddf.rest.DdfRestClient;
import com.connexta.replication.adapters.ddf.rest.DdfRestClientFactory;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.xml.namespace.QName;
import net.opengis.cat.csw.v_2_0_2.AcknowledgementType;
import net.opengis.cat.csw.v_2_0_2.CapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.ElementSetNameType;
import net.opengis.cat.csw.v_2_0_2.ElementSetType;
import net.opengis.cat.csw.v_2_0_2.GetCapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.GetRecordsResponseType;
import net.opengis.cat.csw.v_2_0_2.GetRecordsType;
import net.opengis.cat.csw.v_2_0_2.ObjectFactory;
import net.opengis.cat.csw.v_2_0_2.QueryConstraintType;
import net.opengis.cat.csw.v_2_0_2.QueryType;
import net.opengis.cat.csw.v_2_0_2.ResultType;
import net.opengis.filter.v_1_1_0.PropertyNameType;
import net.opengis.filter.v_1_1_0.SortByType;
import net.opengis.filter.v_1_1_0.SortOrderType;
import net.opengis.filter.v_1_1_0.SortPropertyType;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
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

public class DdfNodeAdapter implements NodeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdfNodeAdapter.class);

  private final DdfRestClientFactory ddfRestClientFactory;

  private final URL hostUrl;

  private String cachedSystemName;

  private SecureCxfClientFactory<Csw> factory;

  private List<String> jaxbElementClassNames = new ArrayList<>();

  private Map<String, String> jaxbElementClassMap = new HashMap<>();

  public DdfNodeAdapter(
      DdfRestClientFactory ddfRestClientFactory, ClientFactoryFactory clientFactory, URL hostUrl) {
    this.ddfRestClientFactory = ddfRestClientFactory;
    this.hostUrl = hostUrl;
    factory =
        clientFactory.getSecureCxfClientFactory(
            hostUrl.toString() + "/csw",
            Csw.class,
            initProviders(),
            null,
            true,
            false,
            30000,
            30000);
  }

  private List<Object> initProviders() {

    CswJAXBElementProvider getRecordsTypeProvider = new CswJAXBElementProvider<>();
    getRecordsTypeProvider.setMarshallAsJaxbElement(true);

    // Adding class names that need to be marshalled/unmarshalled to
    // jaxbElementClassNames list
    jaxbElementClassNames.add(GetRecordsType.class.getName());
    jaxbElementClassNames.add(CapabilitiesType.class.getName());
    jaxbElementClassNames.add(GetCapabilitiesType.class.getName());
    jaxbElementClassNames.add(GetRecordsResponseType.class.getName());
    jaxbElementClassNames.add(AcknowledgementType.class.getName());

    getRecordsTypeProvider.setJaxbElementClassNames(jaxbElementClassNames);

    // Adding map entry of <Class Name>,<Qualified Name> to jaxbElementClassMap
    String expandedName = new QName(Constants.CSW_OUTPUT_SCHEMA, Constants.GET_RECORDS).toString();
    String message = "{} expanded name: {}";
    LOGGER.debug(message, Constants.GET_RECORDS, expandedName);
    jaxbElementClassMap.put(GetRecordsType.class.getName(), expandedName);

    String getCapsExpandedName =
        new QName(Constants.CSW_OUTPUT_SCHEMA, Constants.GET_CAPABILITIES).toString();
    LOGGER.debug(message, Constants.GET_CAPABILITIES, expandedName);
    jaxbElementClassMap.put(GetCapabilitiesType.class.getName(), getCapsExpandedName);

    String capsExpandedName =
        new QName(Constants.CSW_OUTPUT_SCHEMA, Constants.CAPABILITIES).toString();
    LOGGER.debug(message, Constants.CAPABILITIES, capsExpandedName);
    jaxbElementClassMap.put(CapabilitiesType.class.getName(), capsExpandedName);

    String acknowledgmentName =
        new QName(Constants.CSW_OUTPUT_SCHEMA, "Acknowledgement").toString();
    jaxbElementClassMap.put(AcknowledgementType.class.getName(), acknowledgmentName);
    getRecordsTypeProvider.setJaxbElementClassMap(jaxbElementClassMap);

    GetRecordsMessageBodyReader grmbr = new GetRecordsMessageBodyReader();

    return Arrays.asList(getRecordsTypeProvider, new CswResponseExceptionMapper(), grmbr);
  }

  @Override
  public boolean isAvailable() {
    Csw csw = factory.getClient();
    GetCapabilitiesType getCapabilitiesType = new GetCapabilitiesType();
    getCapabilitiesType.setService("CSW");
    try {
      CapabilitiesType response = csw.getCapabilities(getCapabilitiesType);
      LOGGER.info(
          "Successfully contacted CSW server version {} at {}", response.getVersion(), hostUrl);
    } catch (Exception e) {
      LOGGER.warn("Error contacting CSW Server at {}", hostUrl, e);
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
      propertyName.setContent(Arrays.asList(METACARD_MODIFIED));
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
      LOGGER.trace("============================ END CSW GetRecords ============================");

      return response.getCswRecords();
    } catch (Exception e) {
      throw new AdapterException("Error executing csw getRecords", e);
    }
  }

  @Override
  public String getSystemName() {
    if (this.cachedSystemName != null) {
      return this.cachedSystemName;
    }

    String tagFilter = CqlBuilder.equalTo(METACARD_TAGS, REGISTRY_TAG);
    String identityFilter = CqlBuilder.negate(CqlBuilder.isNull(REGISTRY_IDENTITY_NODE));
    String filter = CqlBuilder.allOf(tagFilter, identityFilter);
    List<Metadata> results;

    try {
      results = getRecords(new QueryRequestImpl(filter, 1, 1));
    } catch (Exception e) {
      throw new AdapterException("Failed to retrieve remote system name", e);
    }

    String systemName;
    if (!results.isEmpty()) {
      systemName =
          ((MetacardAttribute) ((Map) results.get(0).getRawMetadata()).get("title")).getValue();
    } else {
      throw new AdapterException(
          String.format(
              "No registry metadata available on remote node %s. Could not retrieve remote system name",
              hostUrl));
    }
    this.cachedSystemName = systemName;
    return this.cachedSystemName;
  }

  @Override
  public QueryResponse query(QueryRequest queryRequest) {
    Iterable<Metadata> results =
        ResultIterable.resultIterable(
            this::getRecords,
            new QueryRequestImpl(
                createQueryFilter(queryRequest),
                queryRequest.getStartIndex(),
                queryRequest.getPageSize()));
    return new QueryResponseImpl(results);
  }

  @Override
  public boolean exists(Metadata metadata) {
    try {
      return !getRecords(new QueryRequestImpl(equalTo(METACARD_ID, metadata.getId()), 1, 1))
          .isEmpty();
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
        METACARD_TAGS,
        new MetacardAttribute(
            METACARD_TAGS, "string", new ArrayList(metadata.getTags()), Collections.emptyList()));
    metadataMap.put(
        Replication.ORIGINS,
        new MetacardAttribute(
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
  String createQueryFilter(QueryRequest queryRequest) {
    String cql = queryRequest.getCql();
    if (!cql.trim().startsWith("[")) {
      cql = "[ " + cql + " ]";
    }
    LOGGER.debug("Base query filter: {}", cql);
    final List<String> filters = new ArrayList<>();

    for (String excludedNode : queryRequest.getExcludedNodes()) {
      filters.add(negate(equalTo(Replication.ORIGINS, excludedNode)));
    }
    filters.add(equalTo(METACARD_TAGS, DEFAULT_TAG));

    final List<String> failedItemFilters = new ArrayList<>();
    for (String itemId : queryRequest.getFailedItemIds()) {
      failedItemFilters.add(
          anyOf(
              equalTo(METACARD_ID, itemId),
              allOf(equalTo(Constants.VERSION_OF_ID, itemId), like(ACTION, "Deleted*"))));
    }

    String finalFilter;

    Date modifiedAfter = queryRequest.getModifiedAfter();
    List<String> deletedFilters = new ArrayList<>();
    if (modifiedAfter != null) {
      filters.add(after(METACARD_MODIFIED, modifiedAfter));

      deletedFilters.add(after(VERSIONED_ON, modifiedAfter));
      deletedFilters.add(equalTo(METACARD_TAGS, VERSION_TAG));
      deletedFilters.add(like(ACTION, "Deleted*"));

      String timeTypeFilter = allOf(filters);
      String deletedItemsFilter = allOf(deletedFilters);
      LOGGER.debug("Time and type filter: {}", timeTypeFilter);
      LOGGER.debug("Deleted items filter: {}", deletedItemsFilter);
      finalFilter = allOf(cql, anyOf(timeTypeFilter, deletedItemsFilter));
    } else {
      filters.add(cql);
      finalFilter = allOf(filters);
    }

    if (!failedItemFilters.isEmpty()) {
      String failedItemsFilter = anyOf(failedItemFilters);
      LOGGER.debug("Failed items filter: {}", failedItemsFilter);
      finalFilter = anyOf(finalFilter, failedItemsFilter);
    }
    LOGGER.debug("Final cql query filter: {}", finalFilter);
    return finalFilter;
  }
}
