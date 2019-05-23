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
package com.connexta.replication.adapters.ddf.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DdfRestClientFactoryTest {
  @Mock Subject subject;
  @Mock ClientFactoryFactory clientFactory;
  @Mock SecureCxfClientFactory secureCxfClientFactory;
  @Mock WebClient webClient;

  @Before
  public void setUp() throws Exception {
    ThreadContext.bind(subject);
    when(clientFactory.getSecureCxfClientFactory(any(String.class), any(Class.class)))
        .thenReturn(secureCxfClientFactory);
    when(secureCxfClientFactory.getWebClientForSubject(any(Subject.class))).thenReturn(webClient);
  }

  @After
  public void tearDown() throws Exception {
    ThreadContext.remove(subject);
  }

  @Test
  public void create() {
    DdfRestClientFactory factory = new DdfRestClientFactory(clientFactory);
    DdfRestClient client = factory.create("https://host:1234/context");
    verify(clientFactory)
        .getSecureCxfClientFactory("https://host:1234/context/catalog", RESTService.class);
    verify(secureCxfClientFactory).getWebClientForSubject(subject);
  }
}
