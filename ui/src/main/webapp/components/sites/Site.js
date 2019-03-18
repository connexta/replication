import React from 'react'
import CardContent from '@material-ui/core/CardContent'
import {
  CardHeader,
  IconButton,
  Card,
  Typography,
  withStyles,
  CircularProgress,
} from '@material-ui/core'
import DeleteForever from '@material-ui/icons/DeleteForever'
import CardActions from '@material-ui/core/CardActions'
import { Mutation } from 'react-apollo'
import { allSites } from './gql/queries'
import PropTypes from 'prop-types'
import { withSnackbar } from 'notistack'
import { deleteSite } from './gql/mutations'

const styles = {
  centered: {
    textAlign: 'center',
    marginTop: -15,
    clear: 'both',
  },
  right: {
    float: 'right',
    marginBottom: -15,
  },
  card: {
    margin: 20,
    width: 200,
    height: 200,
  },
}

class Site extends React.Component {
  render() {
    const { name, content, id, classes, enqueueSnackbar } = this.props

    return (
      <Card className={classes.card}>
        <CardActions className={classes.right}>
          <Mutation
            mutation={deleteSite}
            onError={error => {
              error.graphQLErrors &&
                error.graphQLErrors.forEach(e => {
                  if (e.message === 'SITE_IN_USE') {
                    enqueueSnackbar(
                      'Failed to delete ' +
                        name +
                        '. It is used by a Replication.',
                      {
                        variant: 'error',
                        preventDuplicate: true,
                      }
                    )
                  }
                })
            }}
          >
            {(deleteReplicationSite, { loading }) => (
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
                          query: allSites,
                        })

                        data.replication.sites = data.replication.sites.filter(
                          site => site.id !== id
                        )

                        store.writeQuery({
                          query: allSites,
                          data,
                        })

                        enqueueSnackbar('Deleted node ' + name + '.', {
                          variant: 'success',
                        })
                      },
                    })
                  }}
                >
                  <DeleteForever />
                </IconButton>
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
}

Site.propTypes = {
  name: PropTypes.string.isRequired,
  content: PropTypes.string.isRequired,
  id: PropTypes.string.isRequired,
}

export default withStyles(styles)(withSnackbar(Site))
