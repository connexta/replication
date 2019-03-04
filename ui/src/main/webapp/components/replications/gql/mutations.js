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
      lastRun
      lastSuccess
      firstRun
      biDirectional
      replicationStatus
      filter
      itemsTransferred
      dataTransferred
      suspended
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
  mutation deleteReplication($id: Pid!) {
    deleteReplication(id: $id)
  }
`
