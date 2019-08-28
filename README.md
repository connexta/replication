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
The replication docker stack requires the following to be configured on the swarm before deploying:

###### Configuration
The configuration that replication uses that need to be populated in docker config

|Config Name | Description|
|------------|------------|
|replication-spring-config| The spring-boot application.yml for replication. Example below.|


Example replication-spring-config
```yaml
logging:
  level:
    root: INFO
    org.apache.cxf.interceptor.LoggingOutInterceptor: WARN
    org.apache.cxf.interceptor.LoggingInInterceptor: WARN
    javax.xml.soap: ERROR
spring:
  data:
    solr:
      host: http://replication-solr:8983/solr
replication:
  period: 300
  connectionTimeout: 30
  receiveTimeout: 60
  localSite: some-unique-id-1234
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
###### Adding Site Example
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
###### Adding Replication Jobs Example
Example jobs.json
```json
  [
    {
      "version": 1,
      "id": "unique-job-id-98765",
      "name": "pdf-harvest",
      "bidirectional": false,
      "source":" some-unique-id-1234",
      "destination": "another-unique-id-5678",
      "filter": "\"media.type\" like 'application/pdf'",
      "suspended": false,
    }
  ]
```
```
curl -H "Content-Type: application/json" \
-d @jobs.json \
http://localhost:8983/solr/replication_config/update?commitWithin=1000
```

#### Removing Replication Configuration
Example removing all sites with the name 'Test'
```
curl -X POST \
  'http://localhost:8983/solr/replication_site/update?commit=true' \
  -H 'Content-Type: application/xml' \
  -d '<delete><query>name_txt:Test</query></delete>'
```
