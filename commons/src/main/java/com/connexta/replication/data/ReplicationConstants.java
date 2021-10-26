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
package com.connexta.replication.data;

import org.codice.ddf.configuration.SystemBaseUrl;

public class ReplicationConstants {

  public static final String TLS_PROTOCOL = "TLSv1.2";

  private ReplicationConstants() {}

  public static String getCertAlias() {
    return System.getProperty("javax.net.ssl.certAlias", SystemBaseUrl.EXTERNAL.getHost());
  }

  public static String getKeystore() {
    return System.getProperty("javax.net.ssl.keyStore");
  }

  public static String getCustomKeystore() {
    return System.getProperty("replication.ssl.customKeyStore");
  }

  public static String getCustomKeystorePassword() {
    return System.getProperty("replication.ssl.customKeyStorePassword");
  }

  public static String getCustomKeystoreType() {
    return System.getProperty("replication.ssl.customKeyStoreType");
  }
}
