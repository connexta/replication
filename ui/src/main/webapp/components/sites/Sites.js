import React, { Fragment } from "react";
import Site from "./Site";

export default props => {
  const { sites } = props;

  return (
    <Fragment>
      {sites &&
        props.sites.map(site => (
          <Site
            key={site.id}
            name={site.name}
            connected={true}
            onClick={() =>
              // todo: route to site specific page if clicked
              alert(site.id + "\n" + site.name + "\n" + site.address.url)
            }
          />
        ))}
    </Fragment>
  );
};
