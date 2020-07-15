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

import com.connexta.replication.data.ReplicationConstants;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.codice.ditto.replication.api.AdapterException;
import org.codice.ditto.replication.api.NodeAdapter;
import org.codice.ditto.replication.api.NodeAdapterFactory;
import org.codice.ditto.replication.api.NodeAdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating {@link WebHdfsNodeAdapter} instances for the {@link
 * org.codice.ditto.replication.api.Replicator}
 */
public class WebHdfsNodeAdapterFactory implements NodeAdapterFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsNodeAdapterFactory.class);

  private static final int HTTPS_PORT = 443;

  private final String customKeystorePath = ReplicationConstants.getCustomKeystore();
  private final String customKeystorePassword = ReplicationConstants.getCustomKeystorePassword();

  public WebHdfsNodeAdapterFactory() {
    LOGGER.debug("Created a WebHdfsNodeAdapterFactory");
  }

  @Override
  public NodeAdapter create(URL url) {

    String protocol;
    CloseableHttpClient httpClient;

    if (url.getPort() == HTTPS_PORT) {
      protocol = "https://";

      KeyStore keyStore;
      SSLContext sslContext;
      try {
        keyStore = readCustomKeystore();

        sslContext =
            SSLContexts.custom()
                .loadKeyMaterial(keyStore, customKeystorePassword.toCharArray())
                .build();
      } catch (GeneralSecurityException e) {
        throw new AdapterException("Failed to create adapter", e);
      }
      httpClient =
          HttpClientBuilder.create().setSSLContext(sslContext).disableRedirectHandling().build();
    } else {
      protocol = "http://";
      httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
    }

    String baseUrl = protocol + url.getHost() + ":" + url.getPort() + url.getPath();

    if (!baseUrl.endsWith("/")) {
      baseUrl = baseUrl.concat("/");
    }

    try {
      return new WebHdfsNodeAdapter(new URL(baseUrl), httpClient);
    } catch (MalformedURLException e) {
      throw new AdapterException("Failed to create adapter", e);
    }
  }

  @Override
  public NodeAdapterType getType() {
    return NodeAdapterType.WEBHDFS;
  }

  private KeyStore readCustomKeystore() {
    try (FileInputStream keyStoreStream = new FileInputStream(customKeystorePath)) {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(keyStoreStream, customKeystorePassword.toCharArray());
      return keyStore;
    } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
      throw new AdapterException("Failed to read the custom keystore", e);
    }
  }
}
