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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.connexta.replication.data.ResourceImpl;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import java.util.stream.Collectors;
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
import org.codice.ditto.replication.api.data.ResourceRequest;
import org.codice.ditto.replication.api.data.ResourceResponse;
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

  @SuppressWarnings({"Duplicates", "unchecked"})
  @Test
  public void testReadResource() throws URISyntaxException, IOException {
    String testResourceId = "123456789";
    Date testDate = new Date();
    String testResourceName = String.format("%s_%s", testResourceId, testDate.getTime());
    Long testResourceSize = 256L;
    URI testResourceUri =
        new URI(String.format("http://host1:8000/test/resource/%s.txt", testResourceName));
    String testFileLocation = String.format("{\"Location\":\"%s\"}", testResourceUri.toString());

    ResourceRequest mockResourceRequest = mock(ResourceRequest.class);
    Metadata mockMetadata = mock(Metadata.class);

    when(mockResourceRequest.getMetadata()).thenReturn(mockMetadata);
    when(mockMetadata.getId()).thenReturn(testResourceId);
    when(mockMetadata.getResourceUri()).thenReturn(testResourceUri);
    when(mockMetadata.getResourceModified()).thenReturn(testDate);

    /* Location response mocks */
    HttpResponse mockLocationHttpResponse = mock(HttpResponse.class);
    StatusLine mockLocationStatusLine = mock(StatusLine.class);
    HttpEntity mockLocationHttpEntity = mock(HttpEntity.class);
    Header mockLocationHeader = mock(Header.class);
    InputStream locationContent = new ByteArrayInputStream(testFileLocation.getBytes());

    when(mockLocationHttpResponse.getStatusLine()).thenReturn(mockLocationStatusLine);
    when(mockLocationHttpResponse.getEntity()).thenReturn(mockLocationHttpEntity);
    when(mockLocationStatusLine.getStatusCode()).thenReturn(200);
    when(mockLocationHttpEntity.getContent()).thenReturn(locationContent);
    when(mockLocationHttpEntity.getContentType()).thenReturn(mockLocationHeader);
    when(mockLocationHeader.getValue()).thenReturn("application/json");
    /* End */

    /* Resource content mocks */
    HttpResponse mockResourceHttpResponse = mock(HttpResponse.class);
    StatusLine mockResourceStatusLine = mock(StatusLine.class);
    HttpEntity mockResourceHttpEntity = mock(HttpEntity.class);
    Header mockResourceHeader = mock(Header.class);
    InputStream resourceContent = new ByteArrayInputStream("my-data".getBytes());

    when(mockResourceHttpResponse.getStatusLine()).thenReturn(mockResourceStatusLine);
    when(mockResourceHttpResponse.getEntity()).thenReturn(mockResourceHttpEntity);
    when(mockResourceStatusLine.getStatusCode()).thenReturn(200);
    when(mockResourceHttpEntity.getContent()).thenReturn(resourceContent);
    when(mockResourceHttpEntity.getContentLength()).thenReturn(testResourceSize);
    when(mockResourceHttpEntity.getContentType()).thenReturn(mockResourceHeader);
    when(mockResourceHeader.getValue()).thenReturn("text/plain");
    /* End */

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(mockLocationHttpResponse);
            })
        .doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(mockResourceHttpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    Resource expectedResource =
        new ResourceImpl(
            testResourceId,
            testResourceName,
            testResourceUri,
            null,
            new ByteArrayInputStream("my-data".getBytes()),
            "text/plain",
            testResourceSize,
            mockMetadata);
    ResourceResponse actualResponse = webHdfsNodeAdapter.readResource(mockResourceRequest);
    Resource actualResource = actualResponse.getResource();

    assertThat(actualResource.getId(), is(expectedResource.getId()));
    assertThat(actualResource.getName(), is(expectedResource.getName()));
    assertThat(
        actualResource.getMetadata().getResourceUri(), is(expectedResource.getResourceUri()));
    assertThat(
        readInputStreamToString(actualResource.getInputStream()),
        is(readInputStreamToString(expectedResource.getInputStream())));
  }

  @SuppressWarnings({"Duplicates", "unchecked"})
  @Test
  public void testGetLocationReadSuccess() throws URISyntaxException, IOException {
    URI testResourceUri = new URI("http://host1:8000/test/resource/file.txt");
    String testFileLocation = String.format("{\"Location\":\"%s\"}", testResourceUri.toString());
    ResourceRequest mockResourceRequest = mock(ResourceRequest.class);
    Metadata mockMetadata = mock(Metadata.class);

    HttpResponse mockHttpResponse = mock(HttpResponse.class);
    StatusLine mockStatusLine = mock(StatusLine.class);
    HttpEntity mockHttpEntity = mock(HttpEntity.class);
    InputStream content = new ByteArrayInputStream(testFileLocation.getBytes());

    when(mockResourceRequest.getMetadata()).thenReturn(mockMetadata);
    when(mockMetadata.getId()).thenReturn("123456789");
    when(mockMetadata.getResourceUri()).thenReturn(testResourceUri);

    when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
    when(mockStatusLine.getStatusCode()).thenReturn(200).thenReturn(201);
    when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
    when(mockHttpEntity.getContent()).thenReturn(content);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<String> responseHandler =
                  (ResponseHandler<String>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(mockHttpResponse);
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(webHdfsNodeAdapter.getLocation(mockResourceRequest), is(testResourceUri.toString()));
  }

  @Test
  public void testGetLocationNullMetadata() throws URISyntaxException {
    ResourceRequest mockResourceRequest = mock(ResourceRequest.class);
    when(mockResourceRequest.getMetadata()).thenReturn(null);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("No accessible metadata was found for the request.");

    webHdfsNodeAdapter.getLocation(mockResourceRequest);
  }

  @Test
  public void testReadFileAtLocationBadStatus() throws IOException {
    ResourceRequest mockResourceRequest = mock(ResourceRequest.class);
    Metadata mockMetadata = mock(Metadata.class);

    when(mockMetadata.getId()).thenReturn("404");
    when(mockResourceRequest.getMetadata()).thenReturn(mockMetadata);

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

    webHdfsNodeAdapter.readFileAtLocation(mockResourceRequest, "");
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

    when(createStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);
    when(resource.getMimeType()).thenReturn("application/json");
    when(resource.getInputStream()).thenReturn(IOUtils.toInputStream("my-data", UTF_8));
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
  public void testWriteFileToLocationNoResource() throws IOException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    List<Resource> resources = Collections.emptyList();
    when(createStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("No compatible Resource was found.");

    webHdfsNodeAdapter.writeFileToLocation(createStorageRequest, "http://host:1234/location/");
  }

  @Test
  public void testWriteFileToLocationNullResource() throws IOException {
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

    when(resource.getInputStream()).thenReturn(IOUtils.toInputStream("my-data", UTF_8));
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

    when(resource.getInputStream()).thenReturn(IOUtils.toInputStream("my-data", UTF_8));
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
  public void testWriteFileLocationBadUri() throws IOException {
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

    when(updateStorageRequest.getResources()).thenReturn(resources);
    when(resource.getMetadata()).thenReturn(metadata);
    when(resource.getMimeType()).thenReturn("text/plain");
    when(resource.getInputStream()).thenReturn(IOUtils.toInputStream("my-data", UTF_8));
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

    assertThat(webHdfsNodeAdapter.updateResource(updateStorageRequest), is(false));
  }

  @Test
  public void testUpdateResourceNullResource() {
    UpdateStorageRequest updateStorageRequest = mock(UpdateStorageRequest.class);
    List<Resource> resources = Collections.singletonList(null);
    when(updateStorageRequest.getResources()).thenReturn(resources);

    assertThat(webHdfsNodeAdapter.updateResource(updateStorageRequest), is(false));
  }

  private String readInputStreamToString(InputStream contentStream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(contentStream));
    try {
      return reader.lines().collect(Collectors.joining("\n"));
    } finally {
      reader.close();
    }
  }
}
