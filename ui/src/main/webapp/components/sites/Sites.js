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
            content={site.address.url}
            id={site.id}
          />
        ))}
    </Fragment>
  )
}

Sites.propTypes = {
  sites: PropTypes.object,
}
