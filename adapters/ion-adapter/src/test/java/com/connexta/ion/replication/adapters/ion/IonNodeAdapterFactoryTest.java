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
package com.connexta.ion.replication.adapters.ion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;

import com.connexta.ion.replication.api.NodeAdapter;
import com.connexta.ion.replication.api.NodeAdapterType;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@RunWith(MockitoJUnitRunner.class)
public class IonNodeAdapterFactoryTest {

  private IonNodeAdapterFactory factory;

  @Mock private SimpleClientHttpRequestFactory requestFactory;

  @Before
  public void setup() {
    factory = new IonNodeAdapterFactory(requestFactory, 30000, 60000);
  }

  @Test
  public void create() throws Exception {
    NodeAdapter adapter = factory.create(new URL("https://localhost:1234"));
    assertThat(adapter.getClass().isAssignableFrom(IonNodeAdapter.class), is(true));
    verify(requestFactory).setConnectTimeout(30000);
    verify(requestFactory).setReadTimeout(60000);
  }

  @Test
  public void getType() {
    assertThat(factory.getType(), is(NodeAdapterType.ION));
  }
}
