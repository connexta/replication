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
The configuration that replication uses that need to be populated in docker config

|Config Name | Description|
|------------|------------|
|replication-spring-config| The spring-boot application.properties for replication. Example below.|


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
      host: http://localhost:8983/solr
replication:
  period: 63
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

####Adding Replication Configuration
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
        "remote-managed_b":false,
        "name_txt":"RepSync-Node1",
        "url_txt":"https://host1:8993/services/",
        "id_txt":"some-unique-id-1234",
        "version_int":2
       },
      {
        "remote-managed_b":false,
        "name_txt":"RepSync-Node2",
        "url_txt":"https://host2:8993/services",
        "id_txt":"another-unique-id-5678",
        "version_int":2
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
      "name_txt":"pdf-harvest",
      "bidirectional_b":false,
      "source_txt":"some-unique-id-1234",
      "destination_txt":"another-unique-id-5678",
      "filter_txt":"\"media.type\" like 'application/pdf'",
      "retry_count_int":5,
      "suspended_b":false,
      "id_txt":"unique-job-id-98765",
      "version_int":1
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
