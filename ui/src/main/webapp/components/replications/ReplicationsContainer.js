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
import React, { Fragment } from 'react'
import { Query } from 'react-apollo'
import { allReplications } from './gql/queries'
import ReplicationsTable from './ReplicationsTable'
import AddReplication from './AddReplication'
import { Typography, CardContent, Card, Button } from '@material-ui/core'
import { withStyles } from '@material-ui/core/styles'
import Immutable from 'immutable'
import ServerError from '../common/ServerError'
import CenteredCircularProgress from '../common/CenteredCircularProgress'

const styles = {
  expandingCard: {
    margin: 20,
    width: 360,
    height: 360,
    '&:hover': {
      margin: 15,
      width: 370,
      height: 370,
      cursor: 'pointer',
    },
  },
  centered: {
    margin: 'auto',
    marginTop: 30,
    display: 'flex',
    justifyContent: 'center',
  },
  right: {
    textAlign: 'right',
    marginBottom: 10,
  },
}

const AddButton = props => {
  return (
    <Button color='primary' variant='outlined' onClick={props.onClick}>
      <Typography color='inherit'>Add Replication</Typography>
    </Button>
  )
}

const AddReplicationCard = withStyles(styles)(props => {
  const { classes, onClick } = props

  return (
    <Card className={classes.expandingCard} onClick={onClick}>
      <CardContent>
        <Typography variant='h5' className={classes.centered}>
          Add Replication
        </Typography>
        <Typography variant='h1' className={classes.centered}>
          +
        </Typography>
      </CardContent>
    </Card>
  )
})

function ReplicationsContainer(props) {
  const { classes } = props

  return (
    <Query query={allReplications} pollInterval={10000}>
      {({ data, loading, error }) => {
        if (loading) return <CenteredCircularProgress />
        if (error) return <ServerError />

        if (
          data.replication &&
          data.replication.replications &&
          data.replication.replications.length > 0
        ) {
          const activeReplications = Immutable.List(
            data.replication.replications.filter(r => !r.suspended)
          )
          const inactiveReplications = Immutable.List(
            data.replication.replications.filter(r => r.suspended)
          )

          return (
            <Fragment>
              <div className={classes.right}>
                <AddReplication Button={AddButton} />
              </div>

              {activeReplications.size > 0 && (
                <ReplicationsTable
                  title='Active Replications'
                  replications={activeReplications}
                />
              )}

              {inactiveReplications.size > 0 && (
                <div style={{ marginTop: 10 }}>
                  <ReplicationsTable
                    title='Inactive Replications'
                    replications={inactiveReplications}
                  />
                </div>
              )}
            </Fragment>
          )
        }

        return (
          <div className={classes.centered}>
            <AddReplication Button={AddReplicationCard} />
          </div>
        )
      }}
    </Query>
  )
}

export default withStyles(styles)(ReplicationsContainer)
