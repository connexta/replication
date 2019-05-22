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
package org.codice.ditto.replication.admin.query.sites.fields;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.common.fields.base.BaseListField;
import org.codice.ddf.admin.common.fields.base.BaseObjectField;
import org.codice.ddf.admin.common.fields.base.scalar.IntegerField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.replications.fields.Iso8601Field;

public class ReplicationSiteField extends BaseObjectField {

  private static final String DEFAULT_FIELD_NAME = "site";

  private static final String FIELD_TYPE_NAME = "ReplicationSite";

  private static final String DESCRIPTION = "Contains information about a Site.";

  private PidField id;

  private StringField name;

  private AddressField address;

  private StringField rootContext;

  private Iso8601Field modified;

  private IntegerField version;

  private RemoteManagedField remoteManaged;

  public ReplicationSiteField() {
    this(DEFAULT_FIELD_NAME);
  }

  public ReplicationSiteField(String fieldName) {
    super(fieldName, FIELD_TYPE_NAME, DESCRIPTION);
    this.id = new PidField("id");
    this.name = new StringField("name");
    this.address = new AddressField();
    this.rootContext = new StringField("rootContext");
    this.modified = new Iso8601Field("modified");
    this.version = new IntegerField("version");
    this.remoteManaged = new RemoteManagedField();
  }

  public ReplicationSiteField id(String id) {
    this.id.setValue(id);
    return this;
  }

  public ReplicationSiteField name(String name) {
    this.name.setValue(name);
    return this;
  }

  public ReplicationSiteField address(AddressField address) {
    this.address = address;
    return this;
  }

  public ReplicationSiteField rootContext(String context) {
    this.rootContext.setValue(context);
    return this;
  }

  public ReplicationSiteField modified(Date modified) {
    this.modified.setValue(getInstantOrNull(modified));
    return this;
  }

  public ReplicationSiteField version(int version) {
    this.version.setValue(version);
    return this;
  }

  public ReplicationSiteField isDisableLocal(boolean isDisableLocal) {
    this.remoteManaged.setValue(isDisableLocal);
    return this;
  }

  private static Instant getInstantOrNull(Date date) {
    if (date != null) {
      return date.toInstant();
    }
    return null;
  }

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

  public AddressField address() {
    return address;
  }

  public StringField rootContext() {
    return rootContext;
  }

  public Iso8601Field modified() {
    return modified;
  }

  public IntegerField version() {
    return version;
  }

  public boolean isDisabledLocal() {
    return remoteManaged.getValue();
  }

  @Override
  public List<Field> getFields() {
    return ImmutableList.of(id, name, address, rootContext, modified, version, remoteManaged);
  }

  public static class ListImpl extends BaseListField<ReplicationSiteField> {

    public static final String DEFAULT_FIELD_NAME = "sites";

    public ListImpl() {
      super(DEFAULT_FIELD_NAME);
    }

    @Override
    public Callable<ReplicationSiteField> getCreateListEntryCallable() {
      return ReplicationSiteField::new;
    }
  }
}
