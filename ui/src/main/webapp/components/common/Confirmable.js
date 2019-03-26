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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  DialogContentText,
} from '@material-ui/core'

function Confirmable(props) {
  const {
    onConfirm,
    children,
    message,
    subMessage,
    Button: Trigger,
    onClose,
  } = props
  const [open, setOpen] = React.useState(false)

  return (
    <>
      <Dialog
        open={open}
        onClose={() => {
          onClose()
          setOpen(false)
        }}
        fullWidth
      >
        <DialogTitle>{message}</DialogTitle>
        <DialogContent>
          <DialogContentText>{subMessage}</DialogContentText>
          {children}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              onClose()
              setOpen(false)
            }}
            color='primary'
          >
            Cancel
          </Button>
          <Button
            autoFocus
            onClick={() => {
              onConfirm()
              setOpen(false)
            }}
            color='primary'
          >
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
      <Trigger onClick={() => setOpen(true)} />
    </>
  )
}

export default Confirmable
