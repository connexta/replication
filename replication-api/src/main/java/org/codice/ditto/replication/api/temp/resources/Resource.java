package org.codice.ditto.replication.api.temp.resources;

import java.io.InputStream;
import java.net.URI;
import org.codice.ditto.replication.api.temp.metadata.Metadata;

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
