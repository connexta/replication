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

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.codice.ddf.admin.api.report.ErrorMessage;
import org.codice.ddf.admin.common.fields.base.BaseListField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ditto.replication.admin.query.ReplicationMessages;

public class Iso8601Field extends StringField {

  public static final String DEFAULT_DATE_FIELD_NAME = "iso8601";

  public static final String DEFAULT_DATE_TYPE_NAME = "Iso8601";

  public static final String DESCRIPTION =
      "A date represented as a string. "
          + "Dates should have an ISO-8601 format and be represented in UTC time. "
          + "For example: '2011-12-03T10:15:30Z'";

  protected Iso8601Field(String fieldName, String fieldTypeName, String description) {
    super(fieldName, fieldTypeName, description);
  }

  public Iso8601Field(String fieldName) {
    this(fieldName, DEFAULT_DATE_TYPE_NAME, DESCRIPTION);
  }

  public Iso8601Field() {
    this(DEFAULT_DATE_FIELD_NAME);
  }

  public void setValue(Instant instant) {
    if (instant != null) {
      super.setValue(instant.toString());
    } else {
      super.setValue(null);
    }
  }

  @Override
  public void setValue(String value) {
    super.setValue(value);
  }

  @Override
  public List<ErrorMessage> validate() {
    List<ErrorMessage> validationMsgs = super.validate();
    if (!validationMsgs.isEmpty()) {
      return validationMsgs;
    }

    final String date = getValue();
    if (date != null) {
      try {
        Instant.parse(date);
      } catch (DateTimeParseException e) {
        validationMsgs.add(ReplicationMessages.invalidIso8601(getPath()));
      }
    }

    return validationMsgs;
  }

  @Override
  public Set<String> getErrorCodes() {
    return new ImmutableSet.Builder<String>()
        .addAll(super.getErrorCodes())
        .add(ReplicationMessages.INVALID_ISO8601)
        .build();
  }

  public static class ListImpl extends BaseListField<Iso8601Field> {

    public static final String DEFAULT_FIELD_NAME = "dates";
    private Callable<Iso8601Field> newDate;

    public ListImpl(String fieldName) {
      super(fieldName);
      newDate = Iso8601Field::new;
    }

    public ListImpl() {
      this(DEFAULT_FIELD_NAME);
    }

    @Override
    public Callable<Iso8601Field> getCreateListEntryCallable() {
      return newDate;
    }
  }
}
