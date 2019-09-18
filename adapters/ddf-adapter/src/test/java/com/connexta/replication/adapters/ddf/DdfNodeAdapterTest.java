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
import com.connexta.replication.api.AdapterException;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.QueryRequest;
import com.connexta.replication.api.data.QueryResponse;
import com.connexta.replication.api.data.Resource;
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
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DdfNodeAdapterTest {
  @Mock DdfRestClientFactory ddfRestClientFactory;

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
      assertThat(e.getMessage(), containsString("No registry metadata available"));
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
    assertThat(adapter.createResource(() -> resource), is(true));
  }

  @Test
  public void updateResource() {
    Resource resource = setupRestCall(restClient::put, this::getResource, false);
    assertThat(adapter.updateResource(() -> resource), is(true));
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
                "[ [ title like '*' ] AND [ [ [ NOT [ \"replication.origins\" = 'node1' ] ] AND [ \"metacard-tags\" = 'resource' ] AND [ \"metacard.modified\" after %s ] ] OR [ [ \"metacard.version.versioned-on\" after %s ] AND [ \"metacard-tags\" = 'revision' ] AND [ \"metacard.version.action\" like 'Deleted*' ] ] ] ]",
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
    Map<String, MetacardAttribute> map = new HashMap<>();

    map.put(Constants.METACARD_ID, new MetacardAttribute(Constants.METACARD_ID, null, "123456789"));
    map.put("type", new MetacardAttribute("type", null, "ddf.metacard"));
    map.put(
        Constants.METACARD_TAGS, new MetacardAttribute(Constants.METACARD_TAGS, "string", "tag"));
    map.put("title", new MetacardAttribute("title", null, "mytitle"));

    return new DdfMetadata(
        RAW_METADATA, String.class, UUID.randomUUID().toString(), new Date(), map);
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
    when(function.apply(any())).thenReturn(response);
    return resource;
  }

  private static final String RAW_METADATA =
      "<metacard xmlns=\"urn:catalog:metacard\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:smil=\"http://www.w3.org/2001/SMIL20/\" xmlns:smillang=\"http://www.w3.org/2001/SMIL20/Language\" gml:id=\"000e1520884b4affb844bd76bc74470e\">\n"
          + "  <type>ddf.metacard</type>\n"
          + "  <source>ddf.distribution</source>\n"
          + "  <string name=\"title\">\n"
          + "    <value>DOLLAR SEEN FALLING UNLESS JAPAN SPURS ECONOMY</value>\n"
          + "  </string>\n"
          + "  <dateTime name=\"metacard.modified\">\n"
          + "    <value>1987-03-17T06:29:52.010+00:00</value>\n"
          + "  </dateTime>"
          + "  <dateTime name=\"created\">\n"
          + "    <value>1987-03-17T06:29:52.010+00:00</value>\n"
          + "  </dateTime><ns3:geometry xmlns:ns1=\"http://www.opengis.net/gml\" xmlns:ns2=\"http://www.w3.org/1999/xlink\" xmlns:ns3=\"urn:catalog:metacard\" xmlns:ns4=\"http://www.w3.org/2001/SMIL20/\" xmlns:ns5=\"http://www.w3.org/2001/SMIL20/Language\" name=\"location\">\n"
          + "  <ns3:value>\n"
          + "    <ns1:Point>\n"
          + "      <ns1:pos>139.69171 35.6895</ns1:pos>\n"
          + "    </ns1:Point>\n"
          + "  </ns3:value>\n"
          + "</ns3:geometry><stringxml name=\"metadata\">\n"
          + "    <value><REUTERS CGISPLIT=\"TRAINING-SET\" LEWISSPLIT=\"TRAIN\" NEWID=\"5778\" OLDID=\"10691\" TOPICS=\"YES\">\n"
          + "<DATE>16-MAR-1987 23:29:52.10</DATE>\n"
          + "<TOPICS><D>money-fx</D><D>dlr</D></TOPICS>\n"
          + "<PLACES><D>usa</D><D>japan</D></PLACES>\n"
          + "<PEOPLE/>\n"
          + "<ORGS/>\n"
          + "<EXCHANGES/>\n"
          + "<COMPANIES/>\n"
          + "<UNKNOWN> \n"
          + "RM\n"
          + "f4012reute\n"
          + "u f BC-DOLLAR-SEEN-FALLING-U   03-16 0109</UNKNOWN>\n"
          + "<TEXT>\n"
          + "<TITLE>DOLLAR SEEN FALLING UNLESS JAPAN SPURS ECONOMY</TITLE>\n"
          + "<AUTHOR>    By Rie Sagawa, Reuters</AUTHOR>\n"
          + "<DATELINE>    TOKYO, March 17 - </DATELINE><BODY>Underlying dollar sentiment is bearish,\n"
          + "and operators may push the currency to a new low unless Japan\n"
          + "takes steps to stimulate its economy as pledged in the Paris\n"
          + "accord, foreign exchange analysts polled by Reuters said here.\n"
          + "    \"The dollar is expected to try its psychological barrier of\n"
          + "150.00 yen and to fall even below that level,\" a senior dealer\n"
          + "at one leading bank said.\n"
          + "    The dollar has eased this week, but remains stable at\n"
          + "around 151.50 yen. Six major industrial countries agreed at a\n"
          + "meeting in Paris in February to foster currency stability.\n"
          + "    Some dealers said the dollar may decline in the long term,\n"
          + "but a drastic fall is unlikely because of U.S. Fears of renewed\n"
          + "inflation and fears of reduced Japanese purchases of U.S.\n"
          + "Treasury securities, needed to finance the U.S. Deficit.\n"
          + "    Dealers generally doubted whether any economic package\n"
          + "Japan could adopt soon would be effective enough to reduce its\n"
          + "trade surplus significantly, and said such measures would\n"
          + "probably invite further U.S. Steps to weaken the dollar.\n"
          + "    Under the Paris accord, Tokyo promised a package of\n"
          + "measures after the fiscal 1987 budget was passed to boost\n"
          + "domestic demand, increase imports and cut its trade surplus.\n"
          + "    But debate on the budget has been delayed by an opposition\n"
          + "boycott of Parliamentary business over the proposed imposition\n"
          + "of a five pct sales tax, and the government has only a slim\n"
          + "chance of producing a meaningful economic package in the near\n"
          + "future, the dealers said.\n"
          + "    If no such steps are taken, protectionist sentiment in the\n"
          + "U.S. Congress will grow, putting greater downward pressure on\n"
          + "the dollar, they said.\n"
          + "    The factors affecting the U.S. Currency have not changed\n"
          + "since before the Paris accord, they added.\n"
          + "    \"Underlying sentiment for the dollar remains bearish due to\n"
          + "a still-sluggish U.S. Economic outlook, the international debt\n"
          + "crisis triggered by Brazil's unilateral suspension of interest\n"
          + "payments on its foreign debts and the reduced clout of the\n"
          + "Reagan administration as a result of the Iran/Contra arms\n"
          + "scandal,\" said a senior dealer at a leading trust bank.\n"
          + "    \"There is a possibility that the dollar may decline to\n"
          + "around 140.00 yen by the end of this year,\" said Chemical Bank\n"
          + "Tokyo branch vice president Yukuo Takahashi.\n"
          + "    But operators find it hard to push the dollar either way\n"
          + "for fear of possible concerted central bank intervention.\n"
          + "    Dealers said there were widespread rumours that the U.S.\n"
          + "Federal Reserve telephoned some banks in New York to ask for\n"
          + "quotes last Wednesday, and even intervened to sell the dollar\n"
          + "when it rose to 1.87 marks.\n"
          + "    The Bank of England also apparently sold sterling in London\n"
          + "when it neared 1.60 dlrs on Wednesday, they said.\n"
          + "    But other dealers said they doubted the efficacy of central\n"
          + "bank intervention, saying it may stimulate the dollar's decline\n"
          + "because many dealers are likely to await such dollar buying\n"
          + "intervention as a chance to sell dollars.\n"
          + "    However, First National Bank of Chicago Tokyo Branch\n"
          + "assistant manager Hiroshi Mochizuki said \"The dollar will not\n"
          + "show drastic movement at least to the end of March.\"\n"
          + "    Other dealers said the U.S. Seems unwilling to see any\n"
          + "strong dollar swing until Japanese companies close their books\n"
          + "for the fiscal year ending on March 31, because a weak dollar\n"
          + "would give Japanese institutional investors paper losses on\n"
          + "their foreign holdings, which could make them lose interest in\n"
          + "purchases of U.S. Treasury securities.\n"
          + "    U.S. Monetary officials may refrain from making any\n"
          + "comments this month to avoid influencing rates, they said.\n"
          + " REUTER\n"
          + "</BODY></TEXT>\n"
          + "</REUTERS></value>\n"
          + "  </stringxml>\n"
          + "</metacard>";
}
