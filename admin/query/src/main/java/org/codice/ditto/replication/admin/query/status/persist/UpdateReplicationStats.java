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
package org.codice.ditto.replication.admin.query.status.persist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ddf.admin.common.fields.base.scalar.BooleanField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ddf.admin.common.report.message.DefaultMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.status.fields.ReplicationStats;

public class UpdateReplicationStats extends BaseFunctionField<BooleanField> {

  private static final String FUNCTION_NAME = "updateReplicationStats";

  private static final String DESCRIPTION =
      "Updates an existing replication's stats based on the provided replication name.";

  private static final BooleanField RETURN_TYPE = new BooleanField();

  private final ReplicationUtils replicationUtils;

  private ReplicationStats stats;

  private StringField replicationName;

  public UpdateReplicationStats(ReplicationUtils replicationUtils) {
    super(FUNCTION_NAME, DESCRIPTION);
    this.replicationUtils = replicationUtils;

    stats = new ReplicationStats();
    replicationName = new StringField("replicationName");
    stats.isRequired(true);
    replicationName.isRequired(true);
  }

  @Override
  public BooleanField performFunction() {
    return new BooleanField(replicationUtils.updateReplicationStats(replicationName, stats));
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of(DefaultMessages.NO_EXISTING_CONFIG);
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(replicationName, stats);
  }

  @Override
  public BooleanField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public FunctionField<BooleanField> newInstance() {
    return new UpdateReplicationStats(replicationUtils);
  }

  @Override
  public void validate() {
    super.validate();
    if (containsErrorMsgs()) {
      return;
    }

    if (!replicationUtils.replicationConfigExists(replicationName.getValue())) {
      addErrorMessage(DefaultMessages.noExistingConfigError());
    }
  }
}
