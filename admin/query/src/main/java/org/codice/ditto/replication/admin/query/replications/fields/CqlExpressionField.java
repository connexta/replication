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
package org.codice.ditto.replication.admin.query.replications.fields;

import java.util.concurrent.Callable;
import org.codice.ddf.admin.common.fields.base.BaseListField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;

/**
 * A decorator for the StringField class intended to keep our cql expressions in the proper format.
 */
public class CqlExpressionField extends StringField {

  public static final String DEFAULT_DATE_FIELD_NAME = "cqlExpression";

  public static final String DEFAULT_DATE_TYPE_NAME = "CqlExpression";

  public static final String DESCRIPTION = "A cqlExpression represented as a string.";

  protected CqlExpressionField(String fieldName, String fieldTypeName, String description) {
    super(fieldName, fieldTypeName, description);
  }

  public CqlExpressionField(String fieldName) {
    this(fieldName, DEFAULT_DATE_TYPE_NAME, DESCRIPTION);
  }

  public CqlExpressionField() {
    this(DEFAULT_DATE_FIELD_NAME);
  }

  // TODO: override the validate method and implement cql validation.

  public static class ListImpl extends BaseListField<CqlExpressionField> {

    public static final String DEFAULT_FIELD_NAME = "cqlExpressions";
    private Callable<CqlExpressionField> newCqlExpression;

    public ListImpl(String fieldName) {
      super(fieldName);
      newCqlExpression = CqlExpressionField::new;
    }

    public ListImpl() {
      this(DEFAULT_FIELD_NAME);
    }

    @Override
    public Callable<CqlExpressionField> getCreateListEntryCallable() {
      return newCqlExpression;
    }
  }
}
