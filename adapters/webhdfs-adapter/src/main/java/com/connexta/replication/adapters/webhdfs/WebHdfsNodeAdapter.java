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

import com.connexta.replication.adapters.webhdfs.filesystem.DirectoryListing;
import com.connexta.replication.adapters.webhdfs.filesystem.FileStatus;
import com.connexta.replication.adapters.webhdfs.filesystem.IterativeDirectoryListing;
import com.connexta.replication.data.MetadataAttribute;
import com.connexta.replication.data.MetadataImpl;
import com.connexta.replication.data.QueryRequestImpl;
import com.connexta.replication.data.QueryResponseImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import ddf.catalog.data.types.Core;
import ddf.mime.tika.TikaMimeTypeResolver;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
import org.codice.ditto.replication.api.impl.data.CreateStorageRequestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Interacts with a remote Hadoop instance through the webHDFS REST API */
public class WebHdfsNodeAdapter implements NodeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsNodeAdapter.class);

  private static final String HTTP_OPERATION_KEY = "op";
  private static final String HTTP_OPERATION_CHECK_ACCESS = "CHECKACCESS";
  private static final String HTTP_OPERATION_LIST_STATUS_BATCH = "LISTSTATUS_BATCH";
  private static final String HTTP_OPERATION_CREATE = "CREATE";

  private static final String HTTP_FILE_SYSTEM_ACTION_KEY = "fsaction";
  private static final String HTTP_FILE_SYSTEM_ACTION_ALL = "rwx";

  private static final String HTTP_START_AFTER_KEY = "startAfter";

  private static final String HTTP_NO_REDIRECT_KEY = "noredirect";
  private static final String HTTP_CREATE_OVERWRITE_KEY = "overwrite";

  private static final String METADATA_ATTRIBUTE_TYPE_STRING = "string";
  private static final String METADATA_ATTRIBUTE_TYPE_DATE = "date";

  private final URL webHdfsUrl;

  private final CloseableHttpClient client;

  /**
   * Adapter to interact with a Hadoop instance through the webHDFS REST API
   *
   * @param webHdfsUrl the address of the REST API for the Hadoop instance
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
    LOGGER.debug("Checking access to: {}", getWebHdfsUrl());

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

  /** TEMPORARY METHOD FOR IMPLEMENTATION TESTING */
  public QueryResponse testQuery() {
    Date modifiedAfter = new Date(22222222L);

    QueryRequest queryRequest =
        new QueryRequestImpl("", Collections.emptyList(), Collections.emptyList(), modifiedAfter);
    QueryResponse queryResponse = query(queryRequest);
    return queryResponse;
  }

  @Override
  public QueryResponse query(QueryRequest queryRequest) {

    List<FileStatus> filesToReplicate = getFilesToReplicate(queryRequest.getModifiedAfter());

    filesToReplicate.sort(Comparator.comparing(FileStatus::getModificationTime));

    List<Metadata> results = new ArrayList<>();

    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");

      for (FileStatus file : filesToReplicate) {
        results.add(createMetadata(file, messageDigest));
      }
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("Unable to create unique ID, due to invalid algorithm.", e);
    }
    return new QueryResponseImpl(results);
  }

  private Metadata createMetadata(FileStatus file, MessageDigest messageDigest) {

    Map<String, MetadataAttribute> metadataAttributes = new HashMap<>();

    String fileUrl = String.format("%s/%s", getWebHdfsUrl(), file.getPathSuffix());

    String id = Arrays.toString(messageDigest.digest(file.getPathSuffix().getBytes()));
    MetadataAttribute idAttribute =
        new MetadataAttribute(
            Core.ID,
            METADATA_ATTRIBUTE_TYPE_STRING,
            Collections.singletonList(id),
            Collections.emptyList());
    metadataAttributes.put(Core.ID, idAttribute);

    MetadataAttribute titleAttribute =
        new MetadataAttribute(
            Core.TITLE,
            METADATA_ATTRIBUTE_TYPE_STRING,
            Collections.singletonList(file.getPathSuffix()),
            Collections.emptyList());
    metadataAttributes.put(Core.TITLE, titleAttribute);

    MetadataAttribute createdAttribute =
        new MetadataAttribute(
            Core.CREATED,
            METADATA_ATTRIBUTE_TYPE_DATE,
            Collections.singletonList(file.getModificationTime().toString()),
            Collections.emptyList());
    metadataAttributes.put(Core.CREATED, createdAttribute);

    MetadataAttribute modifiedAttribute =
        new MetadataAttribute(
            Core.MODIFIED,
            METADATA_ATTRIBUTE_TYPE_DATE,
            Collections.singletonList(file.getModificationTime().toString()),
            Collections.emptyList());
    metadataAttributes.put(Core.MODIFIED, modifiedAttribute);

    MetadataAttribute resourceUriAttribute =
        new MetadataAttribute(
            Core.RESOURCE_URI,
            METADATA_ATTRIBUTE_TYPE_STRING,
            Collections.singletonList(fileUrl),
            Collections.emptyList());
    metadataAttributes.put(Core.RESOURCE_URI, resourceUriAttribute);

    Metadata metadata =
        new MetadataImpl(metadataAttributes, Map.class, id, file.getModificationTime());
    try {
      metadata.setResourceUri(new URI(fileUrl));
      return metadata;
    } catch (URISyntaxException e) {
      return null; // ?
    }
  }

  /**
   * Formulates a GET request to send to the HDFS instance. Repeats the request to retrieve
   * additional results if the file system contains additional results to retrieve.
   *
   * @param filterDate specifies a point in time such that older files are excluded
   * @return a resulting {@code List} of {@link FileStatus} objects meeting the criteria
   */
  private List<FileStatus> getFilesToReplicate(Date filterDate) {

    List<FileStatus> filesToReplicate = new ArrayList<>();
    AtomicInteger remainingEntries = new AtomicInteger();
    AtomicReference<String> pathSuffix = new AtomicReference<>();

    do {
      try {
        URIBuilder builder = new URIBuilder(getWebHdfsUrl().toString());
        builder.setParameter(HTTP_OPERATION_KEY, HTTP_OPERATION_LIST_STATUS_BATCH);

        if (pathSuffix.get() != null) {
          builder.setParameter(HTTP_START_AFTER_KEY, pathSuffix.get());
        }

        HttpGet httpGet = new HttpGet(builder.build());

        ResponseHandler<List<FileStatus>> responseHandler =
            response -> {
              int statusCode = response.getStatusLine().getStatusCode();

              if (statusCode == HttpStatus.SC_OK) {
                InputStream content = response.getEntity().getContent();

                ObjectMapper objectMapper = new ObjectMapper();

                IterativeDirectoryListing iterativeDirectoryListing =
                    objectMapper.readValue(content, IterativeDirectoryListing.class);
                DirectoryListing directoryListing = iterativeDirectoryListing.getDirectoryListing();

                remainingEntries.set(directoryListing.getRemainingEntries());

                List<FileStatus> results =
                    directoryListing.getPartialListing().getFileStatuses().getFileStatusList();

                if (remainingEntries.intValue() > 0) {
                  // start after pathSuffix of the last item for the next request
                  pathSuffix.set(results.get(results.size() - 1).getPathSuffix());
                }

                return getRelevantFiles(results, filterDate);

              } else {
                throw new ReplicationException(
                    String.format(
                        "List Status Batch request failed with status code: %d", statusCode));
              }
            };

        filesToReplicate.addAll(sendHttpRequest(httpGet, responseHandler));

      } catch (URISyntaxException e) {
        LOGGER.error("Unable to query, due to invalid URL.", e);
        return filesToReplicate;
      }
    } while (remainingEntries.intValue() > 0);

    return filesToReplicate;
  }

  /**
   * Returns the files relevant for this replication
   *
   * @param files a {@code List} of all {@link FileStatus} objects returned by the GET request
   * @param filterDate specifies a point in time such that older files are excluded
   * @return a resulting {@code List} of {@link FileStatus} objects meeting the criteria
   */
  private List<FileStatus> getRelevantFiles(List<FileStatus> files, Date filterDate) {
    files.removeIf(file -> file.isDirectory() || file.isOlderThan(filterDate));

    return files;
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

    if (createStorageRequest.getResources().isEmpty()
        || createStorageRequest.getResources().get(0) == null) {
      throw new ReplicationException("No compatible Resource was found.");
    }

    String fileUrl = getWebHdfsUrl().toString() + formatFilename(createStorageRequest);
    LOGGER.debug("The complete file URL is: {}", fileUrl);

    URIBuilder builder = new URIBuilder(fileUrl);
    builder
        .setParameter(HTTP_OPERATION_KEY, HTTP_OPERATION_CREATE)
        .setParameter(HTTP_NO_REDIRECT_KEY, "true");
    HttpPut httpPut = new HttpPut(builder.build());

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

    return sendHttpRequest(httpPut, responseHandler);
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

    // only a single resource is supported at this time and is the reason for always retrieving from
    // index zero
    Resource resource = createStorageRequest.getResources().get(0);

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    builder.addBinaryBody(
        "file",
        resource.getInputStream(),
        ContentType.create(resource.getMimeType()),
        formatFilename(createStorageRequest));
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
