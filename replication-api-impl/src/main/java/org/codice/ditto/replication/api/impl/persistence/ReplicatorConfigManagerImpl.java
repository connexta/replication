package org.codice.ditto.replication.api.impl.persistence;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.codice.ditto.replication.api.NotFoundException;
import org.codice.ditto.replication.api.ReplicationPersistenceException;
import org.codice.ditto.replication.api.data.ReplicatorConfig;
import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.codice.ditto.replication.api.impl.spring.ConfigRepository;
import org.codice.ditto.replication.api.persistence.ReplicatorConfigManager;

public class ReplicatorConfigManagerImpl implements ReplicatorConfigManager {

  private ConfigRepository configRepository;

  public ReplicatorConfigManagerImpl(ConfigRepository configRepository) {
    this.configRepository = configRepository;
  }

  @Override
  public ReplicatorConfig create() {
    return new ReplicatorConfigImpl();
  }

  @Override
  public ReplicatorConfig get(String id) {
    return configRepository.findById(id).orElseThrow(NotFoundException::new);
  }

  @Override
  public Stream<ReplicatorConfig> objects() {
    return StreamSupport.stream(configRepository.findAll().spliterator(), false)
        .map(ReplicatorConfig.class::cast);
  }

  @Override
  public void save(ReplicatorConfig replicatorConfig) {
    if (replicatorConfig instanceof ReplicatorConfigImpl) {
      configRepository.save((ReplicatorConfigImpl) replicatorConfig);
    } else {
      throw new IllegalArgumentException(
          "Expected a ReplicatorConfigImpl but got a "
              + replicatorConfig.getClass().getSimpleName());
    }
  }

  @Override
  public void remove(String id) {
    configRepository.deleteById(id);
  }

  @Override
  public boolean configExists(String configId) {
    try {
      configRepository.findById(configId);
    } catch (NotFoundException | ReplicationPersistenceException e) {
      return false;
    }
    return true;
  }
}
