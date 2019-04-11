package org.codice.ditto.replication.api.temp;

import java.io.Closeable;
import org.codice.ditto.replication.api.temp.metadata.CreateRequest;
import org.codice.ditto.replication.api.temp.metadata.DeleteRequest;
import org.codice.ditto.replication.api.temp.metadata.Metadata;
import org.codice.ditto.replication.api.temp.metadata.QueryRequest;
import org.codice.ditto.replication.api.temp.metadata.QueryResponse;
import org.codice.ditto.replication.api.temp.metadata.UpdateRequest;
import org.codice.ditto.replication.api.temp.resources.CreateStorageRequest;
import org.codice.ditto.replication.api.temp.resources.ResourceRequest;
import org.codice.ditto.replication.api.temp.resources.ResourceResponse;
import org.codice.ditto.replication.api.temp.resources.UpdateStorageRequest;

public interface NodeAdapter extends Closeable {

  boolean isAvailable();

  String getSystemName();

  /**
   * Gets the Metadata that should be created, updated, or deleted
   *
   * @param queryRequest
   * @return a {@link QueryResponse} containing the {@link
   *     org.codice.ditto.replication.api.temp.metadata.Metadata} matching the {@link QueryRequest}
   *     criteria
   * @throws {@link AdapterException} if there is an error communicating with the remote server
   */
  QueryResponse query(QueryRequest queryRequest);

  boolean exists(Metadata metadata);

  boolean createRequest(CreateRequest createRequest);

  boolean updateRequest(UpdateRequest updateRequest);

  boolean deleteRequest(DeleteRequest deleteRequest);

  /** Resources */
  ResourceResponse readResource(ResourceRequest resourceRequest);

  boolean createResource(CreateStorageRequest createStorageRequest);

  boolean updateResource(UpdateStorageRequest updateStorageRequest);
}
