package org.codice.ditto.replication.api.temp.metadata;

import java.util.List;

public interface UpdateRequest {

  List<Metadata> getUpdatedMetadata();
}
