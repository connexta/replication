import React from 'react'
import { allSites } from './gql/queries'
import { Query } from 'react-apollo'
import Sites from './Sites'
import Immutable from 'immutable'
import { Typography, Grid } from '@material-ui/core'
import AddSite from './AddSite'
import ServerError from '../common/ServerError'
import { withStyles } from '@material-ui/core/styles'
import CenteredCircularProgress from '../common/CenteredCircularProgress'

const styles = {
  root: {
    width: '90%',
    margin: 'auto',
  },
}

function alphabetical(a, b) {
  if (a.name.toLowerCase() < b.name.toLowerCase()) {
    return -1
  }
  if (a.name.toLowerCase() > b.name.toLowerCase()) {
    return 1
  }
  return 0
}

function SitesContainer(props) {
  const { classes } = props

  return (
    <Query query={allSites} pollInterval={10000}>
      {({ data, loading, error }) => {
        if (loading) return <CenteredCircularProgress />
        if (error) return <ServerError />

        const sites = Immutable.List(data.replication.sites).sort(alphabetical)

        return (
          <div className={classes.root}>
            <Typography variant='h5' color='inherit' noWrap>
              Nodes
            </Typography>
            <Grid container>
              <AddSite />
              <Sites sites={sites} />
            </Grid>
          </div>
        )
      }}
    </Query>
  )
}

export default withStyles(styles)(SitesContainer)
