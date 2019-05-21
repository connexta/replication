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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
  }

  @After
  public void tearDown() throws Exception {
    ThreadContext.remove(subject);
  }

  @Test
  public void create() throws Exception {
    when(secureCxfClientFactory.getWebClient()).thenReturn(webClient);
    DdfRestClientFactory factory = new DdfRestClientFactory(clientFactory);
    DdfRestClient client = factory.create(new URL("https://host:1234/context"));
    verify(clientFactory)
        .getSecureCxfClientFactory("https://host:1234/context/catalog", RESTService.class);
    verify(secureCxfClientFactory).getWebClient();
  }

  @Test
  public void createWithSubject() throws Exception {
    WebClient whoamiClient = mock(WebClient.class);
    when(secureCxfClientFactory.getWebClient()).thenReturn(whoamiClient, webClient);
    DdfRestClientFactory factory = new DdfRestClientFactory(clientFactory);
    Response mockResponse = mock(Response.class);
    NewCookie mockCookie = mock(NewCookie.class);
    Map<String, NewCookie> cookies = new HashMap<>();
    cookies.put("JSESSIONID", mockCookie);
    when(whoamiClient.get()).thenReturn(mockResponse);
    when(mockResponse.getCookies()).thenReturn(cookies);
    DdfRestClient client = factory.createWithSubject(new URL("https://host:1234/context"));
    verify(clientFactory)
        .getSecureCxfClientFactory("https://host:1234/context/catalog", RESTService.class);
    verify(secureCxfClientFactory, times(2)).getWebClient();
    ArgumentCaptor<NewCookie> resultCookie = ArgumentCaptor.forClass(NewCookie.class);
    verify(webClient).cookie(resultCookie.capture());
    assertThat(resultCookie.getValue(), is(mockCookie));
  }
}
