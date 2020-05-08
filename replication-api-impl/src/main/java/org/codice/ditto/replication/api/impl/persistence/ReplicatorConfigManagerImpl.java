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
package org.codice.ditto.replication.api.impl.persistence;

import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;

public class ReplicatorConfigManagerImpl implements ReplicatorConfigManager {

  private ReplicationPersistentStore persistentStore;

  public ReplicatorConfigManagerImpl(ReplicationPersistentStore persistentStore) {
    this.persistentStore = persistentStore;
  }

  @Override
  public ReplicatorConfig create() {
    return new ReplicatorConfigImpl();
  }

  @Override
  public ReplicatorConfig get(String id) {
    return persistentStore.get(ReplicatorConfigImpl.class, id);
  }

  @Override
  public Stream<ReplicatorConfig> objects() {
    return persistentStore.objects(ReplicatorConfigImpl.class).map(ReplicatorConfig.class::cast);
  }

  @Override
  public void save(ReplicatorConfig replicatorConfig) {
    if (replicatorConfig instanceof ReplicatorConfigImpl) {
      persistentStore.save((ReplicatorConfigImpl) replicatorConfig);
    } else {
      throw new IllegalArgumentException(
          "Expected a ReplicatorConfigImpl but got a "
              + replicatorConfig.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    persistentStore.delete(ReplicatorConfigImpl.class, id);
  }

  @Override
  public boolean exists(String id) {
    try {
      persistentStore.get(ReplicatorConfigImpl.class, id);
    } catch (NotFoundException e) {
      return false;
    }
    return true;
  }
}
