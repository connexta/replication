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

import com.connexta.replication.api.NodeAdapter;
import com.connexta.replication.api.data.CreateRequest;
import com.connexta.replication.api.data.CreateStorageRequest;
import com.connexta.replication.api.data.DeleteRequest;
import com.connexta.replication.api.data.Metadata;
import com.connexta.replication.api.data.QueryRequest;
import com.connexta.replication.api.data.QueryResponse;
import com.connexta.replication.api.data.Resource;
import com.connexta.replication.api.data.ResourceRequest;
import com.connexta.replication.api.data.ResourceResponse;
import com.connexta.replication.api.data.UpdateRequest;
import com.connexta.replication.api.data.UpdateStorageRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

public class IonNodeAdapter implements NodeAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(IonNodeAdapter.class);

  private final URL ionUrl;

  private final RestOperations restOps;

  public IonNodeAdapter(URL ionUrl, RestOperations restOps) {
    this.ionUrl = ionUrl;
    this.restOps = restOps;
  }

  @Override
  public boolean isAvailable() {
    try {
      return restOps.optionsForAllow(this.ionUrl.toString() + "/ingest").contains(HttpMethod.POST);
    } catch (Exception e) {
      LOGGER.debug("Failed to get Ion availability.", e);
      return false;
    }
  }

  @Override
  public String getSystemName() {
    return "ION";
  }

  @Override
  public QueryResponse query(QueryRequest queryRequest) {
    // Ion doesn't support query at this time
    LOGGER.debug("Ion Adapter ignoring query request");
    return ArrayList::new;
  }

  @Override
  public boolean exists(Metadata metadata) {
    LOGGER.debug("Ion Adapter exist returning false as always.");
    return false;
  }

  @Override
  public boolean createRequest(CreateRequest createRequest) {
    LOGGER.debug("Ion doesn't support metadata only creation at this time");
    return true;
  }

  @Override
  public boolean updateRequest(UpdateRequest updateRequest) {
    LOGGER.debug("Ion doesn't support metadata updates at this time");
    return true;
  }

  @Override
  public boolean deleteRequest(DeleteRequest deleteRequest) {
    LOGGER.debug("Ion doesn't delete data");
    return true;
  }

  @Override
  public ResourceResponse readResource(ResourceRequest resourceRequest) {
    LOGGER.debug("Ion Adapter doesn't support resource retrieval at this time");
    return null;
  }

  @Override
  public boolean createResource(CreateStorageRequest createStorageRequest) {
    boolean success = true;

    for (Resource resource : createStorageRequest.getResources()) {
      MultipartBodyBuilder builder = new MultipartBodyBuilder();
      builder.part("file", getResourceEntity(resource));
      builder.part("metacard", getMetadataEntity(resource.getMetadata()));
      builder.part("correlationId", resource.getId());
      MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

      HttpHeaders headers = new HttpHeaders();
      headers.set("Accept-Version", "0.1.0");
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, Object>> requestEntity =
          new HttpEntity(multipartBody, headers);

      LOGGER.debug("Sending create request to ION at {}", ionUrl);
      LOGGER.debug("Request body: {}", requestEntity);
      try {
        ResponseEntity<String> response =
            restOps.exchange(
                this.ionUrl.toString() + "/ingest", HttpMethod.POST, requestEntity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
          LOGGER.debug(
              "Failed to replicate {}. Message: {}", resource.getName(), response.getBody());
          success = false;
        }
      } catch (Exception e) {
        LOGGER.debug("Error sending create request to ION.", e);
        success = false;
      }
    }
    return success;
  }

  @Override
  public boolean updateResource(UpdateStorageRequest updateStorageRequest) {
    LOGGER.debug("Ion doesn't support product updates at this time");
    return true;
  }

  @Override
  public void close() throws IOException {
    // nothing to close
  }

  private HttpEntity getMetadataEntity(Metadata metadata) {
    HttpHeaders metadataHeader = new HttpHeaders();
    metadataHeader.setContentType(MediaType.APPLICATION_XML);
    return new HttpEntity(
        new MultipartInputStreamResource(
            metadata.getId(),
            metadata.getMetadataSize(),
            new ByteArrayInputStream(metadata.getRawMetadata().toString().getBytes())),
        metadataHeader);
  }

  private HttpEntity getResourceEntity(Resource resource) {
    HttpHeaders resourceHeader = new HttpHeaders();
    resourceHeader.setContentType(MediaType.parseMediaType(resource.getMimeType()));
    return new HttpEntity(
        new MultipartInputStreamResource(
            resource.getName(), resource.getSize(), resource.getInputStream()),
        resourceHeader);
  }

  @SuppressWarnings("squid:S2160" /*Super equals/hashCode is sufficient here*/)
  private class MultipartInputStreamResource extends InputStreamResource {

    private final String filename;

    private final long length;

    public MultipartInputStreamResource(String fileName, long length, InputStream stream) {
      super(stream);
      this.filename = fileName;
      this.length = length;
    }

    @Override
    public String getFilename() {
      return this.filename;
    }

    @Override
    public long contentLength() {
      return this.length;
    }
  }
}
