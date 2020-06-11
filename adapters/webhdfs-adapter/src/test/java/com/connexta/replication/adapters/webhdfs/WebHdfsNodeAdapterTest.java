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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.adapters.webhdfs.filesystem.DirectoryListing;
import com.connexta.replication.adapters.webhdfs.filesystem.FileStatus;
import com.connexta.replication.adapters.webhdfs.filesystem.FileStatuses;
import com.connexta.replication.adapters.webhdfs.filesystem.IterativeDirectoryListing;
import com.connexta.replication.adapters.webhdfs.filesystem.PartialListing;
import com.connexta.replication.data.MetadataAttribute;
import com.connexta.replication.data.ResourceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.QueryResponse;
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

  @Test
  public void testGetSystemName() {
    assertThat(webHdfsNodeAdapter.getSystemName(), is("webHDFS"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testQuery() throws IOException {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));

    // the filter date
    cal.set(Calendar.MONTH, 4);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.YEAR, 2010);
    Date filter = cal.getTime();

    QueryRequest queryRequest = mock(QueryRequest.class);
    when(queryRequest.getModifiedAfter()).thenReturn(filter);

    // one day after the filter date
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date fileDate = cal.getTime();

    // given a file with a date after the filter date exists on the system being queried
    FileStatus file = getFileStatus(fileDate, "file1.ext", "FILE", 251);
    List<FileStatus> files = Collections.singletonList(file);

    // and there are no additional entries to retrieve
    String idl = getIterativeDirectoryListingAsString(files, 0);
    InputStream inputStream = new ByteArrayInputStream(idl.getBytes(UTF_8));

    HttpResponse response = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(response.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);

    // and the response contains the expected single file
    HttpEntity httpEntity = mock(HttpEntity.class);
    when(response.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getContent()).thenReturn(inputStream);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<List<FileStatus>> responseHandler =
                  (ResponseHandler<List<FileStatus>>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(response);
            })
        .when(client)
        .execute(any(HttpGet.class), any(ResponseHandler.class));

    // when the file system is queried
    QueryResponse queryResponse = webHdfsNodeAdapter.query(queryRequest);

    // then a query response is returned
    Iterable<Metadata> metadataIterable = queryResponse.getMetadata();
    Metadata metadata = Iterables.get(metadataIterable, 0);

    // and the query response contains the metadata with the expected values
    // object
    assertThat(metadata.getId().charAt(14), is('4')); // indicating version-4 UUID
    assertThat(metadata.getId().length(), is(36)); // 32 digits, plus 4 -'s
    assertThat(metadata.getMetadataModified(), is(fileDate));
    assertThat(metadata.getResourceUri().toString(), is("http://host:1234/some/path/file1.ext"));
    assertThat(metadata.getResourceModified(), is(fileDate));
    assertThat(metadata.getResourceSize(), is(251L));

    // and the metadata object's attributes will have values corresponding to the FileStatus object
    Map<String, MetadataAttribute> metadataAttributes =
        (Map<String, MetadataAttribute>) metadata.getRawMetadata();

    assertThat(metadataAttributes.get("id").getValue(), is(metadata.getId()));
    assertThat(metadataAttributes.get("title").getValue(), is("file1.ext"));
    assertThat(metadataAttributes.get("created").getValue(), is(fileDate.toString()));
    assertThat(metadataAttributes.get("modified").getValue(), is(fileDate.toString()));
    assertThat(
        metadataAttributes.get("resource-uri").getValue(),
        is("http://host:1234/some/path/file1.ext"));
    assertThat(metadataAttributes.get("resource-size").getValue(), is("251"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCreateMetadata() {
    FileStatus fileStatus = mock(FileStatus.class);

    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));

    cal.set(Calendar.MONTH, 4);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.YEAR, 2010);
    Date date = cal.getTime();

    // when a FileStatus object has the following attributes
    when(fileStatus.getModificationTime()).thenReturn(date);
    String filename = "test.txt";
    when(fileStatus.getPathSuffix()).thenReturn(filename);
    when(fileStatus.getType()).thenReturn("FILE");
    when(fileStatus.getLength()).thenReturn(251);

    // and createMetadata is called
    Metadata metadata = webHdfsNodeAdapter.createMetadata(fileStatus);

    // then the resulting metadata object will have values corresponding to the FileStatus object
    assertThat(metadata.getId().charAt(14), is('4')); // indicating version-4 UUID
    assertThat(
        metadata.getId().length(),
        is(36)); // 32 digits, plus 4 -'s    assertThat(metadata.getMetadataModified(), is(date));
    assertThat(metadata.getResourceUri().toString(), is("http://host:1234/some/path/test.txt"));
    assertThat(metadata.getResourceModified(), is(date));
    assertThat(metadata.getResourceSize(), is(251L));

    // and the metadata object's attributes will have values corresponding to the FileStatus object
    Map<String, MetadataAttribute> metadataAttributes =
        (Map<String, MetadataAttribute>) metadata.getRawMetadata();
    assertThat(metadataAttributes.get("id").getValue(), is(metadata.getId()));
    assertThat(metadataAttributes.get("title").getValue(), is(filename));
    assertThat(metadataAttributes.get("created").getValue(), is(date.toString()));
    assertThat(metadataAttributes.get("modified").getValue(), is(date.toString()));
    assertThat(
        metadataAttributes.get("resource-uri").getValue(),
        is("http://host:1234/some/path/test.txt"));
    assertThat(metadataAttributes.get("resource-size").getValue(), is("251"));
  }

  @Test
  public void testCreateMetadataBadUri() {
    FileStatus fileStatus = mock(FileStatus.class);

    // when a bad filename is utilized to generate a URI
    Date date = new Date();
    when(fileStatus.getModificationTime()).thenReturn(date);
    String badFilename = "* !";
    when(fileStatus.getPathSuffix()).thenReturn(badFilename);
    when(fileStatus.getType()).thenReturn("FILE");

    // then a ReplicationException is thrown
    thrown.expect(ReplicationException.class);
    thrown.expectMessage("Unable to create a URI from the file's URL.");

    webHdfsNodeAdapter.createMetadata(fileStatus);
  }

  @Test
  public void testGetRelevantFilesNullFilterDate() {

    FileStatus file1 = new FileStatus();
    file1.setType("FILE");
    FileStatus file2 = new FileStatus();
    file2.setType("DIRECTORY");
    FileStatus file3 = new FileStatus();
    file3.setType("FILE");

    List<FileStatus> files = Stream.of(file1, file2, file3).collect(Collectors.toList());

    // when getting a list of relevant files with no specified filter date
    List<FileStatus> result = webHdfsNodeAdapter.getRelevantFiles(files, null);

    // then all items of type FILE are returned
    assertThat(result.get(0), is(file1));
    assertThat(result.get(1), is(file3));
    assertThat(result.size(), is(2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetFilesToReplicate() throws IOException {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));

    // a date occurring before the filter date
    cal.set(Calendar.MONTH, 4);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.YEAR, 2010);
    Date beforeFilter = cal.getTime();

    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date filter = cal.getTime();

    // four dates more recent than the filter date
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date afterFilter1 = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date afterFilter2 = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date afterFilter3 = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date afterFilter4 = cal.getTime();

    // given a set of three files, one before the filter date and two after (one of which is a
    // directory)
    FileStatus file1 = getFileStatus(beforeFilter, "file1.ext", "FILE", 111);
    FileStatus file2 = getFileStatus(afterFilter1, "file2.ext", "FILE", 222);
    FileStatus file3 = getFileStatus(afterFilter2, "someDirectory", "DIRECTORY", 0);

    List<FileStatus> files1 = Stream.of(file1, file2, file3).collect(Collectors.toList());

    // and there are two additional entries to retrieve
    String idl1 = getIterativeDirectoryListingAsString(files1, 2);
    InputStream inputStream1 = new ByteArrayInputStream(idl1.getBytes(UTF_8));

    // and the second set of files contains two files, both after the filter date and of type FILE
    FileStatus file4 = getFileStatus(afterFilter3, "file4.ext", "FILE", 444);
    FileStatus file5 = getFileStatus(afterFilter4, "file5.ext", "FILE", 555);

    List<FileStatus> files2 = Arrays.asList(file4, file5);
    String idl2 = getIterativeDirectoryListingAsString(files2, 0);
    InputStream inputStream2 = new ByteArrayInputStream(idl2.getBytes(UTF_8));

    HttpResponse response = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(response.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);

    // and the first response contains the first set of files and the second response contains the
    // second set of files
    HttpEntity httpEntity = mock(HttpEntity.class);
    when(response.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getContent()).thenReturn(inputStream1).thenReturn(inputStream2);

    doAnswer(
            invocationOnMock -> {
              ResponseHandler<List<FileStatus>> responseHandler =
                  (ResponseHandler<List<FileStatus>>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(response);
            })
        .when(client)
        .execute(any(HttpGet.class), any(ResponseHandler.class));

    // when the adapter retrieves the files to replicate
    List<FileStatus> filesToReplicate = webHdfsNodeAdapter.getFilesToReplicate(filter);

    // then there are three files that fit the criteria
    assertThat(filesToReplicate.size() == 3, is(true));

    // and the modification dates on the files are those meeting the filter criteria
    assertThat(filesToReplicate.get(0).getModificationTime().compareTo(filter), is(1));
    assertThat(filesToReplicate.get(1).getModificationTime().compareTo(filter), is(1));
    assertThat(filesToReplicate.get(2).getModificationTime().compareTo(filter), is(1));

    // and the file names of the results are those meeting the filter criteria
    assertThat(filesToReplicate.get(0).getPathSuffix(), is("file2.ext"));
    assertThat(filesToReplicate.get(1).getPathSuffix(), is("file4.ext"));
    assertThat(filesToReplicate.get(2).getPathSuffix(), is("file5.ext"));

    // and all files identified to be replicated are of type FILE
    for (FileStatus file : filesToReplicate) {
      assertThat(file.getType(), is("FILE"));
    }

    assertThat(filesToReplicate.get(0).getLength(), is(222));
    assertThat(filesToReplicate.get(1).getLength(), is(444));
    assertThat(filesToReplicate.get(2).getLength(), is(555));

    // and this was determined through two HTTP requests to the remote
    verify(client, times(2)).execute(any(HttpGet.class), any(ResponseHandler.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetFilesToReplicateBadStatusCode() throws IOException {
    HttpResponse response = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(response.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(400);

    // when a bad request status code is returned
    doAnswer(
            invocationOnMock -> {
              ResponseHandler<List<FileStatus>> responseHandler =
                  (ResponseHandler<List<FileStatus>>) invocationOnMock.getArguments()[1];
              return responseHandler.handleResponse(response);
            })
        .when(client)
        .execute(any(HttpGet.class), any(ResponseHandler.class));

    // then a replication exception is thrown and
    thrown.expect(ReplicationException.class);
    thrown.expectMessage("List Status Batch request failed with status code: 400");

    webHdfsNodeAdapter.getFilesToReplicate(new Date());
  }

  @SuppressWarnings({"Duplicates", "unchecked"})
  @Test
  public void testReadResource() throws URISyntaxException, IOException {
    String testResourceId = "123456789";
    Date testDate = new Date();
    String testResourceName = String.format("%s_%s", testResourceId, testDate.getTime());
    long testResourceSize = 256L;
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

  /**
   * Reads an {@link InputStream} into a readable {@link String}.
   *
   * @param contentStream - the {@link InputStream} to read
   * @return The resulting {@link String} read
   */
  private String readInputStreamToString(InputStream contentStream) {
    try (final Reader reader = new InputStreamReader(contentStream)) {
      return IOUtils.toString(reader);
    } catch (IOException e) {
      throw new ReplicationException("Failed to read the input stream.", e);
    }
  }

  /**
   * Uses {@link ObjectMapper} to generate a {@code String} representation of an {@link
   * IterativeDirectoryListing} object equivalent in format to what is returned by the HTTP
   * LISTSTATUS_BATCH command.
   *
   * @param fileStatuses a {@code List} containing {@link FileStatus} objects
   * @param remainingEntries the number of remaining entries that exist, but were not returned by a
   *     given command
   * @return a {@code String} representation of an {@link IterativeDirectoryListing} object
   * @throws JsonProcessingException if {@link ObjectMapper} is unable to convert the {@link
   *     IterativeDirectoryListing} object into a {@code String}
   */
  private String getIterativeDirectoryListingAsString(
      List<FileStatus> fileStatuses, int remainingEntries) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    IterativeDirectoryListing iterativeDirectoryListing = new IterativeDirectoryListing();
    iterativeDirectoryListing.setDirectoryListing(
        getDirectoryListing(fileStatuses, remainingEntries));
    return objectMapper.writeValueAsString(iterativeDirectoryListing);
  }

  /**
   * Returns a {@link DirectoryListing} object containing a {@link PartialListing} object and the
   * specified number of remaining entries
   *
   * @param fileStatuses a {@code List} containing {@link FileStatus} objects
   * @param remainingEntries the number of remaining entries that exist, but were not returned by a
   *     given command
   * @return a {@link DirectoryListing} object containing a {@link PartialListing} object and the
   *     specified number of remaining entries
   */
  private DirectoryListing getDirectoryListing(
      List<FileStatus> fileStatuses, int remainingEntries) {
    DirectoryListing directoryListing = new DirectoryListing();
    directoryListing.setPartialListing(getPartialListing(fileStatuses));
    directoryListing.setRemainingEntries(remainingEntries);

    return directoryListing;
  }

  /**
   * Returns a {@link PartialListing} object containing a {@link FileStatuses} object
   *
   * @param fileStatuses a {@code List} containing {@link FileStatus} objects
   * @return a {@link PartialListing} object containing a {@link FileStatuses} object
   */
  private PartialListing getPartialListing(List<FileStatus> fileStatuses) {
    PartialListing partialListing = new PartialListing();
    partialListing.setFileStatuses(getFileStatuses(fileStatuses));

    return partialListing;
  }

  /**
   * Returns a {@link FileStatuses} object containing the provided {@code List} of {@link
   * FileStatus} objects
   *
   * @param fileStatuses a {@code List} containing {@link FileStatus} objects
   * @return a {@link FileStatuses} object containing the provided {@code List} of {@link
   *     FileStatus} objects
   */
  private FileStatuses getFileStatuses(List<FileStatus> fileStatuses) {
    FileStatuses fileStatusesObj = new FileStatuses();
    fileStatusesObj.setFileStatusList(fileStatuses);

    return fileStatusesObj;
  }

  /**
   * Returns a {@link FileStatus} object
   *
   * @param modificationTime the timestamp on the file or directory
   * @param pathSuffix designates the name of the file or directory
   * @param type specifies whether a FILE or DIRECTORY
   * @param length the size of the file
   * @return a {@link FileStatus} object
   */
  private FileStatus getFileStatus(
      Date modificationTime, String pathSuffix, String type, int length) {
    FileStatus fileStatus = new FileStatus();
    fileStatus.setModificationTime(modificationTime);
    fileStatus.setPathSuffix(pathSuffix);
    fileStatus.setType(type);
    fileStatus.setLength(length);

    return fileStatus;
  }
}
