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
