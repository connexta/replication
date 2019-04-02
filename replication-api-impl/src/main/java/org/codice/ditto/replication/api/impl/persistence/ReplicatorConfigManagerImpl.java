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
