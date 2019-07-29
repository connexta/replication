import gql from 'graphql-tag'
import { sites } from '../../sites/gql/fragments'

export const replications = gql`
  fragment ReplicationConfig on ReplicationConfigPayload {
    name
    id
    source {
      ...ReplicationSite
    }
    destination {
      ...ReplicationSite
    }
    biDirectional
    metadataOnly
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
  ${sites}
`
