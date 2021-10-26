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
import {
  Menu,
  MenuItem,
  Typography,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Tooltip,
} from '@material-ui/core'
import HelpIcon from '@material-ui/icons/Help'
import {
  deleteReplication,
  suspendReplication,
  cancelReplication,
  runReplication,
} from './gql/mutations'
import { allReplications } from './gql/queries'
import { Mutation } from 'react-apollo'
import Replications from './replications'
import { withSnackbar, useSnackbar } from 'notistack'
import Confirmable from '../common/Confirmable'

const DeleteReplication = withSnackbar(
  class extends React.Component {
    state = {
      deleteData: true,
    }

    handleCheck = name => event => {
      this.setState({ [name]: event.target.checked })
    }

    render() {
      const { id, onClose, name, enqueueSnackbar } = this.props

      return (
        <Mutation mutation={deleteReplication}>
          {deleteReplication => (
            <Confirmable
              onConfirm={() => {
                deleteReplication({
                  variables: {
                    id: id,
                    deleteData: this.state.deleteData,
                  },
                  update: store => {
                    const data = store.readQuery({
                      query: allReplications,
                    })

                    data.replication.replications = data.replication.replications.filter(
                      r => r.id !== id
                    )
                    store.writeQuery({
                      query: allReplications,
                      data,
                    })

                    enqueueSnackbar('Deleted ' + name + '.', {
                      variant: 'success',
                    })
                  },
                })
                onClose()
              }}
              message={`Are you sure you want to delete ${name}?`}
              subMessage={
                'All historical statistics associated with this Replication will be removed in addition to the Replication.'
              }
              onClose={onClose}
              Button={props => {
                const { onClick } = props
                return (
                  <MenuItem key='Delete' onClick={onClick}>
                    <Typography>Delete</Typography>
                  </MenuItem>
                )
              }}
            >
              <FormGroup>
                <div style={{ display: 'flex' }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={this.state.deleteData}
                        onChange={this.handleCheck('deleteData')}
                        value='deleteData'
                      />
                    }
                    label='Delete Data?'
                  />
                  <Tooltip
                    title='If checked, resources that were copied to this local Node will be deleted along with the Replication.'
                    placement='right'
                  >
                    <HelpIcon fontSize='small' />
                  </Tooltip>
                </div>
              </FormGroup>
            </Confirmable>
          )}
        </Mutation>
      )
    }
  }
)
DeleteReplication.displayName = 'DeleteReplication'

const SuspendReplication = props => {
  const { id, suspend, key, label, onClose, name } = props
  const { enqueueSnackbar } = useSnackbar()

  return (
    <Mutation mutation={suspendReplication}>
      {suspendReplication => (
        <div>
          <MenuItem
            key={key}
            onClick={() => {
              suspendReplication({
                variables: { id: id, suspend: suspend },
                update: (store, { data }) => {
                  if (data && data.suspendReplication) {
                    const data = store.readQuery({
                      query: allReplications,
                    })

                    const suspendedRep = data.replication.replications.find(
                      r => r.id === id
                    )
                    const index = data.replication.replications.indexOf(
                      suspendedRep
                    )
                    suspendedRep.suspended = suspend
                    data.replication.replications[index] = suspendedRep

                    store.writeQuery({
                      query: allReplications,
                      data,
                    })

                    const msg = suspend ? 'Suspended ' : 'Enabled '
                    enqueueSnackbar(msg + 'replication ' + name + '.', {
                      variant: 'success',
                    })
                  }
                },
              })
              onClose()
            }}
          >
            <Typography>{label}</Typography>
          </MenuItem>
        </div>
      )}
    </Mutation>
  )
}

const CancelReplication = props => {
  const { id, onClose, name } = props
  const { enqueueSnackbar } = useSnackbar()

  return (
    <Mutation mutation={cancelReplication}>
      {cancelReplication => (
        <MenuItem
          key='Cancel'
          onClick={() => {
            cancelReplication({
              variables: {
                id: id,
              },
              update: (store, { data }) => {
                if (data && data.cancelReplication) {
                  const data = store.readQuery({
                    query: allReplications,
                  })

                  const canceledRep = data.replication.replications.find(
                    r => r.id === id
                  )
                  const index = data.replication.replications.indexOf(
                    canceledRep
                  )
                  canceledRep.replicationStatus = Replications.Status.CANCELED
                  data.replication.replications[index] = canceledRep

                  store.writeQuery({
                    query: allReplications,
                    data,
                  })

                  enqueueSnackbar(
                    'Canceled currently running replication for ' + name + '.',
                    { variant: 'success' }
                  )
                }
              },
            })
            onClose()
          }}
        >
          <Typography>Cancel</Typography>
        </MenuItem>
      )}
    </Mutation>
  )
}

const RunReplication = props => {
  const { id, onClose, name } = props
  const { enqueueSnackbar } = useSnackbar()

  return (
    <Mutation mutation={runReplication}>
      {runReplication => (
        <MenuItem
          key='Run'
          onClick={() => {
            runReplication({
              variables: {
                id: id,
              },
              update: (store, { data }) => {
                if (data && data.runReplication) {
                  const data = store.readQuery({
                    query: allReplications,
                  })

                  const runRep = data.replication.replications.find(
                    r => r.id === id
                  )
                  const index = data.replication.replications.indexOf(runRep)
                  runRep.replicationStatus = Replications.Status.PENDING
                  data.replication.replications[index] = runRep

                  store.writeQuery({
                    query: allReplications,
                    data,
                  })

                  enqueueSnackbar('Running replication for ' + name + '.', {
                    variant: 'success',
                  })
                }
              },
            })
            onClose()
          }}
        >
          <Typography>Run</Typography>
        </MenuItem>
      )}
    </Mutation>
  )
}

const ActionsMenu = function(props) {
  const { menuId, anchorEl = null, onClose, replication } = props

  if (replication === undefined) {
    return null
  }

  const { id, name } = replication

  return (
    <Menu
      id={menuId}
      anchorEl={anchorEl}
      open={anchorEl !== null}
      disableAutoFocusItem
      onClose={onClose}
    >
      {Replications.cancelable(replication) && (
        <CancelReplication id={id} onClose={onClose} name={name} />
      )}

      {!Replications.cancelable(replication) && (
        <RunReplication id={id} onClose={onClose} name={name} />
      )}

      <DeleteReplication id={id} onClose={onClose} name={name} />
      {replication.suspended ? (
        <SuspendReplication
          id={id}
          name={name}
          onClose={onClose}
          suspend={false}
          key='Enable'
          label='Enable'
        />
      ) : (
        <SuspendReplication
          id={id}
          name={name}
          onClose={onClose}
          suspend={true}
          key='Disable'
          label='Disable'
        />
      )}
    </Menu>
  )
}

export default ActionsMenu
