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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.adapters.ddf.csw.Constants;
import com.connexta.replication.adapters.ddf.csw.Csw;
import com.connexta.replication.adapters.ddf.csw.CswRecordCollection;
import com.connexta.replication.adapters.ddf.rest.DdfRestClient;
import com.connexta.replication.adapters.ddf.rest.DdfRestClientFactory;
import com.connexta.replication.data.MetadataAttribute;
import com.connexta.replication.data.MetadataImpl;
import com.connexta.replication.data.QueryRequestImpl;
import com.connexta.replication.data.ResourceImpl;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import net.opengis.cat.csw.v_2_0_2.CapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.GetCapabilitiesType;
import net.opengis.cat.csw.v_2_0_2.GetRecordsType;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.QueryResponse;
import org.codice.ditto.replication.api.data.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DdfNodeAdapterTest {
  @Mock DdfRestClientFactory ddfRestClientFactory;
  @Mock ClientFactoryFactory clientFactory;

  @Mock DdfRestClient restClient;

  @Mock SecureCxfClientFactory secureFactory;

  @Mock Csw csw;

  private DdfNodeAdapter adapter;

  @Before
  public void setUp() throws Exception {
    when(ddfRestClientFactory.create(any(URL.class))).thenReturn(restClient);
    when(ddfRestClientFactory.createWithSubject(any(URL.class))).thenReturn(restClient);
    when(secureFactory.getClient()).thenReturn(csw);
    adapter =
        new DdfNodeAdapter(
            ddfRestClientFactory, secureFactory, new URL("https://localhost:8994/searvice"));
  }

  @Test
  public void isAvailable() throws Exception {
    CapabilitiesType response = mock(CapabilitiesType.class);
    when(csw.getCapabilities(any(GetCapabilitiesType.class))).thenReturn(response);
    when(response.getVersion()).thenReturn("2.0.2");
    assertThat(adapter.isAvailable(), is(true));
  }

  @Test
  public void isAvailableError() throws Exception {
    when(csw.getCapabilities(any(GetCapabilitiesType.class))).thenThrow(new Exception("error"));
    assertThat(adapter.isAvailable(), is(false));
  }

  @Test
  public void getSystemName() throws Exception {
    CswRecordCollection collection = new CswRecordCollection();
    Metadata metadata = getMetadata();
    collection.setCswRecords(Collections.singletonList(metadata));
    when(csw.getRecords(any(GetRecordsType.class))).thenReturn(collection);
    assertThat(adapter.getSystemName(), is("mytitle"));
  }

  @Test
  public void getSystemNameCached() throws Exception {
    CswRecordCollection collection = new CswRecordCollection();
    Metadata metadata = getMetadata();
    collection.setCswRecords(Collections.singletonList(metadata));
    when(csw.getRecords(any(GetRecordsType.class))).thenReturn(collection);
    assertThat(adapter.getSystemName(), is("mytitle"));
    assertThat(adapter.getSystemName(), is("mytitle"));
    verify(csw, times(1)).getRecords(any(GetRecordsType.class));
  }

  @Test
  public void getSystemNameNoRegistryResult() throws Exception {
    CswRecordCollection collection = new CswRecordCollection();
    when(csw.getRecords(any(GetRecordsType.class))).thenReturn(collection);
    try {
      adapter.getSystemName();
    } catch (AdapterException e) {
      // a little hacky but way to distinguish between exception causes
      assertThat(e.getMessage(), containsString("Failed to retrieve remote system name"));
    }
  }

  @Test
  public void getSystemNameRequestError() throws Exception {
    when(csw.getRecords(any(GetRecordsType.class))).thenThrow(new RuntimeException("error"));
    try {
      adapter.getSystemName();
    } catch (AdapterException e) {
      // a little hacky but way to distinguish between exception causes
      assertThat(e.getMessage(), containsString("Failed to retrieve remote system name"));
    }
  }

  @Test
  public void query() throws Exception {
    QueryRequest request = new QueryRequestImpl("title like '*'");
    CswRecordCollection collection = new CswRecordCollection();
    Metadata metadata = getMetadata();
    collection.setCswRecords(Collections.singletonList(metadata));
    when(csw.getRecords(any(GetRecordsType.class))).thenReturn(collection);
    QueryResponse response = adapter.query(request);
    assertThat(response.getMetadata().iterator().hasNext(), is(true));
  }

  @Test
  public void exists() throws Exception {
    CswRecordCollection collection = new CswRecordCollection();
    Metadata metadata = getMetadata();
    collection.setCswRecords(Collections.singletonList(metadata));
    when(csw.getRecords(any(GetRecordsType.class))).thenReturn(collection);
    assertThat(adapter.exists(metadata), is(true));
  }

  @Test
  public void doesNotExist() throws Exception {
    CswRecordCollection collection = new CswRecordCollection();
    when(csw.getRecords(any(GetRecordsType.class))).thenReturn(collection);
    assertThat(adapter.exists(getMetadata()), is(false));
  }

  @Test
  public void createRequest() {
    Metadata metadata = setupRestCall(restClient::post, this::getMetadata, false);
    assertThat(adapter.createRequest(() -> Collections.singletonList(metadata)), is(true));
  }

  @Test
  public void createRequestFailure() {
    Metadata metadata = setupRestCall(restClient::post, this::getMetadata, true);
    assertThat(adapter.createRequest(() -> Collections.singletonList(metadata)), is(false));
  }

  @Test
  public void updateRequest() {
    Metadata metadata = setupRestCall(restClient::put, this::getMetadata, false);
    assertThat(adapter.updateRequest(() -> Collections.singletonList(metadata)), is(true));
  }

  @Test
  public void updateRequestFailure() {
    Metadata metadata = setupRestCall(restClient::put, this::getMetadata, true);
    assertThat(adapter.updateRequest(() -> Collections.singletonList(metadata)), is(false));
  }

  @Test
  public void deleteRequest() {
    Metadata metadata = getMetadata();
    StatusType status = mock(StatusType.class);
    Response response = mock(Response.class);
    when(status.getFamily()).thenReturn(Family.SUCCESSFUL);
    when(response.getStatusInfo()).thenReturn(status);
    when(restClient.delete(metadata.getId())).thenReturn(response);
    assertThat(adapter.deleteRequest(() -> Collections.singletonList(metadata)), is(true));
  }

  @Test
  public void readResource() {
    Resource resource = getResource();
    when(restClient.get(resource.getMetadata())).thenReturn(resource);
    Resource response = adapter.readResource(() -> resource.getMetadata()).getResource();
    assertThat(response, is(resource));
  }

  @Test
  public void createResource() {
    Resource resource = setupRestCall(restClient::post, this::getResource, false);
    assertThat(adapter.createResource(() -> Collections.singletonList(resource)), is(true));
  }

  @Test
  public void updateResource() {
    Resource resource = setupRestCall(restClient::put, this::getResource, false);
    assertThat(adapter.updateResource(() -> Collections.singletonList(resource)), is(true));
  }

  @Test
  public void createFailedItemsQueryRequest() {
    Date modified = new Date();
    QueryRequest request =
        new QueryRequestImpl(
            "title like '*'",
            Collections.singletonList("node1"),
            Collections.singletonList("123456789"),
            modified);
    assertThat(
        adapter.createDdfFailedItemQueryRequest(request).getCql(),
        is("[ [ \"id\" = '123456789' ] ]"));
  }

  @Test
  public void createFailedItemsQueryRequestNoFailedIds() {
    Date modified = new Date();
    QueryRequest request =
        new QueryRequestImpl(
            "title like '*'",
            Collections.singletonList("node1"),
            Collections.emptyList(),
            modified);
    assertThat(adapter.createDdfFailedItemQueryRequest(request), is(nullValue()));
  }

  @Test
  public void createQueryFilter() {
    Date modified = new Date();
    String modifiedString = modified.toInstant().toString();
    QueryRequest request =
        new QueryRequestImpl(
            "title like '*'",
            Collections.singletonList("node1"),
            Collections.singletonList("123456789"),
            modified);
    assertThat(
        adapter.createDdfQueryRequest(request).getCql(),
        is(
            String.format(
                "[ [ [ title like '*' ] AND [ [ [ NOT [ \"replication.origins\" = 'node1' ] ] AND [ \"metacard-tags\" = 'resource' ] AND [ \"metacard.modified\" after %s ] ] OR [ [ \"metacard.version.versioned-on\" after %s ] AND [ \"metacard-tags\" = 'revision' ] AND [ \"metacard.version.action\" like 'Deleted*' ] ] ] ] OR [ [ [ \"metacard.version.id\" = '123456789' ] AND [ \"metacard.version.action\" like 'Deleted*' ] ] ] ]",
                modifiedString, modifiedString)));
  }

  @Test
  public void createQueryFilterNoDateNoFailedItems() {
    QueryRequest request =
        new QueryRequestImpl("title like '*'", Collections.singletonList("node1"));
    assertThat(
        adapter.createDdfQueryRequest(request).getCql(),
        is(
            "[ [ NOT [ \"replication.origins\" = 'node1' ] ] AND [ \"metacard-tags\" = 'resource' ] AND [ title like '*' ] ]"));
  }

  @Test
  public void createFilterAlreadyHasBrackets() {
    QueryRequest request =
        new QueryRequestImpl("[ title like '*' ]", Collections.singletonList("node1"));
    assertThat(
        adapter.createDdfQueryRequest(request).getCql(),
        is(
            "[ [ NOT [ \"replication.origins\" = 'node1' ] ] AND [ \"metacard-tags\" = 'resource' ] AND [ title like '*' ] ]"));
  }

  private Metadata getMetadata() {
    Map<String, MetadataAttribute> map = new HashMap<>();

    map.put(Constants.METACARD_ID, new MetadataAttribute(Constants.METACARD_ID, null, "123456789"));
    map.put("type", new MetadataAttribute("type", null, "ddf.metacard"));
    map.put(
        Constants.METACARD_TAGS, new MetadataAttribute(Constants.METACARD_TAGS, "string", "tag"));
    map.put("title", new MetadataAttribute("title", null, "mytitle"));
    map.put("source", new MetadataAttribute("source", null, "mytitle"));
    MetadataImpl metadata =
        new MetadataImpl(map, Map.class, UUID.randomUUID().toString(), new Date());
    metadata.setSource("mytitle");
    return metadata;
  }

  private Resource getResource() {
    return new ResourceImpl(
        "123456789",
        "mytitle",
        URI.create("my:uri"),
        null,
        new ByteArrayInputStream("my-data".getBytes()),
        MediaType.TEXT_PLAIN,
        10,
        getMetadata());
  }

  private <T> T setupRestCall(
      Function<T, Response> function, Supplier<T> dataSupplier, boolean failure) {
    T resource = dataSupplier.get();
    StatusType status = mock(StatusType.class);
    Response response = mock(Response.class);
    when(status.getFamily()).thenReturn(failure ? Family.SERVER_ERROR : Family.SUCCESSFUL);
    when(response.getStatusInfo()).thenReturn(status);
    when(function.apply(resource)).thenReturn(response);
    return resource;
  }
}
