package org.codice.ditto.replication.api.temp;

public class AdapterException extends RuntimeException {

  public AdapterException(String msg) {
    super(msg);
  }

  public AdapterException(String msg, Exception cause) {
    super(msg, cause);
  }
}
