package com.connexta.ion.replication.api.impl.persistence;

import com.connexta.ion.replication.api.NotFoundException;
import com.connexta.ion.replication.api.data.ReplicatorConfig;
import com.connexta.ion.replication.api.impl.data.ReplicatorConfigImpl;
import com.connexta.ion.replication.api.impl.spring.ConfigRepository;
import com.connexta.ion.replication.api.persistence.ReplicatorConfigManager;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    return configRepository.findById(configId).isPresent();
  }
}
