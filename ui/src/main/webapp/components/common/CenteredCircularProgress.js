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
import { CircularProgress } from '@material-ui/core'

function CenteredCircularProgress(props) {
  const { size = 60 } = props
  const margin = -(size / 2)

  return (
    <CircularProgress
      size={size}
      style={{
        position: 'fixed',
        top: '50%',
        left: '50%',
        marginTop: margin,
        marginLeft: margin,
      }}
    />
  )
}

export default CenteredCircularProgress
