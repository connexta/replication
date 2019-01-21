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

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Callable;
import org.codice.ddf.admin.common.fields.base.BaseListField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: delete or complete this class depending on the outcome of future api decisions
/** A decorator for the StringField class intended to keep our String date in the proper format. */
public class DateField extends StringField {

  public static final Logger LOGGER = LoggerFactory.getLogger(DateField.class);

  public static final String DEFAULT_DATE_FIELD_NAME = "date";

  public static final String DEFAULT_DATE_TYPE_NAME = "Date";

  public static final String DESCRIPTION =
      "A date represented as a string. "
          + "Dates should have an ISO-8601 format and be represented in UTC time. "
          + "For example: '2011-12-03T10:15:30Z'";

  protected DateField(String fieldName, String fieldTypeName, String description) {
    super(fieldName, fieldTypeName, description);
  }

  public DateField(String fieldName) {
    this(fieldName, DEFAULT_DATE_TYPE_NAME, DESCRIPTION);
  }

  public DateField() {
    this(DEFAULT_DATE_FIELD_NAME);
  }

  public void setValue(Instant instant) {
    super.setValue(instant.toString());
  }

  @Override
  public void setValue(String value) {
    if (value != null) {
      try {
        Instant.parse(value);
      } catch (DateTimeParseException e) {
        // TODO:  the date format should be handled during validation and an error message added to
        // the
        // response when validation is implemented.
        LOGGER.debug("Invalid date format received {}", value, e);
      }
    }
    super.setValue(value);
  }

  public static class ListImpl extends BaseListField<DateField> {

    public static final String DEFAULT_FIELD_NAME = "dates";
    private Callable<DateField> newDate;

    public ListImpl(String fieldName) {
      super(fieldName);
      newDate = DateField::new;
    }

    public ListImpl() {
      this(DEFAULT_FIELD_NAME);
    }

    @Override
    public Callable<DateField> getCreateListEntryCallable() {
      return newDate;
    }
  }
}
