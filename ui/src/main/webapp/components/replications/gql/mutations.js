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
import gql from 'graphql-tag'

export const addReplication = gql`
  mutation createReplication(
    $name: String!
    $sourceId: Pid!
    $destinationId: Pid!
    $filter: String!
    $biDirectional: Boolean
  ) {
    createReplication(
      name: $name
      sourceId: $sourceId
      destinationId: $destinationId
      filter: $filter
      biDirectional: $biDirectional
    ) {
      name
      id
      source {
        id
        name
        address {
          url
        }
      }
      destination {
        id
        name
        address {
          url
        }
      }
      biDirectional
      filter
      suspended
      stats {
        replicationStatus
        pushCount
        pullCount
        pushBytes
        pullBytes
        lastRun
        lastSuccess
        startTime
      }
    }
  }
`
export const suspendReplication = gql`
  mutation suspendReplication($id: Pid!, $suspend: Boolean!) {
    suspendReplication(id: $id, suspend: $suspend)
  }
`

export const cancelReplication = gql`
  mutation cancelReplication($id: Pid!) {
    cancelReplication(id: $id)
  }
`

export const deleteReplication = gql`
  mutation deleteReplication($id: Pid!, $deleteData: Boolean) {
    deleteReplication(id: $id, deleteData: $deleteData)
  }
`

export const runReplication = gql`
  mutation runReplication($id: Pid!) {
    runReplication(id: $id)
  }
`
