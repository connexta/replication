package com.connexta.replication.adapters.webhdfs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.connexta.replication.adapters.ddf.MetacardAttribute;
import com.connexta.replication.adapters.ddf.csw.Constants;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.hadoop.conf.Configuration;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.codice.ditto.replication.api.ReplicationException;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.Resource;
import org.codice.ditto.replication.api.impl.data.CreateStorageRequestImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebHdfsClientTest {
  private static final String HDFS_PORT = "12341";
  private static final String BASE_URL = "http://localhost:" + HDFS_PORT;
  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsClientTest.class);
  private static WebHdfsNodeAdapterFactory adapterFactory = new WebHdfsNodeAdapterFactory();
  private static HdfsLocalCluster hdfsLocalCluster;
  private static WebHdfsNodeAdapter adapter;

  @BeforeClass
  public static void setUpClass() throws Exception {
    LOGGER.info("HDFS instance starting up!");
    adapter = (WebHdfsNodeAdapter) adapterFactory.create(new URL(BASE_URL));
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
    hdfsLocalCluster.start();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    LOGGER.info("HDFS instance shutting down!");
    adapter.close();
    hdfsLocalCluster.stop();
  }

  @Test
  public void testDefaultIsAvailable() {
    assertThat(adapter.isAvailable(), is(true));
  }

  @Test(expected = ReplicationException.class)
  public void testInvalidUrlIsNotAvailable() throws MalformedURLException {
    adapter = (WebHdfsNodeAdapter) adapterFactory.create(new URL("http://foobar:9999"));
    adapter.isAvailable();
  }

  @Test(expected = ReplicationException.class)
  public void testStoppedClusterIsNotAvailable() throws Exception {
    hdfsLocalCluster.stop();
    adapter.isAvailable();
  }

  @Test
  public void testResourceFileIsCreated() throws Exception {
    String testId = "123456789";
    String testName = "testresource";
    String filename = String.format("%s_%s.txt", testId, "946684800000");
    CreateStorageRequest createStorageRequest = generateTestStorageRequest(testId, testName);

    adapter.createResource(createStorageRequest);
    verifyFileExists(filename);
  }

  @Test
  public void testNullResourceNotCreated() {
    List<Resource> resourceList = new ArrayList<>();
    resourceList.add(null);
    CreateStorageRequest createStorageRequest = new CreateStorageRequestImpl(resourceList);

    boolean createResourceSuccessful = adapter.createResource(createStorageRequest);
    assertThat(createResourceSuccessful, is(false));
    // TODO - figure out a good way to track error messages in the logger
  }

  @Test(expected = NullPointerException.class)
  public void testNullResourceListNotCreated() {
    CreateStorageRequest createStorageRequest = new CreateStorageRequestImpl(null);

    boolean createResourceSuccessful = adapter.createResource(createStorageRequest);
    // TODO - figure out a good way to track error messages in the logger
  }

  @Test
  public void testEmptyResourceListNotCreated() {
    List<Resource> resourceList = new ArrayList<>();
    CreateStorageRequest createStorageRequest = new CreateStorageRequestImpl(resourceList);

    boolean createResourceSuccessful = adapter.createResource(createStorageRequest);
    assertThat(createResourceSuccessful, is(false));
    // TODO - figure out a good way to track error messages in the logger
  }

  @Test
  public void testInvalidHostnameFails() throws MalformedURLException {
    String testId = "123456789";
    String testName = "testresource";
    CreateStorageRequest createStorageRequest = generateTestStorageRequest(testId, testName);

    adapter = (WebHdfsNodeAdapter) adapterFactory.create(new URL("http://foobar:12341"));
    boolean createResourceSuccessful = adapter.createResource(createStorageRequest);
    assertThat(createResourceSuccessful, is(false));
    // TODO - figure out a good way to track error messages in the logger
  }

  @Test
  public void testInvalidPortFails() throws MalformedURLException {
    String testId = "123456789";
    String testName = "testresource";
    CreateStorageRequest createStorageRequest = generateTestStorageRequest(testId, testName);

    adapter = (WebHdfsNodeAdapter) adapterFactory.create(new URL("http://localhost:9999"));
    boolean createResourceSuccessful = adapter.createResource(createStorageRequest);
    assertThat(createResourceSuccessful, is(false));
    // TODO - figure out a good way to track error messages in the logger
  }

  /**
   * Utilizes the "Status of a File/Directory" WebHDFS operation to verify that the file with the
   * given <code>filename</code> exists in the expected location.
   *
   * @param filename - the name of the file to verify
   * @throws URISyntaxException If the URL string does not have valid syntax
   */
  private void verifyFileExists(String filename) throws URISyntaxException {
    String filePath = "webhdfs/v1/" + filename;
    String url = String.format("%s/%s", BASE_URL, filePath);
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

  private CreateStorageRequest generateTestStorageRequest(String id, String name) {
    Resource testResource = getResource(id, name);
    List<Resource> resourceList = new ArrayList<>();
    resourceList.add(testResource);

    return new CreateStorageRequestImpl(resourceList);
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
   * Generates a {@link Metadata} from the given <code>id</code>.
   *
   * @param id - the id of the {@link Resource}
   * @return The newly created {@link MetadataImpl}
   */
  private Metadata getMetadata(String id) {
    Map<String, MetacardAttribute> map = new HashMap<>();
    map.put(Constants.METACARD_ID, new MetacardAttribute(Constants.METACARD_ID, null, id));
    map.put("type", new MetacardAttribute("type", null, "hdfs.metacard"));
    map.put(Constants.METACARD_TAGS, new MetacardAttribute(Constants.METACARD_TAGS, null, "tag"));

    Metadata metadata = new MetadataImpl(map, Map.class, id, new Date());
    metadata.setResourceModified(new Date(946684800000L));

    return metadata;
  }

  /**
   * Generates a {@link Resource} from the given <code>id</code> and <code>name</code>.
   *
   * @param id - the id to assign to the {@link Resource}
   * @param name - the name to assign to the {@link Resource}
   * @return The newly created {@link ResourceImpl}
   */
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
