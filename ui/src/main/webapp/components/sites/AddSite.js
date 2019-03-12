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
import sitesQuery from './gql/sitesQuery'
import addSite from './gql/addSite'
import { CircularProgress, Card, withStyles } from '@material-ui/core'

const styles = {
  expandingCard: {
    margin: 20,
    width: 200,
    height: 200,
    '&:hover': {
      margin: 15,
      width: 210,
      height: 210,
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
  nameErrorText: '',
}

const AddSite = class extends React.Component {
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

export default withStyles(styles)(AddSite)
