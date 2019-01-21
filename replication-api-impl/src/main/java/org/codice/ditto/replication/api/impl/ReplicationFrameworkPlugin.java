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

import ddf.catalog.data.Metacard;
import ddf.catalog.data.types.Core;
import ddf.catalog.operation.CreateRequest;
import ddf.catalog.operation.DeleteRequest;
import ddf.catalog.operation.DeleteResponse;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.ResourceRequest;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.plugin.AccessPlugin;
import java.util.Map;
import org.codice.ditto.replication.api.mcard.Replication;

/**
 * Plugin sets a property to skip nitf overview/original generation if it is a replicated metacard
 * request
 */
public class ReplicationFrameworkPlugin implements AccessPlugin {

  @Override
  public CreateRequest processPreCreate(CreateRequest createRequest) {
    return createRequest;
  }

  @Override
  public UpdateRequest processPreUpdate(UpdateRequest updateRequest, Map<String, Metacard> map) {
    updateRequest
        .getUpdates()
        .stream()
        .map(Map.Entry::getValue)
        .filter(mcard -> mcard.getTags().contains(Replication.REPLICATED_TAG))
        .filter(mcard -> mcard.getResourceURI() != null)
        .forEach(mcard -> updateResourceUri(mcard, map.get(mcard.getId())));
    return updateRequest;
  }

  @Override
  public DeleteRequest processPreDelete(DeleteRequest deleteRequest) {
    return deleteRequest;
  }

  @Override
  public DeleteResponse processPostDelete(DeleteResponse deleteResponse) {
    return deleteResponse;
  }

  @Override
  public QueryRequest processPreQuery(QueryRequest queryRequest) {
    return queryRequest;
  }

  @Override
  public QueryResponse processPostQuery(QueryResponse queryResponse) {
    return queryResponse;
  }

  @Override
  public ResourceRequest processPreResource(ResourceRequest resourceRequest) {
    return resourceRequest;
  }

  @Override
  public ResourceResponse processPostResource(
      ResourceResponse resourceResponse, Metacard metacard) {
    return resourceResponse;
  }

  private void updateResourceUri(Metacard newMcard, Metacard original) {
    newMcard.setAttribute(original.getAttribute(Core.RESOURCE_URI));
    newMcard.setAttribute(original.getAttribute(Core.RESOURCE_DOWNLOAD_URL));
  }
}
