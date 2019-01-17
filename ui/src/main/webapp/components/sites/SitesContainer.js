import React, { Fragment } from 'react'
import sitesQuery from './sitesQuery'
import { Query } from 'react-apollo'
import Sites from './Sites'
import Immutable from 'immutable'

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
      <Fragment>
        <Query query={sitesQuery}>
          {({ data, loading, error }) => {
            if (loading) return <p>Loading...</p>
            if (error) return <p>Error...</p>

            const sites = Immutable.List(data.replication.sites).sort(
              alphabetical
            )

            return <Sites sites={sites} />
          }}
        </Query>
      </Fragment>
    )
  }
}
