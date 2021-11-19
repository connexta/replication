/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
/* global window */
import { allQueries } from '../queries/gql/queries'
import Immutable from 'immutable'
import React from 'react'
import Select from 'react-select'
import { Typography, Link, FormLabel } from '@material-ui/core'
import { Query } from 'react-apollo'

const queriesToOptions = queries => {
  const queryList = Immutable.List(queries)
  return queryList.map(query => ({ value: query.cql, label: query.title }))
}

function QuerySelector(props) {
  const { onChange } = props
  const baseUrl = window.location.protocol + '//' + window.location.host

  return (
    <div>
      <FormLabel>
        Select a search to use its filter and sort policies, or create your own
      </FormLabel>
      <Query query={allQueries}>
        {({ loading, error, data, refetch }) => {
          if (error) return <Typography>Error...</Typography>

          return (
            <div>
              <Select
                isClearable={true}
                isLoading={loading}
                options={
                  loading ? [] : queriesToOptions(data.replication.queries)
                }
                placeholder='Custom'
                onChange={onChange}
                menuPosition='fixed'
                onMenuOpen={() => refetch()}
              />
            </div>
          )
        }}
      </Query>
      <Typography>
        You can create new searches to choose from in{' '}
        <Link href={baseUrl + '/search/catalog'} target='_blank'>
          Intrigue
        </Link>
        .
      </Typography>
      <Typography>
        For information about how to create a search see the{' '}
        <Link
          href={
            baseUrl +
            '/admin/documentation/documentation.html#_configuring_a_workspace_in_intrigue'
          }
          target='_blank'
        >
          Documentation
        </Link>
        .
      </Typography>
    </div>
  )
}

export default QuerySelector
