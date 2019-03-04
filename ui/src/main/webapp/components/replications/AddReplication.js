import React from 'react'
import {
  MenuItem,
  CircularProgress,
  Typography,
  Checkbox,
  FormControlLabel,
  FormGroup,
  Tooltip,
  DialogTitle,
  DialogContent,
  DialogActions,
  Dialog,
  TextField,
  Button,
} from '@material-ui/core'
import HelpIcon from '@material-ui/icons/Help'
import sitesQuery from '../sites/gql/sitesQuery'
import { Query, Mutation } from 'react-apollo'
import Immutable from 'immutable'
import { allReplications } from './gql/queries'
import { addReplication } from './gql/mutations'
import { withStyles } from '@material-ui/core/styles'

const styles = {
  tooltip: {
    marginTop: 5,
  },
  replicationForm: {
    width: 360,
    height: 480,
  },
}

const WrappedTextField = props => {
  const {
    label,
    value,
    cursor,
    children,
    autoFocus = false,
    select = false,
    helperText = '',
    error = false,
  } = props

  return (
    <TextField
      autoFocus={autoFocus}
      select={select}
      margin='dense'
      onChange={cursor('id')}
      id='id'
      label={label}
      type='text'
      fullWidth
      value={value}
      helperText={helperText}
      error={error}
    >
      {children}
    </TextField>
  )
}

function alphabetical(a, b) {
  if (a.name.toLowerCase() < b.name.toLowerCase()) {
    return -1
  }
  if (a.name.toLowerCase() > b.name.toLowerCase()) {
    return 1
  }
  return 0
}

const defaultFormState = {
  open: false,
  name: '',
  direction: '',
  sourceId: '',
  destinationId: '',
  filter: '',
  biDirectional: false,
  filterErrorText: '',
  nameErrorText: '',
}

class AddReplication extends React.Component {
  constructor(props) {
    super(props)

    this.state = defaultFormState
  }

  filterSites = function(sites, siteFilterId) {
    const filteredSites = sites.filter(site => site.id !== siteFilterId)
    return Immutable.List(filteredSites).sort(alphabetical)
  }

  sitesToMenuItems = function(sites, siteFilterId) {
    const filteredSites = sites.filter(site => site.id !== siteFilterId)
    const sorted = Immutable.List(filteredSites).sort(alphabetical)
    return sorted.map(site => (
      <MenuItem key={site.id} value={site.id}>
        {site.name}
      </MenuItem>
    ))
  }

  handleChange = map => name => event => {
    this.setState({ [map[name]]: event.target.value })
  }

  handleCheck = name => event => {
    this.setState({ [name]: event.target.checked })
  }

  handleClickOpen = () => {
    this.setState({ open: true })
  }

  handleClose = () => {
    this.setState(defaultFormState)
  }

  handleInvalidFilter() {
    this.setState({
      filterErrorText: 'Invalid CQL Filter!',
    })
  }

  handleInvalidName() {
    this.setState({
      nameErrorText: 'Name already in use!',
    })
  }

  render() {
    const { Button: AddButton, classes } = this.props
    const {
      open,
      name,
      sourceId,
      destinationId,
      filter,
      biDirectional,
      filterErrorText,
      nameErrorText,
    } = this.state

    return (
      <div>
        <AddButton onClick={this.handleClickOpen} />
        <Dialog
          open={open}
          onClose={this.handleClose}
          aria-labelledby='form-dialog-title'
          fullWidth={true}
        >
          <DialogTitle id='form-dialog-title'>
            New Replication Setup
          </DialogTitle>
          <DialogContent>
            <WrappedTextField
              label='Replication Name *'
              value={name}
              cursor={this.handleChange({
                id: 'name',
              })}
              helperText={nameErrorText ? nameErrorText : ''}
              error={nameErrorText ? true : false}
            />
            <Query query={sitesQuery}>
              {({ loading, error, data }) => {
                if (loading) return <Typography>Loading...</Typography>
                if (error) return <Typography>Error...</Typography>

                return (
                  <div>
                    <WrappedTextField
                      label='Source *'
                      value={sourceId}
                      cursor={this.handleChange({
                        id: 'sourceId',
                      })}
                      select={true}
                    >
                      {this.sitesToMenuItems(
                        data.replication.sites,
                        destinationId
                      )}
                    </WrappedTextField>

                    <WrappedTextField
                      label='Destination *'
                      value={destinationId}
                      cursor={this.handleChange({
                        id: 'destinationId',
                      })}
                      select={true}
                    >
                      {this.sitesToMenuItems(data.replication.sites, sourceId)}
                    </WrappedTextField>

                    <FormGroup>
                      <div style={{ display: 'flex' }}>
                        <FormControlLabel
                          control={
                            <Checkbox
                              checked={this.state.biDirectional}
                              onChange={this.handleCheck('biDirectional')}
                              value='biDirectional'
                            />
                          }
                          label='Bidirectional'
                        />
                        <Tooltip
                          className={classes.tooltip}
                          title='If checked, resources will be synchronized between the selected Source and Destination sites based on the filter.'
                          placement='right'
                        >
                          <HelpIcon fontSize='small' />
                        </Tooltip>
                      </div>
                    </FormGroup>
                  </div>
                )
              }}
            </Query>

            <WrappedTextField
              label='Filter *'
              value={filter}
              cursor={this.handleChange({
                id: 'filter',
              })}
              helperText={
                filterErrorText
                  ? filterErrorText
                  : 'A CQL filter specifying resources to replicate.'
              }
              error={filterErrorText ? true : false}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleClose} color='primary'>
              Cancel
            </Button>
            <Mutation
              mutation={addReplication}
              onError={error => {
                error.graphQLErrors &&
                  error.graphQLErrors.forEach(e => {
                    if (e.message === 'INVALID_FILTER') {
                      this.handleInvalidFilter()
                    } else if (e.message === 'DUPLICATE_CONFIGURATION') {
                      this.handleInvalidName()
                    }
                  })
              }}
              onCompleted={() => {
                this.setState(defaultFormState)
              }}
            >
              {(createReplication, { loading }) => (
                <div>
                  <Button
                    disabled={!(name && sourceId && destinationId && filter)}
                    color='primary'
                    onClick={() => {
                      createReplication({
                        variables: {
                          name: name,
                          sourceId: sourceId,
                          destinationId: destinationId,
                          filter: filter,
                          biDirectional: biDirectional,
                        },
                        update: (store, { data: { createReplication } }) => {
                          const data = store.readQuery({
                            query: allReplications,
                          })
                          data.replication.replications.push(createReplication)
                          store.writeQuery({
                            query: allReplications,
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

export default withStyles(styles)(AddReplication)
