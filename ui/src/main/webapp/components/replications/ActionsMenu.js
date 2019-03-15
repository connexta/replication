import React from 'react'
import { Menu, MenuItem, Typography } from '@material-ui/core'
import {
  deleteReplication,
  suspendReplication,
  cancelReplication,
} from './gql/mutations'
import { allReplications } from './gql/queries'
import { Mutation } from 'react-apollo'
import Replications from './replications'
import { withSnackbar } from 'notistack'

const DeleteReplication = withSnackbar(props => {
  const { id, onClose, name, enqueueSnackbar } = props

  return (
    <Mutation mutation={deleteReplication}>
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
                  query: allReplications,
                })

                data.replication.replications = data.replication.replications.filter(
                  r => r.id !== id
                )
                store.writeQuery({
                  query: allReplications,
                  data,
                })

                enqueueSnackbar('Deleted ' + name + '.', { variant: 'success' })
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
})

const SuspendReplication = withSnackbar(props => {
  const { id, suspend, key, label, onClose, enqueueSnackbar, name } = props

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
})

const CancelReplication = withSnackbar(props => {
  const { id, onClose, name, enqueueSnackbar } = props

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
})

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
