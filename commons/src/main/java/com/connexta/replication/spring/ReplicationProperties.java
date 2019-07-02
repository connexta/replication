package com.connexta.replication.spring;

import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This class is a bean which reads from the application properties and populates the fields it
 * contains based on the prefix and the name of the field. For example, if the application is given
 * an application.yml containing the following values:
 *
 * <pre>
 *  replication:
 *    period: 60
 *    connectionTimeout: 30
 *    receiveTimeout: 60
 * </pre>
 *
 * Then the fields below with the corresponding names will be given their respective value.
 */
@Component
@ConfigurationProperties(prefix = "replication")
public class ReplicationProperties {

  /** The interval, in seconds, on which to execute replication jobs */
  private long period;

  /** The connection timeout, in seconds, for any requests made by replication */
  private int connectionTimeout;

  /** The receive timeout, in seconds, for any requests made by replication */
  private int receiveTimeout;

  public long getPeriod() {
    return period;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  // We let the user set the timeouts in seconds for readability but most clients take
  // timeouts in milliseconds so we'll go ahead and do the conversion for them here.
  public int getConnectionTimeout() {
    if (connectionTimeout > 0) {
      return Math.toIntExact(TimeUnit.SECONDS.toMillis(connectionTimeout));
    } else {
      return 30000;
    }
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public int getReceiveTimeout() {
    if (receiveTimeout > 0) {
      return Math.toIntExact(TimeUnit.SECONDS.toMillis(receiveTimeout));
    } else {
      return 60000;
    }
  }

  public void setReceiveTimeout(int receiveTimeout) {
    this.receiveTimeout = receiveTimeout;
  }
}
