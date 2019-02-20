import React, { Fragment } from 'react'
import { Query } from 'react-apollo'
import { AllReplications } from './gql/queries'
import ReplicationsTable from './ReplicationsTable'
import AddReplication from './AddReplication'
import { Typography, CardContent, Card, Button } from '@material-ui/core'
import { withStyles } from '@material-ui/core/styles'

const styles = {
  expandingCard: {
    margin: 20,
    width: 360,
    height: 360,
    '&:hover': {
      margin: 15,
      width: 370,
      height: 370,
    },
  },
  centered: {
    margin: 'auto',
    marginTop: 30,
    display: 'flex',
    justifyContent: 'center',
  },
  right: {
    textAlign: 'right',
    marginBottom: 10,
  },
}

const AddButton = props => {
  return (
    <Button color='primary' variant='outlined' onClick={props.onClick}>
      <Typography color='inherit'>Add Replication</Typography>
    </Button>
  )
}

const AddReplicationCard = withStyles(styles)(props => {
  const { classes, onClick } = props

  return (
    <Card className={classes.expandingCard} onClick={onClick}>
      <CardContent>
        <Typography variant='h5' className={classes.centered}>
          Add Replication
        </Typography>
        <Typography variant='h1' className={classes.centered}>
          +
        </Typography>
      </CardContent>
    </Card>
  )
})

function ReplicationsContainer(props) {
  const { classes } = props

  return (
    <Query query={AllReplications} pollInterval={10000}>
      {({ data, loading, error }) => {
        if (loading) return <Typography>Loading...</Typography>
        if (error) return <Typography>Error...</Typography>

        if (
          data.replication &&
          data.replication.replications &&
          data.replication.replications.length > 0
        ) {
          return (
            <Fragment>
              <div className={classes.right}>
                <AddReplication Button={AddButton} />
              </div>
              <ReplicationsTable replications={data.replication.replications} />
            </Fragment>
          )
        }

        return (
          <div className={classes.centered}>
            <AddReplication Button={AddReplicationCard} />
          </div>
        )
      }}
    </Query>
  )
}

export default withStyles(styles)(ReplicationsContainer)
