import React from 'react'
import AddSite from './AddSite'
import SitesContainer from './SitesContainer'
import { Grid } from '@material-ui/core'
import Typography from '@material-ui/core/Typography'

import styled from 'styled-components'

const Container = styled.div`
  width: 90%;
  margin: auto;
`

export default class SitesPage extends React.Component {
  render() {
    return (
      <Container>
        <Typography variant='h5' color='inherit' noWrap>
          Nodes
        </Typography>
        <Grid container>
          <AddSite />
          <SitesContainer />
        </Grid>
      </Container>
    )
  }
}
