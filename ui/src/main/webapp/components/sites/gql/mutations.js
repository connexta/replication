import gql from 'graphql-tag'

export const deleteSite = gql`
  mutation deleteReplicationSite($id: Pid!) {
    deleteReplicationSite(id: $id)
  }
`
export const addSite = gql`
  mutation createReplicationSite($name: String!, $address: Address!) {
    createReplicationSite(name: $name, address: $address) {
      id
      name
      address {
        url
      }
    }
  }
`
