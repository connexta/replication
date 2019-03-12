import React from 'react'
import sitesQuery from './gql/sitesQuery'
import { Query } from 'react-apollo'
import Sites from './Sites'
import Immutable from 'immutable'
import { CircularProgress, Typography } from '@material-ui/core'

function alphabetical(a, b) {
  if (a.name.toLowerCase() < b.name.toLowerCase()) {
    return -1
  }
  if (a.name.toLowerCase() > b.name.toLowerCase()) {
    return 1
  }
  return 0
}

export default class SitesContainer extends React.Component {
  render() {
    return (
      <Query query={sitesQuery}>
        {({ data, loading, error }) => {
          if (loading) return <CircularProgress />
          if (error) return <Typography>Error...</Typography>

          const sites = Immutable.List(data.replication.sites).sort(
            alphabetical
          )

          return <Sites sites={sites} />
        }}
      </Query>
    )
  }
}
