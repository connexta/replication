import gql from 'graphql-tag'

export const deleteSite = gql`
  mutation deleteReplicationSite($id: Pid!) {
    deleteReplicationSite(id: $id)
  }
`
export const addSite = gql`
  mutation createReplicationSite(
    $name: String!
    $address: Address!
    $rootContext: String!
  ) {
    createReplicationSite(
      name: $name
      address: $address
      rootContext: $rootContext
    ) {
      id
      name
      address {
        url
      }
      rootContext
    }
  }
`
