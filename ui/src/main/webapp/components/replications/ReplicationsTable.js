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
import React, { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Paper,
  Table,
  TableHead,
  TableCell,
  TableBody,
  TableRow,
  IconButton,
  Typography,
  Toolbar,
  Tooltip,
  Grid,
} from '@material-ui/core'
import { MoreVert } from '@material-ui/icons'
import moment from 'moment'
import Immutable from 'immutable'
import ActionsMenu from './ActionsMenu'
import Replications from './replications'
import ReactInterval from 'react-interval'
import { makeStyles } from '@material-ui/styles'
import Cloud from '@material-ui/icons/Cloud'

const useStyles = makeStyles({
  root: {
    width: '100%',
    overflowX: 'auto',
  },
  title: {
    flex: '0 0 auto',
  },
  actions: {
    float: 'right',
  },
  replicationName: {
    flexGrow: 1,
  },
  overflowText: {
    display: 'block',
    width: '100px',
    overflow: 'hidden',
    whiteSpace: 'nowrap',
    textOverflow: 'ellipsis',
  },
})

const format = utc => {
  return utc ? moment.utc(utc).fromNow() : '-'
}

const isRemoteManaged = replication => {
  return (
    replication.source.remoteManaged || replication.destination.remoteManaged
  )
}

function ReplicationName(props) {
  const { name, remoteManaged } = props
  const classes = useStyles()

  return remoteManaged ? (
    <Grid container justify='flex-start'>
      <Tooltip
        style={{ marginRight: '5px' }}
        title='This Replication is remotely managed by the Cloud because one of the source or destination Nodes has been identified as a Cloud ready Node. This Replication will be run in the Cloud.'
      >
        <Cloud fontSize='small' />
      </Tooltip>
      <Tooltip title={name}>
        <Typography className={classes.overflowText}>{name}</Typography>
      </Tooltip>
    </Grid>
  ) : (
    <Tooltip title={name}>
      <Typography className={classes.overflowText}>{name}</Typography>
    </Tooltip>
  )
}

function ReplicationRow(props) {
  const [anchor, setAnchor] = useState(null)
  const [selectedReplication, setSelectedReplication] = useState(undefined)
  const [lastRun, setLastRun] = useState(
    format(props.replication.stats.lastRun)
  )
  const [lastSuccess, setLastSuccess] = useState(
    format(props.replication.stats.lastSuccess)
  )
  const classes = useStyles()

  const { replication } = props

  return (
    <TableRow>
      <ReactInterval
        enabled={true}
        timeout={60000}
        callback={() => {
          setLastRun(format(replication.stats.lastRun))
          setLastSuccess(format(replication.stats.lastSuccess))
        }}
      />

      <TableCell component='th'>
        <ReplicationName
          name={replication.name}
          remoteManaged={isRemoteManaged(replication)}
        />
      </TableCell>
      <TableCell>{Replications.statusDisplayName(replication)}</TableCell>
      <TableCell>{replication.source.name}</TableCell>
      <TableCell>{replication.destination.name}</TableCell>
      <TableCell>{replication.biDirectional ? 'Yes' : 'No'}</TableCell>
      <TableCell>{replication.filter}</TableCell>
      <TableCell>
        {replication.stats.pullCount + replication.stats.pushCount}
      </TableCell>
      <TableCell>
        {Replications.formatBytes(
          replication.stats.pushBytes + replication.stats.pullBytes
        )}
      </TableCell>
      <TableCell>{lastRun}</TableCell>
      <TableCell>{lastSuccess}</TableCell>
      <TableCell>
        <IconButton
          className={classes.actions}
          onClick={e => {
            setSelectedReplication(replication)
            setAnchor(e.currentTarget)
          }}
          aria-label='More'
          aria-owns={anchor !== null ? 'actions-menu' : undefined}
        >
          <MoreVert />
        </IconButton>

        <ActionsMenu
          menuId='actions-menu'
          replication={selectedReplication}
          anchorEl={anchor}
          onClose={() => setAnchor(null)}
        />
      </TableCell>
    </TableRow>
  )
}

function ReplicationsTable(props) {
  const { replications, title } = props
  const classes = useStyles()
  const sorted = Immutable.List(replications.sort(Replications.repSort))

  return (
    <Paper className={classes.root}>
      <Toolbar>
        <Typography variant='h6' id='tableTitle' className={classes.title}>
          {title}
        </Typography>
      </Toolbar>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Last Run Status</TableCell>
            <TableCell>Source</TableCell>
            <TableCell>Destination</TableCell>
            <TableCell>Bidirectional</TableCell>
            <TableCell>Filter</TableCell>
            <TableCell>Items Transferred</TableCell>
            <TableCell>MB Transferred</TableCell>
            <TableCell>Last Run</TableCell>
            <TableCell>Last Success</TableCell>
            <TableCell />
          </TableRow>
        </TableHead>
        <TableBody>
          {sorted &&
            sorted.map(replication => (
              <ReplicationRow key={replication.id} replication={replication} />
            ))}
        </TableBody>
      </Table>
    </Paper>
  )
}

ReplicationsTable.propTypes = {
  replications: PropTypes.object.isRequired,
}

export default ReplicationsTable
