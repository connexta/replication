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

const styles = {
  root: {
    width: '100%',
    overflowX: 'auto',
  },
  title: {
    flex: '0 0 auto',
  },
}

const format = utc => {
  return utc
    ? moment
        .utc(utc)
        .format('MMM DD YYYY HH:mm zZ')
        .toString()
    : '-'
}

class ReplicationsTable extends React.Component {
  state = {
    anchor: null,
  }

  handleClickOpen = replication => event => {
    this.setState({ replication, anchor: event.currentTarget })
  }

  handleClose = () => {
    this.setState({ anchor: null })
  }

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
                <TableRow key={replication.id}>
                  <TableCell component='th'>{replication.name}</TableCell>
                  <TableCell>
                    {Replications.statusDisplayName(replication)}
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
                      onClick={this.handleClickOpen(replication)}
                      aria-label='More'
                      aria-owns={
                        this.state.anchor !== null ? 'actions-menu' : undefined
                      }
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
