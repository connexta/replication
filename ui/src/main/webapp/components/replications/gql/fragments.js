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
  ${sites}
`
