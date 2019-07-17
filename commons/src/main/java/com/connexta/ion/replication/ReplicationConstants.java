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
package com.connexta.ion.replication;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ReplicationConstants {

  public static final String TLS_PROTOCOL = "TLSv1.2";

  private ReplicationConstants() {}

  public static String getCertAlias() {
    try {
      return System.getProperty(
          "javax.net.ssl.certAlias", InetAddress.getLocalHost().getCanonicalHostName());
    } catch (UnknownHostException e) {
      return "localhost";
    }
  }

  public static String getKeystore() {
    return System.getProperty("javax.net.ssl.keyStore");
  }
}
