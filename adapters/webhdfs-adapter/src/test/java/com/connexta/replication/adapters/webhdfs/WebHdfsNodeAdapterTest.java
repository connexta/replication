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
package com.connexta.replication.adapters.webhdfs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.Resource;
import org.codice.ditto.replication.api.data.UpdateStorageRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link WebHdfsNodeAdapter} */
@RunWith(JUnit4.class)
public class WebHdfsNodeAdapterTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  WebHdfsNodeAdapter webHdfsNodeAdapter;

  CloseableHttpClient client;

  @Before
  public void setup() throws MalformedURLException {

    client = mock(CloseableHttpClient.class);
    webHdfsNodeAdapter = new WebHdfsNodeAdapter(new URL("http://host:1234/some/path/"), client);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testIsAvailable() throws IOException {
    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(webHdfsNodeAdapter.isAvailable(), is(true));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testIsNotAvailable() throws IOException {
    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(webHdfsNodeAdapter.isAvailable(), is(false));
  }

  @Test
  public void testCreateResource() throws IOException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);

    Metadata metadata = mock(Metadata.class);
    Date date = new Date();

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    String testJson = "{\"Location\":\"http://host2:5678/some/other/path/file123.json\"}";
    InputStream content = new ByteArrayInputStream(testJson.getBytes());
    InputStream resourceContent = mock(InputStream.class);

    when(createStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);
    when(resource.getMimeType()).thenReturn("application/json");
    when(resource.getInputStream()).thenReturn(resourceContent);
    when(resource.getName()).thenReturn("file123.json");

    when(metadata.getId()).thenReturn("112358");
    when(metadata.getResourceModified()).thenReturn(date);

    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200).thenReturn(201);

    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getContent()).thenReturn(content);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(webHdfsNodeAdapter.createResource(createStorageRequest), is(true));
  }

  @Test
  public void testCreateResourceUriException() {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);
    Metadata metadata = mock(Metadata.class);
    Date date = new Date();

    when(createStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);
    when(metadata.getResourceModified()).thenReturn(date);
    when(metadata.getId()).thenReturn("??* %!");

    assertThat(webHdfsNodeAdapter.createResource(createStorageRequest), is(false));
  }

  @Test
  public void testCreateResourceReplicationException() {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    List<Resource> resources = Collections.singletonList(null);
    when(createStorageRequest.getResources()).thenReturn(resources);

    assertThat(webHdfsNodeAdapter.createResource(createStorageRequest), is(false));
  }

  @Test
  public void testGetLocationNoResource() throws URISyntaxException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    List<Resource> resources = Collections.emptyList();
    when(createStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("No compatible Resource was found.");

    webHdfsNodeAdapter.getLocation(createStorageRequest);
  }

  @Test
  public void testGetLocationNullResource() throws URISyntaxException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    List<Resource> resources = Collections.singletonList(null);
    when(createStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("No compatible Resource was found.");

    webHdfsNodeAdapter.getLocation(createStorageRequest);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendHttpRequestFailure() throws IOException {
    HttpRequestBase request = mock(HttpRequestBase.class);
    ResponseHandler<String> responseHandler = (ResponseHandler<String>) mock(ResponseHandler.class);

    when(request.getMethod()).thenReturn("GET");

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("Failed to send GET to remote system.");

    doThrow(new IOException()).when(client).execute(request, responseHandler);
    webHdfsNodeAdapter.sendHttpRequest(request, responseHandler);
  }

  @Test
  public void testWriteFileToLocationNoResource() {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    List<Resource> resources = Collections.emptyList();
    when(createStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("No compatible Resource was found.");

    webHdfsNodeAdapter.writeFileToLocation(createStorageRequest, "http://host:1234/location/");
  }

  @Test
  public void testWriteFileToLocationNullResource() {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    List<Resource> resources = Collections.singletonList(null);
    when(createStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("No compatible Resource was found.");

    webHdfsNodeAdapter.writeFileToLocation(createStorageRequest, "http://host:1234/location/");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetLocationSuccess() throws URISyntaxException, IOException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);

    Metadata metadata = mock(Metadata.class);
    Date date = new Date();

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    String testJson = "{\"Location\":\"http://host2:5678/some/other/path/\"}";
    InputStream content = new ByteArrayInputStream(testJson.getBytes());

    when(createStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);

    when(metadata.getId()).thenReturn("112358");
    when(metadata.getResourceModified()).thenReturn(date);

    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getContent()).thenReturn(content);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(
        webHdfsNodeAdapter.getLocation(createStorageRequest),
        is("http://host2:5678/some/other/path/"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetLocationRedirect() throws URISyntaxException, IOException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);

    Metadata metadata = mock(Metadata.class);
    Date date = new Date();

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(createStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);

    when(metadata.getId()).thenReturn("112358");
    when(metadata.getResourceModified()).thenReturn(date);

    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_TEMPORARY_REDIRECT);

    Header header = mock(Header.class);
    when(httpResponse.getFirstHeader("Location")).thenReturn(header);
    when(header.getValue()).thenReturn("http://host2:5678/some/other/path/file123.json");

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(
        webHdfsNodeAdapter.getLocation(createStorageRequest),
        is("http://host2:5678/some/other/path/file123.json"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetLocationUnexpectedCode() throws URISyntaxException, IOException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);

    Metadata metadata = mock(Metadata.class);
    Date date = new Date();

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    when(createStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);

    when(metadata.getId()).thenReturn("112358");
    when(metadata.getResourceModified()).thenReturn(date);

    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("Request failed with status code: 400");

    webHdfsNodeAdapter.getLocation(createStorageRequest);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testWriteFileToLocationCreated() throws IOException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);
    Metadata metadata = mock(Metadata.class);
    Date date = new Date();

    when(createStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);
    when(metadata.getId()).thenReturn("1234");
    when(metadata.getResourceModified()).thenReturn(date);

    InputStream inputStream = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(inputStream);
    when(resource.getMimeType()).thenReturn("application/json");
    when(resource.getName()).thenReturn("file123.json");

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(
        webHdfsNodeAdapter.writeFileToLocation(
            createStorageRequest, "http://host:314/some/path/to/file123.json"),
        is(true));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testWriteFileToLocationUnexpectedCode() throws IOException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);
    when(createStorageRequest.getResources()).thenReturn(resources);

    Metadata metadata = mock(Metadata.class);
    Date date = new Date();
    when(resource.getMetadata()).thenReturn(metadata);
    when(metadata.getId()).thenReturn("1234");
    when(metadata.getResourceModified()).thenReturn(date);

    InputStream inputStream = mock(InputStream.class);
    when(resource.getInputStream()).thenReturn(inputStream);
    when(resource.getMimeType()).thenReturn("application/json");
    when(resource.getName()).thenReturn("file123.json");

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("Request failed with status code: 400");

    assertThat(
        webHdfsNodeAdapter.writeFileToLocation(
            createStorageRequest, "http://host:314/some/path/to/file123.json"),
        is(false));
  }

  @Test
  public void testWriteFileLocationBadUri() {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);
    String badUri = "^^^^";

    when(createStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("Failed to write file. The location URI has syntax errors.");

    webHdfsNodeAdapter.writeFileToLocation(createStorageRequest, badUri);
  }

  @Test
  public void testUpdateResource() throws IOException {
    UpdateStorageRequest updateStorageRequest = mock(UpdateStorageRequest.class);
    Resource resource = mock(Resource.class);
    List<Resource> resources = Collections.singletonList(resource);

    Metadata metadata = mock(Metadata.class);
    Date date = new Date();

    String resourceName = "updateTestFile.txt";
    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    String testJson =
        String.format("{\"Location\":\"http://host2:5678/some/other/path/%s\"}", resourceName);
    InputStream content = new ByteArrayInputStream(testJson.getBytes());
    InputStream resourceContent = mock(InputStream.class);

    when(updateStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);
    when(resource.getMimeType()).thenReturn("text/plain");
    when(resource.getInputStream()).thenReturn(resourceContent);
    when(resource.getName()).thenReturn(resourceName);

    when(metadata.getId()).thenReturn("123456789");
    when(metadata.getResourceModified()).thenReturn(date);

    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200).thenReturn(201);

    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getContent()).thenReturn(content);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(httpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(webHdfsNodeAdapter.updateResource(updateStorageRequest), is(true));
  }

  @Test
  public void testUpdateResourceNoResource() {
    UpdateStorageRequest updateStorageRequest = mock(UpdateStorageRequest.class);
    List<Resource> resources = Collections.emptyList();
    when(updateStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("Unable to convert storage request. No compatible Resource was found.");

    webHdfsNodeAdapter.updateResource(updateStorageRequest);
  }

  @Test
  public void testUpdateResourceNullResource() {
    UpdateStorageRequest updateStorageRequest = mock(UpdateStorageRequest.class);
    List<Resource> resources = Collections.singletonList(null);
    when(updateStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("Unable to convert storage request. No compatible Resource was found.");

    webHdfsNodeAdapter.updateResource(updateStorageRequest);
  }
}
