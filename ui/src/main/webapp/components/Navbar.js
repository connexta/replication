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
/* global localStorage */
import React from 'react'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import IconButton from '@material-ui/core/IconButton'
import Typography from '@material-ui/core/Typography'
import Dialog from '@material-ui/core/Dialog'
import DialogTitle from '@material-ui/core/DialogTitle'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import HomeIcon from '@material-ui/icons/Home'
import LanguageIcon from '@material-ui/icons/Language'
import HelpIcon from '@material-ui/icons/Help'
import { Link } from 'react-router-dom'
import { Tooltip, ListItem, ListItemText } from '@material-ui/core'
import { withTheme } from '@material-ui/core/styles'
import { List } from '@material-ui/core'

const Nodes = () => {
  return <LanguageIcon />
}

const Home = () => {
  return <HomeIcon />
}

const HelpDialog = props => {
  const { open, handleClose } = props

  return (
    <Dialog open={open} onClose={handleClose} maxWidth='md'>
      <DialogTitle variant='h6' id='help-dialog-title'>
        {'Welcome to the Project Charleston BETA'}
      </DialogTitle>
      <DialogContent>
        <DialogContentText id='help-dialog-description'>
          Replication is a tool that enables resources to be replicated
          efficiently between <b>Nodes</b>. Once a Node is created, it can be
          used by <b>Replications</b> to start transferring resources. To begin:
        </DialogContentText>
        <List>
          <ListItem>
            <ListItemText>
              1. Add new Nodes by clicking the <Nodes /> icon on the navigation
              menu. The local Node has already been created.
            </ListItemText>
          </ListItem>
          <ListItem>
            <ListItemText>
              2. Return to the home page (<Home />) and click the{' '}
              <b>Add Replication</b> button.
            </ListItemText>
          </ListItem>
        </List>
      </DialogContent>
    </Dialog>
  )
}

class Navbar extends React.Component {
  state = {
    open: false,
  }

  handleOpen = () => {
    this.setState({ open: true })
  }

  handleClose = () => {
    this.setState({ open: false })
  }

  componentWillMount() {
    let firstVisit = localStorage['alreadyVisited']
    if (!firstVisit) {
      localStorage['alreadyVisited'] = true
      this.handleOpen()
    }
  }

  render() {
    const { theme } = this.props

    return (
      <AppBar
        position='static'
        style={
          { backgroundColor: 'rgb(24, 188, 156)' } // todo theme this
        }
      >
        <Toolbar>
          <Typography variant='h6' color='inherit' noWrap>
            Project Charleston BETA
          </Typography>
          <div className='navIcons'>
            <Tooltip
              title='Manage Replications'
              enterDelay={theme.transitions.duration.standard}
            >
              <IconButton component={Link} to='/' color='inherit'>
                <Home />
              </IconButton>
            </Tooltip>
            <Tooltip
              title='Manage Nodes'
              enterDelay={theme.transitions.duration.standard}
            >
              <IconButton component={Link} to='/nodes' color='inherit'>
                <Nodes />
              </IconButton>
            </Tooltip>
            <IconButton onClick={this.handleOpen} color='inherit'>
              <HelpIcon />
            </IconButton>
            <HelpDialog open={this.state.open} handleClose={this.handleClose} />
          </div>
        </Toolbar>
      </AppBar>
    )
  }
}

export default withTheme()(Navbar)
