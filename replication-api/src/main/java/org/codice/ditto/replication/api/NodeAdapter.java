package org.codice.ditto.replication.api;

import java.io.Closeable;
import org.codice.ditto.replication.api.data.CreateRequest;
import org.codice.ditto.replication.api.data.CreateStorageRequest;
import org.codice.ditto.replication.api.data.DeleteRequest;
import org.codice.ditto.replication.api.data.Metadata;
import org.codice.ditto.replication.api.data.QueryRequest;
import org.codice.ditto.replication.api.data.QueryResponse;
import org.codice.ditto.replication.api.data.ResourceRequest;
import org.codice.ditto.replication.api.data.ResourceResponse;
import org.codice.ditto.replication.api.data.UpdateRequest;
import org.codice.ditto.replication.api.data.UpdateStorageRequest;

public interface NodeAdapter extends Closeable {

  boolean isAvailable();

  String getSystemName();

  /**
   * Gets the Metadata that should be created, updated, or deleted
   *
   * @param queryRequest
   * @return a {@link QueryResponse} containing the {@link Metadata} matching the {@link
   *     QueryRequest} criteria
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
