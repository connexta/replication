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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.common.fields.base.BaseObjectField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;

public class UiConfigField extends BaseObjectField {

  private static final String DEFAULT_FIELD_NAME = "uiConfig";

  private static final String FIELD_TYPE_NAME = "UiConfig";

  private static final String DESCRIPTION = "Contains common properties for UI components.";

  private StringField header;

  private StringField footer;

  private StringField color;

  private StringField background;

  public UiConfigField() {
    this(DEFAULT_FIELD_NAME);
  }

  public UiConfigField(String fieldName) {
    super(fieldName, FIELD_TYPE_NAME, DESCRIPTION);
    this.header = new StringField("header");
    this.footer = new StringField("footer");
    this.color = new StringField("color");
    this.background = new StringField("background");
  }

  public UiConfigField header(String header) {
    this.header.setValue(header);
    return this;
  }

  public UiConfigField footer(String footer) {
    this.footer.setValue(footer);
    return this;
  }

  public UiConfigField color(String color) {
    this.color.setValue(color);
    return this;
  }

  public UiConfigField background(String background) {
    this.background.setValue(background);
    return this;
  }

  public String header() {
    return header.getValue();
  }

  public String footer() {
    return footer.getValue();
  }

  public String color() {
    return color.getValue();
  }

  public String background() {
    return background.getValue();
  }

  public StringField headerField() {
    return header;
  }

  public StringField footerField() {
    return footer;
  }

  public StringField colorField() {
    return color;
  }

  public StringField backgroundField() {
    return background;
  }

  @Override
  public List<Field> getFields() {
    return ImmutableList.of(header, footer, color, background);
  }
}
