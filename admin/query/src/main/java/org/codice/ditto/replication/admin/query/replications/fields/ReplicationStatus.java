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
package org.codice.ditto.replication.admin.query.replications.fields;

import com.google.common.collect.ImmutableList;
import org.codice.ddf.admin.api.fields.EnumValue;
import org.codice.ddf.admin.common.fields.base.BaseEnumField;
import org.codice.ditto.replication.api.Status;

public class ReplicationStatus extends BaseEnumField<String> {

  public static final String DEFAULT_FIELD_NAME = "replicationStatus";

  public static final String TYPE_NAME = "ReplicationStatus";

  public static final String DESCRIPTION =
      "The enumeration of possible status values for a replication configuration";

  public ReplicationStatus() {
    this(null);
  }

  public ReplicationStatus(EnumValue<String> status) {
    super(
        DEFAULT_FIELD_NAME,
        TYPE_NAME,
        DESCRIPTION,
        ImmutableList.of(
            new ReplicationStatusEnum(Status.SUCCESS.name(), "Replication successfully run"),
            new ReplicationStatusEnum(Status.PENDING.name(), "Replication is queued to run"),
            new ReplicationStatusEnum(
                Status.PULL_IN_PROGRESS.name(), "Replication is pulling data"),
            new ReplicationStatusEnum(
                Status.PUSH_IN_PROGRESS.name(), "Replication is pushing data"),
            new ReplicationStatusEnum(Status.FAILURE.name(), "Replication failed during run"),
            new ReplicationStatusEnum(
                Status.CONNECTION_LOST.name(), "Network connection lost during replication run"),
            new ReplicationStatusEnum(
                Status.CONNECTION_UNAVAILABLE.name(),
                "Network connection unavailable for replication run"),
            new ReplicationStatusEnum(Status.CANCELED.name(), "Replication run was canceled"),
            new ReplicationStatusEnum("NOT_RUN", "Replication has not been run")),
        status);
  }

  public static final class ReplicationStatusEnum implements EnumValue<String> {

    private String value;
    private String description;

    public ReplicationStatusEnum(String value, String description) {
      this.value = value;
      this.description = description;
    }

    @Override
    public String getEnumTitle() {
      return getValue();
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public String getValue() {
      return value;
    }
  }
}
