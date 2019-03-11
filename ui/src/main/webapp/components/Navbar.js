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
import { Tooltip } from '@material-ui/core'
import { withTheme } from '@material-ui/core/styles'

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
                <HomeIcon />
              </IconButton>
            </Tooltip>
            <Tooltip
              title='Manage Nodes'
              enterDelay={theme.transitions.duration.standard}
            >
              <IconButton component={Link} to='/nodes' color='inherit'>
                <LanguageIcon />
              </IconButton>
            </Tooltip>
            <IconButton onClick={this.handleOpen} color='inherit'>
              <HelpIcon />
            </IconButton>
            <Dialog
              open={this.state.open}
              keepMounted
              onClose={this.handleClose}
            >
              <DialogTitle variant='h6' id='help-dialog-title'>
                {'Welcome to the Project Charleston BETA'}
              </DialogTitle>
              <DialogContent>
                <DialogContentText
                  variant='subtitle1'
                  id='help-dialog-description'
                >
                  Replication is a tool that enables products to be replicated
                  efficiently between DDF based nodes.
                </DialogContentText>
                <DialogContentText variant='subtitle1' gutterBottom>
                  1. Click the Globe Icon at the top to add nodes.
                </DialogContentText>
                <DialogContentText variant='subtitle1' gutterBottom>
                  2. Click ADD REPLICATION to configure a new Replication task.
                </DialogContentText>
              </DialogContent>
            </Dialog>
          </div>
        </Toolbar>
      </AppBar>
    )
  }
}

export default withTheme()(Navbar)
