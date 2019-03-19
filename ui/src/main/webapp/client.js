import ApolloClient from 'apollo-client'
import { InMemoryCache } from 'apollo-cache-inmemory'
import { createHttpLink } from 'apollo-link-http'
import fetch from 'unfetch'

const graphqlUri = '/admin/hub/graphql'

const link = createHttpLink({ uri: graphqlUri, fetch: fetch })

export default new ApolloClient({
  link: link,
  cache: new InMemoryCache(),
})
