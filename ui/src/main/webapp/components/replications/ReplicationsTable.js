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
  MenuItem,
  Select,
  Tooltip,
} from '@material-ui/core'
import { MoreVert } from '@material-ui/icons'
import ArrowDownwardIcon from '@material-ui/icons/ArrowDownward'
import ArrowUpwardIcon from '@material-ui/icons/ArrowUpward'
import moment from 'moment'
import Immutable from 'immutable'
import { withStyles, createMuiTheme, MuiThemeProvider } from '@material-ui/core/styles'
import ActionsMenu from './ActionsMenu'
import Replications from './replications'
import ReactInterval from 'react-interval'
import { changePriority } from './gql/mutations'
import { Mutation } from 'react-apollo'
import RJSON from 'relaxed-json'

const styles = {
  root: {
    width: '100%',
    overflowX: 'auto',
  },
  title: {
    flex: '0 0 auto',
  },
}

const theme = createMuiTheme({
  overrides: {
    MuiTableCell: {
      root: {
        padding: '10px 15px',
      }
    }
  }
});

const format = utc => {
  return utc ? moment.utc(utc).fromNow() : '-'
}

class ReplicationRow extends React.Component {
  state = {
    anchor: null,
    lastRun: format(this.props.replication.stats.lastRun),
    lastSuccess: format(this.props.replication.stats.lastSuccess),
  }

  handleClickOpen = replication => event => {
    this.setState({ replication, anchor: event.currentTarget })
  }

  handleClose = () => {
    this.setState({ anchor: null })
  }

  getSortIcon = (asc) => {
    const Icon = asc ? ArrowUpwardIcon : ArrowDownwardIcon
    return (
      <Tooltip title={asc ? 'Ascending' : 'Descending'}>
        <Icon fontSize='small' style={{ 'color': '#808080' }}/>
      </Tooltip>
    )
  }

  getSorts = (filter) => {
    const parts = filter.split('::')
    if (parts.length > 1 && parts[1] != '[]') {
      const sorts = RJSON.parse(parts[1])
      return sorts.map(s => (
          <div style={{ 'display': 'flex', 'alignItems': 'center' }}>
            {s.attribute}{'  '}
            {this.getSortIcon(s.direction == 'ascending')}
          </div>
        )
      )
    } else {
      return (
        <div style={{ 'display': 'flex', 'alignItems': 'center' }}>
          modified{'  '}
          {this.getSortIcon(false)}
        </div>
      )
    }
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
              lastRun: format(replication.stats.lastRun),
              lastSuccess: format(replication.stats.lastSuccess),
            })
          }}
        />

        <TableCell component='th'>{replication.name}</TableCell>
        <TableCell>{Replications.statusDisplayName(replication)}</TableCell>
        <TableCell>{replication.source.name}</TableCell>
        <TableCell>{replication.destination.name}</TableCell>
        <TableCell>
          <Mutation mutation={changePriority}>
            {changePriority => (
              <Select
                disableUnderline={true}
                value={replication.priority}
                onChange={event => {
                  changePriority({
                    variables: {
                      id: replication.id,
                      priority: event.target.value,
                    },
                    update: () => {
                      replication.priority = event.target.value
                    },
                  })
                }}
              >
                <MenuItem value={10}>10 - High</MenuItem>
                <MenuItem value={9}>9 - High</MenuItem>
                <MenuItem value={8}>8 - High</MenuItem>
                <MenuItem value={7}>7 - Medium</MenuItem>
                <MenuItem value={6}>6 - Medium</MenuItem>
                <MenuItem value={5}>5 - Medium</MenuItem>
                <MenuItem value={4}>4 - Medium</MenuItem>
                <MenuItem value={3}>3 - Low</MenuItem>
                <MenuItem value={2}>2 - Low</MenuItem>
                <MenuItem value={1}>1 - Low</MenuItem>
              </Select>
            )}
          </Mutation>
        </TableCell>
        <TableCell>{replication.biDirectional ? 'Yes' : 'No'}</TableCell>
        <TableCell>{replication.filter.split('::')[0]}</TableCell>
        <TableCell>{this.getSorts(replication.filter)}</TableCell>
        <TableCell>
          {replication.stats.pullCount + replication.stats.pushCount}
        </TableCell>
        <TableCell>
          {Replications.formatBytes(
            replication.stats.pushBytes + replication.stats.pullBytes
          )}
        </TableCell>
        <TableCell>{this.state.lastRun}</TableCell>
        <TableCell>{this.state.lastSuccess}</TableCell>
        <TableCell>
          <IconButton
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
      <MuiThemeProvider theme={theme}>
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
                <TableCell>Priority</TableCell>
                <TableCell>Bidirectional</TableCell>
                <TableCell>Filter</TableCell>
                <TableCell>Sorting</TableCell>
                <TableCell>Items Transferred</TableCell>
                <TableCell>Data Transferred</TableCell>
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
      </MuiThemeProvider>
    )
  }
}

ReplicationsTable.propTypes = {
  replications: PropTypes.object.isRequired,
}

export default withStyles(styles)(ReplicationsTable)
