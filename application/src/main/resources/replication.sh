java -Djavax.net.ssl.trustStore=config/keystores/serverTruststore.jks \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -Djavax.net.ssl.trustStoreType=jks \
  -Djavax.net.ssl.keyStore=config/keystores/serverKeystore.jks \
  -Djavax.net.ssl.keyStorePassword=changeit \
  -Djavax.net.ssl.keyStoreType=jks \
 -jar application-@project.version@.jar