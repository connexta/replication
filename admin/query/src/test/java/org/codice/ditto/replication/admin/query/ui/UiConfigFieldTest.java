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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Collectors;
import org.codice.ddf.admin.api.Field;
import org.junit.Before;
import org.junit.Test;

public class UiConfigFieldTest {

  private UiConfigField configField;

  @Before
  public void setup() {
    configField = new UiConfigField();
  }

  @Test
  public void settersAndGetters() {
    configField.header("header");
    configField.footer("footer");
    configField.color("color");
    configField.background("background");
    assertThat(configField.header(), is("header"));
    assertThat(configField.footer(), is("footer"));
    assertThat(configField.color(), is("color"));
    assertThat(configField.background(), is("background"));
    assertThat(configField.headerField().getValue(), is("header"));
    assertThat(configField.footerField().getValue(), is("footer"));
    assertThat(configField.colorField().getValue(), is("color"));
    assertThat(configField.backgroundField().getValue(), is("background"));
  }

  @Test
  public void getFields() {
    List<Field> fields = configField.getFields();
    assertThat(
        fields.stream().map(Field::getFieldName).collect(Collectors.toList()),
        hasItems("header", "footer", "color", "background"));
  }
}
