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
package org.codice.ditto.replication.admin.query.replications.discover;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;

public class GetReplications extends BaseFunctionField<ListField<ReplicationField>> {

  public static final String FIELD_NAME = "replications";

  public static final String DESCRIPTION = "Retrieves all currently saved replications.";

  private static final ListField<ReplicationField> RETURN_TYPE = new ReplicationField.ListImpl();

  private ReplicationUtils replicationUtils;

  public GetReplications(ReplicationUtils replicationUtils) {
    super(FIELD_NAME, DESCRIPTION);
    this.replicationUtils = replicationUtils;
  }

  @Override
  public ListField<ReplicationField> performFunction() {
    return replicationUtils.getReplications(true);
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of();
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of();
  }

  @Override
  public ListField<ReplicationField> getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public FunctionField<ListField<ReplicationField>> newInstance() {
    return new GetReplications(replicationUtils);
  }
}
