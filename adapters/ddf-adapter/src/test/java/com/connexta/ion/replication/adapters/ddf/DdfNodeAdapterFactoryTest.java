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
package com.connexta.ion.replication.adapters.ddf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import com.connexta.ion.replication.adapters.ddf.csw.Csw;
import java.net.URL;
import java.util.List;
import org.apache.cxf.interceptor.Interceptor;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DdfNodeAdapterFactoryTest {
  @Mock ClientFactoryFactory clientFactoryFactory;

  @Test
  public void create() throws Exception {
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
            eq(60000));
  }
}
