# Getting Started

Before beginning run `yarn install` to download the necessary npm packages.

## Using the Development Server

#### Disabling IDP

IDP needs to be disabled in order to use the development server. This is because IDP redirects to a login page, which is not handled correctly. Under the Web Context Policy Manager configuration change:

```
/=IDP|GUEST
```

to

```
/=BASIC|GUEST
```

Once IDP is disabled, to start the development server run `yarn start`. Any saved changes made to the files under `src/main/webapp` will now be reflected in the browser.

## Miscellaneous

#### GraphiQL Clients

The GraphiQL client can be used to send requests to the GraphQL endpoint. Search your browser's plugins/extensions for an appropriate GraphiQL app. GraphiQL should be configured with the `<schema>://<hostname>:<port>/admin/hub/graphql` endpoint. For example, with a default running instance of DDF enter:

```
https://localhost:8993/admin/hub/graphql
```
