#Replication Docker Image

##Variables

| Name | Default | Description
|------|---------|------------
|truststore_pass|changeit|Password for accessing truststore
|truststore_type|jks|The type of truststore
|truststore_file|/opt/replication/demo-config/keystores/serverTruststore.jks| The truststore file location.
|keystore_pass|changeit|Password for accessing keystore
|keystore_type|jks|The type of keystore
|keystore_file|/opt/replication/demo-config/keystores/serverKeystore.jks| The keystore file location.
|config_dir|/opt/replication/demo-config/|The directory containing the config/ directory which holds the config.json and sites.json files.
|app_config_file|/opt/replication/demo-config/application.properties|The spring boot application.properties file to use.

##Example Configuration Files
site.json
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
config.json
```json
[{
     "name": "pdf-harvest",
     "bidirectional": false,
     "source": "site1",
     "destination": "site2",
     "filter": "\"media.type\" like 'application/pdf'"
 }]
```

application.properties
```properties
logging.level.root=INFO
logging.level.org.apache.cxf.interceptor.LoggingOutInterceptor=WARN
logging.level.org.apache.cxf.interceptor.LoggingInInterceptor=WARN

# replication frequency in seconds
replication.period=300

spring.data.solr.host=http://localhost:8983/solr
```

##Running
####Demo Configuration
When running in "demo" mode default certs will be used. Requires a local solr instance to be running at 8983
```
docker run -v /local/config/dir/:/container/config/ -e config_dir='/container/config/' cnxta/replication:0.3.0-SNAPSHOT
```

To run in a non-demo configuration override the other environment variables.

