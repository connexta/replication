package org.codice.ditto.replication.api;

/** General exception for {@link NodeAdapter} errors. */
public class AdapterException extends RuntimeException {

  public AdapterException(String msg) {
    super(msg);
  }

  public AdapterException(String msg, Exception cause) {
    super(msg, cause);
  }
}
