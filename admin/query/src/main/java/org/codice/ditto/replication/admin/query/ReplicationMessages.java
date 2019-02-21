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
package org.codice.ditto.replication.admin.query;

import org.codice.ddf.admin.api.report.ErrorMessage;
import org.codice.ddf.admin.common.report.message.ErrorMessageImpl;

/**
 * ReplicationMessages contains the error codes/messages that can be returned by the GraphQL
 * endpoint for replication.
 */
public class ReplicationMessages {

  public static final String DUPLICATE_CONFIGURATION = "DUPLICATE_CONFIGURATION";

  public static final String DUPLICATE_SITE = "DUPLICATE_SITE";

  public static final String SAME_SITE = "SAME_SITE";

  public static final String INVALID_FILTER = "INVALID_FILTER";

  private ReplicationMessages() {}

  public static ErrorMessage duplicateConfigurations() {
    return new ErrorMessageImpl(DUPLICATE_CONFIGURATION);
  }

  public static ErrorMessage duplicateSites() {
    return new ErrorMessageImpl(DUPLICATE_SITE);
  }

  public static ErrorMessage sameSite() {
    return new ErrorMessageImpl(SAME_SITE);
  }

  public static ErrorMessage invalidFilter() {
    return new ErrorMessageImpl(INVALID_FILTER);
  }
}
