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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.codice.ditto.replication.api.NodeAdapter;
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

public class WebHdfsNodeAdapter implements NodeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsNodeAdapter.class);

  private final URL webHdfsUrl;

  public WebHdfsNodeAdapter(URL webHdfsUrl) {
    this.webHdfsUrl = webHdfsUrl;
  }

  @Override
  public boolean isAvailable() {
    return false;
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

    String location = getLocation();

    return location != null && writeFileToLocation(createStorageRequest, location);
  }

  /**
   * Sends a PUT request with no file data and without following redirects, in order to retrieve the
   * location to write to
   *
   * @return a {@code String} containing the location to write to; returns {@code null} if request
   *     for location was unsuccessful
   */
  private String getLocation() {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(webHdfsUrl.toString());

      List<BasicNameValuePair> params = new ArrayList<>();
      params.add(new BasicNameValuePair("noredirect", "true"));
      httpPost.setEntity(new UrlEncodedFormEntity(params));

      CloseableHttpResponse response = client.execute(httpPost);

      int status = response.getStatusLine().getStatusCode();
      if (status != 200) {
        LOGGER.debug(
            "Failed to successfully retrieve the location to write to.  Status code: {}", status);
        return null;
      }

      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, String> jsonMap =
          objectMapper.readValue(response.getEntity().getContent(), Map.class);

      return jsonMap.get("Location");

    } catch (IOException e) {
      LOGGER.debug("Failed to get location from remote system", e);
      return null;
    }
  }

  /**
   * Sends a PUT request with file data to the specified location
   *
   * @param createStorageRequest - the request to create the resource
   * @param location - the location to write the resource to
   * @return {@code true} if successful, otherwise {@code false}
   */
  private boolean writeFileToLocation(CreateStorageRequest createStorageRequest, String location) {
    List<Resource> resources = createStorageRequest.getResources();
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpPost httpPost = new HttpPost(location);

      Resource resource = resources.get(0);

      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addBinaryBody(
          "file",
          resource.getInputStream(),
          ContentType.create(resource.getMimeType()),
          "file.ext");
      HttpEntity multipart = builder.build();
      httpPost.setEntity(multipart);

      CloseableHttpResponse response = client.execute(httpPost);
      int status = response.getStatusLine().getStatusCode();
      if (status != 201) {
        LOGGER.debug("Failed to replicate.  Status code: {}", status);
        return false;
      }
    } catch (IOException e) {
      LOGGER.debug("Failed to create resource on remote system", e);
      return false;
    }
    return true;
  }

  @Override
  public boolean updateResource(UpdateStorageRequest updateStorageRequest) {
    return false;
  }

  @Override
  public void close() throws IOException {}
}
