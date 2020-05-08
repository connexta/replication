/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
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
