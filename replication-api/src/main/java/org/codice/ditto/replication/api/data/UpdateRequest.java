package org.codice.ditto.replication.api.data;

import java.util.List;

public interface UpdateRequest {

  List<Metadata> getUpdatedMetadata();
}
