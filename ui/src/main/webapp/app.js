import React from 'react'
import Home from './components/Home'
import Navbar from './components/Navbar'
import SitesPage from './components/sites/SitesPage'
import { HashRouter, Route, Switch } from 'react-router-dom'

const App = () => {
  return (
    <HashRouter>
      <div>
        <Navbar />
        <Switch>
          <Route exact path='/' component={Home} />
          <Route exact path='/sites' component={SitesPage} />
          <Route
            render={function() {
              return <p>Not Found</p>
            }}
          />
        </Switch>
      </div>
    </HashRouter>
  )
}

export default App
