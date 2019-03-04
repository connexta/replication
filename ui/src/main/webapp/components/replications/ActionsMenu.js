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

const DeleteReplication = props => {
  const { id, onClose } = props

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

const SuspendReplication = props => {
  const { id, suspend, key, label, onClose } = props

  return (
    <Mutation mutation={suspendReplication}>
      {suspendReplication => (
        <MenuItem
          key={key}
          onClick={() => {
            suspendReplication({
              variables: {
                id: id,
                suspend: suspend,
              },
            })
            onClose()
          }}
        >
          <Typography>{label}</Typography>
        </MenuItem>
      )}
    </Mutation>
  )
}

const CancelReplication = props => {
  const { id, onClose } = props

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

const ActionsMenu = function(props) {
  const { menuId, anchorEl = null, onClose, replication } = props

  if (replication === undefined) {
    return null
  }

  const { id } = replication

  return (
    <Menu
      id={menuId}
      anchorEl={anchorEl}
      open={anchorEl !== null}
      disableAutoFocusItem
      onClose={onClose}
    >
      {Replications.cancelable(replication) && (
        <CancelReplication id={id} onClose={onClose} />
      )}

      <DeleteReplication id={id} onClose={onClose} />
      {replication.suspended ? (
        <SuspendReplication
          id={id}
          onClose={onClose}
          suspend={false}
          key='Enable'
          label='Enable'
        />
      ) : (
        <SuspendReplication
          id={id}
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
