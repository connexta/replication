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
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;

public class DeleteReplication extends BaseFunctionField<BooleanField> {

  public static final String FIELD_NAME = "deleteReplication";

  public static final String DESCRIPTION =
      "Deletes a Replication and its history (statistics). Optionally delete the data of the Replication. Deleting data will delete "
          + "any local resources and metadata that were replicated by this Replication, but not any resources replicated to a remote Node.";

  public static final BooleanField RETURN_TYPE = new BooleanField();

  private PidField id;

  private BooleanField deleteData;

  private ReplicationUtils replicationUtils;

  public DeleteReplication(ReplicationUtils replicationUtils) {
    super(FIELD_NAME, DESCRIPTION);
    this.replicationUtils = replicationUtils;
    id = new PidField("id");
    deleteData = new BooleanField("deleteData");
    deleteData.setValue(false);

    id.isRequired();
  }

  @Override
  public BooleanField performFunction() {
    BooleanField successful = new BooleanField();
    successful.setValue(replicationUtils.markConfigDeleted(id.getValue(), deleteData.getValue()));

    return successful;
  }

  @Override
  public void validate() {
    super.validate();
    if (containsErrorMsgs()) {
      return;
    }

    if (!replicationUtils.configExists(id.getValue())) {
      addErrorMessage(ReplicationMessages.configDoesNotExist());
    }
  }

  @Override
  public BooleanField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(id, deleteData);
  }

  @Override
  public FunctionField<BooleanField> newInstance() {
    return new DeleteReplication(replicationUtils);
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of(ReplicationMessages.CONFIG_DOES_NOT_EXIST);
  }
}
