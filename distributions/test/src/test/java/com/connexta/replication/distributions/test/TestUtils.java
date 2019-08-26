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
package com.connexta.replication.distributions.test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import com.jayway.restassured.path.xml.XmlPath;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

public class TestUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

  private static final String RESOURCE_VARIABLE_DELIMETER = "@";

  public static final String CATALOG_ENDPOINT = "/catalog";

  public static final String APPLICATION_JSON = "application/json";

  public static final int SOLR_PORT = 8983;

  public static final String CONTENT_TYPE = "Content-Type";

  public static final String APPLICATION_XML = "application/xml";

  public static final String CONFIG = "config";

  public static final String SITE = "site";

  public static final String ITEM = "item";

  public static final Matcher<Integer> SUCCESS =
      is(both(greaterThanOrEqualTo(200)).and(lessThan(300)));

  private TestUtils() {}

  /**
   * Starts a new solr container. The created replication-solr container will use the project
   * version specified in maven to retrieve its docker image, it will be added to the given network
   * with the alias "replication-solr" and port 8983 will be exposed on the local machine a port
   * mapped by the test container. That port can be retrieved by using the {@link
   * GenericContainer#getFirstMappedPort()} method. The test container will wait for the replication
   * config core to be ready before proceeding.
   *
   * @param network The {@link Network} to expose the container on.
   * @return A {@link GenericContainer} which will run the replication-solr service.
   */
  public static GenericContainer defaultSolrContainer(Network network) {
    return new GenericContainer("r.ion.phx.connexta.com/replication-solr:" + getProjectVersion())
        .withNetwork(network)
        .withNetworkAliases("replication-solr")
        .withExposedPorts(SOLR_PORT)
        .waitingFor(Wait.forHttp("/solr/replication_" + CONFIG + "/admin/ping"));
  }

  /**
   * Starts a new replication container. The created container will use the project version
   * specified in maven to retrieve its docker image, it will be added to the given network with the
   * alias "ion-replication" and will be loaded up with a default keystore, truststore, ssl
   * properties, and application configuration. The test container will wait for "Started Main" to
   * be logged before proceeding.
   *
   * @param network The {@link Network} to expose the container on.
   * @return A {@link GenericContainer} which will run the replication service.
   */
  public static GenericContainer defaultReplicationContainer(Network network) {
    return new GenericContainer("r.ion.phx.connexta.com/replication:" + getProjectVersion())
        .withNetwork(network)
        .withNetworkAliases("ion-replication")
        .withClasspathResourceMapping(
            "serverKeystore.jks",
            "/opt/replication/config/keystores/serverKeystore.jks",
            BindMode.READ_ONLY)
        .withClasspathResourceMapping(
            "serverTruststore.jks",
            "/opt/replication/config/keystores/serverTruststore.jks",
            BindMode.READ_ONLY)
        .withClasspathResourceMapping(
            "ssl.properties", "/opt/replication/config/ssl.properties", BindMode.READ_ONLY)
        .withClasspathResourceMapping(
            "application.yml", "/opt/replication/config/application.yml", BindMode.READ_ONLY)
        .waitingFor(Wait.forLogMessage(".*Started Main.*", 1));
  }

  public static String getProjectVersion() {
    Properties pomProps = new Properties();
    InputStream propsIs = getFileAsStream("test.properties");
    String version = "";

    try {
      pomProps.load(propsIs);
      version = pomProps.getProperty("version");
    } catch (IOException e) {
      LOGGER.error("Failed to retrieve project version", e);
    } finally {
      closeQuietly(propsIs);
    }

    return version;
  }

  private static void closeQuietly(Closeable c) {
    try {
      c.close();
    } catch (IOException e) {
    }
  }

  /**
   * Send a post request with the message body to the solr core at the given URL. The message body
   * should be a json object containing documents to be added to the solr core.
   *
   * @param solrUrl The base URL of the solr instance. Includes the protocol and host.
   * @param core The name of the solr core to update with new documents.
   * @param messageBody A json file containing documents for solr to save.
   * @throws AssertionError If the the post request is unsuccessful.
   */
  public static void postToSolrCore(String solrUrl, String core, String messageBody) {
    given()
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .body(messageBody)
        .when()
        .post(solrCoreUpdateUrl(solrUrl, core))
        .then()
        .statusCode(SUCCESS);
  }

  private static String solrCoreUpdateUrl(String baseUrl, String core) {
    return String.format("%s/solr/replication_%s/update?commitWithin=1000", baseUrl, core);
  }

  /**
   * Deletes all documents from all solr cores used by replication.
   *
   * @param solrUrl The base URL of the solr instance to clear of replication documents.
   * @throws AssertionError If clearing one of the cores is unsuccessful.
   */
  public static void clearAllSolrCores(String solrUrl) {
    clearSolrCore(solrUrl, CONFIG);
    clearSolrCore(solrUrl, SITE);
    clearSolrCore(solrUrl, ITEM);
  }

  /**
   * Delete all documents from a specific solr core.
   *
   * @param solrUrl The base URL of the solr instance.
   * @param core The core to clear of documents.
   * @throws AssertionError If the post request is unsuccessful.
   * @throws org.awaitility.core.ConditionTimeoutException If the core isn't cleared within 10
   *     seconds.
   */
  public static void clearSolrCore(String solrUrl, String core) {
    try {
      given()
          .header(CONTENT_TYPE, APPLICATION_XML)
          .body("<delete><query>*:*</query></delete>")
          .when()
          .post(String.format("%s/solr/replication_%s/update?commit=true", solrUrl, core))
          .then()
          .statusCode(SUCCESS);

      // wait for the core to be cleared (100ms poll interval, 10s timeout)
      await().until(() -> coreIsEmpty(solrUrl, core));
    } catch (Exception | AssertionError e) {
      LOGGER.error("An Error occured while trying to clear the solr {} core", core, e);
    }
  }

  /**
   * Returns true if the solr core contains no documents, otherwise, returns false.
   *
   * @param solrUrl The base URL of the solr instance.
   * @param core The core to check for documents.
   * @return True if the core is empty, otherwise false.
   * @throws AssertionError If the get request is unsuccessful.
   */
  public static boolean coreIsEmpty(String solrUrl, String core) {
    return when()
            .get(String.format("%s/solr/replication_%s/select?q=*:*", solrUrl, core))
            .then()
            .statusCode(SUCCESS)
            .extract()
            .jsonPath()
            .getInt("response.numFound")
        == 0;
  }

  /**
   * Returns the contents of the resource at the given path as an {@link InputStream}.
   *
   * @param filePath The path of the resource.
   * @return The contents of the resource as an {@link InputStream}.
   */
  public static InputStream getFileAsStream(String filePath) {
    return TestUtils.class.getClassLoader().getResourceAsStream(filePath);
  }

  /**
   * Retrieves a file and returns the contents within as a string.
   *
   * @param filePath the path of the file
   * @return A String containing the contents of the file
   */
  public static String getFileAsText(String filePath) {
    return getFileAsText(filePath, Map.of());
  }

  /**
   * Retrieves a file and returns the contents within as a string. Text in the file can be marked
   * for variable replacement by giving it the format: $variableName$ . The params should then
   * contain a mapping of variableName to the desired replacement value.
   *
   * @param filePath The path of the file
   * @param params A map of variables in the resource file and their replacement values
   * @return A String containing the contents of the file with any variables replaced
   * @throws IllegalStateException if an error occurs while trying to read the file
   */
  public static String getFileAsText(String filePath, Map<String, String> params) {

    StringSubstitutor strSubstitutor = new StringSubstitutor(params);

    strSubstitutor.setVariablePrefix(RESOURCE_VARIABLE_DELIMETER);
    strSubstitutor.setVariableSuffix(RESOURCE_VARIABLE_DELIMETER);
    String fileContent;

    try {
      fileContent =
          IOUtils.toString(TestUtils.class.getClassLoader().getResourceAsStream(filePath), "UTF-8");
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read filepath: " + filePath);
    }

    return strSubstitutor.replace(fileContent);
  }

  /**
   * Delete the product with the given id from the node. Catch exceptions so we don't break anything
   * if the delete fails. Useful for cleaning up products after a test is finished.
   *
   * @param nodeUrl The URL of the node which contains the product.
   * @param id The ID of the product to delete.
   */
  public static void deleteQuietly(String nodeUrl, String id) {
    try {
      given().relaxedHTTPSValidation().delete(nodeUrl + CATALOG_ENDPOINT + "/" + id);

      await().until(() -> !nodeHasMetadata(nodeUrl, id));
    } catch (Exception | AssertionError e) {
      LOGGER.error("An error occurred while trying to delete test data from {}", nodeUrl, e);
    }
  }

  /**
   * Creates a product on the node and waits for it to be queryable.
   *
   * @param nodeUrl The URL of the node to create the product on.
   * @param metadata The metadata to include in the request.
   * @param resource The resource to include in the request.
   * @return The ID of the created product.
   * @throws org.awaitility.core.ConditionTimeoutException if the product isn't queryable within 10
   *     seconds.
   */
  public static String createProductOnNode(
      String nodeUrl, InputStream metadata, InputStream resource) {
    String createdProductId =
        given()
            .relaxedHTTPSValidation()
            .multiPart("parse.metadata", "testMetadata", metadata)
            .multiPart("parse.resource", "testResource", resource)
            .expect()
            .statusCode(SUCCESS)
            .when()
            .post(nodeUrl + CATALOG_ENDPOINT)
            .getHeader("id");

    await().until(() -> nodeHasMetadata(nodeUrl, createdProductId));
    return createdProductId;
  }

  /**
   * Checks if the condition is satisfied every second for a maximum of 30 seconds. This long poll
   * interval is best used only when necessary, such as when waiting for a product to replicate.
   *
   * @param conditional A {@link Callable} to call every one second until it returns true or until
   *     the 30 second timeout occurs
   * @throws org.awaitility.core.ConditionTimeoutException if the condition isn't satisfied in 30
   *     seconds.
   */
  public static void waitAtMost30SecondsUntil(Callable<Boolean> conditional) {
    await()
        .with()
        .pollInterval(1, TimeUnit.SECONDS)
        .atMost(30, TimeUnit.SECONDS)
        .until(conditional);
  }

  /**
   * Returns true if the node has metadata with the given id.
   *
   * @param nodeUrl The URL of the node.
   * @param id The ID of the metadata to check for.
   * @return True if the node has metadata with the ID, false otherwise.
   */
  public static Boolean nodeHasMetadata(String nodeUrl, String id) {
    String xml = getById(nodeUrl, id).getString("metacard.@gml:id");
    return xml != null;
  }

  /**
   * Updates a product on the node and waits for the update to be reflected in queries. It does this
   * by first querying for the product and getting the metacard.modified date. Then, after the
   * update request is sent, it queries for the product until the metacard.modified date has
   * changed, or the wait times out.
   *
   * @param nodeUrl The URL of the node.
   * @param id The ID of the product.
   * @param metadata The metadata to replace the old metadata.
   * @param resource The resource to replace the old resource.
   * @throws AssertionError if the put request is unsuccessful.
   * @throws org.awaitility.core.ConditionTimeoutException if the product doesn't actually update in
   *     10 seconds.
   */
  public static void updateProductOnNode(
      String nodeUrl, String id, InputStream metadata, InputStream resource) {
    // retrieve the modified time as it currently exists (we do this instead of comparing to a
    // timestamp we create ourselves because system times can differ).
    Instant originalModifiedTime =
        extractDateTimeAttribute(getById(nodeUrl, id), "metacard.modified");

    given()
        .relaxedHTTPSValidation()
        .multiPart("parse.metadata", "testMetadata", metadata)
        .multiPart("parse.resource", "testResource", resource)
        .expect()
        .statusCode(SUCCESS)
        .when()
        .put(nodeUrl + CATALOG_ENDPOINT + "/" + id);

    await().until(() -> nodeHasMetadataModifiedAfter(nodeUrl, id, originalModifiedTime));
  }

  /**
   * Returns true if querying the node for a product with the given ID returns a metacard with a
   * metacard.modified date that is after the given timestamp.
   *
   * @param nodeUrl The URL of the node.
   * @param id The ID of the product to fetch.
   * @param timeStamp The timestamp to compare to the metacard.modified date.
   * @return True if the metacard.modified date of the metacard is after the given timestamp, false
   *     otherwise.
   * @throws NullPointerException if the modified date can't be retrieved from the response.
   */
  public static Boolean nodeHasMetadataModifiedAfter(String nodeUrl, String id, Instant timeStamp) {
    Instant modifiedTime = extractDateTimeAttribute(getById(nodeUrl, id), "metacard.modified");
    return modifiedTime.isAfter(timeStamp);
  }

  /**
   * Fetch a product by ID. Returns the {@link XmlPath} of the response for easy retreival of
   * metacard attributes. Also logs the body of the response.
   *
   * @param nodeUrl The URL of the node.
   * @param id The ID of the product to fetch.
   * @return The {@link XmlPath} of the response.
   */
  public static XmlPath getById(String nodeUrl, String id) {
    LOGGER.info(
        "============================ START getById Response Body ============================");
    XmlPath xmlPath =
        given()
            .relaxedHTTPSValidation()
            .when()
            .get(nodeUrl + CATALOG_ENDPOINT + "/" + id)
            .then()
            .log()
            .body()
            .extract()
            .body()
            .xmlPath();
    LOGGER.info(
        "============================ END getById Response Body ============================");
    return xmlPath;
  }

  /**
   * Extracts an {@link OffsetDateTime} from the XmlPath of a metacard and returns it as an {@link
   * Instant}.
   *
   * @param metacardXml The {@link XmlPath} of a metacard.
   * @param attributeName The name of the dateTime attribute to extract.
   * @return The extracted dateTime as an {@link Instant}.
   * @throws NullPointerException if the attribute isn't contained in the response.
   */
  public static Instant extractDateTimeAttribute(XmlPath metacardXml, String attributeName) {
    String modifiedString =
        metacardXml.getString(
            String.format("metacard.dateTime.find{ it.@name == '%s' }.value", attributeName));
    return OffsetDateTime.parse(modifiedString).toInstant();
  }

  /**
   * Extracts a String from the XmlPath of a metacard.
   *
   * @param metacardXml The {@link XmlPath} of a metacard.
   * @param attributeName The name of the String attribute to extract.
   * @return The extracted String, or null if the attribute isn't contained in the response.
   */
  @Nullable
  public static String extractStringAttribute(XmlPath metacardXml, String attributeName) {
    return metacardXml.getString(
        String.format("metacard.string.find{ it.@name == '%s' }.value", attributeName));
  }

  /**
   * Delete a Product.
   *
   * @param nodeUrl The base URL of the node.
   * @param id The ID of the product to delete.
   * @throws AssertionError If the delete request is unsuccessful.
   * @throws org.awaitility.core.ConditionTimeoutException If the product is still retrievable after
   *     10 seconds.
   */
  public static void deleteProductFromNode(String nodeUrl, String id) {
    given()
        .relaxedHTTPSValidation()
        .expect()
        .statusCode(SUCCESS)
        .when()
        .delete(nodeUrl + CATALOG_ENDPOINT + "/" + id);

    await().until(() -> !nodeHasMetadata(nodeUrl, id));
  }

  /**
   * Generates an ID for a test. This is useful for creating a unique ID to replicate products by.
   *
   * @param testName The name of the test. This is just for logging to help with debugging.
   * @return The generated test ID.
   */
  public static String generateTestId(String testName) {
    String testId = UUID.randomUUID().toString().replaceAll("-", "");
    LOGGER.info("{} test ID: {}", testName, testId);
    return testId;
  }
}
