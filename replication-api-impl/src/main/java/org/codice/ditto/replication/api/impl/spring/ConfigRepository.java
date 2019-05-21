package org.codice.ditto.replication.api.impl.spring;

import org.codice.ditto.replication.api.impl.data.ReplicatorConfigImpl;
import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called configRepository
public interface ConfigRepository extends CrudRepository<ReplicatorConfigImpl, String> {}
