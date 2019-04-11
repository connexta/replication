package org.codice.ditto.replication.api.data;

import java.util.List;

public interface UpdateStorageRequest {

  List<Resource> getResources();
}
