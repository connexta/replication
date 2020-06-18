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
import com.connexta.replication.data.QueryResponseImpl;
import com.connexta.replication.data.ResourceImpl;
import com.connexta.replication.data.ResourceResponseImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import ddf.catalog.data.types.Core;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
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
  private static final String HTTP_OPERATION_LIST_STATUS_BATCH = "LISTSTATUS_BATCH";
  private static final String HTTP_OPERATION_CREATE = "CREATE";
  private static final String HTTP_OPERATION_OPEN = "OPEN";

  private static final String HTTP_FILE_SYSTEM_ACTION_KEY = "fsaction";
  private static final String HTTP_FILE_SYSTEM_ACTION_ALL = "rwx";

  private static final String HTTP_START_AFTER_KEY = "startAfter";

  private static final String HTTP_NO_REDIRECT_KEY = "noredirect";
  private static final String HTTP_CREATE_OVERWRITE_KEY = "overwrite";

  private static final String METADATA_ATTRIBUTE_TYPE_STRING = "string";
  private static final String METADATA_ATTRIBUTE_TYPE_DATE = "date";

  private static final int UUID_VERSION_INDEX = 14;

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
    List<String> failedItemIds = queryRequest.getFailedItemIds();

    if (!failedItemIds.isEmpty()) {
      LOGGER.info(String.format("Found failed item IDs: %s", failedItemIds));
    }

    List<FileStatus> filesToReplicate =
        getFilesToReplicate(failedItemIds, queryRequest.getModifiedAfter());

    filesToReplicate.sort(Comparator.comparing(FileStatus::getModificationTime));

    List<Metadata> results = new ArrayList<>();

    for (FileStatus file : filesToReplicate) {
      results.add(createMetadata(file));
    }

    return new QueryResponseImpl(results);
  }

  /**
   * Creates {@link MetadataAttribute}s to use in creating a {@link MetadataImpl} object for a file
   * being replicated.
   *
   * @param fileStatus information about the file being replicated
   * @return a {@link MetadataImpl} object for the file being replicated
   */
  @VisibleForTesting
  Metadata createMetadata(FileStatus fileStatus) {

    Map<String, MetadataAttribute> metadataAttributes = new HashMap<>();

    String fileUrl = getWebHdfsUrl().toString() + fileStatus.getPathSuffix();
    LOGGER.debug("Creating Metadata object from file at: {}", fileUrl);

    Date modificationTime = fileStatus.getModificationTime();
    String id = getVersion4Uuid(fileUrl, modificationTime);

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
            Collections.singletonList(fileStatus.getPathSuffix()),
            Collections.emptyList());
    metadataAttributes.put(Core.TITLE, titleAttribute);

    MetadataAttribute createdAttribute =
        new MetadataAttribute(
            Core.CREATED,
            METADATA_ATTRIBUTE_TYPE_DATE,
            Collections.singletonList(modificationTime.toString()),
            Collections.emptyList());
    metadataAttributes.put(Core.CREATED, createdAttribute);

    MetadataAttribute modifiedAttribute =
        new MetadataAttribute(
            Core.MODIFIED,
            METADATA_ATTRIBUTE_TYPE_DATE,
            Collections.singletonList(modificationTime.toString()),
            Collections.emptyList());
    metadataAttributes.put(Core.MODIFIED, modifiedAttribute);

    MetadataAttribute resourceUriAttribute =
        new MetadataAttribute(
            Core.RESOURCE_URI,
            METADATA_ATTRIBUTE_TYPE_STRING,
            Collections.singletonList(fileUrl),
            Collections.emptyList());
    metadataAttributes.put(Core.RESOURCE_URI, resourceUriAttribute);

    MetadataAttribute lengthAttribute =
        new MetadataAttribute(
            Core.RESOURCE_SIZE,
            METADATA_ATTRIBUTE_TYPE_STRING,
            Collections.singletonList(String.valueOf(fileStatus.getLength())),
            Collections.emptyList());
    metadataAttributes.put(Core.RESOURCE_SIZE, lengthAttribute);

    Metadata metadata = new MetadataImpl(metadataAttributes, Map.class, id, modificationTime);

    try {
      metadata.setResourceUri(new URI(fileUrl));
      metadata.setResourceModified(modificationTime);
      metadata.setResourceSize(fileStatus.getLength());

      return metadata;
    } catch (URISyntaxException e) {
      throw new ReplicationException("Unable to create a URI from the file's URL.", e);
    }
  }

  /**
   * Using the file URL and modification time as input, a version-3 UUID is generated which is then
   * modified to appear as a version-4 UUID by changing the version number in the UUID from 3 to 4.
   * The URL is not guaranteed to be unique if a file is removed and replaced by another file with
   * the same URL. Because of this, the URL is combined with the modification time to serve as input
   * for UUID generation.
   *
   * @param fileUrl the full URL of the file
   * @param modificationTime modification time of the file
   * @return a {@code String} representing a version-4 UUID
   */
  @VisibleForTesting
  String getVersion4Uuid(String fileUrl, Date modificationTime) {
    String input = String.format("%s_%d", fileUrl, modificationTime.getTime());
    String version3Uuid = UUID.nameUUIDFromBytes(input.getBytes()).toString();
    StringBuilder version4Uuid = new StringBuilder(version3Uuid);
    version4Uuid.setCharAt(UUID_VERSION_INDEX, '4');

    LOGGER.debug("UUID for {} is {}", fileUrl, version4Uuid);
    return version4Uuid.toString();
  }

  /**
   * Formulates a GET request to send to the HDFS instance. Repeats the request to retrieve
   * additional results if the file system contains more results than can be returned in a single
   * response.
   *
   * @param failedItemIds the list of IDs that failed to be created
   * @param filterDate specifies a point in time such that only files more recent are returned
   * @return a resulting {@code List} of {@link FileStatus} objects meeting the criteria
   */
  @VisibleForTesting
  List<FileStatus> getFilesToReplicate(List<String> failedItemIds, @Nullable Date filterDate) {

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
              LOGGER.debug("Response contains status code: {}", statusCode);

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

                return getRelevantFiles(results, failedItemIds, filterDate);

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

    LOGGER.info("Identified {} files to replicate.", filesToReplicate.size());
    return filesToReplicate;
  }

  /**
   * Returns the files meeting the criteria for replication by only including elements that: 1) are
   * of type FILE or 2) are one of the failed IDs or 3) have a modification time after the filter
   * date
   *
   * @param files a {@code List} of all {@link FileStatus} objects returned by the GET request
   * @param failedItemIds a {@code List} of the IDs that failed to be created
   * @param filterDate specifies a point in time such that only files more recent are included; this
   *     value will be set to {@code null} during the first execution of replication
   * @return a resulting {@code List} of {@link FileStatus} objects meeting the criteria
   */
  @VisibleForTesting
  List<FileStatus> getRelevantFiles(
      List<FileStatus> files, List<String> failedItemIds, @Nullable Date filterDate) {
    List<FileStatus> results = new ArrayList<>();

    // this map contains the files that are after the filter date with UUID as keys and the
    // associated FileStatus as values
    Map<String, FileStatus> oldFiles = new HashMap<>();

    for (FileStatus file : files) {
      // skip over any directories
      if (file.isDirectory()) {
        continue;
      }

      // files modified after the filter date should always be added to the returned results
      if (filterDate == null || file.getModificationTime().after(filterDate)) {
        results.add(file);
      } else {
        String fileUrl = getWebHdfsUrl().toString() + file.getPathSuffix();
        String id = getVersion4Uuid(fileUrl, file.getModificationTime());

        oldFiles.put(id, file);
      }
    }

    return addFailedItemsToRelevantFiles(results, oldFiles, failedItemIds);
  }

  /**
   * Iterates through the list of failed IDs to look up the associated {@link FileStatus} in the
   * map.
   *
   * @param relevantFiles - the list of {@link FileStatus} to be returned
   * @param oldFiles - the list of {@link FileStatus} with a modification date after the filter date
   * @param failedItemIds - the list of failed item IDs
   * @return A list of {@link FileStatus} representing the full set of files to be returned
   */
  private List<FileStatus> addFailedItemsToRelevantFiles(
      List<FileStatus> relevantFiles,
      Map<String, FileStatus> oldFiles,
      List<String> failedItemIds) {
    // only need to do the lookup if there are items in the failed ID list
    if (!oldFiles.isEmpty() && !failedItemIds.isEmpty()) {
      for (String failedId : failedItemIds) {
        if (oldFiles.containsKey(failedId)) {
          relevantFiles.add(oldFiles.get(failedId));
        }
      }
    }

    return relevantFiles;
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
