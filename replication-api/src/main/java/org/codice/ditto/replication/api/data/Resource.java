package org.codice.ditto.replication.api.data;

import java.io.InputStream;
import java.net.URI;
import javax.annotation.Nullable;

public interface Resource {

  /**
   * A globally unique ID.
   *
   * @return the id
   */
  String getId();

  /** @return the human-readable name of this resource */
  String getName();

  /** @return the location of this resource */
  URI getResourceUri();

  /** @return the qualifier of this resource, or null if there isn't one */
  @Nullable
  String getQualifier();

  /** @return the input stream of the resource */
  InputStream getInputStream();

  /** @return the mime type of the input stream */
  String getMimeType();

  /** @return the resource size, otherwise -1 if unknown */
  long getSize();

  /** @return the {@link Metadata} associated with this resource */
  Metadata getMetadata();
}
