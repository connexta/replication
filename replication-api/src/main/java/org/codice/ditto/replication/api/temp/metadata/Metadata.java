package org.codice.ditto.replication.api.temp.metadata;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface Metadata {

  String getId();

  Class getType();

  Object getRawMetadata();

  URI getResourceUri();

  void setResourceUri(URI resourceUri);

  long getResourceSize();

  void setResourceSize(long resourceSize);

  Date getMetadataModified();

  Date getResourceModified();

  void setResourceModified(Date resourceModified);

  Set<String> getTags();

  void addTag(String tag);

  List<String> getLineage();

  void addLineage(String name);
}
