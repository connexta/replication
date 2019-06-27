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
package com.connexta.ion.replication.adapters.ddf.rest;

import com.connexta.ion.replication.adapters.ddf.csw.MetacardMarshaller;
import com.connexta.ion.replication.api.AdapterException;
import com.connexta.ion.replication.api.data.Metadata;
import com.connexta.ion.replication.api.data.Resource;
import com.connexta.ion.replication.data.ResourceImpl;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A wrapper around the {@link WebClient}. */
public class DdfRestClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdfRestClient.class);

  private static final String CONTENT_DISPOSITION = "Content-Disposition";

  private final WebClient webClient;

  public DdfRestClient(WebClient webClient) {
    this.webClient = webClient;
  }

  /**
   * Post new metadata to the DDF REST endpoint.
   *
   * @param metadata the {@link Metadata} to post
   * @return the response
   */
  public Response post(Metadata metadata) {
    String rawMetadata = MetacardMarshaller.marshal(metadata);
    webClient.type(MediaType.APPLICATION_XML);
    return webClient.post(rawMetadata);
  }

  /**
   * Put updated metadata on the DDF REST endpoint.
   *
   * @param metadata the {@link Metadata} to put
   * @return the response
   */
  public Response put(Metadata metadata) {
    webClient.path(metadata.getId());
    webClient.type(MediaType.APPLICATION_XML);
    String rawMetadata = MetacardMarshaller.marshal(metadata);
    Response response = webClient.put(rawMetadata);
    webClient.back(true); // reset path
    return response;
  }

  /**
   * Post new content to the DDF REST endpoint.
   *
   * @param resource the {@link Resource} to post
   * @return the response
   */
  public Response post(Resource resource) {
    try {
      MultipartBody multipartBody = createBody(resource);
      webClient.type(MediaType.MULTIPART_FORM_DATA);
      return webClient.post(multipartBody);
    } catch (Exception e) {
      LOGGER.debug("Post error", e);
      return null;
    }
  }

  /**
   * Put updated content on the DDF REST endpoint.
   *
   * @param resource the updated {@link Resource}
   * @return the response
   */
  public Response put(Resource resource) {
    webClient.path(resource.getId());
    webClient.type(MediaType.MULTIPART_FORM_DATA);
    MultipartBody multipartBody = createBody(resource);
    Response response = webClient.put(multipartBody);
    webClient.back(true);
    return response;
  }

  public Response delete(String id) {
    webClient.path(id);
    Response response = webClient.delete();
    webClient.back(true);
    return response;
  }

  public Resource get(Metadata metadata) {
    webClient
        .path(metadata.getId())
        .query("transform", "resource")
        .accept(MediaType.APPLICATION_OCTET_STREAM);
    Response response = webClient.get();
    if (!response.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
      throw new AdapterException(
          "Failed to retrieve resource. Received status code: " + response.getStatus());
    }
    String contentDisposition = response.getHeaderString(CONTENT_DISPOSITION);
    if (contentDisposition == null) {
      throw new AdapterException("Failed to retrieve resource. Content-Disposition missing");
    }
    String name = contentDisposition.split("filename=")[1].replaceAll("\"", "");

    return new ResourceImpl(
        metadata.getId(),
        name,
        webClient.getCurrentURI(),
        null,
        (InputStream) response.getEntity(),
        response.getMediaType().toString(),
        metadata.getResourceSize(),
        metadata);
  }

  private MultipartBody createBody(Resource resource) {
    List<Attachment> attachments = new ArrayList<>();
    attachments.add(createParseResourceAttachment(resource));
    attachments.add(createMetadataAttachment(resource));
    return new MultipartBody(attachments);
  }

  private Attachment createParseResourceAttachment(Resource resource) {
    ContentDisposition contentDisposition = createResourceContentDisposition(resource.getName());

    MultivaluedMap<String, String> headers = new MetadataMap<>(false, true);
    headers.putSingle(CONTENT_DISPOSITION, contentDisposition.toString());
    headers.putSingle("Content-ID", "parse.resource");
    headers.putSingle("Content-Type", resource.getMimeType());

    return new Attachment(resource.getInputStream(), headers);
  }

  private Attachment createMetadataAttachment(Resource resource) {
    ContentDisposition contentDisposition = createMetadataContentDisposition(resource.getId());

    InputStream bais =
        new ByteArrayInputStream(
            MetacardMarshaller.marshal(resource.getMetadata()).getBytes(StandardCharsets.UTF_8));

    return new Attachment("parse.metadata", bais, contentDisposition);
  }

  private ContentDisposition createResourceContentDisposition(String resourceFileName) {
    String contentDisposition =
        String.format("form-data; name=parse.resource; filename=%s", resourceFileName);
    return new ContentDisposition(contentDisposition);
  }

  private ContentDisposition createMetadataContentDisposition(String metadataFilename) {
    String contentDisposition =
        String.format("form-data; name=parse.metadata; filename=%s", metadataFilename);
    return new ContentDisposition(contentDisposition);
  }
}
