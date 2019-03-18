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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetUiConfigTest {

  @Mock UiConfigurationFactory factory;

  GetUiConfig getUiConfig;

  @Before
  public void setup() {
    getUiConfig = new GetUiConfig(factory);
  }

  @Test
  public void performFunction() {
    getUiConfig.performFunction();
    verify(factory).getConfig();
  }

  @Test
  public void getFunctionErrorCodes() {
    assertThat(getUiConfig.getFunctionErrorCodes(), is(Collections.emptySet()));
  }

  @Test
  public void getArguments() {
    assertThat(getUiConfig.getArguments(), is(Collections.emptyList()));
  }

  @Test
  public void getReturnType() {
    assertThat(getUiConfig.getReturnType(), is(instanceOf(UiConfigField.class)));
  }

  @Test
  public void newInstance() {
    FunctionField<UiConfigField> newInstance = getUiConfig.newInstance();
    assertThat(newInstance, is(instanceOf(GetUiConfig.class)));
    assertThat(newInstance.getReturnType(), is(instanceOf(UiConfigField.class)));
  }
}
