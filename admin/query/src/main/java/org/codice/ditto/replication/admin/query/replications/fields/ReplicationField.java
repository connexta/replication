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
import org.codice.ditto.replication.admin.query.status.fields.ReplicationStats;

public class ReplicationField extends BaseObjectField {

  public static final String DESCRIPTION =
      "Contains instructions on how replication should be performed.";

  private PidField id;

  private StringField name;

  private ReplicationSiteField source;

  private ReplicationSiteField destination;

  private CqlExpressionField filter;

  private BooleanField biDirectional;

  private BooleanField suspended;

  private ReplicationStats stats;

  private Iso8601Field modified;

  private IntegerField version;

  private IntegerField priority;

  public ReplicationField() {
    super("replication", "ReplicationConfig", DESCRIPTION);
    id = new PidField("id");
    name = new StringField("name");
    source = new ReplicationSiteField("source");
    destination = new ReplicationSiteField("destination");
    filter = new CqlExpressionField("filter");
    biDirectional = new BooleanField("biDirectional");
    suspended = new BooleanField("suspended");
    modified = new Iso8601Field("modified");
    version = new IntegerField("version");
    priority = new IntegerField("priority");
    stats = new ReplicationStats();
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

  public ReplicationField stats(ReplicationStats stats) {
    this.stats = stats;
    return this;
  }

  public ReplicationStats getStats() {
    return this.stats;
  }

  public ReplicationField suspended(Boolean suspended) {
    this.suspended.setValue(suspended);
    return this;
  }

  public ReplicationField modified(String modified) {
    this.modified.setValue(modified);
    return this;
  }

  public ReplicationField modified(Date modified) {
    this.modified.setValue(getInstantOrNull(modified));
    return this;
  }

  public ReplicationField version(int version) {
    this.version.setValue(version);
    return this;
  }

  public ReplicationField priority(int priority) {
    this.priority.setValue(priority);
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

  public Boolean suspended() {
    return suspended.getValue();
  }

  public Iso8601Field modified() {
    return modified;
  }

  public IntegerField version() {
    return version;
  }

  public IntegerField priority() {
    return priority;
  }

  @Override
  public List<Field> getFields() {
    return ImmutableList.of(
        id,
        name,
        source,
        destination,
        filter,
        biDirectional,
        suspended,
        modified,
        version,
        priority,
        stats);
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
