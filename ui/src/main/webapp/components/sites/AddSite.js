import React from 'react'
import { Mutation } from 'react-apollo'
import Button from '@material-ui/core/Button'
import TextField from '@material-ui/core/TextField'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import ExpandingCard from '../common/ExpandingCard'
import CardContent from '@material-ui/core/CardContent'
import AddIcon from '@material-ui/icons/Add'
import sitesQuery from './gql/sitesQuery'
import styled from 'styled-components'
import addSite from './gql/addSite'
import { CircularProgress } from '@material-ui/core'

const CenteredCardContent = styled(CardContent)`
  margin: auto;
  margin-top: 30%;
  display: flex;
  justify-content: center;
`

const defaultState = {
  open: false,
  name: '',
  hostname: '',
  port: 0,
  nameErrorText: '',
}

export default class AddSite extends React.Component {
  state = defaultState

  handleClickOpen = () => {
    this.setState({ open: true })
  }

  handleClose = () => {
    this.setState({ open: false })
  }

  handleChange = key => event => {
    this.setState({ [key]: event.target.value })
  }

  handleInvalidName() {
    this.setState({
      nameErrorText: 'Name already in use!',
    })
  }

  render() {
    const { open, name, hostname, port, nameErrorText } = this.state

    return (
      <div>
        <ExpandingCard onClick={this.handleClickOpen}>
          <CenteredCardContent>
            <AddIcon fontSize={'large'} />
          </CenteredCardContent>
        </ExpandingCard>

        <Dialog
          open={open}
          onClose={this.handleClose}
          aria-labelledby='form-dialog-title'
        >
          <DialogTitle id='form-dialog-title'>Create new Node</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Create a new Node to allow replication to and from.
            </DialogContentText>
            <TextField
              autoFocus
              margin='dense'
              id='name'
              label='Name *'
              type='text'
              onChange={this.handleChange('name')}
              fullWidth
              helperText={nameErrorText ? nameErrorText : ''}
              error={nameErrorText ? true : false}
            />
            <TextField
              margin='dense'
              id='hostname'
              label='Hostname *'
              type='text'
              onChange={this.handleChange('hostname')}
              fullWidth
            />
            <TextField
              margin='dense'
              id='port'
              label='Port *'
              type='number'
              onChange={this.handleChange('port')}
              fullWidth
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleClose} color='primary'>
              Cancel
            </Button>

            <Mutation
              mutation={addSite}
              onError={error => {
                error.graphQLErrors &&
                  error.graphQLErrors.forEach(e => {
                    if (e.message === 'DUPLICATE_SITE') {
                      this.handleInvalidName()
                    }
                  })
              }}
              onCompleted={() => {
                this.setState(defaultState)
              }}
            >
              {(createReplicationSite, { loading }) => (
                <div>
                  <Button
                    disabled={!(name && hostname && port)}
                    color='primary'
                    onClick={() => {
                      createReplicationSite({
                        variables: {
                          name: name,
                          address: {
                            host: {
                              hostname: hostname,
                              port: port,
                            },
                          },
                        },
                        update: (
                          store,
                          { data: { createReplicationSite } }
                        ) => {
                          const data = store.readQuery({
                            query: sitesQuery,
                          })
                          data.replication.sites.push(createReplicationSite)
                          store.writeQuery({
                            query: sitesQuery,
                            data,
                          })
                        },
                      })
                    }}
                  >
                    Save {loading && <CircularProgress size={10} />}
                  </Button>
                </div>
              )}
            </Mutation>
          </DialogActions>
        </Dialog>
      </div>
    )
  }
}
