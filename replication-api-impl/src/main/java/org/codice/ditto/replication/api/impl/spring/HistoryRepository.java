package org.codice.ditto.replication.api.impl.spring;

import org.codice.ditto.replication.api.impl.data.ReplicationStatusImpl;
import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called historyRepository
public interface HistoryRepository extends CrudRepository<ReplicationStatusImpl, String> {}
