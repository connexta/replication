package org.codice.ditto.replication.api.impl.spring;

import org.codice.ditto.replication.api.impl.data.ReplicationSiteImpl;
import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called siteRepository
public interface SiteRepository extends CrudRepository<ReplicationSiteImpl, String> {}
