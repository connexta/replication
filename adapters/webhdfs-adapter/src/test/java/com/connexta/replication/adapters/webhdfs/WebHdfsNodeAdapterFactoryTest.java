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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.util.StandardCharset;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreSpi;
import java.util.Enumeration;
import org.apache.commons.io.FileUtils;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link WebHdfsNodeAdapterFactory} */
@RunWith(JUnit4.class)
public class WebHdfsNodeAdapterFactoryTest {

  private WebHdfsNodeAdapterFactory webHdfsNodeAdapterFactory;

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private static URL urlWithPath;
  private static URL urlWithPathNoTrailingSlash;

  private static URL urlWithNoPath;
  private static URL urlWithNoPathNoTrailingSlash;

  private static URL secureUrlWithPath;

  @Before
  public void setup() throws MalformedURLException {
    webHdfsNodeAdapterFactory = new WebHdfsNodeAdapterFactory();

    urlWithPath = new URL("http://localhost:9870/webhdfs/v1/some/path/");
    urlWithPathNoTrailingSlash = new URL("http://localhost:9870/webhdfs/v1/some/path");

    urlWithNoPath = new URL("http://localhost:9870/webhdfs/v1/");
    urlWithNoPathNoTrailingSlash = new URL("http://localhost:9870/webhdfs/v1");

    secureUrlWithPath = new URL("https://localhost:443/webhdfs/v1/some/path/");
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
  public void testCreateWithHttps() throws IOException {
    File keyStore = folder.newFile("testKeyStore.jks");
    FileUtils.writeStringToFile(keyStore, "1234", StandardCharsets.UTF_8);
    File keyStorePassword = folder.newFile("testKeyStorePassword");
    FileUtils.writeStringToFile(keyStorePassword, "password", StandardCharset.UTF_8);

    System.setProperty("replication.ssl.customKeyStore", keyStore.getAbsolutePath());
    System.setProperty(
        "replication.ssl.customKeyStorePassword", keyStorePassword.getAbsolutePath());

    WebHdfsNodeAdapterTestFactory webHdfsNodeAdapterTestFactory =
        new WebHdfsNodeAdapterTestFactory();

    NodeAdapter webHdfsNodeAdapter = webHdfsNodeAdapterTestFactory.create(secureUrlWithPath);

    assertThat(webHdfsNodeAdapter.getClass().isAssignableFrom(WebHdfsNodeAdapter.class), is(true));
    assertThat(
        ((WebHdfsNodeAdapter) webHdfsNodeAdapter).getWebHdfsUrl().toString(),
        is(secureUrlWithPath.toString()));
  }

  @Test
  public void testGetType() {
    assertThat(webHdfsNodeAdapterFactory.getType(), is(NodeAdapterType.WEBHDFS));
  }

  /**
   * Extends {@link WebHdfsNodeAdapterFactory} to allow for a {@link KeyStore} that uses a mocked
   * {@link KeyStoreSpi}
   */
  private static class WebHdfsNodeAdapterTestFactory extends WebHdfsNodeAdapterFactory {
    public WebHdfsNodeAdapterTestFactory() {
      super();
    }

    /**
     * Creates a {@link KeyStore} using a mock {@link KeyStoreSpi} that overrides behavior for
     * {@code engineAliases()}
     *
     * @return a {@link KeyStore} instantiated using a mock {@link KeyStoreSpi}
     */
    @Override
    KeyStore getKeyStore() {
      KeyStoreSpi mockKeyStoreSpi = mock(KeyStoreSpi.class);
      KeyStore mockKeyStore = new KeyStore(mockKeyStoreSpi, null, "test") {};

      when(mockKeyStoreSpi.engineAliases())
          .thenReturn(
              new Enumeration<String>() {
                @Override
                public boolean hasMoreElements() {
                  return false;
                }

                @Override
                public String nextElement() {
                  return null;
                }
              });

      return mockKeyStore;
    }
  }
}
