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

import com.thoughtworks.xstream.converters.Converter;
import ddf.catalog.CatalogFramework;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.resource.ResourceReader;
import ddf.security.encryption.EncryptionService;
import ddf.security.service.SecurityManager;
import java.net.URL;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.spatial.ogc.csw.catalog.common.CswAxisOrder;
import org.codice.ddf.spatial.ogc.csw.catalog.common.CswSourceConfiguration;
import org.codice.ddf.spatial.ogc.csw.catalog.common.source.writer.CswTransactionRequestWriter;
import org.codice.ddf.spatial.ogc.csw.catalog.common.transformer.TransformerManager;
import org.codice.ditto.replication.api.ReplicationStore;
import org.codice.ditto.replication.api.ReplicatorStoreFactory;
import org.osgi.framework.BundleContext;

public class ReplicatorStoreFactoryImpl implements ReplicatorStoreFactory {

  private static final String ID = "CSWFederatedSource";

  private static final boolean DISABLE_CN_CHECK = false;

  private static final CswAxisOrder COORDINATE_ORDER = LON_LAT;

  private static final boolean USE_POS_LIST = false;

  private static final Integer POLL_INTERVAL_MINUTES = 5;

  private static final Integer CONNECTION_TIMEOUT = 30000;

  private static final Integer RECEIVE_TIMEOUT = 60000;

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

  public ReplicationStore createReplicatorStore(URL url) {

    if (url.toString().startsWith(SystemBaseUrl.INTERNAL.getBaseUrl())
        || url.toString().startsWith(SystemBaseUrl.EXTERNAL.getBaseUrl())) {
      return new LocalCatalogResourceStore(framework);
    }

    CswSourceConfiguration cswConfiguration = new CswSourceConfiguration(encryptionService);
    cswConfiguration.setCswUrl(url.toString() + "/csw");
    cswConfiguration.setConnectionTimeout(CONNECTION_TIMEOUT);
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
    cswConfiguration.setReceiveTimeout(RECEIVE_TIMEOUT);
    cswConfiguration.setUsePosList(USE_POS_LIST);
    cswConfiguration.setRegisterForEvents(REGISTER_FOR_EVENTS);

    HybridStore hybridStore =
        new HybridStore(
            bundleContext,
            cswConfiguration,
            provider,
            clientFactoryFactory,
            encryptionService,
            url);
    hybridStore.setFilterBuilder(filterBuilder);
    hybridStore.setFilterAdapter(filterAdapter);
    hybridStore.setResourceReader(resourceReader);
    hybridStore.setSecurityManager(securityManager);
    hybridStore.setSchemaTransformerManager(metacardTransformerManager);
    hybridStore.setCswTransactionWriter(cswTransactionWriter);
    hybridStore.init();

    return hybridStore;
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
}
