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
import { withStyles } from '@material-ui/core/styles'
import ActionsMenu from './ActionsMenu'
import Replications from './replications'
import ReactInterval from 'react-interval'

const styles = {
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
}

const format = utc => {
  return utc ? moment.utc(utc).fromNow() : '-'
}

class ReplicationRow extends React.Component {
  state = {
    anchor: null,
    lastRun: format(this.props.replication.lastRun),
    lastSuccess: format(this.props.replication.lastSuccess),
  }

  handleClickOpen = replication => event => {
    this.setState({ replication, anchor: event.currentTarget })
  }

  handleClose = () => {
    this.setState({ anchor: null })
  }

  render() {
    const { replication } = this.props

    return (
      <TableRow>
        <ReactInterval
          enabled={true}
          timeout={60000}
          callback={() => {
            this.setState({
              lastRun: format(replication.lastRun),
              lastSuccess: format(replication.lastSuccess),
            })
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
        <TableCell>{this.state.lastRun}</TableCell>
        <TableCell>{this.state.lastSuccess}</TableCell>
        <TableCell>
          <IconButton
            className={classes.actions}
            onClick={this.handleClickOpen(replication)}
            aria-label='More'
            aria-owns={this.state.anchor !== null ? 'actions-menu' : undefined}
          >
            <MoreVert />
          </IconButton>

          <ActionsMenu
            menuId='actions-menu'
            replication={this.state.replication}
            anchorEl={this.state.anchor}
            onClose={this.handleClose}
          />
        </TableCell>
      </TableRow>
    )
  }
}

class ReplicationsTable extends React.Component {
  render() {
    const { replications, title, classes } = this.props
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
                <ReplicationRow
                  key={replication.id}
                  replication={replication}
                />
              ))}
          </TableBody>
        </Table>
      </Paper>
    )
  }
}

ReplicationsTable.propTypes = {
  replications: PropTypes.object.isRequired,
}

export default withStyles(styles)(ReplicationsTable)
