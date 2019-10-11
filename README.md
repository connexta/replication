# Replication [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=replication&metric=alert_status)](https://sonarcloud.io/dashboard?id=replication) [![Known Vulnerabilities](https://snyk.io/test/github/connexta/replication/badge.svg)](https://snyk.io/test/github/connexta/replication) [![CircleCI](https://circleci.com/gh/connexta/replication.svg?style=svg)](https://circleci.com/gh/connexta/replication)

## Overview
Replication is the process of creating a copy of a subset of data and storing it on another DDF or ION based System. Data can be pulled from a remote DDF and saved to another DDF or ION system. Metacards produced by replication are marked with a "Replication Origins" attribute and a tag of "replicated". Replication will automatically start transferring data once an admin creates a replication configuration.


## Known Issues, Limitations, and Assumptions
Replication is still at an early stage in its lifecycle, so there are a few details that the user should be aware of.

#### Fanout Proxies
Replicating from a DDF system that is configured as a Fanout Proxy will result in the replication of records from sources configured in that system.

Replicating to a DDF system that is configured as a Fanout Proxy will result in the replication of records only to the fanout and not its sources.

#### Connected Sources
Replicating from a DDF sytem that is configured with Connected Sources will result in the replication of records from the Connected Sources in addition to any local records.

#### Derived Resources
Derived resources, from products such as NITFs, will not be replicated.

## Docker Compose Deployment

#### Prerequisites
Replication is deployed as a docker stack in a docker swarm. So, before deploying replication,
you need a running docker instance with an initialized swarm. Once you have a swarm running you can 
configure it for replication with the following steps.

###### Configuration
The configuration that replication uses needs to be populated in docker config

|Config Name | Description|
|------------|------------|
|replication-spring-config| The spring-boot application.yml for replication. Example below.|


Example replication-spring-config
```yaml
logging:
  level: 
  #You can adjust the log levels of a package or class in this section 
    root: INFO
    org.apache.cxf.interceptor.LoggingOutInterceptor: WARN
    org.apache.cxf.interceptor.LoggingInInterceptor: WARN
    javax.xml.soap: ERROR
spring:
  data:
    solr:
    #This is the URL the replication service will use to communicate with solr.
    #As long as you use the docker compose file you won't need to change this.
      host: http://replication-solr:8983/solr 
  profiles.active: Classic
replication:
  #This is the number of seconds between each replication, lower it if you're going to be testing.
  period: 300
  #Timeouts for calls to sites
  connectionTimeout: 30
  receiveTimeout: 60
  #The ID of the local site. All replications will go to/from this site. Direction will be determined 
  #by the type and kind of site being replicated with. This field needs to be set, and a site with
  #this ID needs to be saved before any replication will take place. 
  localSite: some-unique-id-1234
  #The remote sites to handle replication for, remove this to handle replication for all sites.
  sites:
  - site1
  - site2
  
# Exposes metrics
management:
  endpoint:
    metrics:
      enabled: true
    prometheus:
      enabled: true
  endpoints:
    web:
      exposure:
        include: 'prometheus,metrics,health,info'
  metrics:
    export:
      prometheus:
        enabled: true
```

To create a docker config use the `config create` command, which uses this syntax:
`docker config create <CONFIG_NAME> <FILE_DIRECTORY>`

Example:
`docker config create replication-spring-config replication/configs/application.yml`

###### Profiles

Replication can be run with one of two profiles. You can specify which profile to use in the 'spring.profiles.active'
property as demonstrated in the example above. "Classic" will use the classic monolithic implementation.
"Ion" will use the new scalable, cloud oriented implementation.

###### Metrics 

Replication supports reporting metrics through Micrometer. Prometheus is used as the metrics collection platform. The replication-spring-config provides example configuration for exposing metrics from within the application.

###### Grafana

A Grafana dashboard `grafana-dashboard.json` is provided, which can be imported into Grafana.

###### Secrets
Replication requires certs and ssl configurations in order to talk with remote DDF based systems. This information is stored in docker secrets.

|Secret Name | Description|
|------------|------------|
|replication-truststore|A truststore to use for TLS|
|replication-keystore|A keystore for this system to use TLS|
|replication-ssl|SSL properties for TLS including passwords for the truststore and keystore|

Example replication-ssl
```properties
javax.net.ssl.trustStorePassword=changeit
javax.net.ssl.trustStoreType=jks
javax.net.ssl.keyStorePassword=changeit
javax.net.ssl.keyStoreType=jks
javax.net.ssl.certAlias=localhost
```
Only the properties that differ from the defaults above need to be specified in replication-ssl

To add a docker secret use the `secret create` command, which uses this syntax:
`docker secret create <SECRET_NAME> <FILE_DIRECTORY>`

Example:
`docker secret create replication-truststore replication/secrets/truststore.jks`

#### Running
Running the stack will start up a solr service and the replication service.

```
docker stack deploy -c docker-compose.yml repsync
```

#### Adding Replication Configuration
Replication can be configured by using the Solr rest endpoint.
```
curl -H "Content-Type: application/json" \
-d @/path/to/json/config/file.json \
http://localhost:8983/solr/<target-core>/update?commitWithin=1000
``` 

#### Types and Kinds of sites
Sites can be of different types and kinds. The type and kind of the remote site (the one that's not 
the local site) will determine whether the replication is a push, pull, harvest, or both push and pull.

|Site Type | Site Kind | Replication Direction |
|------------|------------|------------|
|DDF | TACTICAL | BIDIRECTIONAL |
|DDF | REGIONAL | HARVEST |
|ION | TACTICAL | BIDIRECTIONAL |
|ION | REGIONAL | PUSH |

###### Directions
Here's how the various replication directions are defined:
PUSH - Send information to the remote site to be stored.
PULL - Retrieve information from the remote site and store it locally.
BIDIRECTIONAL - Perform both a push and a pull.
HARVEST - Similar to a pull but harvesting will ignore updates and deletes on replicated information.  

###### Adding Site Example
Create a json file with site descriptions like the example below and you can use the following curl 
command to save those sites for replication to use.
Example sites.json
```json
  [
      {
        "version": 1,
        "id": "some-unique-id-1234",
        "name": "RepSync-Node1",
        "description": "Replication Site 1",
        "url": "https://host1:8993/services/",
        "type": "DDF",
        "kind": "TACTICAL",
        "polling_period": 600000,
        "parallelism_factor": 1
       },
      {
        "version":1,
        "id": "another-unique-id-5678",
        "name": "RepSync-Node2",
        "description": "Replication Site 2",
        "url": "https://host2:8993/services",
        "type": "DDF",
        "kind": "TACTICAL",
        "polling_period": 600000,
        "parallelism_factor": 1
      }
   ]
```
```
curl -H "Content-Type: application/json" \
-d @sites.json \
http://localhost:8983/solr/replication_site/update?commitWithin=1000
```
###### Adding Replication Filters Example
Create a json file with filter descriptions like the example below and you can use the following curl 
command to save those filters for replication to use.
Example filters.json
```json
   [
      {
        "name":"pdf-harvest",
        "site_id":"remote-site-id",
        "filter":"\"media.type\" like 'application/pdf'",
        "suspended":false,
        "priority": 0,
        "id":"unique-filter-id-98765",
        "version":1
      }
    ]
```
```
curl -H "Content-Type: application/json" \
-d @filters.json \
http://localhost:8983/solr/replication_filter/update?commitWithin=1000
```

#### Removing Replication Configuration
Example removing all sites with the name 'Test'
```
curl -X POST \
  'http://localhost:8983/solr/replication_site/update?commit=true' \
  -H 'Content-Type: application/xml' \
  -d '<delete><query>name_txt:Test</query></delete>'
```
Example removing all sites
```
curl -X POST \
  'http://localhost:8983/solr/replication_site/update?commit=true' \
  -H 'Content-Type: application/xml' \
  -d '<delete><query>*:*</query></delete>'
```

#### Try it out
You can try replication for yourself using the steps below as a high level overview. Details on how 
to complete steps related to setting up replication can be found above, starting with 
"Docker Compose Deployment".
1. Make sure docker is up and running. Start up a docker swarm if you haven't already.
3. Create docker config
4. Create docker secrets
5. Deploy stack
6. Create two sites, Both as REGIONAL DDFs, with URLs pointing to running DDF instances.
7. The remote site (The site that isn't the local site) will be your source of data. Upload test data to the source site if it has none.
8. Create filter. The "site_id" should match the ID of remote site. The filter can be changed to something like "\"title\" like 'test'" or 
	"\"title\" like '*'" to replicate everything.
9. execute `docker service logs -f repsync_ion-replication` to view the logs and wait for replication to occur. Once you start seeing logs check the
	local site to see the data start coming in.
