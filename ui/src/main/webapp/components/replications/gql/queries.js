import gql from 'graphql-tag'

export const AllReplications = gql`
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
      }
    }
  }
`
