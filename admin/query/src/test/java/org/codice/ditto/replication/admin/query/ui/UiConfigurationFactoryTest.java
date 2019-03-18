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
package org.codice.ditto.replication.admin.query.ui;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Dictionary;
import org.codice.ddf.configuration.DictionaryMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@RunWith(MockitoJUnitRunner.class)
public class UiConfigurationFactoryTest {

  @Mock ConfigurationAdmin configurationAdmin;

  UiConfigurationFactory factory;

  @Before
  public void setup() {
    factory = new UiConfigurationFactory(configurationAdmin);
  }

  @Test
  public void getConfig() throws Exception {
    Configuration config = mock(Configuration.class);
    Configuration[] configArray = new Configuration[] {};
    when(configurationAdmin.listConfigurations(anyString())).thenReturn(configArray);
    when(configurationAdmin.getConfiguration(any(), any())).thenReturn(config);
    Dictionary<String, Object> properties = new DictionaryMap<>();
    properties.put("header", "header");
    properties.put("footer", "footer");
    properties.put("color", "white");
    properties.put("background", "green");
    when(config.getProperties()).thenReturn(properties);

    UiConfigField configField = factory.getConfig();
    assertThat(configField.header(), is("header"));
    assertThat(configField.footer(), is("footer"));
    assertThat(configField.color(), is("white"));
    assertThat(configField.background(), is("green"));
  }

  @Test
  public void getConfigNullConfigList() throws Exception {
    UiConfigField configField = factory.getConfig();
    assertThat(configField.header(), is(""));
    assertThat(configField.footer(), is(""));
    assertThat(configField.color(), is(""));
    assertThat(configField.background(), is(""));
  }

  @Test
  public void getConfigIOException() throws Exception {
    when(configurationAdmin.listConfigurations(anyString())).thenThrow(new IOException());
    UiConfigField configField = factory.getConfig();
    assertThat(configField.header(), is(""));
    assertThat(configField.footer(), is(""));
    assertThat(configField.color(), is(""));
    assertThat(configField.background(), is(""));
  }

  @Test
  public void getConfigInvalidSyntaxException() throws Exception {
    when(configurationAdmin.listConfigurations(anyString())).thenThrow(new IOException());
    UiConfigField configField = factory.getConfig();
    assertThat(configField.header(), is(""));
    assertThat(configField.footer(), is(""));
    assertThat(configField.color(), is(""));
    assertThat(configField.background(), is(""));
  }
}
