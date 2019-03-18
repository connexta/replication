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
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;

public class GetUiConfig extends BaseFunctionField<UiConfigField> {

  public static final String FIELD_NAME = "getUiConfig";

  public static final String DESCRIPTION = "Retrieves the ui configuration.";

  private static final UiConfigField RETURN_TYPE = new UiConfigField();

  private final UiConfigurationFactory factory;

  public GetUiConfig(UiConfigurationFactory factory) {
    super(FIELD_NAME, DESCRIPTION);
    this.factory = factory;
  }

  @Override
  public UiConfigField performFunction() {
    return factory.getConfig();
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of();
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of();
  }

  @Override
  public UiConfigField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public FunctionField<UiConfigField> newInstance() {
    return new GetUiConfig(factory);
  }
}
