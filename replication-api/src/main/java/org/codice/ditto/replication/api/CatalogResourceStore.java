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
package org.codice.ditto.replication.api;

import ddf.catalog.content.operation.CreateStorageRequest;
import ddf.catalog.content.operation.UpdateStorageRequest;
import ddf.catalog.operation.CreateResponse;
import ddf.catalog.operation.UpdateResponse;
import ddf.catalog.source.CatalogStore;
import ddf.catalog.source.IngestException;
import ddf.catalog.source.SourceUnavailableException;

/**
 * This interface should exist in the catalog core api but it doesn't exist there yet. This adds the
 * content store operations for create and update to the existing CatalogStore interface.
 */
public interface CatalogResourceStore extends CatalogStore {

  CreateResponse create(CreateStorageRequest createStorageRequest)
      throws IngestException, SourceUnavailableException;

  UpdateResponse update(UpdateStorageRequest updateStorageRequest)
      throws IngestException, SourceUnavailableException;
}
