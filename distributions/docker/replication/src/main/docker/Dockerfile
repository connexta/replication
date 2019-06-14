FROM openjdk:11-jre-slim
COPY maven/${app.artifactId}-${project.version}.jar /opt/replication/app.jar
COPY maven/demo-config/keystores /opt/replication/demo-config/keystores
COPY maven/demo-config/application.properties /opt/replication/demo-config/application.properties

ENV truststore_pass changeit
ENV truststore_type jks
ENV truststore_file /opt/replication/demo-config/keystores/serverTruststore.jks
ENV keystore_pass changeit
ENV keystore_type jks
ENV keystore_file /opt/replication/demo-config/keystores/serverKeystore.jks
ENV app_config_file /opt/replication/demo-config/application.properties

ENTRYPOINT java \
    -Djavax.net.ssl.trustStorePassword=${truststore_pass} \
    -Djavax.net.ssl.keyStoreType=${truststore_type} \
    -Djavax.net.ssl.trustStore=${truststore_file} \
    -Djavax.net.ssl.keyStorePassword=${keystore_pass} \
    -Djavax.net.ssl.trustStoreType=${keystore_type} \
    -Djavax.net.ssl.keyStore=${keystore_file} \
    -jar /opt/replication/app.jar --spring.config.location=file:${app_config_file}
