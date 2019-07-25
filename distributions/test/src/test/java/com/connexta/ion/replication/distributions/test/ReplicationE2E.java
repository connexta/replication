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
package com.connexta.ion.replication.distributions.test;

import static com.connexta.ion.replication.distributions.test.TestUtils.CONFIG;
import static com.connexta.ion.replication.distributions.test.TestUtils.SITE;
import static com.connexta.ion.replication.distributions.test.TestUtils.clearAllSolrCores;
import static com.connexta.ion.replication.distributions.test.TestUtils.createProductOnNode;
import static com.connexta.ion.replication.distributions.test.TestUtils.defaultReplicationContainer;
import static com.connexta.ion.replication.distributions.test.TestUtils.defaultSolrContainer;
import static com.connexta.ion.replication.distributions.test.TestUtils.deleteProductFromNode;
import static com.connexta.ion.replication.distributions.test.TestUtils.deleteQuietly;
import static com.connexta.ion.replication.distributions.test.TestUtils.extractDateTimeAttribute;
import static com.connexta.ion.replication.distributions.test.TestUtils.extractStringAttribute;
import static com.connexta.ion.replication.distributions.test.TestUtils.generateTestId;
import static com.connexta.ion.replication.distributions.test.TestUtils.getById;
import static com.connexta.ion.replication.distributions.test.TestUtils.getFileAsStream;
import static com.connexta.ion.replication.distributions.test.TestUtils.getFileAsText;
import static com.connexta.ion.replication.distributions.test.TestUtils.nodeHasMetadata;
import static com.connexta.ion.replication.distributions.test.TestUtils.nodeHasMetadataModifiedAfter;
import static com.connexta.ion.replication.distributions.test.TestUtils.postToSolrCore;
import static com.connexta.ion.replication.distributions.test.TestUtils.updateProductOnNode;
import static com.connexta.ion.replication.distributions.test.TestUtils.waitAtMost30SecondsUntil;
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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class ReplicationE2E {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationE2E.class);

  // used to collect the replication service logs and print them with the test logs
  private static final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);

  @ClassRule public static Network network = Network.newNetwork();

  @ClassRule public static GenericContainer solr = defaultSolrContainer(network);

  @ClassRule public static GenericContainer replication = defaultReplicationContainer(network);

  private static String solrUrl;
  private static final String sourceUrl = System.getenv("REPLICATION_SRC");
  private static final String destinationUrl = System.getenv("REPLICATION_DEST");

  private final Set<String> idsToDelete = new HashSet<>();

  @BeforeClass
  public static void beforeClass() {
    assertTrue("Source and destination URLs not set!", sourceUrl != null && destinationUrl != null);
    LOGGER.info("Source URL: {}", sourceUrl);
    LOGGER.info("Destination URL: {}", destinationUrl);
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    replication.followOutput(logConsumer);
    solrUrl = "http://" + solr.getContainerIpAddress() + ":" + solr.getFirstMappedPort();
    LOGGER.info("Solr URL: {}", solrUrl);
  }

  @After
  public void cleanUp() {
    clearAllSolrCores(solrUrl);
    idsToDelete.forEach(this::deleteFromNodes);
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
        () -> nodeHasMetadataModifiedAfter(destinationUrl, productId, originalModifiedTime));
    LOGGER.info("product with ID: {} updated through replication on the destination node");

    // verify resource was updated by checking the extracted text
    XmlPath metacardXml = getById(destinationUrl, productId);
    String resourceText = extractStringAttribute(metacardXml, "ext.extracted.text");
    LOGGER.info("Extracted Text: {}", resourceText);

    assertTrue(containsIgnoreCase(resourceText, "updated"));
  }

  @Test
  public void syncDelete() throws Exception {
    String testId = generateTestId("syncDelete");
    String productId = createAndReplicateTestProduct(testId);

    // delete product
    deleteProductFromNode(sourceUrl, productId);
    LOGGER.info("product with ID: {} deleted on the source node", productId);

    // wait for the delete to be replicated
    waitAtMost30SecondsUntil(() -> !nodeHasMetadata(destinationUrl, productId));
    LOGGER.info(
        "product with ID: {} deleted through replication on the destination node", productId);
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

    // create nodes and jobs
    String sites =
        getFileAsText("sites.json", Map.of("source", sourceUrl, "destination", destinationUrl));
    String jobs = getFileAsText("jobs.json", Map.of("testId", testId));
    postToSolrCore(solrUrl, SITE, sites);
    LOGGER.info("Sites Created");
    postToSolrCore(solrUrl, CONFIG, jobs);
    LOGGER.info("Jobs Created");

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
