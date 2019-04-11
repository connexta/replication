package org.codice.ditto.replication.api;

import java.io.IOException;
import java.util.List;
import org.codice.ditto.replication.api.data.Metadata;

public interface MetadataStore {

  List<Metadata> getMetadata() throws IOException;

  List<Metadata> getMetadata(Class type) throws IOException;

  String store(Metadata metadata) throws IOException;

  Metadata load(String id) throws IOException;

  boolean delete(String id) throws IOException;
}
