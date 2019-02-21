import React from 'react'
import CardContent from '@material-ui/core/CardContent'
import {
  CardHeader,
  IconButton,
  Card,
  Typography,
  withStyles,
  SnackbarContent,
  Snackbar,
  CircularProgress,
} from '@material-ui/core'
import DeleteForever from '@material-ui/icons/DeleteForever'
import CardActions from '@material-ui/core/CardActions'
import gql from 'graphql-tag'
import { Mutation } from 'react-apollo'
import sitesQuery from './gql/sitesQuery'
import PropTypes from 'prop-types'

const DELETE_SITE = gql`
  mutation deleteReplicationSite($id: Pid!) {
    deleteReplicationSite(id: $id)
  }
`

const styles = {
  centered: {
    'text-align': 'center',
    'margin-top': -15,
    clear: 'both',
  },
  right: {
    float: 'right',
    'margin-bottom': -15,
  },
  card: {
    margin: 20,
    width: 200,
    height: 200,
  },
}

function Site(props) {
  const { name, content, id, classes } = props

  return (
    <Card className={classes.card}>
      <CardActions className={classes.right}>
        <Mutation mutation={DELETE_SITE}>
          {(deleteReplicationSite, { loading, error }) => (
            <div>
              <IconButton
                color='primary'
                onClick={() => {
                  deleteReplicationSite({
                    variables: {
                      id: id,
                    },
                    update: store => {
                      const data = store.readQuery({
                        query: sitesQuery,
                      })

                      data.replication.sites = data.replication.sites.filter(
                        site => site.id !== id
                      )

                      store.writeQuery({
                        query: sitesQuery,
                        data,
                      })
                    },
                  })
                }}
              >
                <DeleteForever />
              </IconButton>
              {error && (
                <Snackbar
                  anchorOrigin={{
                    vertical: 'bottom',
                    horizontal: 'left',
                  }}
                  open
                >
                  <SnackbarContent
                    message={
                      <Typography style={{ color: '#fff' }}>
                        Failed to delete node {name}. It is used by a configured
                        Replication.
                      </Typography>
                    }
                    style={{ backgroundColor: '#d32f2f' }}
                  />
                </Snackbar>
              )}
              {loading && <CircularProgress size={10} />}
            </div>
          )}
        </Mutation>
      </CardActions>
      <CardHeader title={name} className={classes.centered} />
      <CardContent className={classes.centered}>
        <Typography>{content}</Typography>
      </CardContent>
    </Card>
  )
}

Site.propTypes = {
  name: PropTypes.string.isRequired,
  content: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
}

export default withStyles(styles)(Site)
