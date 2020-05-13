/*
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
  public void testGetLocation() throws URISyntaxException, IOException {
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
    when(statusLine.getStatusCode()).thenReturn(200);

    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getContent()).thenReturn(content);

    doAnswer(
            new Answer<Object>() {
              public Object answer(InvocationOnMock invocation) throws IOException {
                ResponseHandler<String> responseHandler =
                    (ResponseHandler<String>) invocation.getArguments()[1];
                return responseHandler.handleResponse(httpResponse);
              }
            })
        .when(client)
        .execute(any(HttpRequestBase.class), any(ResponseHandler.class));

    assertThat(
        webHdfsNodeAdapter.getLocation(createStorageRequest),
        is("http://host2:5678/some/other/path/"));
  }

  @Test
  public void testGetLocationNullResource() throws URISyntaxException {
    CreateStorageRequest createStorageRequest = mock(CreateStorageRequest.class);
    List<Resource> resources = Collections.singletonList(null);
    when(createStorageRequest.getResources()).thenReturn(resources);

    thrown.expect(ReplicationException.class);
    thrown.expectMessage("Null resource encountered.");

    webHdfsNodeAdapter.getLocation(createStorageRequest);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSendHttpRequestSuccess() throws IOException {
    HttpRequestBase request = mock(HttpRequestBase.class);
    ResponseHandler<String> responseHandler = (ResponseHandler<String>) mock(ResponseHandler.class);
    when(client.execute(request, responseHandler)).thenReturn("12345");

    assertThat(webHdfsNodeAdapter.sendHttpRequest(request, responseHandler), is("12345"));
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
}
