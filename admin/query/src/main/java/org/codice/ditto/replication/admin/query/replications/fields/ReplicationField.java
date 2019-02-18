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

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.common.fields.base.BaseListField;
import org.codice.ddf.admin.common.fields.base.BaseObjectField;
import org.codice.ddf.admin.common.fields.base.scalar.BooleanField;
import org.codice.ddf.admin.common.fields.base.scalar.IntegerField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;

public class ReplicationField extends BaseObjectField {

  public static final String DESCRIPTION =
      "Contains instructions on how replication should be performed.";

  private PidField id;

  private StringField name;

  private ReplicationSiteField source;

  private ReplicationSiteField destination;

  private CqlExpressionField filter;

  private IntegerField itemsTransferred;

  private StringField dataTransferred;

  private BooleanField biDirectional;

  private ReplicationStatus status;

  private DateField lastRun;

  private DateField firstRun;

  private DateField lastSuccess;

  public ReplicationField() {
    super("replication", "ReplicationConfig", DESCRIPTION);
    id = new PidField("id");
    name = new StringField("name");
    source = new ReplicationSiteField("source");
    destination = new ReplicationSiteField("destination");
    filter = new CqlExpressionField("filter");
    itemsTransferred = new IntegerField("itemsTransferred");
    dataTransferred = new StringField("dataTransferred");
    biDirectional = new BooleanField("biDirectional");
    status = new ReplicationStatus();
    lastRun = new DateField("lastRun");
    firstRun = new DateField("firstRun");
    lastSuccess = new DateField("lastSuccess");
  }

  // setters
  public ReplicationField id(String id) {
    this.id.setValue(id);
    return this;
  }

  public ReplicationField name(String name) {
    this.name.setValue(name);
    return this;
  }

  public ReplicationField source(ReplicationSiteField source) {
    this.source.setValue(source.getValue());
    return this;
  }

  public ReplicationField destination(ReplicationSiteField destination) {
    this.destination.setValue(destination.getValue());
    return this;
  }

  public ReplicationField filter(String cqlExpression) {
    this.filter.setValue(cqlExpression);
    return this;
  }

  public ReplicationField biDirectional(Boolean biDirectional) {
    this.biDirectional.setValue(biDirectional);
    return this;
  }

  public ReplicationField itemsTransferred(int itemsTransferred) {
    this.itemsTransferred.setValue(itemsTransferred);
    return this;
  }

  public ReplicationField dataTransferred(String dataTransferred) {
    this.dataTransferred.setValue(dataTransferred);
    return this;
  }

  public ReplicationField status(String status) {
    this.status.setValue(status);
    return this;
  }

  public ReplicationField lastRun(String last) {
    this.lastRun.setValue(last);
    return this;
  }

  public ReplicationField lastRun(Date last) {
    this.lastRun.setValue(getInstantOrNull(last));
    return this;
  }

  public ReplicationField firstRun(String first) {
    this.firstRun.setValue(first);
    return this;
  }

  public ReplicationField firstRun(Date first) {
    this.firstRun.setValue(getInstantOrNull(first));
    return this;
  }

  public ReplicationField lastSuccess(String success) {
    this.lastSuccess.setValue(success);
    return this;
  }

  public ReplicationField lastSuccess(Date success) {
    this.lastSuccess.setValue(getInstantOrNull(success));
    return this;
  }

  private Instant getInstantOrNull(Date date) {
    if (date != null) {
      return date.toInstant();
    }
    return null;
  }

  // getters
  public String id() {
    return id.getValue();
  }

  public PidField idField() {
    return id;
  }

  public String name() {
    return name.getValue();
  }

  public StringField nameField() {
    return name;
  }

  public ReplicationSiteField source() {
    return source;
  }

  public ReplicationSiteField destination() {
    return destination;
  }

  public String filter() {
    return filter.getValue();
  }

  public CqlExpressionField filterField() {
    return filter;
  }

  public Boolean biDirectional() {
    return biDirectional.getValue();
  }

  public int itemsTransferred() {
    return itemsTransferred.getValue();
  }

  public IntegerField itemsTransferredField() {
    return itemsTransferred;
  }

  public String dataTransferred() {
    return dataTransferred.getValue();
  }

  public StringField dataTransferredField() {
    return dataTransferred;
  }

  public ReplicationStatus status() {
    return status;
  }

  public DateField lastRun() {
    return lastRun;
  }

  public DateField lastSuccess() {
    return lastSuccess;
  }

  public DateField firstRun() {
    return firstRun;
  }

  @Override
  public List<Field> getFields() {
    return ImmutableList.of(
        id,
        name,
        source,
        destination,
        filter,
        itemsTransferred,
        dataTransferred,
        biDirectional,
        status,
        lastRun,
        lastSuccess,
        firstRun);
  }

  public static class ListImpl extends BaseListField<ReplicationField> {

    public static final String DEFAULT_FIELD_NAME = "replications";

    public ListImpl() {
      super(DEFAULT_FIELD_NAME);
    }

    @Override
    public Callable<ReplicationField> getCreateListEntryCallable() {
      return ReplicationField::new;
    }
  }
}
