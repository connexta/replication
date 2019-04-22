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

public interface ReplicationHistory {

  String METACARD_TAG = "replication-history";

  String START_TIME = "replication-history.start-time";

  String DURATION = "replication-history.duration";

  String PUSH_COUNT = "replication-history.push-count";

  String PUSH_FAIL_COUNT = "replication-history.push-fail-count";

  String PUSH_BYTES = "replication-history.push-bytes";

  String PULL_COUNT = "replication-history.pull-count";

  String PULL_FAIL_COUNT = "replication-history.pull-fail-count";

  String PULL_BYTES = "replication-history.pull-bytes";

  String STATUS = "replication-history.status";

  String LAST_RUN = "replication-history.last-run";

  String LAST_SUCCESS = "replication-history.last-success";

  String LAST_METADATA_MODIFIED = "replication-history.last-metadata-modified";
}
