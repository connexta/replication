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
package com.connexta.replication.adapters.ddf;

import ddf.catalog.content.data.ContentItem;
import ddf.catalog.data.BinaryContent;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.MetacardTransformer;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.ddf.endpoints.rest.RESTService;
import org.codice.ddf.security.common.Security;

public class DdfRestClientFactory {

  private static final String CONTENT_DISPOSITION = "Content-Disposition";

  private static final String DEFAULT_REST_ENDPOINT = "/catalog";

  private final MetacardTransformer xmlMetacardTransformer;

  private final ClientFactoryFactory clientFactoryFactory;

  public DdfRestClientFactory(
      ClientFactoryFactory clientFactoryFactory, MetacardTransformer xmlMetacardTransformer) {
    this.xmlMetacardTransformer = xmlMetacardTransformer;
    this.clientFactoryFactory = clientFactoryFactory;
  }

  public DdfRestClient create(String host) {
    final SecureCxfClientFactory<RESTService> restClientFactory =
        clientFactoryFactory.getSecureCxfClientFactory(
            host + DEFAULT_REST_ENDPOINT, RESTService.class);

    Security security = Security.getInstance();
    WebClient webClient =
        security.runAsAdmin(
            () ->
                AccessController.doPrivilegedWithCombiner(
                    (PrivilegedAction<WebClient>)
                        () ->
                            restClientFactory.getWebClientForSubject(security.getSystemSubject())));

    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA);
    webClient.headers(headers);

    webClient.accept(MediaType.APPLICATION_JSON);

    return new DdfRestClient(webClient);
  }

  public class DdfRestClient {

    private final WebClient webClient;

    public DdfRestClient(WebClient webClient) {
      this.webClient = webClient;
    }

    public Response post(ContentItem contentItem) {
      try {
        MultipartBody multipartBody = createBody(contentItem);
        return webClient.post(multipartBody);
      } catch (IOException | CatalogTransformerException e) {
        return null;
      }
    }

    public Response post(ContentItem contentItem, String metacardId) {
      try {
        webClient.replacePath(metacardId);
        MultipartBody multipartBody = createBody(contentItem);
        return webClient.post(multipartBody);
      } catch (IOException | CatalogTransformerException e) {
        return null;
      }
    }

    private MultipartBody createBody(ContentItem item)
        throws IOException, CatalogTransformerException {
      List<Attachment> attachments = new ArrayList<>();
      attachments.add(createParseResourceAttachment(item));
      attachments.add(createMetadataAttachment(item));
      return new MultipartBody(attachments);
    }

    private Attachment createParseResourceAttachment(ContentItem item) throws IOException {
      ContentDisposition contentDisposition = createResourceContentDisposition(item.getFilename());

      MultivaluedMap<String, String> headers = new MetadataMap<>(false, true);
      headers.putSingle(CONTENT_DISPOSITION, contentDisposition.toString());
      headers.putSingle("Content-ID", "parse.resource");
      headers.putSingle("Content-Type", item.getMimeTypeRawData());

      return new Attachment(item.getInputStream(), headers);
    }

    private Attachment createMetadataAttachment(ContentItem item)
        throws CatalogTransformerException {
      ContentDisposition contentDisposition =
          createMetadataContentDisposition(item.getMetacard().getId());

      BinaryContent serializedMetacard =
          xmlMetacardTransformer.transform(item.getMetacard(), Collections.emptyMap());

      return new Attachment(
          "parse.metadata", serializedMetacard.getInputStream(), contentDisposition);
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
}
