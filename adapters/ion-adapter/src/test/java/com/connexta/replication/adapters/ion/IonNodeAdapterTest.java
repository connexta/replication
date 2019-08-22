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
package com.connexta.replication.adapters.ion;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.connexta.replication.api.data.CreateStorageRequest;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.Resource;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

public class IonNodeAdapterTest {

  RestTemplate restTemplate;

  MockRestServiceServer mockServer;

  IonNodeAdapter adapter;

  @Before
  public void setup() throws Exception {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    adapter = new IonNodeAdapter(new URL("http://localhost:1234"), restTemplate);
  }

  @Test
  public void isAvailable() {
    DefaultResponseCreator response = MockRestResponseCreators.withSuccess();
    HttpHeaders headers = new HttpHeaders();
    headers.addAll("Allow", ImmutableList.of("POST", "OPTIONS"));
    response.headers(headers);
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.OPTIONS))
        .andRespond(response);
    assertThat(adapter.isAvailable(), is(true));
    mockServer.verify();
  }

  @Test
  public void isNotAvailable() throws Exception {
    restTemplate = new RestTemplate();
    adapter = new IonNodeAdapter(new URL("http://localhost:1234321"), restTemplate);
    assertThat(adapter.isAvailable(), is(false));
  }

  @Test
  public void getSystemName() {
    assertThat(adapter.getSystemName(), is("ION"));
  }

  @Test
  public void createResource() {
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(containsString(resourceFormData())))
        .andExpect(content().string(containsString(correlationIdFormData())))
        .andExpect(content().string(containsString(metadataFormData())))
        .andRespond(withStatus(HttpStatus.ACCEPTED));

    assertThat(adapter.createResource(getCreateStorageRequest()), is(true));
    mockServer.verify();
  }

  @Test
  public void createResource200() {
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.OK));

    assertThat(adapter.createResource(getCreateStorageRequest()), is(true));
    mockServer.verify();
  }

  @Test
  public void createResourceFailed300() {
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.MULTIPLE_CHOICES));

    assertThat(adapter.createResource(getCreateStorageRequest()), is(false));
    mockServer.verify();
  }

  @Test
  public void createResourceFailed401() {
    mockServer
        .expect(requestTo("http://localhost:1234/ingest"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    assertThat(adapter.createResource(getCreateStorageRequest()), is(false));
    mockServer.verify();
  }

  @Test
  public void unimplementedMethods() {
    // many of the unimplemented methods return true since we don't want to wast time retrying them
    // later
    // when we know they will never succeed. We will just let replication think it was successful.
    assertThat(adapter.createRequest(null), is(true));
    assertThat(adapter.updateRequest(null), is(true));
    assertThat(adapter.deleteRequest(null), is(true));
    assertThat(adapter.readResource(null), is(nullValue()));
    assertThat(adapter.updateResource(null), is(true));
    assertThat(adapter.query(null).getMetadata().iterator().hasNext(), is(false));
  }

  private CreateStorageRequest getCreateStorageRequest() {
    CreateStorageRequest request =
        () ->
            Collections.singletonList(
                new Resource() {
                  @Override
                  public String getId() {
                    return "1234";
                  }

                  @Override
                  public String getName() {
                    return "test";
                  }

                  @Override
                  public URI getResourceUri() {
                    return URI.create("https://host:1234/path/to/resource");
                  }

                  @Nullable
                  @Override
                  public String getQualifier() {
                    return null;
                  }

                  @Override
                  public InputStream getInputStream() {
                    return new ByteArrayInputStream("This is a test".getBytes());
                  }

                  @Override
                  public String getMimeType() {
                    return MediaType.TEXT_PLAIN.toString();
                  }

                  @Override
                  public long getSize() {
                    return 14;
                  }

                  @Override
                  public Metadata getMetadata() {
                    Metadata metadata = mock(Metadata.class);
                    when(metadata.getMetadataSize()).thenReturn((long) getRawMetadata().length());
                    when(metadata.getRawMetadata()).thenReturn(getRawMetadata());
                    return metadata;
                  }
                });
    return request;
  }

  private String metadataFormData() {
    String metadata = getRawMetadata();
    return "Content-Disposition: form-data; name=\"metacard\"\r\n"
        + "Content-Type: application/xml\r\n"
        + "Content-Length: "
        + metadata.length()
        + "\r\n\r\n"
        + metadata;
  }

  private String resourceFormData() {
    return "Content-Disposition: form-data; name=\"file\"; filename=\"test\"\r\n"
        + "Content-Type: text/plain\r\n"
        + "Content-Length: 14\r\n\r\n"
        + "This is a test";
  }

  private String correlationIdFormData() {
    return "Content-Disposition: form-data; name=\"correlationId\"\r\n"
        + "Content-Type: text/plain;charset=UTF-8\r\n"
        + "Content-Length: 4\r\n\r\n"
        + "1234";
  }

  private String getRawMetadata() {
    return "<metacard xmlns=\"urn:catalog:metacard\" xmlns:ns2=\"http://www.opengis.net/gml\"\n"
        + "          xmlns:ns3=\"http://www.w3.org/1999/xlink\" xmlns:ns4=\"http://www.w3.org/2001/SMIL20/\"\n"
        + "          xmlns:ns5=\"http://www.w3.org/2001/SMIL20/Language\">\n"
        + "    <type>myType</type>\n"
        + "    <source>mySource</source>\n"
        + "    <string name=\"metadata-content-type-version\">\n"
        + "        <value>myVersion</value>\n"
        + "    </string>\n"
        + "    <string name=\"title\">\n"
        + "        <value>warning metacard</value>\n"
        + "    </string>\n"
        + "    <dateTime name=\"created\">\n"
        + "        <value>2019-04-18T10:50:27.371-07:00</value>\n"
        + "    </dateTime>\n"
        + "    <dateTime name=\"modified\">\n"
        + "        <value>2019-04-18T10:50:27.371-07:00</value>\n"
        + "    </dateTime>\n"
        + "</metacard>";
  }
}
