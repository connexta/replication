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

import static org.codice.ditto.replication.admin.query.ReplicationUtils.updateReplication;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;

public class UpdateReplication extends BaseFunctionField<ReplicationField> {

  public static final String FIELD_NAME = "updateReplication";

  public static final String DESCRIPTION = "Updates a replication.";

  public static final ReplicationField RETURN_TYPE = new ReplicationField();

  private PidField id;

  private StringField name;

  private PidField source;

  private PidField destination;

  private StringField filter;

  public UpdateReplication() {
    super(FIELD_NAME, DESCRIPTION);

    id = new PidField("id");
    name = new StringField("name");
    source = new PidField("sourceId");
    destination = new PidField("destinationId");
    filter = new StringField("filter");
  }

  @Override
  public ReplicationField performFunction() {
    return updateReplication(
        id.getValue(),
        name.getValue(),
        source.getValue(),
        destination.getValue(),
        filter.getValue());
  }

  @Override
  public ReplicationField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(id, name, source, destination, filter);
  }

  @Override
  public FunctionField<ReplicationField> newInstance() {
    return new UpdateReplication();
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of();
  }
}
