import React from "react";
import ReactDOM from "react-dom";
import client from "./client";

import { ApolloProvider } from "react-apollo";
import { AppContainer } from "react-hot-loader";

import App from "./app";

const render = Component =>
  ReactDOM.render(
    <AppContainer>
      <ApolloProvider client={client}>
        <Component />
      </ApolloProvider>
    </AppContainer>,
    document.getElementById("app")
  );

render(App);

if (process.env.NODE_ENV !== "production") {
  module.hot.accept("./app", () => render(require("./app").default));
}
