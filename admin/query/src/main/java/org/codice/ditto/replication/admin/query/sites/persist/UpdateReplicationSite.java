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
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ddf.admin.common.fields.base.scalar.StringField;
import org.codice.ddf.admin.common.fields.common.AddressField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ddf.admin.common.report.message.DefaultMessages;
import org.codice.ditto.replication.admin.query.ReplicationMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.sites.fields.IsDisabledLocalField;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;

public class UpdateReplicationSite extends BaseFunctionField<ReplicationSiteField> {

  public static final String FIELD_NAME = "updateReplicationSite";

  public static final String DESCRIPTION = "Updates a replication Site.";

  public static final ReplicationSiteField RETURN_TYPE = new ReplicationSiteField();

  private PidField id;

  private StringField name;

  private AddressField address;

  private StringField rootContext;

  private IsDisabledLocalField isDisabledLocal;

  private ReplicationUtils replicationUtils;

  public UpdateReplicationSite(ReplicationUtils replicationUtils) {
    super(FIELD_NAME, DESCRIPTION);
    this.replicationUtils = replicationUtils;

    this.id = new PidField("id");
    this.name = new StringField("name");
    this.address = new AddressField();
    this.rootContext = new StringField("rootContext");
    this.isDisabledLocal = new IsDisabledLocalField();

    this.id.isRequired(true);
  }

  @Override
  public ReplicationSiteField performFunction() {
    return replicationUtils.updateSite(
        id.getValue(),
        name.getValue(),
        address,
        rootContext.getValue(),
        isDisabledLocal.getValue());
  }

  @Override
  public ReplicationSiteField getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(id, name, address, rootContext, isDisabledLocal);
  }

  @Override
  public FunctionField<ReplicationSiteField> newInstance() {
    return new UpdateReplicationSite(replicationUtils);
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of(ReplicationMessages.DUPLICATE_SITE, DefaultMessages.NO_EXISTING_CONFIG);
  }

  @Override
  public void validate() {
    super.validate();
    if (containsErrorMsgs()) {
      return;
    }

    final String updatedName = name.getValue();
    final String updateId = id.getValue();
    if (!replicationUtils.siteIdExists(updateId)) {
      addErrorMessage(DefaultMessages.noExistingConfigError());
    }
    if (updatedName != null
        && (replicationUtils.isNotUpdatedSiteName(updateId, updatedName)
            || replicationUtils.isDuplicateSiteName(updatedName))) {
      addErrorMessage(ReplicationMessages.duplicateSites());
    }
  }
}
