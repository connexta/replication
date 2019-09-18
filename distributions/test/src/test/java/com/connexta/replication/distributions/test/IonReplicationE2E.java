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

import static com.connexta.replication.distributions.test.TestUtils.FILTER;
import static com.connexta.replication.distributions.test.TestUtils.ITEM;
import static com.connexta.replication.distributions.test.TestUtils.SITE;
import static com.connexta.replication.distributions.test.TestUtils.clearSolrCore;
import static com.connexta.replication.distributions.test.TestUtils.createProductOnNode;
import static com.connexta.replication.distributions.test.TestUtils.defaultReplicationContainer;
import static com.connexta.replication.distributions.test.TestUtils.defaultSolrContainer;
import static com.connexta.replication.distributions.test.TestUtils.deleteQuietly;
import static com.connexta.replication.distributions.test.TestUtils.extractDateTimeAttribute;
import static com.connexta.replication.distributions.test.TestUtils.extractStringAttribute;
import static com.connexta.replication.distributions.test.TestUtils.generateTestId;
import static com.connexta.replication.distributions.test.TestUtils.getById;
import static com.connexta.replication.distributions.test.TestUtils.getFileAsStream;
import static com.connexta.replication.distributions.test.TestUtils.getFileAsText;
import static com.connexta.replication.distributions.test.TestUtils.nodeHasMetadata;
import static com.connexta.replication.distributions.test.TestUtils.nodeHasResourceModifiedAfter;
import static com.connexta.replication.distributions.test.TestUtils.postToSolrCore;
import static com.connexta.replication.distributions.test.TestUtils.updateProductOnNode;
import static com.connexta.replication.distributions.test.TestUtils.waitAtMost30SecondsUntil;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.junit.Assert.assertTrue;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.xml.XmlPath;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class IonReplicationE2E {
  private static final Logger LOGGER = LoggerFactory.getLogger(IonReplicationE2E.class);

  // used to collect the replication service logs and print them with the test logs
  private static final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);

  @ClassRule public static Network network = Network.newNetwork();

  private static final String sourceUrl = System.getenv("REPLICATION_SRC");
  private static final String destinationUrl = System.getenv("REPLICATION_DEST");

  public static GenericContainer solr = defaultSolrContainer(network);

  public static GenericContainer replication = defaultReplicationContainer(network, "Ion");

  private static String solrUrl;

  private final Set<String> idsToDelete = new HashSet<>();

  @BeforeClass
  public static void beforeClass() {
    solr.start();
    assertTrue("Source and destination URLs not set!", sourceUrl != null && destinationUrl != null);
    LOGGER.info("Source URL: {}", sourceUrl);
    LOGGER.info("Destination URL: {}", destinationUrl);
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    solrUrl = "http://" + solr.getContainerIpAddress() + ":" + solr.getFirstMappedPort();
    LOGGER.info("Solr URL: {}", solrUrl);
    String sites =
        getFileAsText("sites.json", Map.of("source", sourceUrl, "destination", destinationUrl));
    postToSolrCore(solrUrl, SITE, sites);
    LOGGER.info("Sites Created");
    replication.start();
    replication.followOutput(logConsumer);
  }

  @After
  public void cleanUp() {
    clearSolrCore(solrUrl, FILTER);
    clearSolrCore(solrUrl, ITEM);
    idsToDelete.forEach(this::deleteFromNodes);
  }

  @AfterClass
  public static void afterClass() {
    replication.stop();
    solr.stop();
  }

  @Test
  public void syncCreate() throws Exception {
    String testId = generateTestId("syncCreate");
    String id = createAndReplicateTestProduct(testId);

    XmlPath metacardXml = getById(destinationUrl, id);
    String resourceDownloadUrl = extractStringAttribute(metacardXml, "resource-download-url");
    LOGGER.info("resource-download-url retrieved: {}", resourceDownloadUrl);

    // assert the product was replicated by confirming the resource-download-url is present and
    // properly populated with a url that contains the id.
    assertTrue(containsIgnoreCase(resourceDownloadUrl, id));
  }

  @Test
  public void syncUpdate() throws Exception {
    String testId = generateTestId("syncUpdate");
    String productId = createAndReplicateTestProduct(testId);

    // update the product
    InputStream updatedMetadataIs =
        new ByteArrayInputStream(
            getFileAsText("data/UpdatedXmlSample", Map.of("testId", testId)).getBytes());

    Instant originalModifiedTime =
        extractDateTimeAttribute(getById(destinationUrl, productId), "metacard.modified");

    updateProductOnNode(
        sourceUrl, productId, updatedMetadataIs, getFileAsStream("data/UpdatedTestResource.txt"));
    LOGGER.info("product with ID: {} updated on the source node", productId);
    waitAtMost30SecondsUntil(
        () -> nodeHasResourceModifiedAfter(destinationUrl, productId, originalModifiedTime));
    LOGGER.info("product with ID: {} updated through replication on the destination node");

    // verify resource was updated by checking the extracted text
    XmlPath metacardXml = getById(destinationUrl, productId);
    String resourceText = extractStringAttribute(metacardXml, "ext.extracted.text");
    LOGGER.info("Extracted Text: {}", resourceText);

    assertTrue(containsIgnoreCase(resourceText, "updated"));
  }

  private String createAndReplicateTestProduct(String testId) {
    // create product
    InputStream metadataIs =
        new ByteArrayInputStream(
            getFileAsText("data/XmlSample", Map.of("testId", testId)).getBytes());
    String productId =
        createProductOnNode(sourceUrl, metadataIs, getFileAsStream("data/TestResource.txt"));
    idsToDelete.add(productId);
    LOGGER.info("Product created on the source node with ID: {}", productId);

    String filters = getFileAsText("filters.json", Map.of("testId", testId));
    postToSolrCore(solrUrl, FILTER, filters);
    LOGGER.info("Filters Created");

    // wait for metadata to be replicated
    waitAtMost30SecondsUntil(() -> nodeHasMetadata(destinationUrl, productId));
    LOGGER.info("Product replicated to the destination node");

    return productId;
  }

  private void deleteFromNodes(String id) {
    deleteQuietly(sourceUrl, id);
    LOGGER.info("delete request for product: {} sent to source node", id);
    deleteQuietly(destinationUrl, id);
    LOGGER.info("delete request for product: {} sent to destination node", id);
  }
}
