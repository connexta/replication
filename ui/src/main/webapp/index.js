/*global document, require, process, module */
import React from 'react'
import ReactDOM from 'react-dom'
import { AppContainer } from 'react-hot-loader'

import App from './app'

const render = function(Component) {
  ReactDOM.render(
    <AppContainer>
      <Component />
    </AppContainer>,
    document.getElementById('app')
  )
}

render(App)

if (process.env.NODE_ENV !== 'production') {
  module.hot.accept('./app', () => render(require('./app').default))
}
