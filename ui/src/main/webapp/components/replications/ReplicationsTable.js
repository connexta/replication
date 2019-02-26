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
  Menu,
  MenuItem,
  Typography,
} from '@material-ui/core'
import { MoreVert } from '@material-ui/icons'
import { Mutation } from 'react-apollo'
import gql from 'graphql-tag'
import { AllReplications } from './gql/queries'
import moment from 'moment'
import Immutable from 'immutable'
import { withStyles } from '@material-ui/core/styles'

const DELETE_REPLICATION = gql`
  mutation deleteReplication($id: Pid!) {
    deleteReplication(id: $id)
  }
`

const styles = {
  root: {
    width: '100%',
    overflowX: 'auto',
  },
}

const repStatusMapping = {
  SUCCESS: 'Success',
  PENDING: 'Pending',
  PULL_IN_PROGRESS: 'Pulling resources...',
  PUSH_IN_PROGRESS: 'Pushing resources...',
  FAILURE: 'Failure',
  CONNECTION_LOST: 'Connection Lost',
  CONNECTION_UNAVAILABLE: 'Connection Unavailable',
  CANCELED: 'Canceled',
  SUSPENDED: 'Suspended',
  NOT_RUN: 'Not run',
}

function isInProgress(replication) {
  return (
    replication.replicationStatus === 'PULL_IN_PROGRESS' ||
    replication.replicationStatus === 'PUSH_IN_PROGRESS'
  )
}

function repSort(a, b) {
  if (isInProgress(a) || isInProgress(b)) {
    return 1
  }

  if (a.name.toLowerCase() < b.name.toLowerCase()) {
    return -1
  }
  if (a.name.toLowerCase() > b.name.toLowerCase()) {
    return 1
  }
  return 0
}

const DeleteReplication = props => {
  const { id, onClose } = props

  return (
    <Mutation mutation={DELETE_REPLICATION}>
      {deleteReplication => (
        <MenuItem
          key={'Delete'}
          onClick={() => {
            deleteReplication({
              variables: {
                id: id,
              },
              update: store => {
                const data = store.readQuery({
                  query: AllReplications,
                })

                data.replication.replications = data.replication.replications.filter(
                  r => r.id !== id
                )
                store.writeQuery({
                  query: AllReplications,
                  data,
                })
              },
            })
            onClose()
          }}
        >
          <Typography>Delete</Typography>
        </MenuItem>
      )}
    </Mutation>
  )
}

const format = utc => {
  return utc ? moment.utc(utc, 'YYYY-MM-DD HH:mm').toString() : '-'
}

class ReplicationsTable extends React.Component {
  state = {
    anchor: null,
  }

  handleClickOpen = id => event => {
    this.setState({ id, anchor: event.currentTarget })
  }

  handleClose = () => {
    this.setState({ anchor: null })
  }

  render() {
    const { replications } = this.props
    const open = Boolean(this.state.anchor)

    const sorted = Immutable.List(replications).sort(repSort)

    return (
      <Paper className={this.props.classes.root}>
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
                <TableRow key={replication.id}>
                  <TableCell component='th'>{replication.name}</TableCell>
                  <TableCell>
                    {repStatusMapping[replication.replicationStatus]}
                  </TableCell>
                  <TableCell>{replication.source.name}</TableCell>
                  <TableCell>{replication.destination.name}</TableCell>
                  <TableCell>
                    {replication.biDirectional ? 'Yes' : 'No'}
                  </TableCell>
                  <TableCell>{replication.filter}</TableCell>
                  <TableCell>{replication.itemsTransferred}</TableCell>
                  <TableCell>{replication.dataTransferred}</TableCell>
                  <TableCell>{format(replication.lastRun)}</TableCell>
                  <TableCell>{format(replication.lastSuccess)}</TableCell>
                  <TableCell>
                    <IconButton
                      onClick={this.handleClickOpen(replication.id)}
                      aria-label='More'
                      aria-owns={open ? 'actions-menu' : undefined}
                    >
                      <MoreVert />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            <Menu
              open={open}
              id='actions-menu'
              anchorEl={this.state.anchor}
              onClose={this.handleClose}
            >
              <DeleteReplication
                id={this.state.id}
                onClose={() => {
                  this.setState({ anchor: null })
                }}
              />
            </Menu>
          </TableBody>
        </Table>
      </Paper>
    )
  }
}

ReplicationsTable.propTypes = {
  replications: PropTypes.array.isRequired,
}

export default withStyles(styles)(ReplicationsTable)
