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
package com.connexta.replication.api.impl.data;

import com.connexta.replication.api.data.NonTransientReplicationPersistenceException;
import com.connexta.replication.api.data.NotFoundException;
import com.connexta.replication.api.data.RecoverableReplicationPersistenceException;
import com.connexta.replication.api.data.ReplicatorConfig;
import com.connexta.replication.api.data.TransientReplicationPersistenceException;
import com.connexta.replication.api.impl.persistence.pojo.ConfigPojo;
import com.connexta.replication.api.impl.persistence.spring.ConfigRepository;
import com.connexta.replication.api.persistence.ReplicatorConfigManager;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;

public class ReplicatorConfigManagerImpl implements ReplicatorConfigManager {

  private ConfigRepository configRepository;

  public ReplicatorConfigManagerImpl(ConfigRepository configRepository) {
    this.configRepository = configRepository;
  }

  @Override
  public ReplicatorConfig get(String id) {
    try {
      return configRepository
          .findById(id)
          .map(ReplicatorConfigImpl::new)
          .orElseThrow(() -> new NotFoundException("replication config not found: " + id));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public Stream<ReplicatorConfig> objects() {
    try {
      return StreamSupport.stream(configRepository.findAll().spliterator(), false)
          .map(ReplicatorConfigImpl::new)
          .map(ReplicatorConfig.class::cast);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void save(ReplicatorConfig replicatorConfig) {
    if (!(replicatorConfig instanceof ReplicatorConfigImpl)) {
      throw new IllegalArgumentException(
          "Expected a ReplicatorConfigImpl but got a "
              + replicatorConfig.getClass().getSimpleName());
    }
    try {
      configRepository.save(((ReplicatorConfigImpl) replicatorConfig).writeTo(new ConfigPojo()));
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public void remove(String id) {
    try {
      configRepository.deleteById(id);
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }

  @Override
  public boolean configExists(String configId) {
    try {
      return configRepository.findById(configId).isPresent();
    } catch (NonTransientDataAccessException e) {
      throw new NonTransientReplicationPersistenceException(e);
    } catch (TransientDataAccessException e) {
      throw new TransientReplicationPersistenceException(e);
    } catch (RecoverableDataAccessException e) {
      throw new RecoverableReplicationPersistenceException(e);
    }
  }
}
