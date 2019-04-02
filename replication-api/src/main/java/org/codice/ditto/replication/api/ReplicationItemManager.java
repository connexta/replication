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

import java.util.List;
import java.util.Optional;
import org.codice.ddf.persistence.PersistenceException;

public interface ReplicationItemManager {

  Optional<ReplicationItem> getItem(String metacardId, String source, String destination);

  List<ReplicationItem> getItemsForConfig(String configId, int startIndex, int pageSize)
      throws PersistenceException;

  void saveItem(ReplicationItem replicationItem);

  void deleteAllItems() throws PersistenceException;

  void deleteItem(String metacardId, String source, String destination);

  List<String> getFailureList(int maximumFailureCount, String source, String destination);

  void deleteItemsForConfig(String configId) throws PersistenceException;
}
