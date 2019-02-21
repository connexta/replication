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
package org.codice.ditto.replication.admin.query.sites.persist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ddf.admin.common.fields.base.scalar.BooleanField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;

public class DeleteReplicationSite extends BaseFunctionField<BooleanField> {

  public static final String FIELD_NAME = "deleteReplicationSite";

  public static final String DESCRIPTION = "Deletes a replication site.";

  public static final BooleanField RETURN_TYPE = new BooleanField();

  private PidField id;

  private ReplicationUtils replicationUtils;

  public DeleteReplicationSite(ReplicationUtils replicationUtils) {
    super(FIELD_NAME, DESCRIPTION);

    this.replicationUtils = replicationUtils;
    id = new PidField("id");
  }

  @Override
  public BooleanField performFunction() {
    BooleanField success = new BooleanField();
    success.setValue(replicationUtils.deleteSite(id.getValue()));
    return success;
  }

  @Override
  public void validate() {
    super.validate();
    if (containsErrorMsgs()) {
      return;
    }

    ListField<ReplicationField> repFields = replicationUtils.getReplications();
    String idToDelete = id.getValue();
    for (ReplicationField repField : repFields.getList()) {
      if (idToDelete.equals(repField.source().id())
          || idToDelete.equals(repField.destination().id())) {
        addErrorMessage(ReplicationMessages.siteInUse());
        return;
      }
    }
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
    return new DeleteReplicationSite(replicationUtils);
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of();
  }
}
