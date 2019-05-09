import gql from 'graphql-tag'

export const sites = gql`
  fragment ReplicationSite on ReplicationSitePayload {
    id
    name
    remoteManaged
    address {
      url
    }
  }
`
