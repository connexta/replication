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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.data.ReplicationConstants;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.junit.rules.RestoreSystemProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DdfRestClientFactoryTest {

  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  @Mock Subject subject;
  @Mock ClientFactoryFactory clientFactory;
  @Mock SecureCxfClientFactory secureCxfClientFactory;
  @Mock WebClient webClient;

  private static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 30000;

  private static final int DEFAULT_RECEIVE_TIMEOUT_MILLIS = 60000;

  @Before
  public void setUp() throws Exception {
    ThreadContext.bind(subject);
    System.setProperty("javax.net.ssl.keyStore", "/my/keystore.jks");
    System.setProperty("javax.net.ssl.certAlias", "myAlias");
    when(clientFactory.getSecureCxfClientFactory(
            any(String.class),
            any(Class.class),
            any(List.class),
            any(Interceptor.class),
            anyBoolean(),
            anyBoolean(),
            any(Integer.class),
            any(Integer.class),
            any(String.class),
            any(String.class),
            any(String.class)))
        .thenReturn(secureCxfClientFactory);
  }

  @After
  public void tearDown() throws Exception {
    ThreadContext.remove(subject);
  }

  @Test
  public void create() throws Exception {
    when(secureCxfClientFactory.getWebClient()).thenReturn(webClient);
    DdfRestClientFactory factory =
        new DdfRestClientFactory(
            clientFactory, DEFAULT_CONNECTION_TIMEOUT_MILLIS, DEFAULT_RECEIVE_TIMEOUT_MILLIS);
    DdfRestClient client = factory.create(new URL("https://host:1234/context"));
    verify(clientFactory)
        .getSecureCxfClientFactory(
            "https://host:1234/context/catalog",
            RESTService.class,
            null,
            null,
            false,
            false,
            DEFAULT_CONNECTION_TIMEOUT_MILLIS,
            DEFAULT_RECEIVE_TIMEOUT_MILLIS,
            "myAlias",
            "/my/keystore.jks",
            ReplicationConstants.TLS_PROTOCOL);
    verify(secureCxfClientFactory).getWebClient();
  }

  @Test
  public void createWithSubject() throws Exception {
    WebClient whoamiClient = mock(WebClient.class);
    when(secureCxfClientFactory.getWebClient()).thenReturn(whoamiClient, webClient);
    DdfRestClientFactory factory =
        new DdfRestClientFactory(
            clientFactory, DEFAULT_CONNECTION_TIMEOUT_MILLIS, DEFAULT_RECEIVE_TIMEOUT_MILLIS);
    Response mockResponse = mock(Response.class);
    NewCookie mockCookie = mock(NewCookie.class);
    Map<String, NewCookie> cookies = new HashMap<>();
    cookies.put("JSESSIONID", mockCookie);
    when(whoamiClient.get()).thenReturn(mockResponse);
    when(mockResponse.getCookies()).thenReturn(cookies);
    DdfRestClient client = factory.createWithSubject(new URL("https://host:1234/context"));
    verify(clientFactory)
        .getSecureCxfClientFactory(
            "https://host:1234/context/catalog",
            RESTService.class,
            null,
            null,
            false,
            false,
            DEFAULT_CONNECTION_TIMEOUT_MILLIS,
            DEFAULT_RECEIVE_TIMEOUT_MILLIS,
            "myAlias",
            "/my/keystore.jks",
            ReplicationConstants.TLS_PROTOCOL);
    verify(secureCxfClientFactory, times(2)).getWebClient();
    ArgumentCaptor<NewCookie> resultCookie = ArgumentCaptor.forClass(NewCookie.class);
    verify(webClient).cookie(resultCookie.capture());
    assertThat(resultCookie.getValue(), is(mockCookie));
  }
}
