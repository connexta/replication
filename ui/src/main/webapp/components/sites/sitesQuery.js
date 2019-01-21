import gql from "graphql-tag";

export default gql`
  {
    replication {
      sites {
        id
        name
        address {
          url
        }
      }
    }
  }
`;
