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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connexta.replication.adapters.ddf.DdfRestClientFactory.DdfRestClient;
import ddf.catalog.content.data.ContentItem;
import ddf.catalog.data.Metacard;
import ddf.catalog.transform.MetacardTransformer;
import java.security.PrivilegedAction;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.shiro.subject.Subject;
import org.codice.ddf.cxf.client.ClientFactoryFactory;
import org.codice.ddf.cxf.client.SecureCxfClientFactory;
import org.codice.ddf.endpoints.rest.RESTService;
import org.codice.ddf.security.common.Security;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DdfRestClientFactoryTest {

  private static final String HOST = "host";

  @Mock MetacardTransformer metacardTransformer;

  @Mock ClientFactoryFactory clientFactoryFactory;

  @Mock Security security;

  @Mock SecureCxfClientFactory secureCxfClientFactory;

  @Mock WebClient webClient;

  private DdfRestClientFactory ddfRestClientFactory;

  @Before
  public void setup() {
    when(security.runAsAdmin(any(PrivilegedAction.class)))
        .thenAnswer(invocation -> invocation.getArgumentAt(0, PrivilegedAction.class).run());

    when(secureCxfClientFactory.getWebClientForSubject(any(Subject.class))).thenReturn(webClient);

    when(clientFactoryFactory.getSecureCxfClientFactory(
            HOST + "/services/catalog", RESTService.class))
        .thenReturn(secureCxfClientFactory);

    ddfRestClientFactory =
        new DdfRestClientFactory(clientFactoryFactory, metacardTransformer, security);
  }

  @Test
  public void testReusingWebClientForPostsUpdatesClientPathCorrectly() {
    // setup
    final String metacardId = "metacardId";
    Metacard metacard = mock(Metacard.class);
    when(metacard.getId()).thenReturn(metacardId);
    ContentItem contentItem = mock(ContentItem.class);
    when(contentItem.getMetacard()).thenReturn(metacard);

    DdfRestClient ddfRestClient = ddfRestClientFactory.create(HOST);

    // when
    ddfRestClient.post(contentItem, metacardId);

    // then
    verify(webClient, times(1)).replacePath(metacardId);
  }
}
