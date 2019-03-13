import React from 'react'
import { Grid, Typography } from '@material-ui/core'

function ServerError() {
  return (
    <div style={{ flexGrow: 1, width: '90%', margin: '65px auto' }}>
      <Grid container spacing={24}>
        <Grid item xs={12}>
          <Typography variant='h3'>Oops!</Typography>
        </Grid>
        <Grid item xs={12}>
          <Typography variant='body1'>
            Something went wrong. Try refreshing the page to login again. If the
            problem persists, please contact your administator.
          </Typography>
        </Grid>
      </Grid>
    </div>
  )
}

export default ServerError
