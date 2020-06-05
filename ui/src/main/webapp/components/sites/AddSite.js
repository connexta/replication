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
import { Mutation } from 'react-apollo'
import Button from '@material-ui/core/Button'
import TextField from '@material-ui/core/TextField'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import CardContent from '@material-ui/core/CardContent'
import AddIcon from '@material-ui/icons/Add'
import { allSites } from './gql/queries'
import { addSite } from './gql/mutations'
import { CircularProgress, Card, withStyles } from '@material-ui/core'

const styles = {
  expandingCard: {
    margin: 20,
    width: 220,
    height: 220,
    '&:hover': {
      margin: 15,
      width: 230,
      height: 230,
      cursor: 'pointer',
    },
  },
  centered: {
    margin: 'auto',
    marginTop: '30%',
    display: 'flex',
    justifyContent: 'center',
  },
}

const defaultState = {
  open: false,
  name: '',
  hostname: '',
  port: 0,
  rootContext: 'services',
  nameErrorText: '',
  hostnameErrorText: '',
  portErrorText: '',
}

const AddSite = class extends React.Component {
  state = defaultState

  handleClickOpen = () => {
    this.setState({ open: true })
  }

  handleClose = () => {
    this.setState(defaultState)
  }

  handleChange = key => event => {
    this.setState({ [key]: event.target.value })
  }

  handleInvalidName() {
    this.setState({
      nameErrorText: 'Name already in use. Please choose a new one.',
    })
  }

  handleInvalidHostname() {
    this.setState({ hostnameErrorText: 'Not a valid hostname.' })
  }

  handleInvalidPort() {
    this.setState({
      portErrorText:
        'Port is not in valid range. Valid range is between 0 and 65535.',
    })
  }

  render() {
    const {
      open,
      name,
      hostname,
      port,
      rootContext,
      nameErrorText,
      hostnameErrorText,
      portErrorText,
    } = this.state
    const { classes } = this.props

    return (
      <div>
        <Card className={classes.expandingCard} onClick={this.handleClickOpen}>
          <CardContent className={classes.centered}>
            <AddIcon fontSize={'large'} />
          </CardContent>
        </Card>

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
              helperText={hostnameErrorText ? hostnameErrorText : ''}
              error={hostnameErrorText ? true : false}
            />
            <TextField
              margin='dense'
              id='port'
              label='Port *'
              type='number'
              onChange={this.handleChange('port')}
              fullWidth
              helperText={portErrorText ? portErrorText : ''}
              error={portErrorText ? true : false}
            />
            <TextField
              margin='dense'
              id='rootContext'
              label='Root Context *'
              type='text'
              onChange={this.handleChange('rootContext')}
              fullWidth
              helperText='The path under which the replication services can be found. Typically this is under `services` for DDF-based nodes. HDFS-based systems require entering the full path after the port; ie `/webhdfs/v1/desiredDrectoryPath`'
              value={rootContext}
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
                    if (e.message === 'INVALID_HOSTNAME') {
                      this.handleInvalidHostname()
                    }
                    if (e.message === 'INVALID_PORT_RANGE') {
                      this.handleInvalidPort()
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
                    disabled={!(name && hostname && port && rootContext)}
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
                          rootContext: rootContext,
                        },
                        update: (
                          store,
                          { data: { createReplicationSite } }
                        ) => {
                          const data = store.readQuery({
                            query: allSites,
                          })
                          data.replication.sites.push(createReplicationSite)
                          store.writeQuery({
                            query: allSites,
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

export default withStyles(styles)(AddSite)
