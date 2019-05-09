import React from 'react'
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
} from '@material-ui/core'
import { MoreVert } from '@material-ui/icons'
import HelpIcon from '@material-ui/icons/Help'
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
  actions: {
    float: 'right',
  },
}

function RemoteReplicationsTable(props) {
  const [anchor, setAnchor] = React.useState(null)
  const [selectedReplication, setSelectedReplication] = React.useState({})

  const { replications, classes } = props
  const sorted = Immutable.List(replications.sort(Replications.repSort))

  return (
    <Paper className={classes.root}>
      <Toolbar>
        <Typography variant='h6' id='tableTitle' className={classes.title}>
          {'Remote Managed Replications'}
          <Tooltip
            title='Replications in this table are remotely managed by the Cloud because one of the source or destination Nodes has been identified as a Cloud ready Node. They will be run by the Cloud and statistics for these Replications will not be available locally.'
            placement='bottom'
          >
            <HelpIcon fontSize='small' />
          </Tooltip>
        </Typography>
      </Toolbar>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Source</TableCell>
            <TableCell>Destination</TableCell>
            <TableCell>Bidirectional</TableCell>
            <TableCell>Suspended</TableCell>
            <TableCell />
          </TableRow>
        </TableHead>
        <TableBody>
          {sorted &&
            sorted.map(replication => (
              <TableRow key={replication.id}>
                <TableCell component='th'>{replication.name}</TableCell>
                <TableCell>{replication.source.name}</TableCell>
                <TableCell>{replication.destination.name}</TableCell>
                <TableCell>
                  {replication.biDirectional ? 'Yes' : 'No'}
                </TableCell>
                <TableCell>{replication.suspended ? 'Yes' : 'No'}</TableCell>
                <TableCell>
                  <IconButton
                    onClick={e => {
                      setSelectedReplication(replication)
                      setAnchor(e.currentTarget)
                    }}
                    aria-label='More'
                    aria-owns={anchor !== null ? 'actions-menu' : undefined}
                    className={classes.actions}
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
            ))}
        </TableBody>
      </Table>
    </Paper>
  )
}

export default withStyles(styles)(RemoteReplicationsTable)
