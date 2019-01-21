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

import static org.codice.ditto.replication.admin.query.ReplicationUtils.deleteConfig;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ddf.admin.common.fields.base.scalar.BooleanField;
import org.codice.ddf.admin.common.fields.common.PidField;

public class DeleteReplication extends BaseFunctionField<BooleanField> {

  public static final String FIELD_NAME = "deleteReplication";

  public static final String DESCRIPTION = "Deletes a replication.";

  public static final BooleanField RETURN_TYPE = new BooleanField();

  private PidField id;

  public DeleteReplication() {
    super(FIELD_NAME, DESCRIPTION);

    id = new PidField("id");
  }

  @Override
  public BooleanField performFunction() {
    BooleanField successful = new BooleanField();
    successful.setValue(deleteConfig(id.getValue()));

    return successful;
  }

  @Override
  public BooleanField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(id);
  }

  @Override
  public FunctionField<BooleanField> newInstance() {
    return new DeleteReplication();
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of();
  }
}
