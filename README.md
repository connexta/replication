# Replication


| | |
|----------|----------|
|SonarQube | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=replication&metric=alert_status)](https://sonarcloud.io/dashboard?id=replication)|
|Snyk | [![Known Vulnerabilities](https://snyk.io/test/github/connexta/replication/badge.svg)](https://snyk.io/test/github/connexta/replication)|

##Docker Compose Deployment

####Prerequisites
The replication docker stack requires the following to be configured on the swarm before deploying:
######Network
An overlay network named `replication`
```
docker network create -d overlay replication
```
######Volume
A volume for storing replication stats and progress named `replication-data`
```
docker volume create replication-data
```
######Configuration
There are a few configurations that replication uses that need to be populated in docker config

|Config Name | Description|
|------------|------------|
|replication-sites| The json config holding the site configurations. Example below.|
|replication-jobs| The json config holding the replication jobs. Example below.|
|replication-spring-config| The spring-boot application.properties for replication. Example below.|

Example replication-sites
```json
[{
    "name": "site1",
    "url": "https://host1:8993/services"
 },
 {
    "name": "site2",
    "url": "https://host2:8993/services"
 }]
```
Example replication-jobs
```json
[{
     "name": "pdf-harvest",
     "bidirectional": false,
     "source": "site1",
     "destination": "site2",
     "filter": "\"media.type\" like 'application/pdf'"
 }]
```
Example replication-spring-config
```properties
logging.level.root=INFO
logging.level.org.apache.cxf.interceptor.LoggingOutInterceptor=WARN
logging.level.org.apache.cxf.interceptor.LoggingInInterceptor=WARN

# replication frequency in seconds
replication.period=300

spring.data.solr.host=http://replication-solr:8983/solr
```

######Secrets
Replication requires certs and ssl configurations in order to talk with remote DDF based systems. This information is stored in docker secrets.

|Secret Name | Description|
|------------|------------|
|replication-truststore|A truststore to use for TLS|
|replication-keystore|A keystore for this system to use TLS|
|replication-ssl|SSL properties for TLS including passwords for the truststore and keystore|

Example replication-ssl
```properties
javax.net.ssl.trustStorePassword=changeit
javax.net.ssl.keyStorePassword=changeit
javax.net.ssl.trustStoreType=jks
javax.net.ssl.keyStoreType=jks
```
####Running
Running the stack will start up a solr service and the replication service.

```
docker stack deploy -c docker-compose.yml repsync
```
