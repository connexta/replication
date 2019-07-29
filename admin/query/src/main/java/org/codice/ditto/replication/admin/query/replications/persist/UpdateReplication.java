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
package org.codice.ditto.replication.admin.query.replications.persist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ddf.admin.common.fields.base.scalar.BooleanField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.ReplicationUtils;

public class UpdateReplication extends BaseFunctionField<BooleanField> {

  public static final String FIELD_NAME = "updateReplication";

  public static final String DESCRIPTION = "Updates a replication.";

  public static final BooleanField RETURN_TYPE = new BooleanField();

  private PidField id;

  private StringField name;

  private PidField source;

  private PidField destination;

  private StringField filter;

  private BooleanField biDirectional;

  private BooleanField suspended;

  private BooleanField metadataOnly;

  private ReplicationUtils replicationUtils;

  public UpdateReplication(ReplicationUtils replicationUtils) {
    super(FIELD_NAME, DESCRIPTION);
    this.replicationUtils = replicationUtils;
    id = new PidField("id");
    name = new StringField("name");
    source = new PidField("sourceId");
    destination = new PidField("destinationId");
    filter = new StringField("filter");
    biDirectional = new BooleanField("biDirectional");
    suspended = new BooleanField("suspended");
    metadataOnly = new BooleanField("metadataOnly");
    id.isRequired(true);
  }

  @Override
  public BooleanField performFunction() {
    return new BooleanField(
        replicationUtils.updateReplication(
            id.getValue(),
            name.getValue(),
            source.getValue(),
            destination.getValue(),
            filter.getValue(),
            biDirectional.getValue()));
  }

  @Override
  public BooleanField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(
        id, name, source, destination, filter, biDirectional, suspended, metadataOnly);
  }

  @Override
  public FunctionField<BooleanField> newInstance() {
    return new UpdateReplication(replicationUtils);
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of();
  }
}
