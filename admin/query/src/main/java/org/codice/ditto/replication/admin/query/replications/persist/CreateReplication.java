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
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.replications.fields.ReplicationField;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;

public class CreateReplication extends BaseFunctionField<ReplicationField> {

  public static final String FIELD_NAME = "createReplication";

  public static final String DESCRIPTION =
      "Creates a replication. biDirectional defaults to false.";

  public static final ReplicationField RETURN_TYPE = new ReplicationField();

  private StringField name;

  private PidField source;

  private PidField destination;

  private StringField filter;

  private BooleanField biDirectional;

  private ReplicationUtils replicationUtils;

  public CreateReplication(ReplicationUtils replicationUtils) {
    super(FIELD_NAME, DESCRIPTION);
    this.replicationUtils = replicationUtils;
    name = new StringField("name");
    source = new PidField("sourceId");
    destination = new PidField("destinationId");
    filter = new StringField("filter");
    biDirectional = new BooleanField("biDirectional");
    name.isRequired(true);
    source.isRequired(true);
    destination.isRequired(true);
    filter.isRequired(true);
  }

  @Override
  public ReplicationField performFunction() {
    return replicationUtils.createReplication(
        name.getValue(),
        source.getValue(),
        destination.getValue(),
        filter.getValue(),
        biDirectional.getValue() == null ? false : biDirectional.getValue());
  }

  @Override
  public ReplicationField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(name, source, destination, filter, biDirectional);
  }

  @Override
  public FunctionField<ReplicationField> newInstance() {
    return new CreateReplication(replicationUtils);
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of(
        ReplicationMessages.DUPLICATE_CONFIGURATION,
        ReplicationMessages.SAME_SITE,
        ReplicationMessages.INVALID_FILTER,
        ReplicationMessages.SOURCE_DOES_NOT_EXIST,
        ReplicationMessages.DESTINATION_DOES_NOT_EXIST);
  }

  @Override
  public void validate() {
    super.validate();
    if (containsErrorMsgs()) {
      return;
    }
    if (replicationUtils.replicationConfigExists(name.getValue())) {
      addErrorMessage(ReplicationMessages.duplicateConfigurations());
    }
    if (!replicationUtils.siteIdExists(source.getValue())) {
      addErrorMessage(ReplicationMessages.sourceDoesNotExist());
    }
    if (!replicationUtils.siteIdExists(destination.getValue())) {
      addErrorMessage(ReplicationMessages.destinationDoesNotExist());
    }
    if (source.getValue().equals(destination.getValue())) {
      addErrorMessage(ReplicationMessages.sameSite());
    }

    try {
      ECQL.toFilter(filter.getValue());
    } catch (CQLException e) {
      addErrorMessage(ReplicationMessages.invalidFilter());
    }
  }
}
