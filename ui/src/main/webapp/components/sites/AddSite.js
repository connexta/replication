import React from 'react'
import { Mutation } from 'react-apollo'
import gql from 'graphql-tag'

import Button from '@material-ui/core/Button'
import TextField from '@material-ui/core/TextField'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import Card from '@material-ui/core/Card'
import CardContent from '@material-ui/core/CardContent'
import AddIcon from '@material-ui/icons/Add'
import sitesQuery from './sitesQuery'
import { expandingTile, centered } from './styles.css'

const ADD_SITE = gql`
  mutation createReplicationSite($name: String!, $address: Address!) {
    createReplicationSite(name: $name, address: $address) {
      id
      name
      address {
        url
      }
    }
  }
`

export default class AddSite extends React.Component {
  state = {
    open: false,
    name: '',
    hostname: '',
    port: 0,
  }

  handleClickOpen = () => {
    this.setState({ open: true })
  }

  handleClose = () => {
    this.setState({ open: false })
  }

  handleChange = key => event => {
    this.setState({ [key]: event.target.value })
  }

  render() {
    return (
      <div>
        <Card className={expandingTile} onClick={this.handleClickOpen}>
          <CardContent className={centered}>
            <AddIcon fontSize={'large'} />
          </CardContent>
        </Card>

        <Dialog
          open={this.state.open}
          onClose={this.handleClose}
          aria-labelledby='form-dialog-title'
        >
          <DialogTitle id='form-dialog-title'>Create new Site</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Create a new Site to allow replication to and from.
            </DialogContentText>
            <TextField
              autoFocus
              margin='dense'
              id='name'
              label='Name'
              type='text'
              onChange={this.handleChange('name')}
              fullWidth
            />
            <TextField
              margin='dense'
              id='hostname'
              label='Hostname'
              type='text'
              onChange={this.handleChange('hostname')}
              fullWidth
            />
            <TextField
              margin='dense'
              id='port'
              label='Port'
              type='number'
              onChange={this.handleChange('port')}
              fullWidth
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleClose} color='primary'>
              Cancel
            </Button>

            <Mutation mutation={ADD_SITE}>
              {(createReplicationSite, { loading, error }) => (
                <div>
                  <Button
                    color='primary'
                    onClick={() => {
                      createReplicationSite({
                        variables: {
                          name: this.state.name,
                          address: {
                            host: {
                              hostname: this.state.hostname,
                              port: this.state.port,
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
                      this.setState({
                        name: '',
                        hostname: '',
                        port: 0,
                        open: false,
                      })
                    }}
                  >
                    Save
                  </Button>
                  {error && <p>Error...:(</p>}
                  {loading && <p>Loading...</p>}
                </div>
              )}
            </Mutation>
          </DialogActions>
        </Dialog>
      </div>
    )
  }
}
