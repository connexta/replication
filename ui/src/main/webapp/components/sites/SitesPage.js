import React from 'react'
import AddSite from './AddSite'
import SitesContainer from './SitesContainer'
import { Grid, withStyles } from '@material-ui/core'
import Typography from '@material-ui/core/Typography'

const styles = {
  root: {
    width: '90%',
    margin: 'auto',
  },
}

const SitePages = class extends React.Component {
  render() {
    const { classes } = this.props

    return (
      <div className={classes.root}>
        <Typography variant='h5' color='inherit' noWrap>
          Nodes
        </Typography>
        <Grid container>
          <AddSite />
          <SitesContainer />
        </Grid>
      </div>
    )
  }
}

export default withStyles(styles)(SitePages)
