/*global alert */
import React, { Fragment } from 'react'
import Site from './Site'
import PropTypes from 'prop-types'

export default function Sites(props) {
  const { sites } = props

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
              alert(site.id + '\n' + site.name + '\n' + site.address.url)
            }
          />
        ))}
    </Fragment>
  )
}

Sites.propTypes = {
  sites: PropTypes.object,
}
