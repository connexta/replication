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
package org.codice.ditto.replication.admin.query.sites.discover;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.codice.ddf.admin.api.Field;
import org.codice.ddf.admin.api.fields.FunctionField;
import org.codice.ddf.admin.api.fields.ListField;
import org.codice.ddf.admin.common.fields.base.BaseFunctionField;
import org.codice.ddf.admin.common.fields.common.PidField;
import org.codice.ddf.admin.common.report.message.DefaultMessages;
import org.codice.ditto.replication.admin.query.ReplicationUtils;
import org.codice.ditto.replication.admin.query.sites.fields.ReplicationSiteField;
import org.codice.ditto.replication.api.data.ReplicationSite;

public class GetReplicationSites extends BaseFunctionField<ListField<ReplicationSiteField>> {

  public static final String FIELD_NAME = "sites";

  public static final String DESCRIPTION =
      "Retrieves all the sites currently configured for replication, or a site given its identifier.";

  private static final ListField<ReplicationSiteField> RETURN_TYPE =
      new ReplicationSiteField.ListImpl();

  private final ReplicationUtils replicationUtils;

  private PidField id;

  public GetReplicationSites(ReplicationUtils replicationUtils) {
    super(FIELD_NAME, DESCRIPTION);
    this.replicationUtils = replicationUtils;
    this.id = new PidField("id");
  }

  @Override
  public ListField<ReplicationSiteField> performFunction() {
    final String fetchId = id.getValue();

    if (fetchId != null) {
      final ReplicationSite site = replicationUtils.getSite(fetchId);
      ListField<ReplicationSiteField> siteList = new ReplicationSiteField.ListImpl();
      siteList.add(replicationUtils.getSiteFieldForSite(site));
      return siteList;
    }

    return replicationUtils.getSites();
  }

  @Override
  public Set<String> getFunctionErrorCodes() {
    return ImmutableSet.of(DefaultMessages.NO_EXISTING_CONFIG);
  }

  @Override
  public List<Field> getArguments() {
    return ImmutableList.of(id);
  }

  @Override
  public ListField<ReplicationSiteField> getReturnType() {
    return RETURN_TYPE;
  }

  @Override
  public FunctionField<ListField<ReplicationSiteField>> newInstance() {
    return new GetReplicationSites(replicationUtils);
  }

  @Override
  public void validate() {
    super.validate();
    if (containsErrorMsgs()) {
      return;
    }

    final String fetchId = id.getValue();
    if (fetchId != null && !replicationUtils.siteIdExists(fetchId)) {
      addErrorMessage(DefaultMessages.noExistingConfigError());
    }
  }
}
