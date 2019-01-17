import React from 'react'
import AddSite from './AddSite'
import SitesContainer from './SitesContainer'
import { Grid } from '@material-ui/core'

export default class SitesPage extends React.Component {
  render() {
    return (
      <div>
        <div style={{ width: '90%', margin: 'auto' }}>
          <h1>Sites</h1>
          <Grid container>
            <AddSite />
            <SitesContainer />
          </Grid>
        </div>
      </div>
    )
  }
}
