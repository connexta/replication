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
package com.connexta.replication.adapters.webhdfs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.net.MalformedURLException;
import java.net.URL;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link WebHdfsNodeAdapterFactory} */
@RunWith(JUnit4.class)
public class WebHdfsNodeAdapterFactoryTest {

  private WebHdfsNodeAdapterFactory webHdfsNodeAdapterFactory;

  @Before
  public void setup() {
    webHdfsNodeAdapterFactory = new WebHdfsNodeAdapterFactory();
  }

  @Test
  public void testCreate() throws MalformedURLException {
    URL url = new URL("http://localhost:8993");

    NodeAdapter webHdfsNodeAdapter = webHdfsNodeAdapterFactory.create(url);
    assertThat(webHdfsNodeAdapter.getClass().isAssignableFrom(WebHdfsNodeAdapter.class), is(true));
  }

  @Test
  public void testGetType() {
    assertThat(webHdfsNodeAdapterFactory.getType(), is(NodeAdapterType.WEBHDFS));
  }
}
