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

import com.connexta.replication.data.ResourceImpl;
import com.connexta.replication.data.ResourceResponseImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import ddf.mime.tika.TikaMimeTypeResolver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
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
import org.codice.ditto.replication.api.impl.data.CreateStorageRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Interacts with a remote Hadoop instance through the webHDFS REST API */
public class WebHdfsNodeAdapter implements NodeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsNodeAdapter.class);

  private static final String HTTP_OPERATION_KEY = "op";
  private static final String HTTP_OPERATION_CHECK_ACCESS = "CHECKACCESS";
  private static final String HTTP_OPERATION_CREATE = "CREATE";
  private static final String HTTP_OPERATION_OPEN = "OPEN";

  private static final String HTTP_FILE_SYSTEM_ACTION_KEY = "fsaction";
  private static final String HTTP_FILE_SYSTEM_ACTION_ALL = "rwx";

  private static final String HTTP_NO_REDIRECT_KEY = "noredirect";
  private static final String HTTP_CREATE_OVERWRITE_KEY = "overwrite";

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

  @VisibleForTesting
  URL getWebHdfsUrl() {
    return webHdfsUrl;
  }

  @Override
  public boolean isAvailable() {
    LOGGER.info("Checking access to: {}", getWebHdfsUrl());

    try {
      URIBuilder builder = new URIBuilder(getWebHdfsUrl().toString());
      builder
          .setParameter(HTTP_OPERATION_KEY, HTTP_OPERATION_CHECK_ACCESS)
          .setParameter(HTTP_FILE_SYSTEM_ACTION_KEY, HTTP_FILE_SYSTEM_ACTION_ALL);

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
    LOGGER.debug("The WebHDFS adapter doesn't support metadata only creation at this time.");
    return false;
  }

  @Override
  public boolean updateRequest(UpdateRequest updateRequest) {
    LOGGER.debug("The WebHDFS adapter doesn't support metadata only updates at this time.");
    return false;
  }

  @Override
  public boolean deleteRequest(DeleteRequest deleteRequest) {
    LOGGER.debug("The WebHDFS adapter doesn't support metadata only deletion at this time.");
    return false;
  }

  @Override
  public ResourceResponse readResource(ResourceRequest resourceRequest) {
    ResourceResponse resourceResponse = null;

    try {
      String location = getLocation(resourceRequest);
      resourceResponse = readFileAtLocation(resourceRequest, location);
    } catch (URISyntaxException e) {
      LOGGER.error("Unable to get location, due to invalid URL.", e);
    } catch (ReplicationException e) {
      LOGGER.error("Unable to read resource.", e);
    }

    return resourceResponse;
  }

  @Override
  public boolean createResource(CreateStorageRequest createStorageRequest) {
    try {
      String location = getLocation(createStorageRequest);

      return writeFileToLocation(createStorageRequest, location);

    } catch (URISyntaxException e) {
      LOGGER.error("Unable to get location, due to invalid URL.", e);
    } catch (IOException | ReplicationException e) {
      LOGGER.error("Unable to create resource.", e);
    }
    return false;
  }

  /**
   * Sends a GET request to retrieve the location to read from. The <code>resourceRequest</code>
   * provides the URL to send the request as the resource URI in its metadata.
   *
   * @param resourceRequest - the {@link ResourceRequest} containing the resource URI to send the
   *     request to
   * @return The {@link String} of the location URL
   * @throws URISyntaxException When the resulting URI built from the resource URI has syntax issues
   */
  @VisibleForTesting
  String getLocation(ResourceRequest resourceRequest) throws URISyntaxException {
    if (resourceRequest.getMetadata() == null) {
      throw new ReplicationException("No accessible metadata was found for the request.");
    }

    Metadata metadata = resourceRequest.getMetadata();
    URI resourceUri = metadata.getResourceUri();

    URIBuilder uriBuilder = new URIBuilder(resourceUri);
    uriBuilder
        .setParameter(HTTP_OPERATION_KEY, HTTP_OPERATION_OPEN)
        .setParameter(HTTP_NO_REDIRECT_KEY, "true");
    HttpGet httpGet = new HttpGet(uriBuilder.build());

    return handleLocationRequest(httpGet);
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

    if (createStorageRequest.getResources().isEmpty()
        || createStorageRequest.getResources().get(0) == null) {
      throw new ReplicationException("No compatible Resource was found.");
    }

    String fileUrl = getWebHdfsUrl().toString() + formatFilename(createStorageRequest);
    LOGGER.info("The complete file URL is: {}", fileUrl);

    URIBuilder builder = new URIBuilder(fileUrl);
    builder
        .setParameter(HTTP_OPERATION_KEY, HTTP_OPERATION_CREATE)
        .setParameter(HTTP_NO_REDIRECT_KEY, "true");
    HttpPut httpPut = new HttpPut(builder.build());

    return handleLocationRequest(httpPut);
  }

  /**
   * Provides a {@link ResponseHandler} for handling HTTP requests for location URL strings and
   * calls {@link WebHdfsNodeAdapter#sendHttpRequest(HttpRequestBase, ResponseHandler)}.
   *
   * @param httpRequest - the {@link HttpRequestBase} to send with the {@link ResponseHandler}
   * @return The {@link String} of the location URL
   */
  private String handleLocationRequest(HttpRequestBase httpRequest) {
    ResponseHandler<String> responseHandler =
        response -> {
          int status = response.getStatusLine().getStatusCode();

          switch (status) {
            case HttpStatus.SC_OK:
              InputStream content = response.getEntity().getContent();

              ObjectMapper objectMapper = new ObjectMapper();
              Map<String, Object> jsonMap =
                  objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});

              return jsonMap.get("Location").toString();
            case HttpStatus.SC_TEMPORARY_REDIRECT:
              Header locationHeader = response.getFirstHeader("Location");

              return locationHeader.getValue();
            default:
              throw new ReplicationException(
                  String.format("Request failed with status code: %d", status));
          }
        };

    return sendHttpRequest(httpRequest, responseHandler);
  }

  private String formatFilename(CreateStorageRequest createStorageRequest) {

    // only a single resource is supported at this time and is the reason for always retrieving from
    // index zero
    Resource resource = createStorageRequest.getResources().get(0);
    Metadata metadata = resource.getMetadata();
    TikaMimeTypeResolver tikaMimeTypeResolver = new TikaMimeTypeResolver();

    return metadata.getId()
        + "_"
        + metadata.getResourceModified().getTime()
        + tikaMimeTypeResolver.getFileExtensionForMimeType(resource.getMimeType());
  }

  /**
   * Clones an {@link InputStream} to a more reusable input stream object.
   *
   * @param inputStream - the {@link InputStream} to clone
   * @return The cloned {@link InputStream}
   * @throws IOException When the read or flush operation fails
   */
  private InputStream cloneInputStream(InputStream inputStream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int len;

    while ((len = inputStream.read(buffer)) > -1) {
      baos.write(buffer, 0, len);
    }

    baos.flush();

    return new ByteArrayInputStream(baos.toByteArray());
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

  @VisibleForTesting
  ResourceResponse readFileAtLocation(ResourceRequest resourceRequest, String location) {
    HttpGet httpGet = new HttpGet(location);
    ResponseHandler<ResourceResponse> responseHandler =
        response -> {
          int status = response.getStatusLine().getStatusCode();

          if (status == HttpStatus.SC_OK) {
            InputStream content = response.getEntity().getContent();
            String mimeType = response.getEntity().getContentType().getValue();
            long size = response.getEntity().getContentLength();

            Metadata metadata = resourceRequest.getMetadata();
            String id = metadata.getId();
            String name = String.format("%s_%s", id, metadata.getResourceModified().getTime());
            URI uri = resourceRequest.getMetadata().getResourceUri();
            InputStream resourceContentStream = cloneInputStream(content);

            Resource resource =
                new ResourceImpl(
                    id, name, uri, null, resourceContentStream, mimeType, size, metadata);

            return new ResourceResponseImpl(resource);
          } else {
            throw new ReplicationException(
                String.format("Request failed with status code: %d", status));
          }
        };

    return sendHttpRequest(httpGet, responseHandler);
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
  boolean writeFileToLocation(CreateStorageRequest createStorageRequest, String location)
      throws IOException {

    if (createStorageRequest.getResources().isEmpty()
        || createStorageRequest.getResources().get(0) == null) {
      throw new ReplicationException("No compatible Resource was found.");
    }

    String locationUri;
    try {
      URIBuilder uriBuilder = new URIBuilder(location);
      uriBuilder.setParameter(HTTP_CREATE_OVERWRITE_KEY, "false");
      locationUri = uriBuilder.build().toString();
      LOGGER.debug("The location being written to is: {}", locationUri);
    } catch (URISyntaxException e) {
      throw new ReplicationException(
          "Failed to write file. The location URI has syntax errors. {}", e);
    }

    HttpPut httpPut = new HttpPut(locationUri);

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

    // only a single resource is supported at this time and is the reason for always retrieving from
    // index zero
    Resource resource = createStorageRequest.getResources().get(0);

    final File tempFile = File.createTempFile(formatFilename(createStorageRequest), ".tmp");
    tempFile.deleteOnExit();

    boolean successful;
    try (FileOutputStream out = new FileOutputStream(tempFile)) {
      IOUtils.copy(resource.getInputStream(), out);
      FileEntity entity = new FileEntity(tempFile, ContentType.create(resource.getMimeType()));
      httpPut.setEntity(entity);

      successful = sendHttpRequest(httpPut, responseHandler);
    } finally {
      tempFile.delete();
    }

    return successful;
  }

  /**
   * Converts an {@link UpdateStorageRequest} to a {@link CreateStorageRequest} by simply extracting
   * the contained list of {@link Resource} and creating a new object from it.
   *
   * @param updateStorageRequest - the {@link UpdateStorageRequest} to convert
   * @return The new {@link CreateStorageRequest} with the same {@link Resource}s
   */
  private CreateStorageRequest convertToCreateStorageRequest(
      UpdateStorageRequest updateStorageRequest) {
    if (updateStorageRequest.getResources().isEmpty()
        || updateStorageRequest.getResources().get(0) == null) {
      throw new ReplicationException(
          "Unable to convert storage request. No compatible Resource was found.");
    }

    return new CreateStorageRequestImpl(updateStorageRequest.getResources());
  }

  @Override
  public boolean updateResource(UpdateStorageRequest updateStorageRequest) {
    try {
      CreateStorageRequest createStorageRequest =
          convertToCreateStorageRequest(updateStorageRequest);
      return createResource(createStorageRequest);
    } catch (ReplicationException e) {
      LOGGER.error("Unable to update resource.", e);
    }
    return false;
  }

  @Override
  public void close() throws IOException {
    client.close();
  }
}
