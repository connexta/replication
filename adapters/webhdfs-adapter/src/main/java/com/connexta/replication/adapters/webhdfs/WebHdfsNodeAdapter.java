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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.ReplicationException;
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

/** Interacts with a remote Hadoop instance through the webHDFS REST API */
public class WebHdfsNodeAdapter implements NodeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsNodeAdapter.class);

  private final URL webHdfsUrl;

  private final CloseableHttpClient client;

  /**
   * Adapter to interact with a Hadoop instance through the webHDFS REST API
   *
   * @param webHdfsUrl the address of the REST API for the
   * @param client performs the HTTP requests
   */
  public WebHdfsNodeAdapter(URL webHdfsUrl, CloseableHttpClient client) {
    this.webHdfsUrl = webHdfsUrl;
    this.client = client;
  }

  @Override
  public boolean isAvailable() {
    LOGGER.debug("Checking access to: {}", webHdfsUrl);

    try {
      URIBuilder builder = new URIBuilder(webHdfsUrl.toString());
      builder.setParameter("op", "CHECKACCESS");

      HttpGet httpGet = new HttpGet(builder.build());

      ResponseHandler<Boolean> responseHandler =
          response -> {
            int status = response.getStatusLine().getStatusCode();

            return status == HttpStatus.SC_OK;
          };

      return sendHttpRequest(httpGet, responseHandler);
    } catch (URISyntaxException e) {
      LOGGER.error("Unable to check availability, due to invalid URL.", e);
      return false;
    }
  }

  @Override
  public String getSystemName() {
    return "webHDFS";
  }

  @Override
  public QueryResponse query(QueryRequest queryRequest) {
    return null;
  }

  @Override
  public boolean exists(Metadata metadata) {
    return false;
  }

  @Override
  public boolean createRequest(CreateRequest createRequest) {
    return false;
  }

  @Override
  public boolean updateRequest(UpdateRequest updateRequest) {
    return false;
  }

  @Override
  public boolean deleteRequest(DeleteRequest deleteRequest) {
    return false;
  }

  @Override
  public ResourceResponse readResource(ResourceRequest resourceRequest) {
    return null;
  }

  @Override
  public boolean createResource(CreateStorageRequest createStorageRequest) {
    try {
      String location = getLocation(createStorageRequest);

      return writeFileToLocation(createStorageRequest, location);

    } catch (URISyntaxException e) {
      LOGGER.error("Unable to get location, due to invalid URL.", e);
    } catch (ReplicationException e) {
      LOGGER.error("Unable to create resource.", e);
    }
    return false;
  }

  /**
   * Sends a PUT request with no file data and without following redirects, in order to retrieve the
   * location to write to
   *
   * @param createStorageRequest request containing the {@link
   *     org.codice.ditto.replication.api.data.Resource} to create
   * @return a {@code String} containing the location to write to
   * @throws URISyntaxException when the fileUrl cannot be parsed as a URI reference
   */
  @VisibleForTesting
  String getLocation(CreateStorageRequest createStorageRequest) throws URISyntaxException {

    if (createStorageRequest.getResources().get(0) == null) {
      throw new ReplicationException("Null resource encountered.");
    }

    // only a single resource is supported at this time and is the reason for always retrieving from
    // index zero
    Metadata metadata = createStorageRequest.getResources().get(0).getMetadata();
    String filename = metadata.getId() + "_" + metadata.getResourceModified().getTime();

    String fileUrl = webHdfsUrl.toString() + filename;
    LOGGER.debug("The complete file URL is: {}", fileUrl);

    URIBuilder builder = new URIBuilder(fileUrl);
    builder.setParameter("op", "CREATE").setParameter("noredirect", "true");
    HttpPut httpPut = new HttpPut(builder.build());

    ResponseHandler<String> responseHandler =
        response -> {
          int status = response.getStatusLine().getStatusCode();

          switch (status) {
            case HttpStatus.SC_OK:
              InputStream content = response.getEntity().getContent();

              ObjectMapper objectMapper = new ObjectMapper();
              Map jsonMap = objectMapper.readValue(content, Map.class);

              return jsonMap.get("Location").toString();
            case HttpStatus.SC_TEMPORARY_REDIRECT:
              Header locationHeader = response.getFirstHeader("Location");

              return locationHeader.getValue();
            default:
              throw new ReplicationException(
                  String.format("Request failed with status code: %d", status));
          }
        };

    return sendHttpRequest(httpPut, responseHandler);
  }

  /**
   * Executes an HTTP request
   *
   * @param request the HTTP request to execute
   * @param responseHandler a value based on the response
   * @param <T> the type expected in the response
   * @return the response body
   */
  @VisibleForTesting
  <T> T sendHttpRequest(HttpRequestBase request, ResponseHandler<T> responseHandler) {
    try {
      return client.execute(request, responseHandler);
    } catch (IOException e) {
      throw new ReplicationException(
          String.format("Failed to send %s to remote system.", request.getMethod()), e);
    }
  }

  /**
   * Sends a PUT request with file data to the specified location
   *
   * @param createStorageRequest request containing the {@link
   *     org.codice.ditto.replication.api.data.Resource} to create
   * @param location the full address of the location to write the resource to
   * @return {@code true} if successful, otherwise {@code false}
   */
  @VisibleForTesting
  boolean writeFileToLocation(CreateStorageRequest createStorageRequest, String location) {

    if (createStorageRequest.getResources().get(0) == null) {
      throw new ReplicationException("Null resource encountered.");
    }

    LOGGER.debug("The location being written to is: {}", location);
    HttpPut httpPut = new HttpPut(location);

    // only a single resource is supported at this time and is the reason for always retrieving from
    // index zero
    Resource resource = createStorageRequest.getResources().get(0);

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.addBinaryBody(
        "file", resource.getInputStream(), ContentType.create(resource.getMimeType()), "file.ext");
    HttpEntity multipart = builder.build();
    httpPut.setEntity(multipart);

    ResponseHandler<Boolean> responseHandler =
        response -> {
          int status = response.getStatusLine().getStatusCode();

          if (status == HttpStatus.SC_CREATED) {
            return true;
          } else {
            throw new ReplicationException(
                String.format("Request failed with status code: %d", status));
          }
        };

    return sendHttpRequest(httpPut, responseHandler);
  }

  @Override
  public boolean updateResource(UpdateStorageRequest updateStorageRequest) {
    return false;
  }

  @Override
  public void close() throws IOException {
    client.close();
  }
}
