package com.connexta.ion.replication.api.impl.spring;

import com.connexta.ion.replication.api.impl.data.ReplicationStatusImpl;
import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called historyRepository
public interface HistoryRepository extends CrudRepository<ReplicationStatusImpl, String> {}
