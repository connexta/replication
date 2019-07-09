package org.codice.ditto.replication.api;

/** Exception used to wrap exceptions thrown by querying a ddf. */
public class QueryException extends RuntimeException {

  /**
   * Creates an Exception with a message and cause
   *
   * @param message A message describing why the exception was thrown
   * @param cause An exception that will be wrapped by this one
   */
  public QueryException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates an Exception with a cause
   *
   * @param cause An exception that will be wrapped by this one
   */
  public QueryException(Throwable cause) {
    super(cause);
  }
}
