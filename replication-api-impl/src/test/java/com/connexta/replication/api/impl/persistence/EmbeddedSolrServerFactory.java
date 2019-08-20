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
package com.connexta.replication.api.impl.persistence;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.solr.server.SolrClientFactory;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

/**
 * This class provides an implementation for creating an embedded Solr server factory. If will
 * automatically shutdown if it is registered as a bean with spring.
 */
public class EmbeddedSolrServerFactory implements SolrClientFactory, DisposableBean {
  private final String solrHome;
  private final String core;
  private CoreContainer container = null;

  /**
   * Instantiates a new embedded Solr server factory.
   *
   * @param solrHome any Path expression valid for use with {@link ResourceUtils} that points to the
   *     {@code solr.solr.home} directory
   * @param core the name of the core to create
   */
  public EmbeddedSolrServerFactory(String solrHome, String core) {
    Assert.hasText(solrHome, "SolrHome must not be null nor empty!");
    Assert.hasText(solrHome, "core must not be null nor empty!");
    this.solrHome = solrHome;
    this.core = core;
  }

  @Override
  public EmbeddedSolrServer getSolrClient() {
    return new EmbeddedSolrServer(getCoreContainer(), core);
  }

  @Override
  public synchronized void destroy() throws Exception {
    final CoreContainer container = this.container;

    if (container != null) {
      container.shutdown();
    }
  }

  private synchronized CoreContainer getCoreContainer() {
    if (container == null) {
      try {
        this.container = createCoreContainer();
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }
    return container;
  }

  private CoreContainer createCoreContainer() throws IOException {
    final Path solrHome =
        Paths.get(URLDecoder.decode(ResourceUtils.getFile(this.solrHome).getPath(), "utf-8"));
    final Path defaultDir = solrHome.resolve("configsets").resolve("_default");
    final Path coreDir = solrHome.resolve(core);
    final Path coreProps = coreDir.resolve("core.properties");

    // cleanup old core directory
    FileUtils.deleteDirectory(coreDir.toFile());
    // create/re-create core directory
    FileUtils.forceMkdir(coreDir.toFile());
    // cpy default Solr schema and config
    FileUtils.copyDirectory(defaultDir.toFile(), coreDir.toFile());
    // create the core.properties file
    FileUtils.writeStringToFile(
        coreProps.toFile(),
        "name="
            + core
            + System.lineSeparator()
            + "config="
            + "solrconfig-inmemory.xml"
            + System.lineSeparator()
            + "schema=replication-solr-managed-schema.xml",
        "utf-8");
    return CoreContainer.createAndLoad(solrHome);
  }
}
