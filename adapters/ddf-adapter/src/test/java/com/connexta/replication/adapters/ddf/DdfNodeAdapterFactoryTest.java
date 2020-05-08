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
package com.connexta.replication.adapters.ddf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import com.connexta.replication.adapters.ddf.csw.Csw;
import com.connexta.replication.adapters.ddf.rest.DdfRestClientFactory;
import com.connexta.replication.data.ReplicationConstants;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import org.apache.cxf.interceptor.Interceptor;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.junit.rules.RestoreSystemProperties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DdfNodeAdapterFactoryTest {

  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  @Mock DdfRestClientFactory ddfRestClientFactory;

  @Mock ClientFactoryFactory clientFactoryFactory;

  @Test
  public void create() throws Exception {
    System.setProperty("javax.net.ssl.keyStore", "/my/keystore.jks");
    DdfNodeAdapterFactory factory = new DdfNodeAdapterFactory(clientFactoryFactory, 30000, 60000);
    assertThat(factory.create(new URL("https://localhost:8993/context")), is(notNullValue()));
    verify(clientFactoryFactory)
        .getSecureCxfClientFactory(
            eq("https://localhost:8993/context/csw"),
            eq(Csw.class),
            any(List.class),
            (Interceptor) eq(null),
            eq(true),
            eq(false),
            eq(30000),
            eq(60000),
            eq(InetAddress.getLocalHost().getCanonicalHostName()),
            eq("/my/keystore.jks"),
            eq(ReplicationConstants.TLS_PROTOCOL));
  }

  @Test
  public void createWithAlias() throws Exception {
    System.setProperty("javax.net.ssl.keyStore", "/my/keystore.jks");
    System.setProperty("javax.net.ssl.certAlias", "myAlias");
    DdfNodeAdapterFactory factory = new DdfNodeAdapterFactory(clientFactoryFactory, 30000, 60000);
    assertThat(factory.create(new URL("https://localhost:8993/context")), is(notNullValue()));
    verify(clientFactoryFactory)
        .getSecureCxfClientFactory(
            eq("https://localhost:8993/context/csw"),
            eq(Csw.class),
            any(List.class),
            (Interceptor) eq(null),
            eq(true),
            eq(false),
            eq(30000),
            eq(60000),
            eq("myAlias"),
            eq("/my/keystore.jks"),
            eq(ReplicationConstants.TLS_PROTOCOL));
  }
}
