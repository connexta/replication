/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ditto.replication.api.impl;

import ddf.catalog.CatalogFramework;
import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.UpdateStorageRequest;
import ddf.catalog.data.ContentType;
import ddf.catalog.data.Metacard;
import ddf.catalog.federation.FederationException;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.SourceResponse;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.operation.impl.ResourceRequestByProductUri;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceMonitor;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.codice.ddf.configuration.SystemInfo;
import org.codice.ditto.replication.api.ReplicationStore;

/** Wraps the catalog framework in the CatalogResourceStore interface. */
public class LocalCatalogResourceStore implements ReplicationStore {

  private final CatalogFramework catalogFramework;

  public LocalCatalogResourceStore(CatalogFramework catalogFramework) {
    this.catalogFramework = catalogFramework;
  }

  @Override
  public CreateResponse create(CreateStorageRequest createStorageRequest)
      throws IngestException, SourceUnavailableException {
    return catalogFramework.create(createStorageRequest);
  }

  @Override
  public UpdateResponse update(UpdateStorageRequest updateStorageRequest)
      throws IngestException, SourceUnavailableException {
    return catalogFramework.update(updateStorageRequest);
  }

  @Override
  public CreateResponse create(CreateRequest createRequest) throws IngestException {
    try {
      return catalogFramework.create(createRequest);
    } catch (SourceUnavailableException e) {
      throw new IngestException(e);
    }
  }

  @Override
  public UpdateResponse update(UpdateRequest updateRequest) throws IngestException {
    try {
      return catalogFramework.update(updateRequest);
    } catch (SourceUnavailableException e) {
      throw new IngestException(e);
    }
  }

  @Override
  public DeleteResponse delete(DeleteRequest deleteRequest) throws IngestException {
    try {
      return catalogFramework.delete(deleteRequest);
    } catch (SourceUnavailableException e) {
      throw new IngestException(e);
    }
  }

  @Override
  public ResourceResponse retrieveResource(URI uri, Map<String, Serializable> map)
      throws IOException, ResourceNotFoundException, ResourceNotSupportedException {
    return catalogFramework.getLocalResource(new ResourceRequestByProductUri(uri, map));
  }

  @Override
  public Set<String> getSupportedSchemes() {
    return Collections.emptySet();
  }

  @Override
  public Set<String> getOptions(Metacard metacard) {
    return Collections.emptySet();
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public boolean isAvailable(SourceMonitor sourceMonitor) {
    return catalogFramework.getSourceIds().contains(this.getId());
  }

  @Override
  public SourceResponse query(QueryRequest queryRequest) throws UnsupportedQueryException {
    try {
      return catalogFramework.query(queryRequest);
    } catch (SourceUnavailableException | FederationException e) {
      throw new UnsupportedQueryException(e);
    }
  }

  @Override
  public Set<ContentType> getContentTypes() {
    return Collections.emptySet();
  }

  @Override
  public Map<String, Set<String>> getSecurityAttributes() {
    return Collections.emptyMap();
  }

  @Override
  public String getVersion() {
    return SystemInfo.getVersion();
  }

  @Override
  public String getId() {
    return SystemInfo.getSiteName();
  }

  @Override
  public String getTitle() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDescription() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getOrganization() {
    return SystemInfo.getOrganization();
  }

  @Override
  public String getRemoteName() {
    return SystemInfo.getSiteName();
  }

  @Override
  public void close() {
    // Do nothing
  }
}
