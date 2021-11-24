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
  MenuItem,
  Select,
  FormLabel,
  FormControl,
  TextField,
  IconButton,
} from '@material-ui/core'
import ClearIcon from '@material-ui/icons/Clear'
import AddIcon from '@material-ui/icons/Add'
import { withStyles } from '@material-ui/core/styles'

const styles = {
  sortsFormControl: {
    paddingTop: '16px',
    width: '100%',
  },
  sortsFormLabel: {
    display: 'flex',
    alignItems: 'center',
  },
  iconButton: {
    '&:hover': {
      backgroundColor: 'transparent',
    },
  },
}

function SortPolicies(props) {
  const {
    sortPolicies = [],
    onAddSort,
    onRemoveSort,
    onChangeSort,
    classes,
  } = props

  return (
    <FormControl className={classes.sortsFormControl}>
      <FormLabel>
        <div className={classes.sortsFormLabel}>
          Sort Policies
          <IconButton onClick={onAddSort} className={classes.iconButton}>
            <AddIcon />
          </IconButton>
        </div>
      </FormLabel>
      {sortPolicies.map((sort, index) => (
        <div key={index} style={{ display: 'flex', alignItems: 'center' }}>
          <TextField
            margin='none'
            fullWidth
            onChange={e =>
              onChangeSort({ id: 'attribute', value: e.target.value }, index)
            }
            value={sort.attribute}
            placeholder='Attribute name'
          />
          <Select
            style={{ paddingRight: '12px' }}
            value={sort.direction}
            onChange={e =>
              onChangeSort({ id: 'direction', value: e.target.value }, index)
            }
          >
            <MenuItem value='ascending'>Ascending</MenuItem>
            <MenuItem value='descending'>Descending</MenuItem>
          </Select>
          <IconButton
            size='small'
            onClick={() => onRemoveSort(sort)}
            className={classes.iconButton}
          >
            <ClearIcon />
          </IconButton>
        </div>
      ))}
    </FormControl>
  )
}

export default withStyles(styles)(SortPolicies)
