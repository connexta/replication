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
package org.codice.ditto.replication.admin.query.sites.fields;

import org.codice.ddf.admin.common.fields.base.scalar.BooleanField;

public class RemoteManagedField extends BooleanField {

  private static final String DESCRIPTION =
      "Specifies if replications including this Site are remotely managed. If not remotely managed, replications including this Site will run locally.";

  private static final String FIELD_NAME = "remoteManaged";

  public RemoteManagedField() {
    super(FIELD_NAME, null, DESCRIPTION);
  }
}
