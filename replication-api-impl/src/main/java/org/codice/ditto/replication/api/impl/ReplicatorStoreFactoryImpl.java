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
package org.codice.ditto.replication.api.impl;

import static org.codice.ddf.spatial.ogc.csw.catalog.common.CswAxisOrder.LON_LAT;

import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.converters.Converter;
import ddf.catalog.CatalogFramework;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.resource.ResourceReader;
import ddf.catalog.transform.QueryFilterTransformerProvider;
import ddf.security.encryption.EncryptionService;
import ddf.security.permission.Permissions;
import ddf.security.service.SecurityManager;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.cxf.client.ClientBuilderFactory;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.ddf.endpoints.rest.RESTService;
import org.codice.ddf.security.Security;
import org.codice.ddf.spatial.ogc.csw.catalog.common.CswAxisOrder;
import org.codice.ddf.spatial.ogc.csw.catalog.common.CswSourceConfiguration;
import org.codice.ddf.spatial.ogc.csw.catalog.common.source.writer.CswTransactionRequestWriter;
import org.codice.ddf.spatial.ogc.csw.catalog.common.transformer.TransformerManager;
import org.codice.ditto.replication.api.ReplicationStore;
import org.codice.ditto.replication.api.ReplicatorStoreFactory;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicatorStoreFactoryImpl implements ReplicatorStoreFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplicatorStoreFactoryImpl.class);

  private static final String ID = "CSWFederatedSource";

  private static final boolean DISABLE_CN_CHECK = false;

  private static final CswAxisOrder COORDINATE_ORDER = LON_LAT;

  private static final boolean USE_POS_LIST = false;

  private static final Integer POLL_INTERVAL_MINUTES = 5;

  private static final Integer DEFAULT_CONNECTION_TIMEOUT_SECS = 30;

  private static final Integer DEFAULT_RECEIVE_TIMEOUT_SECS = 60;

  private static final boolean IS_CQL_FORCED = false;

  private static final String OUTPUT_SCHEMA = "urn:catalog:metacard";

  private static final String QUERY_TYPE_NAME = "csw:Record";

  private static final String QUERY_TYPE_NAMESPACE = "http://www.opengis.net/cat/csw/2.0.2";

  private static final boolean REGISTER_FOR_EVENTS = true;

  private BundleContext bundleContext;

  private Converter provider;

  private EncryptionService encryptionService;

  private FilterBuilder filterBuilder;

  private FilterAdapter filterAdapter;

  private ResourceReader resourceReader;

  private SecurityManager securityManager;

  private TransformerManager metacardTransformerManager;

  private CswTransactionRequestWriter cswTransactionWriter;

  private ClientFactoryFactory clientFactoryFactory;

  private CatalogFramework framework;

  private QueryFilterTransformerProvider cswQueryFilterTransformerProvider;

  private Security security;

  private ClientBuilderFactory clientBuilderFactory;

  private Permissions permissions;

  public ReplicationStore createReplicatorStore(URL url) {
    if (url.toString().startsWith(SystemBaseUrl.INTERNAL.getBaseUrl())
        || url.toString().startsWith(SystemBaseUrl.EXTERNAL.getBaseUrl())) {
      return new LocalCatalogResourceStore(framework);
    }

    HybridStore hybridStore = createHybridStore(url);
    hybridStore.init();

    return hybridStore;
  }

  @VisibleForTesting
  HybridStore createHybridStore(URL url) {
    int connectionTimeout =
        Integer.getInteger("replication.connection.timeout", DEFAULT_CONNECTION_TIMEOUT_SECS);
    int receiveTimeout =
        Integer.getInteger("replication.receive.timeout", DEFAULT_RECEIVE_TIMEOUT_SECS);
    LOGGER.debug(
        "Creating a HybridStore with a connection timeout of {} and a receive timeout of {}",
        connectionTimeout,
        receiveTimeout);

    int connectionTimeoutMs = Math.toIntExact(TimeUnit.SECONDS.toMillis(connectionTimeout));
    int receiveTimeoutMs = Math.toIntExact(TimeUnit.SECONDS.toMillis(receiveTimeout));
    SecureCxfClientFactory<RESTService> restClientFactory =
        clientFactoryFactory.getSecureCxfClientFactory(
            url.toString() + "/catalog",
            RESTService.class,
            null,
            null,
            false,
            false,
            connectionTimeoutMs,
            receiveTimeoutMs);
    CswSourceConfiguration cswConfig = createCswConfig(url, connectionTimeoutMs, receiveTimeoutMs);

    HybridStore hybridStore =
        new HybridStore(
            bundleContext,
            cswConfig,
            provider,
            clientBuilderFactory,
            encryptionService,
            restClientFactory,
            security,
            permissions);
    hybridStore.setFilterBuilder(filterBuilder);
    hybridStore.setFilterAdapter(filterAdapter);
    hybridStore.setResourceReader(resourceReader);
    hybridStore.setSecurityManager(securityManager);
    hybridStore.setSchemaTransformerManager(metacardTransformerManager);
    hybridStore.setCswTransactionWriter(cswTransactionWriter);
    hybridStore.setCswQueryFilterTransformerProvider(cswQueryFilterTransformerProvider);
    hybridStore.setCswTransformConverter(provider);
    return hybridStore;
  }

  @VisibleForTesting
  CswSourceConfiguration createCswConfig(URL url, int connectionTimeoutMs, int receiveTimeoutMs) {
    CswSourceConfiguration cswConfiguration =
        new CswSourceConfiguration(encryptionService, permissions);
    cswConfiguration.setCswUrl(url.toString() + "/csw");
    cswConfiguration.setConnectionTimeout(connectionTimeoutMs);
    cswConfiguration.setReceiveTimeout(receiveTimeoutMs);
    cswConfiguration.setCswAxisOrder(COORDINATE_ORDER);
    cswConfiguration.setDisableCnCheck(DISABLE_CN_CHECK);
    cswConfiguration.setIsCqlForced(IS_CQL_FORCED);
    cswConfiguration.setOutputSchema(OUTPUT_SCHEMA);
    cswConfiguration.setId(ID);
    cswConfiguration.setPassword("");
    cswConfiguration.setUsername("");
    cswConfiguration.setPollIntervalMinutes(POLL_INTERVAL_MINUTES);
    cswConfiguration.setQueryTypeNamespace(QUERY_TYPE_NAMESPACE);
    cswConfiguration.setQueryTypeName(QUERY_TYPE_NAME);
    cswConfiguration.setUsePosList(USE_POS_LIST);
    cswConfiguration.setRegisterForEvents(REGISTER_FOR_EVENTS);
    return cswConfiguration;
  }

  public void setBundleContext(BundleContext bundleContext) {
    this.bundleContext = bundleContext;
  }

  public void setCswTransformConverter(Converter provider) {
    this.provider = provider;
  }

  public void setEncryptionService(EncryptionService encryptionService) {
    this.encryptionService = encryptionService;
  }

  public void setFilterBuilder(FilterBuilder filterBuilder) {
    this.filterBuilder = filterBuilder;
  }

  public void setFilterAdapter(FilterAdapter filterAdapter) {
    this.filterAdapter = filterAdapter;
  }

  public void setResourceReader(ResourceReader resourceReader) {
    this.resourceReader = resourceReader;
  }

  public void setSecurityManager(SecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  public void setCatalogFramework(CatalogFramework framework) {
    this.framework = framework;
  }

  public void setSchemaTransformerManager(TransformerManager transformerManager) {
    this.metacardTransformerManager = transformerManager;
  }

  public void setCswTransactionWriter(CswTransactionRequestWriter cswTransactionWriter) {
    this.cswTransactionWriter = cswTransactionWriter;
  }

  public void setClientFactoryFactory(ClientFactoryFactory clientFactoryFactory) {
    this.clientFactoryFactory = clientFactoryFactory;
  }

  public void setCswQueryFilterTransformerProvider(
      QueryFilterTransformerProvider cswQueryFilterTransformerProvider) {
    this.cswQueryFilterTransformerProvider = cswQueryFilterTransformerProvider;
  }

  public void setSecurity(Security security) {
    this.security = security;
  }

  public void setClientBuilderFactory(ClientBuilderFactory clientBuilderFactory) {
    this.clientBuilderFactory = clientBuilderFactory;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }
}
