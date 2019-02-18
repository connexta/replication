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
package org.codice.ditto.replication.api.mcard;

public interface ReplicationConfig {

  String METACARD_TAG = "replication-config";

  String NAME = "replication-config.name";

  String DESCRIPTION = "replication-config.description";

  String URL = "replication-config.url";

  String SOURCE = "replication-config.source-id";

  String DESTINATION = "replication-config.destination-id";

  String CQL = "replication-config.cql";

  String DIRECTION = "replication-config.direction";

  String TYPE = "replication-config.type";

  String FAILURE_RETRY_COUNT = "replication-config.failure-retry-count";
}
