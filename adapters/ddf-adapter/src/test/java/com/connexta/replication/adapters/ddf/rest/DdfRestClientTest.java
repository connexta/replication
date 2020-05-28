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
package com.connexta.replication.adapters.ddf.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.adapters.ddf.csw.Constants;
import com.connexta.replication.adapters.ddf.csw.MetacardMarshaller;
import com.connexta.replication.data.MetadataAttribute;
import com.connexta.replication.data.MetadataImpl;
import com.connexta.replication.data.ResourceImpl;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DdfRestClientTest {

  @Mock WebClient webClient;

  private DdfRestClient client;

  @Before
  public void setUp() throws Exception {
    client = new DdfRestClient(webClient);
  }

  @Test
  public void postMetadata() {
    Metadata metadata = getMetadata("123456789");
    client.post(metadata);
    verify(webClient).type(MediaType.APPLICATION_XML);
    verify(webClient).post(MetacardMarshaller.marshal(metadata));
  }

  @Test
  public void putMetadata() {
    Metadata metadata = getMetadata("123456789");
    client.put(metadata);
    verify(webClient).type(MediaType.APPLICATION_XML);
    verify(webClient).path(metadata.getId());
    verify(webClient).put(MetacardMarshaller.marshal(metadata));
  }

  @Test
  public void postResource() throws Exception {
    Resource resource = getResource("123456789", "myresource.bin");
    client.post(resource);
    verify(webClient).type(MediaType.MULTIPART_FORM_DATA);
    ArgumentCaptor<MultipartBody> captor = ArgumentCaptor.forClass(MultipartBody.class);
    verify(webClient).post(captor.capture());
    MultipartBody mpb = captor.getValue();
    Attachment resourceAttachemnt = mpb.getAttachment("parse.resource");
    assertThat(resourceAttachemnt.getContentType().toString(), is(MediaType.TEXT_PLAIN));
    assertThat(
        resourceAttachemnt.getContentDisposition().toString(),
        is(String.format("form-data; name=parse.resource; filename=%s", resource.getName())));
    assertThat(
        IOUtils.toString(
            resourceAttachemnt.getDataHandler().getInputStream(), StandardCharsets.UTF_8),
        is("my-data"));
    Attachment metadataAttachemnt = mpb.getAttachment("parse.metadata");
    assertThat(
        metadataAttachemnt.getContentDisposition().toString(),
        is(String.format("form-data; name=parse.metadata; filename=%s", resource.getId())));
    assertThat(
        IOUtils.toString(
            metadataAttachemnt.getDataHandler().getInputStream(), StandardCharsets.UTF_8),
        is(MetacardMarshaller.marshal(resource.getMetadata())));
  }

  @Test
  public void putResource() throws Exception {
    Resource resource = getResource("123456789", "myresource.bin");
    client.put(resource);
    verify(webClient).type(MediaType.MULTIPART_FORM_DATA);
    ArgumentCaptor<MultipartBody> captor = ArgumentCaptor.forClass(MultipartBody.class);
    verify(webClient).put(captor.capture());
    MultipartBody mpb = captor.getValue();
    Attachment resourceAttachemnt = mpb.getAttachment("parse.resource");
    assertThat(resourceAttachemnt.getContentType().toString(), is(MediaType.TEXT_PLAIN));
    assertThat(
        resourceAttachemnt.getContentDisposition().toString(),
        is(String.format("form-data; name=parse.resource; filename=%s", resource.getName())));
    assertThat(
        IOUtils.toString(
            resourceAttachemnt.getDataHandler().getInputStream(), StandardCharsets.UTF_8),
        is("my-data"));
    Attachment metadataAttachemnt = mpb.getAttachment("parse.metadata");
    assertThat(
        metadataAttachemnt.getContentDisposition().toString(),
        is(String.format("form-data; name=parse.metadata; filename=%s", resource.getId())));
    assertThat(
        IOUtils.toString(
            metadataAttachemnt.getDataHandler().getInputStream(), StandardCharsets.UTF_8),
        is(MetacardMarshaller.marshal(resource.getMetadata())));
  }

  @Test
  public void delete() {
    client.delete("123456789");
    verify(webClient).path("123456789");
    verify(webClient).delete();
  }

  @Test
  public void getResource() {
    Metadata metadata = getMetadata("123456789");
    Response response = mock(Response.class);
    when(response.getHeaderString("Content-Disposition")).thenReturn("filename=myfile.txt");
    StatusType status = mock(StatusType.class);
    when(status.getFamily()).thenReturn(Family.SUCCESSFUL);
    when(response.getStatusInfo()).thenReturn(status);
    when(response.getMediaType()).thenReturn(MediaType.APPLICATION_OCTET_STREAM_TYPE);
    when(response.getLength()).thenReturn(123);
    when(webClient.get()).thenReturn(response);
    when(webClient.getCurrentURI())
        .thenReturn(URI.create("https://host:1234/context/catalog/123456789"));
    when(webClient.path(any(String.class))).thenReturn(webClient);
    when(webClient.accept(any(String.class))).thenReturn(webClient);
    when(webClient.query(any(String.class), any(Object.class))).thenReturn(webClient);
    Resource resource = client.get(metadata);
    verify(webClient).path("123456789");
    verify(webClient).query("transform", "resource");
    verify(webClient).accept(MediaType.APPLICATION_OCTET_STREAM);

    assertThat(resource.getName(), is("myfile.txt"));
    assertThat(resource.getId(), is("123456789"));
    assertThat(resource.getSize(), is(123L));
    assertThat(resource.getMimeType(), is(MediaType.APPLICATION_OCTET_STREAM));
  }

  @Test(expected = AdapterException.class)
  public void getResourceFailed() {
    Metadata metadata = getMetadata("123456789");
    Response response = mock(Response.class);
    when(response.getHeaderString("Content-Disposition")).thenReturn("filename=myfile.txt");
    StatusType status = mock(StatusType.class);
    when(status.getFamily()).thenReturn(Family.SERVER_ERROR);
    when(response.getStatusInfo()).thenReturn(status);
    when(webClient.path(any(String.class))).thenReturn(webClient);
    when(webClient.accept(any(String.class))).thenReturn(webClient);
    when(webClient.query(any(String.class), any(Object.class))).thenReturn(webClient);
    when(webClient.get()).thenReturn(response);
    client.get(metadata);
  }

  private Metadata getMetadata(String id) {
    Map<String, MetadataAttribute> map = new HashMap<>();

    map.put(Constants.METACARD_ID, new MetadataAttribute(Constants.METACARD_ID, null, id));
    map.put("type", new MetadataAttribute("type", null, "ddf.metacard"));
    map.put(Constants.METACARD_TAGS, new MetadataAttribute(Constants.METACARD_TAGS, null, "tag"));

    return new MetadataImpl(map, Map.class, id, new Date());
  }

  private Resource getResource(String id, String name) {
    return new ResourceImpl(
        id,
        name,
        URI.create("my:uri"),
        null,
        new ByteArrayInputStream("my-data".getBytes()),
        MediaType.TEXT_PLAIN,
        10,
        getMetadata(id));
  }
}
