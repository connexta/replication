package com.connexta.replication.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
  public void testLoadBootSystemProperties() {
    System.setProperty(
        "ssl.system.properties",
        getClass().getClassLoader().getResource("ssl.properties").getFile());
    Main.loadBootSystemProperties();
    assertThat(System.getProperty("javax.net.ssl.trustStorePassword"), is("changeit"));
    assertThat(System.getProperty("javax.net.ssl.keyStorePassword"), is("changeit"));
    assertThat(System.getProperty("javax.net.ssl.trustStoreType"), is("jks"));
    assertThat(System.getProperty("javax.net.ssl.keyStoreType"), is("jks"));
  }

  @Test
  public void testLoadBootSystemPropertiesNoFileDefined() {
    Main.loadBootSystemProperties();
    assertThat(System.getProperty("javax.net.ssl.trustStorePassword"), is(nullValue()));
    assertThat(System.getProperty("javax.net.ssl.keyStorePassword"), is(nullValue()));
    assertThat(System.getProperty("javax.net.ssl.trustStoreType"), is(nullValue()));
    assertThat(System.getProperty("javax.net.ssl.keyStoreType"), is(nullValue()));
  }

  @Test
  public void testLoadBootSystemPropertiesWithExistingProperty() {
    System.setProperty(
        "ssl.system.properties",
        getClass().getClassLoader().getResource("ssl.properties").getFile());
    System.setProperty("javax.net.ssl.trustStorePassword", "mypass");
    Main.loadBootSystemProperties();
    assertThat(System.getProperty("javax.net.ssl.trustStorePassword"), is("mypass"));
    assertThat(System.getProperty("javax.net.ssl.keyStorePassword"), is("changeit"));
    assertThat(System.getProperty("javax.net.ssl.trustStoreType"), is("jks"));
    assertThat(System.getProperty("javax.net.ssl.keyStoreType"), is("jks"));
  }

  @Test
  public void testLoadBootSystemPropertiesBadProperties() {
    System.setProperty(
        "ssl.system.properties", getClass().getClassLoader().getResource("bad.json").getFile());
    Main.loadBootSystemProperties();
    assertThat(System.getProperty("javax.net.ssl.trustStorePassword"), is(nullValue()));
    assertThat(System.getProperty("javax.net.ssl.keyStorePassword"), is(nullValue()));
    assertThat(System.getProperty("javax.net.ssl.trustStoreType"), is(nullValue()));
    assertThat(System.getProperty("javax.net.ssl.keyStoreType"), is(nullValue()));
  }
}
