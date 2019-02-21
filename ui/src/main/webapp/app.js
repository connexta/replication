import React from 'react'
import Home from './components/Home'
import Navbar from './components/Navbar'
import SitesPage from './components/sites/SitesPage'
import { HashRouter, Route, Switch } from 'react-router-dom'
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import client from './client'
import { ApolloProvider } from 'react-apollo'

const theme = createMuiTheme({
  // see: https://material-ui.com/style/typography/#migration-to-typography-v2
  typography: {
    useNextVariants: true,
  },
})

const App = () => {
  return (
    <MuiThemeProvider theme={theme}>
      <ApolloProvider client={client}>
        <HashRouter>
          <div>
            <Navbar />
            <div style={{ margin: 10 }}>
              <Switch>
                <Route exact path='/' component={Home} />
                <Route exact path='/nodes' component={SitesPage} />
                <Route
                  render={function() {
                    return <p>Not Found</p>
                  }}
                />
              </Switch>
            </div>
          </div>
        </HashRouter>
      </ApolloProvider>
    </MuiThemeProvider>
  )
}

export default App
