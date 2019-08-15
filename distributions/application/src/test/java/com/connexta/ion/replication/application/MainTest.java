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
package com.connexta.ion.replication.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.codice.junit.rules.RestoreSystemProperties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MainTest {

  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  @Test
  public void testLoadSslExternalSystemProperties() {
    System.setProperty(
        "ssl.system.properties",
        getClass().getClassLoader().getResource("external-ssl.properties").getFile());
    Main.loadSslSystemProperties();
    assertThat(System.getProperty("javax.net.ssl.trustStorePassword"), is("pass"));
    assertThat(System.getProperty("javax.net.ssl.keyStorePassword"), is("pass"));
    assertThat(System.getProperty("javax.net.ssl.trustStoreType"), is("abc"));
    assertThat(System.getProperty("javax.net.ssl.keyStoreType"), is("abc"));
  }

  @Test
  public void testLoadSslSystemPropertiesNoExternal() {
    Main.loadSslSystemProperties();
    assertThat(System.getProperty("javax.net.ssl.trustStorePassword"), is("changeit"));
    assertThat(System.getProperty("javax.net.ssl.keyStorePassword"), is("changeit"));
    assertThat(System.getProperty("javax.net.ssl.trustStoreType"), is("jks"));
    assertThat(System.getProperty("javax.net.ssl.keyStoreType"), is("jks"));
  }

  @Test
  public void testLoadSslSystemPropertiesWithExistingProperty() {
    System.setProperty("javax.net.ssl.trustStorePassword", "mypass");
    Main.loadSslSystemProperties();
    assertThat(System.getProperty("javax.net.ssl.trustStorePassword"), is("mypass"));
    assertThat(System.getProperty("javax.net.ssl.keyStorePassword"), is("changeit"));
    assertThat(System.getProperty("javax.net.ssl.trustStoreType"), is("jks"));
    assertThat(System.getProperty("javax.net.ssl.keyStoreType"), is("jks"));
  }

  @Test
  public void testLoadBootSystemPropertiesBadProperties() {
    System.setProperty(
        "ssl.system.properties", getClass().getClassLoader().getResource("bad.json").getFile());
    Main.loadSslSystemProperties();
    assertThat(System.getProperty("javax.net.ssl.trustStorePassword"), is("changeit"));
    assertThat(System.getProperty("javax.net.ssl.keyStorePassword"), is("changeit"));
    assertThat(System.getProperty("javax.net.ssl.trustStoreType"), is("jks"));
    assertThat(System.getProperty("javax.net.ssl.keyStoreType"), is("jks"));
  }
}
