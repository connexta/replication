# Replication [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=replication&metric=alert_status)](https://sonarcloud.io/dashboard?id=replication) [![Known Vulnerabilities](https://snyk.io/test/github/connexta/replication/badge.svg)](https://snyk.io/test/github/connexta/replication)

## Overview
Replication is the process of creating a copy of a subset of data and storing it on another DDF- or HDFS-based system. Data can be pulled from a remote DDF and saved to another DDF or HDFS system. Metacards produced by replication are marked with a "Replication Origins" attribute and a tag of "replicated." Replication will automatically start transferring data once an admin creates a replication configuration.

## Installing Replication
Replication is not installed by default with a standard installation. There are several installation options available.

#### Installing Via Kar Feature File
An administrator can install the replication feature by following the following steps:
* Locate and download the desired replication artifact kar file.
* On a running and configured DDF instance, place the kar file in the ${DDF_HOME}/deploy directory.
* Confirm the feature is running in the Admin Console or the Karaf command line interface.

#### Additional Installation Steps
Installation on a DDF or Alliance instance requires additional steps
* Start the registry feature: `feature:install registry-app`.
* Copy the replication.policy into the ${DDF_HOME}/security directory.

## Configuring Replication Nodes
This version of Replication supports nodes created for DDF-based applications as well as HDFS-based systems.

#### Configuring DDF Nodes
To configure replication to/from a DDF node, a node must first be created. This can be done in a few steps:
* Navigate to https://<host>:<port>/admin/replication/#/nodes and click the + 
* On the Create new Node screen, fill out the following fields:
  * Name: The display name to give the node. 
  * Hostname: The hostname of the node.
  * Port: The port where the service can be found. This is `8993` by default in DDF.
  * Root Context: The path under which replication services can be found. This is `services` by default in DDF.
  
#### Configuring HDFS Nodes
Support for HDFS replication is only available if webHDFS REST endpoints are enabled on the remote HDFS cluster.

To configure replication to/from a HDFS node, a node must first be created. This can be done in a few steps:
* Navigate to https://<host>:<port>/admin/replication/#/nodes and click the + 
* On the Create new Node screen, fill out the following fields:
  * Name: The display name to give the node. 
  * Hostname: The hostname of the node.
  * Port: The port where the service can be found. This is `9870` by default for HDFS Namenodes.
  * Root Context: This is the full path after the port for any webHDFS URL.

* localhost HDFS example configuration : 
```
To write to directory /user/tonystark on a local HDFS instance
- The webHDFS URL for that directory might look like: http://localhost:9870/webhdfs/v1/user/tonystark
- The hostname would be: localhost
- The port would be: 9870
- The root context would be webhdfs/v1/user/tonystark
```  
* remote HDFS example configuration:  
```
To write to directory /user/tonystark on a remote AWS HDFS installation
- The webHDFS URL for that directory might look like: https://testnode1.us-gov-west-1.compute.amazonaws.com:8443/gateway/default/webhdfs/v1/user/tonystark
- The hostname would be: testnode1.us-gov-west-1.compute.amazonaws.com
- The port would be: 8443
- The root context would be gateway/default/webhdfs/v1/user/tonystark
```  

## Configuring Replication
After the nodes are created, a replication can be configured. This can be done in a few steps:
* Navigate to https://<host>:<port>/admin/replication click the Add Replication 
* On the New Replication Setup screen, fill out the following fields:
  * Replication Name: The display name for this replication.
  * Source Node: The node in the dropdown to use as the source (replicating FROM this source).
  * Destination Node: The node in the dropdown to use as the destination (replicating TO this node).
  * Bidirectional: Select this checkbox if replication should synchronize the contents of source and destination nodes.
  * Filter: A previously used Intrigue query can be selected from the dropdown, otherwise enter CQL into the Filter text field.
  
Notes: 
* At present, filters on files stored in HDFS are not applied. In the future CQL applied to filenames will be possible, but that is not available as of this writing.
* Any replication using HDFS adapter should NOT use the bidirectional flag. This implementation assumes the user will not be reading from and writing to the same directory. 
To make use of multiple directories -ie, writing to /output and reading from /input- will require two distinct replications be created.

#### Deleting a Replication Configuration
* Replication configurations can be deleted by selected the Delete option from the vertical ellipsis on the right side of the replication. 
* A confirmation modal will open that allow the Replicated Data to also be deleted.

#### Disabling Replication
* A replication may be disabled after it has been configured. To disable a replication select the Disable option from the vertical ellipsis on the right side of the replication. 
* The replication will move from the Active Replications to the Inactive Replications.

#### Running Replication
After replication has been configured, it will automatically run every 5 minutes. 
   
#### Failure Retry
Items that failed to be replicated for any reason other than connection loss will be retried up to five times, except for deletion, which is only tried once.  

## Known Issues, Limitations, and Assumptions for Replication
Replication is still at an early stage in its lifecycle, so there are a few details that the user should be aware of.

#### Federation and Replication
* When mixing replication and federation there is the chance that duplicate results will appear in search results. It is recommended that systems do not setup replication to systems they are also federated with.

#### Fanout Proxies
* Replicating from a DDF that is configured as a Fanout Proxy will result in the replication of records from sources configured in that DDF.
* Replicating to a DDF that is configured as a Fanout Proxy will result in the replication of records only to the fanout and not its sources.

#### Connected Sources
* Replicating from a DDF that is configured with Connected Sources will result in the replication of records from the Connected Sources in addition to any local records.

#### Derived Resources
* Derived resources, from products such as NITFs, will not be replicated. New derived resources will, however, be generated by the receiving DDF.

#### Confluence Sources
* Metadata received from a Confluence source cannot be replicated. This is because Confluence Metadata is generated when the source is queried and contains certain information which cannot be persisted on a DDF.
* Any metadata that is generated upon querying a source, similar to how Confluence metadata is generated, cannot be replicated either.


