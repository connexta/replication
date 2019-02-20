import gql from 'graphql-tag'

export default gql`
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
