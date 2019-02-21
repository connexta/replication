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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

import com.thoughtworks.xstream.converters.Converter;
import ddf.catalog.CatalogFramework;
import ddf.catalog.filter.FilterAdapter;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.resource.ResourceReader;
import ddf.security.encryption.EncryptionService;
import ddf.security.service.SecurityManager;
import java.net.URL;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.spatial.ogc.csw.catalog.common.source.writer.CswTransactionRequestWriter;
import org.codice.ddf.spatial.ogc.csw.catalog.common.transformer.TransformerManager;
import org.codice.ditto.replication.api.ReplicationStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

@RunWith(MockitoJUnitRunner.class)
public class ReplicatorStoreFactoryImplTest {

  ReplicatorStoreFactoryImpl factory;
  @Mock BundleContext bundleContext;

  @Mock Converter provider;

  @Mock EncryptionService encryptionService;

  @Mock FilterBuilder filterBuilder;

  @Mock FilterAdapter filterAdapter;

  @Mock ResourceReader resourceReader;

  @Mock SecurityManager securityManager;

  @Mock TransformerManager metacardTransformerManager;

  @Mock CswTransactionRequestWriter cswTransactionWriter;

  @Mock ClientFactoryFactory clientFactoryFactory;

  @Mock CatalogFramework framework;

  @Before
  public void setUp() throws Exception {
    factory = spy(new ReplicatorStoreFactoryImpl());
    factory.setBundleContext(bundleContext);
    factory.setCswTransformConverter(provider);
    factory.setEncryptionService(encryptionService);
    factory.setFilterBuilder(filterBuilder);
    factory.setFilterAdapter(filterAdapter);
    factory.setResourceReader(resourceReader);
    factory.setSecurityManager(securityManager);
    factory.setSchemaTransformerManager(metacardTransformerManager);
    factory.setCswTransactionWriter(cswTransactionWriter);
    factory.setClientFactoryFactory(clientFactoryFactory);
    factory.setCatalogFramework(framework);
  }

  /*
  Expected a NoClassDefFoundError because of the Hybrid creation.
  The test is to see if we would create a Hybrid store for the URL
   */
  @Test(expected = NoClassDefFoundError.class)
  public void createReplicatorStore() throws Exception {
    factory.createReplicatorStore(new URL("https://somehost:1234"));
  }

  @Test
  public void createReplicatorStoreLocal() throws Exception {
    ReplicationStore store = factory.createReplicatorStore(new URL("https://localhost:8993"));
    assertThat(store.getClass().getName(), is(LocalCatalogResourceStore.class.getName()));
  }
}
