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
import React from 'react'
import CardContent from '@material-ui/core/CardContent'
import {
  IconButton,
  Card,
  Typography,
  withStyles,
  CircularProgress,
  Tooltip,
} from '@material-ui/core'
import DeleteForever from '@material-ui/icons/DeleteForever'
import CardActions from '@material-ui/core/CardActions'
import { Mutation } from 'react-apollo'
import { allSites } from './gql/queries'
import PropTypes from 'prop-types'
import { withSnackbar } from 'notistack'
import { deleteSite } from './gql/mutations'
import Confirmable from '../common/Confirmable'

const styles = {
  centered: {
    textAlign: 'center',
    marginTop: -15,
    clear: 'both',
  },
  right: {
    float: 'right',
    marginBottom: -15,
  },
  card: {
    margin: 20,
    width: 220,
    height: 220,
  },
  multilineOverflow: {
    display: '-webkit-box',
    maxHeight: '3.2rem',
    '-webkit-box-orient': 'vertical',
    overflow: 'hidden',
    'text-overflow': 'ellipsis',
    '-webkit-line-clamp': 2,
    lineHeight: '1.6rem',
  },
}

class Site extends React.Component {
  render() {
    const { name, content, id, classes, enqueueSnackbar } = this.props

    return (
      <Card className={classes.card}>
        <CardActions className={classes.right}>
          <Mutation
            mutation={deleteSite}
            onError={error => {
              error.graphQLErrors &&
                error.graphQLErrors.forEach(e => {
                  if (e.message === 'SITE_IN_USE') {
                    enqueueSnackbar(
                      'Failed to delete ' +
                        name +
                        '. It is used by a Replication.',
                      {
                        variant: 'error',
                        preventDuplicate: true,
                      }
                    )
                  }
                })
            }}
          >
            {(deleteReplicationSite, { loading }) => (
              <div>
                <Confirmable
                  onConfirm={() => {
                    deleteReplicationSite({
                      variables: {
                        id: id,
                      },
                      update: store => {
                        const data = store.readQuery({
                          query: allSites,
                        })

                        data.replication.sites = data.replication.sites.filter(
                          site => site.id !== id
                        )

                        store.writeQuery({
                          query: allSites,
                          data,
                        })

                        enqueueSnackbar('Deleted node ' + name + '.', {
                          variant: 'success',
                        })
                      },
                    })
                  }}
                  message={`Are you sure you want to delete the node ${name}?`}
                  Button={props => {
                    const { onClick } = props
                    return (
                      <IconButton color='primary' onClick={onClick}>
                        <DeleteForever />
                      </IconButton>
                    )
                  }}
                />
                {loading && <CircularProgress size={10} />}
              </div>
            )}
          </Mutation>
        </CardActions>
        <CardContent className={classes.centered}>
          <Tooltip title={name}>
            <Typography variant='h5' className={classes.multilineOverflow}>
              {name}
            </Typography>
          </Tooltip>
        </CardContent>
        <CardContent className={classes.centered}>
          <Tooltip title={content}>
            <Typography className={classes.multilineOverflow}>
              {content}
            </Typography>
          </Tooltip>
        </CardContent>
      </Card>
    )
  }
}

Site.propTypes = {
  name: PropTypes.string.isRequired,
  content: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
}

export default withStyles(styles)(withSnackbar(Site))
