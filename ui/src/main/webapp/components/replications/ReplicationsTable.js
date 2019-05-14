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
} from '@material-ui/core'
import { MoreVert } from '@material-ui/icons'
import moment from 'moment'
import Immutable from 'immutable'
import ActionsMenu from './ActionsMenu'
import Replications from './replications'
import ReactInterval from 'react-interval'
import { makeStyles } from '@material-ui/styles'

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
})

const format = utc => {
  return utc ? moment.utc(utc).fromNow() : '-'
}

function ReplicationRow(props) {
  const [anchor, setAnchor] = useState(null)
  const [selectedReplication, setSelectedReplication] = useState({})
  const [lastRun, setLastRun] = useState(format(props.replication.lastRun))
  const [lastSuccess, setLastSuccess] = useState(
    format(props.replication.lastSuccess)
  )
  const classes = useStyles()

  const { replication } = props

  return (
    <TableRow>
      <ReactInterval
        enabled={true}
        timeout={60000}
        callback={() => {
          setLastRun(format(replication.lastRun))
          setLastSuccess(format(replication.lastSuccess))
        }}
      />

      <TableCell component='th'>{replication.name}</TableCell>
      <TableCell>{Replications.statusDisplayName(replication)}</TableCell>
      <TableCell>{replication.source.name}</TableCell>
      <TableCell>{replication.destination.name}</TableCell>
      <TableCell>{replication.biDirectional ? 'Yes' : 'No'}</TableCell>
      <TableCell>{replication.filter}</TableCell>
      <TableCell>{replication.itemsTransferred}</TableCell>
      <TableCell>{replication.dataTransferred}</TableCell>
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
