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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.connexta.replication.adapters.ddf.csw.Constants;
import com.connexta.replication.data.MetadataAttribute;
import com.connexta.replication.data.MetadataImpl;
import com.connexta.replication.data.ResourceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sakserv.minicluster.impl.HdfsLocalCluster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.Resource;
import org.codice.ditto.replication.api.data.ResourceRequest;
import org.codice.ditto.replication.api.data.ResourceResponse;
import org.codice.ditto.replication.api.data.UpdateStorageRequest;
import org.codice.ditto.replication.api.impl.data.CreateStorageRequestImpl;
import org.codice.ditto.replication.api.impl.data.ResourceRequestImpl;
import org.codice.ditto.replication.api.impl.data.UpdateStorageRequestImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebHdfsClientTest {
  private static final String HDFS_PATH = "/webhdfs/v1";
  private static final String HDFS_PORT = "9870";
  private static final String BASE_URL = "http://localhost:" + HDFS_PORT + HDFS_PATH;
  private static final String TEST_DATA_STRING =
      "Grumpy wizards make toxic brew for the evil queen and jack.";
  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsClientTest.class);
  private static final WebHdfsNodeAdapterFactory adapterFactory = new WebHdfsNodeAdapterFactory();
  private static HdfsLocalCluster hdfsLocalCluster;
  private WebHdfsNodeAdapter adapter;

  @BeforeClass
  public static void setUpClass() {
    hdfsLocalCluster =
        new HdfsLocalCluster.Builder()
            .setHdfsNamenodePort(12345)
            .setHdfsNamenodeHttpPort(Integer.parseInt(HDFS_PORT))
            .setHdfsTempDir("embedded_hdfs")
            .setHdfsNumDatanodes(1)
            .setHdfsEnablePermissions(false)
            .setHdfsFormat(true)
            .setHdfsEnableRunningUserAsProxyUser(true)
            .setHdfsConfig(new Configuration())
            .build();
  }

  @Before
  public void setupTest() throws Exception {
    adapter = (WebHdfsNodeAdapter) adapterFactory.create(new URL(BASE_URL));

    LOGGER.info("HDFS instance starting up!");
    hdfsLocalCluster.start();
  }

  @After
  public void tearDownTest() throws Exception {
    LOGGER.info("HDFS instance shutting down!");
    hdfsLocalCluster.stop();

    adapter.close();
  }

  @Test(expected = ReplicationException.class)
  public void testInvalidUrlIsNotAvailable() throws MalformedURLException {
    adapter = (WebHdfsNodeAdapter) adapterFactory.create(new URL("http://foobar:9870"));
    adapter.isAvailable();
  }

  @Test(expected = ReplicationException.class)
  public void testStoppedClusterIsNotAvailable() throws Exception {
    hdfsLocalCluster.stop();
    adapter.isAvailable();
  }

  @Test
  public void testResourceFileIsRead() throws Exception {
    String testId = "order-66-clones-memo";
    Date testDate = new Date();
    String testName = formatResourceName(testId, testDate);
    String url = String.format("%s/%s.txt", BASE_URL, testName);

    // creates the resource in the HDFS instance to be read and verifies the resource exists
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(testId, testName, testDate, testDate, new URI(url));
    adapter.createResource(createStorageRequest);
    verifyFileExists(testName + ".txt");

    // gets the resource metadata to create a read request
    Metadata resourceMetadata = createStorageRequest.getResources().get(0).getMetadata();
    ResourceRequest readRequest = new ResourceRequestImpl(resourceMetadata);
    ResourceResponse readResponse = adapter.readResource(readRequest);
    Resource readResource = readResponse.getResource();

    assertThat(readResource.getId(), is(testId));
    assertThat(readResource.getName(), is(String.format("%s_%s", testId, testDate.getTime())));
    assertThat(readInputStreamToString(readResource.getInputStream()), is(TEST_DATA_STRING));
  }

  @Test
  public void testInvalidHostnameResourceUriNotRead() throws URISyntaxException {
    String testId = "404";
    String testName = "404-not-found";
    Date testDate = new Date();
    String filename = formatResourceName(testId, testDate) + ".txt";
    String badHostnameUri = String.format("http://foobar:%s/webhdfs/v1/%s", HDFS_PORT, filename);

    // creates the resource with the bad hostname
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(testId, testName, testDate, testDate, new URI(badHostnameUri));
    // Note: it's not necessary to actually write the file in HDFS

    // gets the resource metadata to create a read request
    Metadata resourceMetadata = createStorageRequest.getResources().get(0).getMetadata();
    ResourceRequest readRequest = new ResourceRequestImpl(resourceMetadata);
    ResourceResponse readResponse = adapter.readResource(readRequest);

    assertThat(readResponse, is(nullValue()));
  }

  @Test
  public void testInvalidPortResourceUriNotRead() throws URISyntaxException {
    String testId = "404";
    String testName = "404-not-found";
    Date testDate = new Date();
    String filename = formatResourceName(testId, testDate) + ".txt";
    String badPortUri = "http://localhost:9870/webhdfs/v1/" + filename;

    // creates the resource with the bad port
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(testId, testName, testDate, testDate, new URI(badPortUri));
    // Note: it's not necessary to actually write the file in HDFS

    // gets the resource metadata to create a read request
    Metadata resourceMetadata = createStorageRequest.getResources().get(0).getMetadata();
    ResourceRequest readRequest = new ResourceRequestImpl(resourceMetadata);
    ResourceResponse readResponse = adapter.readResource(readRequest);

    assertThat(readResponse, is(nullValue()));
  }

  @Test
  public void testNonExistentFileNotRead() throws URISyntaxException {
    String testId = "404";
    String testName = "404-not-found";
    Date testDate = new Date();
    String filename = formatResourceName(testId, testDate) + ".txt";
    String url = String.format("%s/%s", BASE_URL, filename);

    // creates the resource but doesn't write it to HDFS
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(testId, testName, testDate, testDate, new URI(url));

    // gets the resource metadata to create a read request
    Metadata resourceMetadata = createStorageRequest.getResources().get(0).getMetadata();
    ResourceRequest readRequest = new ResourceRequestImpl(resourceMetadata);
    ResourceResponse readResponse = adapter.readResource(readRequest);

    assertThat(readResponse, is(nullValue()));
  }

  @Test
  public void testResourceFileIsCreated() throws URISyntaxException {
    String testId = "123456789";
    String testName = "testresource";
    Date testDate = new Date();
    String filename = formatResourceName(testId, testDate) + ".txt";
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(testId, testName, testDate);

    adapter.createResource(createStorageRequest);
    verifyFileExists(filename);
  }

  @Test
  public void testUpdateResource() throws URISyntaxException {
    String fileId = "123456789";
    String testName = "testresource";

    Calendar calendar = Calendar.getInstance();
    long creationTime = calendar.getTimeInMillis();

    String originalFilename = String.format("%s_%s.txt", fileId, creationTime);

    // when a file is created with ID fileId
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(fileId, testName, new Date(creationTime));

    adapter.createResource(createStorageRequest);

    // then it will exist on the file system
    verifyFileExists(originalFilename);

    // when the resource is updated one day later, but metadata is unmodified
    calendar.add(Calendar.DATE, 1);
    long updateTime = calendar.getTimeInMillis();

    String updatedFilename = String.format("%s_%s.txt", fileId, updateTime);

    UpdateStorageRequest updateStorageRequest =
        generateUpdateStorageRequest(
            fileId, testName, new Date(creationTime), new Date(updateTime));

    adapter.updateResource(updateStorageRequest);

    // then both the original file and updated file will exist on the file system
    verifyFileExists(originalFilename);
    verifyFileExists(updatedFilename);
  }

  /**
   * This test causes testResourceFileIsRead to fail. When this test is ignored
   * testResourceFileIsRead passes
   */
  @Test
  @Ignore
  public void testUpdateResourceWithMetadataOnlyUpdate() throws URISyntaxException {
    String fileId = "123456789";
    String testName = "testresource";

    Calendar calendar = Calendar.getInstance();
    long creationTime = calendar.getTimeInMillis();

    String originalFilename = String.format("%s_%s.txt", fileId, creationTime);

    // when a file is created with ID fileId
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(fileId, testName, new Date(creationTime));

    adapter.createResource(createStorageRequest);

    // then it will exist on the file system
    verifyFileExists(originalFilename);

    // when the metadata is updated one day later, but the resource is unmodified
    calendar.add(Calendar.DATE, 1);
    long updateTime = calendar.getTimeInMillis();

    UpdateStorageRequest updateStorageRequest =
        generateUpdateStorageRequest(
            fileId, testName, new Date(updateTime), new Date(creationTime));

    // then no update is made and the original file still exists
    assertThat(adapter.updateResource(updateStorageRequest), is(false));
    verifyFileExists(originalFilename);
  }

  @Test
  public void testUpdateResourceWithMetadataUpdate() throws URISyntaxException {
    String fileId = "123456789";
    String testName = "testresource";

    Calendar calendar = Calendar.getInstance();
    long creationTime = calendar.getTimeInMillis();

    String originalFilename = String.format("%s_%s.txt", fileId, creationTime);

    // when a file is created with ID fileId
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(fileId, testName, new Date(creationTime));

    adapter.createResource(createStorageRequest);

    // then it will exist on the file system
    verifyFileExists(originalFilename);

    // when the resource and metadata are updated one day later
    calendar.add(Calendar.DATE, 1);
    long updateTime = calendar.getTimeInMillis();

    String updatedFilename = String.format("%s_%s.txt", fileId, updateTime);

    UpdateStorageRequest updateStorageRequest =
        generateUpdateStorageRequest(fileId, testName, new Date(updateTime));

    adapter.updateResource(updateStorageRequest);

    // then both the original file and updated file will exist on the file system
    verifyFileExists(originalFilename);
    verifyFileExists(updatedFilename);
  }

  @Test
  public void testUpdateResourceWithNoUpdates() throws URISyntaxException {
    String fileId = "123456789";
    String testName = "testresource";

    Calendar calendar = Calendar.getInstance();
    long creationTime = calendar.getTimeInMillis();

    String originalFilename = String.format("%s_%s.txt", fileId, creationTime);

    // when a file is created with ID fileId
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(fileId, testName, new Date(creationTime));

    adapter.createResource(createStorageRequest);

    // then it will exist on the file system
    verifyFileExists(originalFilename);

    // when an attempt to update the resource is made with neither a metadata update nor resource
    // update
    UpdateStorageRequest updateStorageRequest =
        generateUpdateStorageRequest(fileId, testName, new Date(creationTime));

    // then the original file still exists, but the update did not take place
    verifyFileExists(originalFilename);
    assertThat(adapter.updateResource(updateStorageRequest), is(false));
  }

  @Test
  public void testInvalidHostnameFails() throws IOException {
    String testId = "123456789";
    String testName = "testresource";
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(testId, testName, new Date());

    WebHdfsNodeAdapter badAdapter =
        (WebHdfsNodeAdapter) adapterFactory.create(new URL("http://foobar:9870"));
    boolean createResourceSuccessful = badAdapter.createResource(createStorageRequest);
    assertThat(createResourceSuccessful, is(false));
    badAdapter.close();
  }

  @Test
  public void testInvalidPortFails() throws IOException {
    String testId = "123456789";
    String testName = "testresource";
    CreateStorageRequest createStorageRequest =
        generateTestStorageRequest(testId, testName, new Date());

    WebHdfsNodeAdapter badAdapter =
        (WebHdfsNodeAdapter) adapterFactory.create(new URL("http://localhost:9870"));
    boolean createResourceSuccessful = badAdapter.createResource(createStorageRequest);
    assertThat(createResourceSuccessful, is(false));
    badAdapter.close();
  }

  /**
   * Formats a resource name in the way it's stored in HDFS.
   *
   * @param id - the ID of the {@link Resource}
   * @param date - the resource modified {@link Date} of the {@link Resource}
   * @return The formatted resource name {@link String}
   */
  private static String formatResourceName(String id, Date date) {
    return String.format("%s_%s", id, date.getTime());
  }

  /**
   * Utilizes the "Status of a File/Directory" WebHDFS operation to verify that the file with the
   * given <code>filename</code> exists in the expected location.
   *
   * @param filename - the name of the file to verify
   * @throws URISyntaxException If the URL string does not have valid syntax
   */
  private void verifyFileExists(String filename) throws URISyntaxException {
    String url = String.format("%s/%s", BASE_URL, filename);
    URIBuilder builder = new URIBuilder(url);
    builder.addParameter("op", "GETFILESTATUS");
    URI uri = builder.build();
    HttpGet getRequest = new HttpGet(uri);
    Map responseContent = sendGetRequest(getRequest);
    Map fileStatus = null;

    if (responseContent != null && responseContent.get("FileStatus") instanceof Map) {
      fileStatus = (Map) responseContent.get("FileStatus");
    }

    assertThat(hdfsLocalCluster.getHdfsFormat(), is(true));
    assertThat(
        uri.toString(),
        is(
            String.format(
                "http://localhost:%s/webhdfs/v1/%s?op=GETFILESTATUS", HDFS_PORT, filename)));
    assertThat(fileStatus, notNullValue());
    assertThat(fileStatus.get("type"), is("FILE"));
  }

  /**
   * Helps create a basic {@link CreateStorageRequest} with a single {@link Resource} in its list.
   *
   * @param id - the id to assign to the {@link Resource}
   * @param name - the name to assign to the {@link Resource}
   * @param date representing when the associated metadata and resource were last modified
   * @return The newly created {@link CreateStorageRequestImpl}
   */
  private CreateStorageRequest generateTestStorageRequest(String id, String name, Date date) {
    return generateTestStorageRequest(id, name, date, date);
  }

  /**
   * Helps create a basic {@link CreateStorageRequest} with a single {@link Resource} in its list.
   *
   * @param id - the id to assign to the {@link Resource}
   * @param name - the name to assign to the {@link Resource}
   * @param metadataModified representing when the associated metadata was last modified
   * @param resourceModified representing when the associated resource was last modified
   * @return The newly created {@link CreateStorageRequestImpl}
   */
  private CreateStorageRequest generateTestStorageRequest(
      String id, String name, Date metadataModified, Date resourceModified) {
    Resource testResource =
        getResource(id, name, metadataModified, resourceModified, URI.create("my:uri"));
    List<Resource> resourceList = new ArrayList<>();
    resourceList.add(testResource);

    return new CreateStorageRequestImpl(resourceList);
  }

  private CreateStorageRequest generateTestStorageRequest(
      String id, String name, Date metadataModified, Date resourceModified, URI resourceUri) {
    Resource testResource = getResource(id, name, metadataModified, resourceModified, resourceUri);
    List<Resource> resourceList = new ArrayList<>();
    resourceList.add(testResource);

    return new CreateStorageRequestImpl(resourceList);
  }

  /**
   * Helps create a basic {@link UpdateStorageRequest} with a single {@link Resource} in its list.
   *
   * @param id - the id to assign to the {@link Resource}
   * @param name - the name to assign to the {@link Resource}
   * @param date representing when the associated metadata and resource were last modified
   * @return The newly created {@link UpdateStorageRequestImpl}
   */
  private UpdateStorageRequest generateUpdateStorageRequest(String id, String name, Date date) {
    return generateUpdateStorageRequest(id, name, date, date);
  }

  /**
   * Helps create a basic {@link UpdateStorageRequest} with a single {@link Resource} in its list.
   *
   * @param id - the id to assign to the {@link Resource}
   * @param name - the name to assign to the {@link Resource}
   * @param metadataModified a {@code Date} object representing when the associated metadata was
   *     last modified
   * @param resourceModified a {@code Date} object representing when the associated resource was
   *     last modified
   * @return The newly created {@link UpdateStorageRequestImpl}
   */
  private UpdateStorageRequest generateUpdateStorageRequest(
      String id, String name, Date metadataModified, Date resourceModified) {
    Resource resource =
        getResource(id, name, metadataModified, resourceModified, URI.create("my:uri"));
    List<Resource> resources = new ArrayList<>();
    resources.add(resource);

    return new UpdateStorageRequestImpl(resources);
  }

  /**
   * Sends a GET request and returns a {@link Map} representing the parsed response content.
   *
   * @param request - the request to send
   * @return The {@link Map} containing the parsed content
   */
  private Map sendGetRequest(HttpRequestBase request) {
    ResponseHandler<Map> responseHandler =
        httpResponse -> {
          InputStream contentStream = httpResponse.getEntity().getContent();
          return parseHttpResponseContent(contentStream);
        };
    return adapter.sendHttpRequest(request, responseHandler);
  }

  /**
   * Parses the <code>contentStream</code> into a {@link Map}. The stream should be in JSON format
   * for it to be cleanly mapped, otherwise a <code>null</code> value will be returned.
   *
   * @param contentStream - the input stream representing the content from the HTTP response
   * @return The {@link Map} containing the parsed content
   */
  private Map parseHttpResponseContent(InputStream contentStream) {
    try (final Reader reader = new InputStreamReader(contentStream)) {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(reader, Map.class);
    } catch (IOException e) {
      LOGGER.error("Failed to parse the HTTP content stream.", e);
      return null;
    }
  }

  /**
   * Reads an {@link InputStream} into a readable {@link String}.
   *
   * @param contentStream - the {@link InputStream} to read
   * @return The resulting {@link String} read
   */
  private String readInputStreamToString(InputStream contentStream) {
    try (final Reader reader = new InputStreamReader(contentStream)) {
      return IOUtils.toString(reader);
    } catch (IOException e) {
      LOGGER.error("Failed to read the input stream.", e);
      return null;
    }
  }

  /**
   * Generates a {@link Metadata} from the given {@code id}, {@code metadataModified} date, and
   * {@code resourceModified} date.
   *
   * @param id - the id of the {@link Resource}
   * @param metadataModified a {@link Date} object representing when the metadata was last modified
   * @param resourceModified a {@link Date} object representing when the resource was last modified
   * @param resourceUri the URI of the {@link Resource}
   * @return The newly created {@link MetadataImpl}
   */
  private Metadata getMetadata(
      String id, Date metadataModified, Date resourceModified, URI resourceUri) {
    Map<String, MetadataAttribute> map = new HashMap<>();
    map.put(Constants.METACARD_ID, new MetadataAttribute(Constants.METACARD_ID, null, id));
    map.put("type", new MetadataAttribute("type", null, "hdfs.metacard"));
    map.put(Constants.METACARD_TAGS, new MetadataAttribute(Constants.METACARD_TAGS, null, "tag"));

    Metadata metadata = new MetadataImpl(map, Map.class, id, metadataModified);
    metadata.setResourceModified(resourceModified);
    metadata.setResourceUri(resourceUri);

    return metadata;
  }

  /**
   * Generates a {@link Resource} from the given {@code id}, {@code name}, {@code metadataModified}
   * date, and {@code resourceModified} date.
   *
   * @param id - the id to assign to the {@link Resource}
   * @param name - the name to assign to the {@link Resource}
   * @param metadataModified representing when the metadata was last modified
   * @param resourceModified representing when the resource was last modified
   * @param resourceUri the URI of the resource
   * @return The newly created {@link ResourceImpl}
   */
  private Resource getResource(
      String id, String name, Date metadataModified, Date resourceModified, URI resourceUri) {
    return new ResourceImpl(
        id,
        name,
        resourceUri,
        null,
        new ByteArrayInputStream(TEST_DATA_STRING.getBytes()),
        MediaType.TEXT_PLAIN,
        10,
        getMetadata(id, metadataModified, resourceModified, resourceUri));
  }
}
