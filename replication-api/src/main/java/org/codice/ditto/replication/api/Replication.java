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
package org.codice.ditto.replication.api;

public class Replication {

  public static final String REPLICATED_TAG = "replicated";

  public static final String REPLICATION_RUN_TAG = "replication-run";

  public static final String ORIGINS = "replication.origins";

  public static final String CHANGE_USER = "metacard.change.user";

  public static final String CHANGE_LOCATION = "metacard.change.location";

  public static final String CHANGE_DATE = "metacard.change.date";

  private Replication() {}
}
