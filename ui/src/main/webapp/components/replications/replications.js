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
const Status = {
  SUCCESS: 'SUCCESS',
  PENDING: 'PENDING',
  SUSPENDED: 'SUSPENDED',
  CANCELED: 'CANCELED',
  PULL_IN_PROGRESS: 'PULL_IN_PROGRESS',
  PUSH_IN_PROGRESS: 'PUSH_IN_PROGRESS',
  FAILURE: 'FAILURE',
  CONNECTION_LOST: 'CONNECTION_LOST',
  CONNECTION_UNAVAILABLE: 'CONNECTION_UNAVAILABLE',
  NOT_RUN: 'NOT_RUN',
}

const statusDisplayNameMapping = {
  SUCCESS: 'Success',
  PENDING: 'Pending',
  SUSPENDED: 'Suspended',
  CANCELED: 'Canceled',
  PULL_IN_PROGRESS: 'Pulling resources...',
  PUSH_IN_PROGRESS: 'Pushing resources...',
  FAILURE: 'Failure',
  CONNECTION_LOST: 'Connection Lost',
  CONNECTION_UNAVAILABLE: 'Connection Unavailable',
  NOT_RUN: 'Not run',
}

function isInProgress(replication) {
  const status = replication.stats.replicationStatus
  return (
    status === Status.PULL_IN_PROGRESS || status === Status.PUSH_IN_PROGRESS
  )
}

function statusDisplayName(replication) {
  return statusDisplayNameMapping[replication.stats.replicationStatus]
}

function cancelable(replication) {
  const status = replication.stats.replicationStatus
  return (
    status === Status.PUSH_IN_PROGRESS ||
    status === Status.PULL_IN_PROGRESS ||
    status === Status.PENDING
  )
}

function repSort(a, b) {
  if (isInProgress(a) || isInProgress(b)) {
    return 1
  }

  if (a.name.toLowerCase() < b.name.toLowerCase()) {
    return -1
  }
  if (a.name.toLowerCase() > b.name.toLowerCase()) {
    return 1
  }
  return 0
}

export default {
  Status,
  isInProgress,
  statusDisplayName,
  cancelable,
  repSort,
}
