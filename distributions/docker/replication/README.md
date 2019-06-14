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
|app_config_file|/opt/replication/demo-config/application.properties|The spring boot application.properties file to use.

##Example Configuration Files


application.properties
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

##Running
####Demo Configuration
When running in "demo" mode default certs will be used. Requires a local solr instance to be running at 8983
```
docker run cnxta/replication:0.3.0-SNAPSHOT
```

To run in a non-demo configuration override the other environment variables.

