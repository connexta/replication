package org.codice.ditto.replication.api.data;

import java.io.InputStream;
import java.net.URI;

public interface Resource {

  String getId();

  String getName();

  URI getResourceUri();

  String getQualifier();

  InputStream getInputStream();

  String getMimeType();

  long getSize();

  Metadata getMetadata();
}
