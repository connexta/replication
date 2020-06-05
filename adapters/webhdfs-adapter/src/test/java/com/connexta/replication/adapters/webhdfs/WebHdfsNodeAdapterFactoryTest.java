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

  private static URL urlWithPath;
  private static URL urlWithPathNoTrailingSlash;

  private static URL urlWithNoPath;
  private static URL urlWithNoPathNoTrailingSlash;

  @Before
  public void setup() throws MalformedURLException {
    webHdfsNodeAdapterFactory = new WebHdfsNodeAdapterFactory();

    urlWithPath = new URL("http://localhost:8993/webhdfs/v1/some/path/");
    urlWithPathNoTrailingSlash = new URL("http://localhost:8993/webhdfs/v1/some/path");

    urlWithNoPath = new URL("http://localhost:8993/webhdfs/v1/");
    urlWithNoPathNoTrailingSlash = new URL("http://localhost:8993/webhdfs/v1");
  }

  @Test
  public void testCreate() {
    NodeAdapter webHdfsNodeAdapter = webHdfsNodeAdapterFactory.create(urlWithPath);
    assertThat(webHdfsNodeAdapter.getClass().isAssignableFrom(WebHdfsNodeAdapter.class), is(true));
    assertThat(
        ((WebHdfsNodeAdapter) webHdfsNodeAdapter).getWebHdfsUrl().toString(),
        is(urlWithPath.toString()));
  }

  @Test
  public void testCreateNoTrailingSlash() {
    NodeAdapter webHdfsNodeAdapter = webHdfsNodeAdapterFactory.create(urlWithPathNoTrailingSlash);
    assertThat(webHdfsNodeAdapter.getClass().isAssignableFrom(WebHdfsNodeAdapter.class), is(true));
    assertThat(
        ((WebHdfsNodeAdapter) webHdfsNodeAdapter).getWebHdfsUrl().toString(),
        is(urlWithPath.toString()));
  }

  @Test
  public void testCreateNoPath() {
    NodeAdapter webHdfsNodeAdapter = webHdfsNodeAdapterFactory.create(urlWithNoPath);
    assertThat(webHdfsNodeAdapter.getClass().isAssignableFrom(WebHdfsNodeAdapter.class), is(true));
    assertThat(
        ((WebHdfsNodeAdapter) webHdfsNodeAdapter).getWebHdfsUrl().toString(),
        is(urlWithNoPath.toString()));
  }

  @Test
  public void testCreateNoPathNoSlash() {
    NodeAdapter webHdfsNodeAdapter = webHdfsNodeAdapterFactory.create(urlWithNoPathNoTrailingSlash);
    assertThat(webHdfsNodeAdapter.getClass().isAssignableFrom(WebHdfsNodeAdapter.class), is(true));
    assertThat(
        ((WebHdfsNodeAdapter) webHdfsNodeAdapter).getWebHdfsUrl().toString(),
        is(urlWithNoPath.toString()));
  }

  @Test
  public void testGetType() {
    assertThat(webHdfsNodeAdapterFactory.getType(), is(NodeAdapterType.WEBHDFS));
  }
}
