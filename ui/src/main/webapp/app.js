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
import Home from './components/Home'
import Navbar from './components/Navbar'
import { HashRouter, Route, Switch, Link } from 'react-router-dom'
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import client from './client'
import { ApolloProvider } from 'react-apollo'
import { SnackbarProvider } from 'notistack'
import { Grid, Typography } from '@material-ui/core'
import SitesContainer from './components/sites/SitesContainer'

const theme = createMuiTheme({
  // see: https://material-ui.com/style/typography/#migration-to-typography-v2
  typography: {
    useNextVariants: true,
  },
})

const NotFoundPage = () => {
  return (
    <div style={{ flexGrow: 1, width: '90%', margin: '65px auto' }}>
      <Grid container spacing={24}>
        <Grid item xs={12}>
          <Typography variant='h3'>Oops!</Typography>
        </Grid>
        <Grid item xs={12}>
          <Typography variant='body1'>
            The page you are looking for cannot be found. Try using the menu to
            navigate the site.
          </Typography>
        </Grid>
      </Grid>
      <Grid item xs={12}>
        <Typography variant='body1'>
          Or click <Link to='/'>here</Link> to return home.
        </Typography>
      </Grid>
    </div>
  )
}

const App = () => {
  return (
    <MuiThemeProvider theme={theme}>
      <ApolloProvider client={client}>
        <SnackbarProvider>
          <HashRouter>
            <div>
              <Navbar />
              <div style={{ margin: 10 }}>
                <Switch>
                  <Route exact path='/' component={Home} />
                  <Route exact path='/nodes' component={SitesContainer} />
                  <Route
                    render={function() {
                      return <NotFoundPage />
                    }}
                  />
                </Switch>
              </div>
            </div>
          </HashRouter>
        </SnackbarProvider>
      </ApolloProvider>
    </MuiThemeProvider>
  )
}

export default App
