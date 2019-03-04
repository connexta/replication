import gql from 'graphql-tag'

export const allReplications = gql`
  {
    replication {
      replications {
        id
        name
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
        itemsTransferred
        dataTransferred
        filter
        suspended
      }
    }
  }
`
