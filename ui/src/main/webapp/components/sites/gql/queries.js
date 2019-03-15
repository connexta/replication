import gql from 'graphql-tag'

export const allSites = gql`
  {
    replication {
      sites {
        id
        name
        address {
          url
        }
      }
    }
  }
`
